package libWebsiteTools.imead;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

/**
 * Internationalization Made Easy And Dynamic
 *
 * run DB scripts before using
 *
 * @author alpha
 */
@Singleton
public class IMEADHolder {

    public static final String LOCAL_NAME = "java:module/IMEADHolder";
    @PersistenceUnit
    private EntityManagerFactory PU;
    private static final String DISTINCT_LOCALE_QUERY = "SELECT DISTINCT l.localizationPK.localecode FROM Localization l ORDER BY l.localizationPK.localecode ASC";
    private static final String VALUES_BY_LOCALE = "SELECT l FROM Localization l WHERE l.localizationPK.localecode = :locale";
    private static final String KEYVALUE_ALL_QUERY = "SELECT k FROM Keyvalue k";
    private static final Logger log = Logger.getLogger(IMEADHolder.class.getName());
    private Map<String, String> keyValues = new HashMap<>();
    private Map<Locale, IMEADResource> localizedCache = new HashMap<>();

    public static Locale getLanguageOnly(Locale in) {
        Locale.Builder build = new Locale.Builder();
        build.setLanguage(in.getLanguage());
        return build.build();
    }

    @PostConstruct
    public void populateCache() {
        log.entering(IMEADHolder.class.getName(), "populateCache");
        PU.getCache().evict(Keyvalue.class);
        PU.getCache().evict(Localization.class);
        EntityManager em = PU.createEntityManager();
        try {
            List<Keyvalue> kvs = em.createQuery(KEYVALUE_ALL_QUERY, Keyvalue.class).getResultList();
            Map<String, String> temp = new HashMap<>(kvs.size() * 2);
            for (Keyvalue kv : kvs) {
                temp.put(kv.getKey(), kv.getValue());
                log.log(Level.CONFIG, "{0}: {1}", new Object[]{kv.getKey(), kv.getValue()});
            }
            keyValues = Collections.unmodifiableMap(temp);

            List<String> supportedLocales = em.createQuery(DISTINCT_LOCALE_QUERY, String.class).getResultList();
            Map<Locale, IMEADResource> localeTemp = new HashMap<>(supportedLocales.size() * 2);
            for (String supportedLocale : supportedLocales) {
                Locale l = Locale.forLanguageTag(supportedLocale);
                IMEADResource parent = localeTemp.get(getLanguageOnly(l));
                localeTemp.put(l, new IMEADResource(l, parent, em.createQuery(VALUES_BY_LOCALE, Localization.class).setParameter("locale", supportedLocale).getResultList()));
            }
            localizedCache = Collections.unmodifiableMap(localeTemp);
        } finally {
            em.close();
        }
        log.exiting(IMEADHolder.class.getName(), "populateCache");
    }

    /**
     * @param key
     * @return value from keyValue map (from DB)
     */
    public String getValue(String key) {
        return keyValues.get(key);
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
                String retrun = localizedCache.get(l).getString(key);
                if (retrun != null) {
                    return retrun;
                }
            }
        }
        log.log(Level.FINE, "Key {0} not found in locales {1}", new Object[]{key, Arrays.toString(locales.toArray())});
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
            return localizedCache.get(locale).getString(key);
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
        return localizedCache.get(l).getString(key);
    }
}
