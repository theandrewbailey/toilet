package libWebsiteTools.security;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import libWebsiteTools.db.Exceptionevent;
import libWebsiteTools.db.Honeypot;
import libWebsiteTools.db.Repository;
import libWebsiteTools.imead.IMEADHolder;

/**
 * used for tracking IP blocks, errors, and what the script kiddies picking at.
 *
 * @author alpha
 */
@Singleton
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SecurityRepo implements Repository<Exceptionevent> {

    public static final String LOCAL_NAME = "java:module/ExceptionRepo";
    public static final String NEWLINE = "<br/>";
    // [ origin (with protocol), protocol (with ://), domain and port, path ]
    public static final Pattern ORIGIN_PATTERN = Pattern.compile("^((https?://)([^/]+))(/.*)?$");
    public static final String BASE_URL = "security_baseURL";
    public static final String ALLOWED_ORIGINS = "security_allowedOrigins";
    public static final Map<String, Object> USE_CACHE_HINT = Collections.unmodifiableMap(new HashMap<String, Object>() {
        {
            put("javax.persistence.cache.retrieveMode", "USE");
            put("javax.persistence.cache.storeMode", "USE");
        }
    });
    public static final String DENIED_USER_AGENTS = "security_deniedAgents";
    public static final String HONEYPOTS = "security_honeypots";
    private static final String HONEYPOT_INITIAL_BLOCK_TIME = "security_initialBlock";
    private static final Logger LOG = Logger.getLogger(SecurityRepo.class.getName());
    private static final Long EVENT_HORIZON = 86400000L * 90L;
    @Resource
    private ManagedExecutorService exec;
    @EJB
    private IMEADHolder imead;
    @PersistenceUnit
    private EntityManagerFactory PU;

    @PostConstruct
    @Schedule(hour = "1")
    @Override
    public void evict() {
        exec.submit(() -> {
            EntityManager em = PU.createEntityManager();
            try {
                em.getTransaction().begin();
                em.createNamedQuery("Honeypot.clean").executeUpdate();
                em.createNamedQuery("Exceptionevent.clean").setParameter("past", new Date(new Date().getTime() - EVENT_HORIZON)).executeUpdate();
                em.getTransaction().commit();
            } finally {
                em.close();
            }
            PU.getCache().evict(Honeypot.class);
            PU.getCache().evict(Exceptionevent.class);
        });
    }

    public void logException(HttpServletRequest req, String title, String desc, Throwable t) {
        LOG.finest("Saving exception");
        if (title == null && req != null) {
            title = req.getRemoteAddr() + ' ' + req.getMethod() + ' ' + req.getRequestURI();
        } else if (title == null && t != null) {
            title = t.getClass().getName();
        }
        StringBuilder additionalDesc = new StringBuilder(1000);
        if (req != null) {
            additionalDesc.append("Headers<br/>");
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                Enumeration<String> headers = req.getHeaders(headerName);
                while (headers.hasMoreElements()) {
                    String header = headers.nextElement();
                    additionalDesc.append(headerName).append(": ").append(htmlFormat(header)).append(NEWLINE);
                }
            }
            String requestParams = getParameters(req, NEWLINE);
            if (requestParams != null) {
                additionalDesc.append("<br/>Parameters<br/>").append(requestParams);
            }
        }
        if (desc != null) {
            additionalDesc.append(desc).append(SecurityRepo.NEWLINE);
        }
        if (t != null) {
            StringWriter w = new StringWriter();
            PrintWriter p = new PrintWriter(w, false);
            t.printStackTrace(p);
            p.flush();
            additionalDesc.append(w.toString().replace("\n\tat ", SecurityRepo.NEWLINE + " at "));
        }
        desc = additionalDesc.toString();
        upsert(Arrays.asList(new Exceptionevent(null, new Date(), desc, title)));
    }

    @Override
    public Collection<Exceptionevent> upsert(Collection<Exceptionevent> entities) {
        EntityManager em = PU.createEntityManager();
        try {
            em.getTransaction().begin();
            for (Exceptionevent e : entities) {
                em.persist(e);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return entities;
    }

    public static String getParameters(HttpServletRequest req, String newline) {
        StringBuilder params = new StringBuilder(10000);
        for (Entry<String, String[]> p : req.getParameterMap().entrySet()) {
            for (String s : p.getValue()) {
                params.append(p.getKey()).append(": ").append(htmlFormat(s)).append(newline);
            }
        }
        return params.length() > 0 ? params.toString() : null;
    }

    @Override
    public List<Exceptionevent> getAll(Integer limit) {
        PU.getCache().evict(Exceptionevent.class);
        EntityManager em = PU.createEntityManager();
        try {
            TypedQuery<Exceptionevent> q = em.createNamedQuery("Exceptionevent.findAll", Exceptionevent.class);
            if (null != limit) {
                q.setMaxResults(limit);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * removes validation breaking characters from the given string
     *
     * @param in input strings
     * @return formatted string
     */
    public static String htmlFormat(String in) {
        if (null == in) {
            return "";
        }
        StringBuilder sb = new StringBuilder(in.length() + 1000);
        in = removeSpaces(in);
        for (char c : in.toCharArray()) {
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '\r':
                    continue;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * removes gratoutious amounts of spaces in the given string
     *
     * @param in input string
     * @return sans extra spaces
     */
    public static String removeSpaces(String in) {
        StringBuilder sb = new StringBuilder();
        for (String r : in.split(" ")) {
            if (!r.isEmpty()) {
                sb.append(r);
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    @Override
    public Exceptionevent get(Object id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Exceptionevent delete(Object id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processArchive(Consumer<Exceptionevent> operation, Boolean transaction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long count() {
        throw new UnsupportedOperationException();
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
        Long honeypotFirstBlockTime = Long.valueOf(imead.getValue(HONEYPOT_INITIAL_BLOCK_TIME));
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
}
