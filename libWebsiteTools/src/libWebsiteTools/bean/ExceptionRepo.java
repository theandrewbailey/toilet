package libWebsiteTools.bean;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import libWebsiteTools.db.Exceptionevent;

/**
 * used for tracking errors and see what the script kiddies are picking at.
 *
 * @author alpha
 */
@Singleton
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ExceptionRepo {

    public static final String LOCAL_NAME = "java:module/ExceptionRepo";
    public static final String NEWLINE = "<br/>";
    private static final Logger LOG = Logger.getLogger(ExceptionRepo.class.getName());
    private static final Long EVENT_HORIZON = 86400000L * 90L;
    @Resource
    private ManagedExecutorService exec;
    @PersistenceUnit
    private EntityManagerFactory PU;

    @PostConstruct
    @Schedule(hour = "1")
    public void init() {
        exec.submit(() -> {
            EntityManager em = PU.createEntityManager();
            try {
                em.getTransaction().begin();
                TypedQuery<Exceptionevent> oldErrors = em.createQuery("SELECT e FROM Exceptionevent e WHERE e.atime < :past ORDER BY e.atime DESC", Exceptionevent.class);
                oldErrors.setParameter("past", new Date(new Date().getTime() - EVENT_HORIZON));
                for (Exceptionevent e : oldErrors.getResultList()) {
                    em.remove(e);
                }
                em.getTransaction().commit();
            } finally {
                em.close();
            }
        });
    }

    public void add(HttpServletRequest req, String title, String desc, Throwable t) {
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
            additionalDesc.append(desc).append(ExceptionRepo.NEWLINE);
        }

        if (t != null) {
            StringWriter w = new StringWriter();
            PrintWriter p = new PrintWriter(w, false);
            t.printStackTrace(p);
            p.flush();
            additionalDesc.append(w.toString().replace("\n\tat ", ExceptionRepo.NEWLINE + " at "));
        }

        desc = additionalDesc.toString();

        Exceptionevent e = new Exceptionevent(null, new Date(), desc, title);
        EntityManager em = PU.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(e);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
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

    public List<Exceptionevent> getAll() {
        PU.getCache().evict(Exceptionevent.class);
        EntityManager em = PU.createEntityManager();
        try {
            return em.createNamedQuery("Exceptionevent.findAll", Exceptionevent.class).getResultList();
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

}
