package libOdyssey.bean;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.http.HttpServletRequest;
import libOdyssey.db.Exceptionevent;

/**
 * used for tracking errors and such and see what the chinese are trying to pick
 * at
 *
 * @author alpha
 */
@Singleton
public class ExceptionRepo {

    public static final String LOCAL_NAME = "java:module/ExceptionRepo";
    public static final String NEWLINE = "<br/>";
    private static final Logger log = Logger.getLogger(ExceptionRepo.class.getName());
    private static final String EXCEPTION_QUERY = "SELECT e FROM Exceptionevent e ORDER BY e.atime";
    @PersistenceUnit
    private EntityManagerFactory PU;

    public void add(HttpServletRequest req, String title, String desc, Throwable t) {
        log.finest("Saving exception");

        if (title == null && req != null) {
            title = req.getRemoteAddr() + ' ' + req.getMethod() + ' ' + req.getRequestURI();
        } else if (title == null && t != null) {
            title = t.getClass().getName();
        }

        StringBuilder additionalDesc = new StringBuilder();
        if (req != null) {
            if (req.getHeader("User-Agent") != null) {
                additionalDesc.append("User-Agent: ").append(req.getHeader("User-Agent")).append(NEWLINE);
            }
            if (req.getHeader("Referer") != null) {
                additionalDesc.append("Referrer: ").append(req.getHeader("Referer")).append(NEWLINE);
            }
            String requestParams = getParameters(req, NEWLINE);
            if (requestParams != null) {
                additionalDesc.append(requestParams);
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
                params.append(p.getKey()).append(": ").append(s).append(newline);
            }
        }
        return params.length() > 0 ? params.toString() : null;
    }

    public List<Exceptionevent> getAll() {
        PU.getCache().evict(Exceptionevent.class);
        EntityManager em = PU.createEntityManager();
        try {
            return em.createQuery(ExceptionRepo.EXCEPTION_QUERY, Exceptionevent.class).getResultList();
        } finally {
            em.close();
        }
    }
}
