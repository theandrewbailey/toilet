package libWebsiteTools.imead;

import at.gadermaier.argon2.Argon2Factory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import libOdyssey.bean.ExceptionRepo;
import libWebsiteTools.JVMNotSupportedError;

/**
 * Internationalization Made Easy And Dynamic
 *
 * run DB scripts before using
 *
 * @author alpha
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class IMEADHolder {

    public static final String LOCAL_NAME = "java:module/IMEADHolder";
    private static final Logger LOG = Logger.getLogger(IMEADHolder.class.getName());
    private static final String ARGON2_SALT_KEY = "argon2_salt";
    private Map<Locale, Properties> localizedCache = new HashMap<>();

    @PersistenceUnit
    private EntityManagerFactory PU;
    @EJB
    private ExceptionRepo error;

    public static Locale getLanguageOnly(Locale in) {
        Locale.Builder build = new Locale.Builder();
        build.setLanguage(in.getLanguage());
        return build.build();
    }

    @PostConstruct
    public void populateCache() {
        LOG.entering(IMEADHolder.class.getName(), "populateCache");
        PU.getCache().evict(Localization.class);
        localizedCache = Collections.unmodifiableMap(getProperties());
        LOG.exiting(IMEADHolder.class.getName(), "populateCache");
    }

    public Map<Locale, Properties> getProperties() {
        EntityManager em = PU.createEntityManager();
        try {
            List<String> supportedLocales = em.createNamedQuery("Localization.getDistinctLocales", String.class).getResultList();
            Map<Locale, Properties> output = new HashMap<>(supportedLocales.size() * 2);
            for (String supportedLocale : supportedLocales) {
                Locale l = Locale.forLanguageTag(null != supportedLocale ? supportedLocale : "");
                Properties props = new Properties();
                for (Localization prop : em.createNamedQuery("Localization.findByLocalecode", Localization.class).setParameter("localecode", supportedLocale).getResultList()) {
                    props.put(prop.getLocalizationPK().getKey(), prop.getValue());
                }
                output.put(l, props);
            }
            return output;
        } finally {
            em.close();
        }
    }

    /**
     *
     * @return mapping of locale code to .properties file contents
     */
    public Map<String, String> backup() {
        Map<String, String> localeFiles = new HashMap<>(localizedCache.size() * 2);
        for (Locale l : localizedCache.keySet()) {
            try {
                StringWriter propertiesContent = new StringWriter(10000);
                localizedCache.get(l).store(propertiesContent, null);
                String localeString = l != Locale.ROOT ? l.toLanguageTag() : "";
                localeFiles.put(localeString, propertiesContent.toString());
            } catch (IOException ex) {
                error.add(null, "Can't backup properties", "Can't backup properties for locale " + l.toLanguageTag(), ex);
            }
        }
        return localeFiles;
    }

    /**
     *
     * @param locale
     * @param propertiesContent contents of a .properties file
     * @return the new Properties object
     */
    public Properties restore(String locale, String propertiesContent) {
        locale = null != locale ? locale : "";
        Properties props = new Properties();
        Map<Locale, Properties> localeTemp = new HashMap<>(localizedCache);
        try {
            props.load(new StringReader(propertiesContent));
            localeTemp.put(Locale.forLanguageTag(locale), restore(locale, props));
            localizedCache = Collections.unmodifiableMap(localeTemp);
        } catch (IOException ex) {
            error.add(null, "Can't restore properties", "Can't restore properties for locale " + locale, ex);
        } finally {
        }
        return props;
    }

    public Properties restore(String locale, Properties props) {
        EntityManager em = PU.createEntityManager();
        em.getTransaction().begin();
        try {
            for (Localization prop : em.createNamedQuery("Localization.findByLocalecode", Localization.class).setParameter("localecode", locale).getResultList()) {
                em.remove(prop);
            }
            em.getTransaction().commit();
            em.getTransaction().begin();
            for (Object key : props.keySet()) {
                em.persist(new Localization(new LocalizationPK(key.toString(), locale), props.getProperty(key.toString())));
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            em.close();
        }
        return props;
    }

    /**
     * run Argon2 on toVerify and compare result to value stored at key
     *
     * @param toVerify
     * @param key
     * @return does it match?
     */
    public boolean verifyArgon2(String toVerify, String key) {
        try {
            return Argon2Factory.create().setIterations(16).setMemoryInKiB(8192).setParallelism(2)
                    .hash(toVerify.getBytes("UTF-8"), getValue(ARGON2_SALT_KEY).getBytes("UTF-8"))
                    .equals(getValue(key));
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        }
    }

    /**
     * @param key
     * @return value from keyValue map (from DB)
     */
    public String getValue(String key) {
        return getLocal(key, Locale.ROOT);
    }

    /**
     * try to get value for key, using order of given locales
     *
     * @param key
     * @param locales
     * @return value
     * @throws RuntimeException if key cannot be found in locales
     */
    public String getLocal(String key, Collection<Locale> locales) {
        for (Locale l : locales) {
            if (localizedCache.containsKey(l)) {
                String retrun = localizedCache.get(l).getProperty(key);
                if (retrun != null) {
                    return retrun;
                }
            }
        }
        LOG.log(Level.FINE, "Key {0} not found in locales {1}", new Object[]{key, Arrays.toString(locales.toArray())});
        throw new LocalizedStringNotFoundException(key, Arrays.toString(locales.toArray()));
    }

    /**
     * try to get value for key in specified locale (as locale).
     *
     * @param key
     * @param locale
     * @return value || null
     */
    public String getLocal(String key, Locale locale) {
        try {
            return localizedCache.get(locale).getProperty(key);
        } catch (NullPointerException n) {
            return null;
        }
    }

    /**
     * try to get value for key in specified locale (as String). will return
     * null if key not in locale, will throw NullPointerException if locale does
     * not exist.
     *
     * @param key
     * @param locale
     * @return value || null
     * @throws NullPointerException
     */
    public String getLocal(String key, String locale) {
        Locale l = Locale.forLanguageTag(locale);
        if (null == l) {
            throw new NullPointerException("Locale " + locale + " is not valid.");
        }
        return localizedCache.get(l).getProperty(key);
    }
}
