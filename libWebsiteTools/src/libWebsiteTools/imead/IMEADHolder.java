package libWebsiteTools.imead;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.db.Repository;

/**
 * Internationalization Made Easy And Dynamic
 *
 * @author alpha
 */
@Startup
@Singleton
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class IMEADHolder implements Repository<Localization> {

    private static final Logger LOG = Logger.getLogger(IMEADHolder.class.getName());
    private Map<Locale, Properties> localizedCache = new HashMap<>();
    private final Map<String, List<Pattern>> patterns = new HashMap<>();
    private String localizedHash = "";
    @PersistenceUnit
    private EntityManagerFactory PU;

    /**
     * refresh cache of all properties from the DB
     */
    @PostConstruct
    @Override
    public void evict() {
        LOG.entering(IMEADHolder.class.getName(), "evict");
        PU.getCache().evict(Localization.class);
        localizedCache = Collections.unmodifiableMap(getProperties());
        localizedHash = HashUtil.getSHA256Hash(localizedCache.toString());
        patterns.clear();
        LOG.exiting(IMEADHolder.class.getName(), "evict");
    }

    /**
     * remove property from DB and refresh cache
     *
     * @param localPK
     */
    @Override
    public Localization delete(Object localPK) {
        EntityManager em = PU.createEntityManager();
        Localization out = em.find(Localization.class, localPK);
        try {
            em.getTransaction().begin();
            em.remove(out);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        evict();
        return out;
    }

    /**
     * add all specified Localizations to DB and refresh cache
     *
     * @param entities
     */
    @Override
    public List<Localization> upsert(Collection<Localization> entities) {
        ArrayList<Localization> out = new ArrayList<>(entities.size());
        EntityManager em = PU.createEntityManager();
        boolean dirty = false;
        try {
            em.getTransaction().begin();
            for (Localization l : entities) {
                Localization existing = em.find(Localization.class, l.localizationPK);
                if (null != existing && !existing.getValue().equals(l.getValue())) {
                    existing.setValue(l.getValue());
                    dirty = true;
                    out.add(existing);
                } else if (null == existing) {
                    em.persist(l);
                    dirty = true;
                    out.add(l);
                }
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        if (dirty) {
            evict();
        }
        return out;
    }

    @Override
    public Localization get(Object localPK) {
        EntityManager em = PU.createEntityManager();
        try {
            return em.find(Localization.class, localPK);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Localization> getAll(Integer limit) {
        EntityManager em = PU.createEntityManager();
        try {
            TypedQuery<Localization> q = em.createNamedQuery("Localization.findAll", Localization.class);
            if (null != limit) {
                q.setMaxResults(limit);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void processArchive(Consumer<Localization> operation, Boolean transaction) {
        EntityManager em = PU.createEntityManager();
        try {
            if (transaction) {
                em.getTransaction().begin();
                em.createNamedQuery("Localization.findAll", Localization.class).getResultStream().forEachOrdered(operation);
                em.getTransaction().commit();
            } else {
                em.createNamedQuery("Localization.findAll", Localization.class).getResultStream().forEachOrdered(operation);
            }
        } finally {
            em.close();
        }
    }

    @Override
    public Long count() {
        EntityManager em = PU.createEntityManager();
        try {
            TypedQuery<Long> qn = em.createNamedQuery("Localization.count", Long.class);
            return qn.getSingleResult();
        } finally {
            em.close();
        }
    }

    /**
     * load all properties from DB
     *
     * @return map of Locale to Properties
     */
    public Map<Locale, Properties> getProperties() {
        EntityManager em = PU.createEntityManager();
        try {
            List<String> supportedLocales = getLocaleStrings();
            Map<Locale, Properties> output = new HashMap<>(supportedLocales.size() * 2);
            for (String supportedLocale : supportedLocales) {
                Locale l = Locale.forLanguageTag(null != supportedLocale ? supportedLocale : "");
                Properties props = new SortedProperties();
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

    public List<Pattern> getPatterns(String key) {
        if (!patterns.containsKey(key)) {
            List<Pattern> temps = new ArrayList<>();
            for (String line : getValue(key).split("\n")) {
                temps.add(Pattern.compile(line.replaceAll("\r", "")));
            }
            patterns.put(key, Collections.unmodifiableList(temps));
        }
        return patterns.get(key);
    }

    public static boolean matchesAny(CharSequence subject, List<Pattern> regexes) {
        for (Pattern p : regexes) {
            if (p.matcher(subject).matches()) {
                return true;
            }
        }
        return false;
    }

    public Collection<Locale> getLocales() {
        return localizedCache.keySet();
    }

    /**
     *
     * @return supported locales in DB
     */
    public List<String> getLocaleStrings() {
        EntityManager em = PU.createEntityManager();
        try {
            return em.createNamedQuery("Localization.getDistinctLocales", String.class).getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * equivalent to calling getLocal with the root locale
     *
     * @param key
     * @return value from keyValue map (from DB)
     */
    public String getValue(String key) {
        try {
            return localizedCache.get(Locale.ROOT).getProperty(key);
        } catch (NullPointerException n) {
            return null;
        }
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
            throw new LocalizedStringNotFoundException("anything (as in, not set up)", locale);
        }
        try {
            return localizedCache.get(l).getProperty(key);
        } catch (NullPointerException n) {
            throw new LocalizedStringNotFoundException(key, locale);
        }
    }

    /**
     * @return the localizedHash
     */
    public String getLocalizedHash() {
        return localizedHash;
    }
}
