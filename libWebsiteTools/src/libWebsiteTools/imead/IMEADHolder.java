package libWebsiteTools.imead;

import java.util.Arrays;
import java.util.Collection;
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
import libWebsiteTools.imead.db.Keyvalue;
import libWebsiteTools.imead.db.Localization;

/**
 * Internationalization Made Easy And Dynamic
 * 
 * run DB scripts before using
 * 
 * @author alpha
 */
@Singleton
public class IMEADHolder {

    @PersistenceUnit
    private EntityManagerFactory PU;
    private static final String DISTINCT_LOCALE_QUERY = "SELECT DISTINCT l.localizationPK.localecode FROM Localization l";
    private static final String DISTINCT_KEY_QUERY = "SELECT DISTINCT l.localizationPK.key FROM Localization l";
    private static final String LOCALE_LIKE_QUERY = "SELECT l FROM Localization l WHERE l.localizationPK.localecode LIKE :locale";
    private static final String KEYVALUE_ALL_QUERY = "SELECT k FROM Keyvalue k";
    private static final Logger log = Logger.getLogger(IMEADHolder.class.getName());
    private final Map<String, String> keyValues = new HashMap<String, String>();
    private final Map<String, Map<String, String>> localizedCache = new HashMap<String, Map<String, String>>();

    @PostConstruct
    public void populateCache() {
        log.entering(IMEADHolder.class.getName(), "populateCache");
        PU.getCache().evict(Keyvalue.class);
        PU.getCache().evict(Localization.class);
        EntityManager em = PU.createEntityManager();
        List<Keyvalue> kvs = em.createQuery(KEYVALUE_ALL_QUERY, Keyvalue.class).getResultList();
        Map<String, String> temp = new HashMap<String, String>(kvs.size());

        for (Keyvalue kv : kvs) {
            temp.put(kv.getKey(), kv.getValue());
            log.log(Level.CONFIG, "{0}: {1}", new Object[]{kv.getKey(), kv.getValue()});
        }

        synchronized (keyValues) {
            keyValues.clear();
            keyValues.putAll(temp);
        }
        Map<String, Map<String, String>> localeTemp = new HashMap<String, Map<String, String>>();

        List<String> supportedLocales = em.createQuery(DISTINCT_LOCALE_QUERY, String.class).getResultList();
        List<String> allKeys = em.createQuery(DISTINCT_KEY_QUERY, String.class).getResultList();

        for (String l : supportedLocales) {
            temp = new HashMap<String, String>();
            String[] parts = l.split("_");
            if (parts.length == 0) {
                throw new RuntimeException("Bad locale detected: " + l);
            }
            String lang = parts[0];
            String first = parts.length == 1 ? parts[0] : parts[0] + parts[1];
            String second = parts.length == 2 ? parts[0] : null;
            List<Localization> values = em.createQuery(LOCALE_LIKE_QUERY, Localization.class).setParameter("locale", lang + "%").getResultList();
            for (String k : allKeys) {
                for (Localization locale : values) {
                    if (locale.getLocalizationPK().getLocalecode().equals(first)) {
                        temp.put(locale.getLocalizationPK().getKey(), locale.getValue());
                        log.log(Level.CONFIG, "({0}){1}: {2}", new Object[]{locale.getLocalizationPK().getLocalecode(), locale.getLocalizationPK().getKey(), locale.getValue()});
                        continue;
                    } else if (locale.getLocalizationPK().getLocalecode().equals(second)) {
                        temp.put(locale.getLocalizationPK().getKey(), locale.getValue());
                        log.log(Level.CONFIG, "({0}){1}: {2}", new Object[]{locale.getLocalizationPK().getLocalecode(), locale.getLocalizationPK().getKey(), locale.getValue()});
                    }
                }
            }
            localeTemp.put(l, temp);
        }
        synchronized (localizedCache) {
            localizedCache.clear();
            localizedCache.putAll(localeTemp);
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
     * @param key
     * @param localePref
     * @return value
     * @throws RuntimeException if key cannot be found in locales
     */
    public String getLocal(String key, Collection<Locale> locales) {
        for (Locale l : locales) {
            String retrun = getLocal(key, l);
            if (retrun != null) {
                return retrun;
            }
        }
        log.log(Level.FINE, "Key {0} not found in locales {1}", new Object[]{key, Arrays.toString(locales.toArray())});
        throw new LocalizedStringNotFoundException(key, Arrays.toString(locales.toArray()));
    }

    /**
     * try to get value for key in specified locale (as locale).
     * @param key
     * @param locale
     * @return value || null
     */
    public String getLocal(String key, Locale locale) {
        String[] parts = locale.toString().split("_");
        try {
            return getLocal(key, parts.length == 1 ? parts[0] : parts[0] + "-" + parts[1]);
        } catch (NullPointerException n) {
            return null;
        }
    }

    /**
     * try to get value for key in specified locale (as String). will return null if key not in locale, will throw NullPointerException if locale does not exist.
     * @param key
     * @param locale
     * @return value || null
     * @throws NullPointerException
     */
    public String getLocal(String key, String locale) {
        return localizedCache.get(locale).get(key);
    }
}
