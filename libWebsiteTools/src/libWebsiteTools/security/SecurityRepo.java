package libWebsiteTools.security;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.http.HttpServletRequest;
import libWebsiteTools.db.Exceptionevent;
import libWebsiteTools.db.Honeypot;
import libWebsiteTools.db.Repository;
import libWebsiteTools.imead.IMEADHolder;

/**
 * used for tracking IP blocks, errors, and what the script kiddies picking at.
 *
 * @author alpha
 */
public class SecurityRepo implements Repository<Exceptionevent> {

    public static final String LOCAL_NAME = "java:module/ExceptionRepo";
    public static final String NEWLINE = "<br/>";
    // [ origin (with protocol), protocol (with ://), domain and port, path ]
    public static final Pattern ORIGIN_PATTERN = Pattern.compile("^((https?://)([^/]+))(/.*)?$");
    public static final String BASE_URL = "security_baseURL";
    public static final String ALLOWED_ORIGINS = "security_allowedOrigins";
    public static final String DENIED_USER_AGENTS = "security_deniedAgents";
    public static final String HONEYPOTS = "security_honeypots";
    private static final String HONEYPOT_INITIAL_BLOCK_TIME = "security_initialBlock";
    private static final Logger LOG = Logger.getLogger(SecurityRepo.class.getName());
    private final EntityManagerFactory PU;
    private final IMEADHolder imead;
    private final CertUtil certs = new CertUtil();

    public SecurityRepo(EntityManagerFactory PU, IMEADHolder imead) {
        this.PU = PU;
        this.imead = imead;
        evict();
    }

    @Override
    public void evict() {
        EntityManager em = PU.createEntityManager();
        OffsetDateTime localNow = OffsetDateTime.now();
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Honeypot.clean").setParameter("now", localNow).executeUpdate();
            em.createNamedQuery("Exceptionevent.clean").setParameter("past", localNow.minusDays(90)).executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        PU.getCache().evict(Honeypot.class);
        PU.getCache().evict(Exceptionevent.class);
    }

    public void logException(HttpServletRequest req, String title, String desc, Throwable t) {
        LOG.finest("Saving exception");
        if (title == null && req != null) {
            title = getIP(req) + ' ' + req.getMethod() + ' ' + req.getRequestURI();
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
        upsert(Arrays.asList(new Exceptionevent(null, OffsetDateTime.now(), desc, title)));
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

    public static String getIP(HttpServletRequest req) {
        String proxyAddress = req.getHeader("x-forwarded-for");
        return null == proxyAddress ? req.getRemoteAddr() : proxyAddress;
    }

    public boolean inHoneypot(String ip) {
        EntityManager em = PU.createEntityManager();
        try {
            em.createNamedQuery("Honeypot.findByIpBeforeNow", Honeypot.class).setParameter("ip", ip).setParameter("now", OffsetDateTime.now()).getSingleResult();
            return true;
        } catch (NoResultException n) {
            return false;
        } finally {
            em.close();
        }
    }

    public boolean putInHoneypot(String ip) {
        OffsetDateTime localNow = OffsetDateTime.now();
        Long honeypotFirstBlockTime = Long.valueOf(imead.getValue(HONEYPOT_INITIAL_BLOCK_TIME));
        EntityManager em = PU.createEntityManager();
        boolean created = false;
        try {
            em.getTransaction().begin();
            try {
                Honeypot h = em.createNamedQuery("Honeypot.findByIp", Honeypot.class).setParameter("ip", ip).getSingleResult();
                h.setStartedatatime(localNow);
                long delta = Duration.between(h.getExpiresatatime(), localNow).abs().getSeconds();
                long time = localNow.getLong(ChronoField.INSTANT_SECONDS) + Math.max(delta * 2, honeypotFirstBlockTime);
                h.setExpiresatatime(OffsetDateTime.of(LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.from(localNow)), ZoneOffset.from(localNow)));
            } catch (NoResultException n) {
                em.persist(new Honeypot(null, localNow.plusSeconds(honeypotFirstBlockTime), ip, localNow));
                created = true;
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return created;
    }

    public CertUtil getCerts() {
        return certs;
    }
}
