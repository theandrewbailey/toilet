package libOdyssey.bean;

import java.util.Date;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.RequestTime;
import libOdyssey.ResponseTag;
import libOdyssey.db.Page;
import libOdyssey.db.Pagerequest;

/**
 *
 * @author alpha
 */
@Stateful(mappedName = SessionBean.LOCAL_NAME)
public class SessionBean {

    public static final String LOCAL_NAME = "java:module/SessionBean";
    public static final String HTTP_SESSION_OBJECT = "$_LIBODYSSEY_HTTP_SESSION_OBJECT";
    public static final String SESSION_BEAN = "$_LIBODYSSEY_SESSION_BEAN";
    private static final String FIRST_REQUEST = "SELECT p FROM Pagerequest p WHERE p.pageid.pageid= :page ORDER BY p.atime DESC";
    private static final String SUBSEQUENT_REQUEST = "SELECT p FROM Pagerequest p WHERE p.httpsessionid= :http AND p.pageid.pageid= :page ORDER BY p.atime DESC";
    @PersistenceUnit
    private EntityManagerFactory PU;
    @EJB
    private Analyzer anal;
    @EJB
    private ExceptionRepo exr;
    @EJB
    private GuardHolder guard;

    public void logRequest(HttpServletRequest req, HttpServletResponse res) {
        EntityManager em = PU.createEntityManager();
        boolean newSession = false;
        Long renderTime=(Long)req.getAttribute(ResponseTag.RENDER_TIME_PARAM);

        // log it
        String urlstr = getURL(req);
        if (urlstr == null) {
            return;
        }

        try {
            em.getTransaction().begin();

            // get DB row corresponding to URL
            Page page = anal.getPageByUrl(urlstr);

            // create DB row for this page request
//            Pagerequest pr = new Pagerequest(null, new java.util.Date(), req.getMethod(), res.getStatus(), 0);
//            pr.setPageid(page);
//            if (renderTime != null) {
//                pr.setRendered(renderTime.intValue());
//            }
//            if (req.getHeader("Referer") != null) {
//                TypedQuery<Pagerequest> q = em.createQuery(newSession ? FIRST_REQUEST : SUBSEQUENT_REQUEST, Pagerequest.class);
//                String referred=req.getHeader("Referer");
//                if (!newSession) {
//                    referred = referred.substring(referred.indexOf(guard.getHostValue()) + guard.getHostValue().length());
//                    referred = getURL(req.getServletContext().getContextPath(), referred, null);
//                }
//                q.setParameter("page", anal.getPageByUrl(referred).getPageid());
//                q.setMaxResults(1);
//                try {
//                    pr.setCamefrompagerequestid(q.getSingleResult());
//                } catch (NoResultException x) {
//                }
//            }
//            pr.setParameters(ExceptionRepo.getParameters(req, "\n"));
//
//            pr.setServed((int)(new Date().getTime() - ((Date)req.getAttribute(RequestTime.TIME_PARAM)).getTime()));
//            em.persist(pr);
        } catch (Exception x) {
            exr.add(req, null, null, x);
        } finally {
            em.getTransaction().commit();
        }
    }

    /**
     * @param req
     * @return the url string, with parameters
     */
    public static String getURL(HttpServletRequest req) {
        return getURL(req.getServletContext().getContextPath(), req.getRequestURI(), req.getQueryString());
    }

    public static String getURL(String contextPath, String uri, String query) {
        if (uri == null) {
            uri = "/";
        }
        if (uri.startsWith(contextPath)){
            uri=uri.substring(contextPath.length());
        }
        return query != null ? uri + "?" + query : uri;
    }
}
