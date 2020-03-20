package libWebsiteTools.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import libWebsiteTools.db.Honeypot;
import libWebsiteTools.imead.IMEADHolder;

/**
 *
 * @author alpha
 */
@Singleton
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class GuardRepo {

    // group 1 is the origin (with http), group 2 is domain and port specifically
    public static final Pattern ORIGIN_PATTERN = Pattern.compile("^((https?://)([^/]+))(/.*)?$");
    public static final String CANONICAL_URL = "security_baseURL";
    public static final String ACCEPTABLE_CONTENT_DOMAINS = "security_acceptableContentDomains";
    public static final Map<String, Object> USE_CACHE_HINT = Collections.unmodifiableMap(new HashMap<String, Object>() {
        {
            put("javax.persistence.cache.retrieveMode", "USE");
            put("javax.persistence.cache.storeMode", "USE");
        }
    });
    private static final String DENY_USER_AGENTS = "security_denyUserAgents";
    private static final String HONEYPOTS = "security_honeypots";
    private static final Logger LOG = Logger.getLogger(GuardRepo.class.getName());
    private List<Pattern> denyUAs;
    private List<Pattern> honeyList;
    private List<Pattern> acceptableDomains;
    private String canonicalOrigin;
    private String canonicalDomain;
    private Long honeypotFirstBlockTime;

    private static final String FIRST_HONEYPOT_BLOCK_TIME = "security_first_honeypot_block_time";
    @PersistenceUnit
    private EntityManagerFactory PU;
    @EJB
    private IMEADHolder imead;

    @Schedule(hour = "1")
    public void cleanHoneypot() {
        EntityManager em = PU.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Honeypot.cleanHoneypot").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public boolean inHoneypot(String ip) {
        EntityManager em = PU.createEntityManager();
        try {
            em.createNamedQuery("Honeypot.findByIpBeforeNow", Honeypot.class).setParameter("ip", ip).getSingleResult();
            return true;
        } catch (NoResultException n) {
            return false;
        } finally {
            em.close();
        }
    }

    public void putInHoneypot(String ip) {
        if (null == honeypotFirstBlockTime) {
            return;
        }
        Date now = new Date();
        EntityManager em = PU.createEntityManager();
        try {
            em.getTransaction().begin();
            try {
                Honeypot h = em.createNamedQuery("Honeypot.findByIp", Honeypot.class).setParameter("ip", ip).getSingleResult();
                h.setStartedatatime(now);
                long delta = h.getExpiresatatime().getTime() - now.getTime();
                long time = now.getTime() + Math.max(delta * 2, honeypotFirstBlockTime);
                if (time > 9224257554000000L) {
                    time = 9224257554000000L;
                }
                h.setExpiresatatime(new Date(time));
            } catch (NoResultException n) {
                em.persist(new Honeypot(null, new Date(now.getTime() + honeypotFirstBlockTime), ip, now));
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public static boolean matchesAny(CharSequence subject, List<Pattern> regexes) {
        for (Pattern p : regexes) {
            if (p.matcher(subject).matches()) {
                return true;
            }
        }
        return false;
    }

    @PostConstruct
    public void refresh() {
        try {
            honeyList = getPatterns(HONEYPOTS);
            denyUAs = getPatterns(DENY_USER_AGENTS);
            acceptableDomains = getPatterns(ACCEPTABLE_CONTENT_DOMAINS);
            honeypotFirstBlockTime = Long.valueOf(imead.getValue(FIRST_HONEYPOT_BLOCK_TIME));
            Matcher canonicalMatcher = ORIGIN_PATTERN.matcher(imead.getValue(CANONICAL_URL));
            canonicalMatcher.matches();
            canonicalOrigin = canonicalMatcher.group(1);
            canonicalDomain = canonicalMatcher.group(3);
        } catch (NullPointerException n) {
            LOG.log(Level.SEVERE, "A null pointer exception occured while getting security parameters. Are parameters set?");
        }
    }

    private List<Pattern> getPatterns(String key) {
        List<Pattern> temps = new ArrayList<>();
        for (String line : imead.getValue(key).split("\n")) {
            temps.add(Pattern.compile(line.replaceAll("\r", "")));
        }
        return Collections.unmodifiableList(temps);
    }

    /**
     *
     * @return
     * GuardHolder.ORIGIN_PATTERN.matcher(imead.getValue(CANONICAL_URL)).group(1)
     */
    public String getCanonicalOrigin() {
        return canonicalOrigin;
    }

    /**
     *
     * @return
     * GuardHolder.ORIGIN_PATTERN.matcher(imead.getValue(CANONICAL_URL)).group(3)
     */
    public String getCanonicalDomain() {
        return canonicalDomain;
    }

    public List<Pattern> getDenyUAs() {
        return denyUAs;
    }

    public List<Pattern> getHoneyList() {
        return honeyList;
    }

    public Long getHoneypotFirstBlockTime() {
        return honeypotFirstBlockTime;
    }

    /**
     * @return the acceptableDomains to send content to (like google and feedly)
     */
    public List<Pattern> getAcceptableDomains() {
        return acceptableDomains;
    }
}
