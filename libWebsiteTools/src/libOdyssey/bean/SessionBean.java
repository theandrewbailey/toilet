package libOdyssey.bean;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.http.HttpServletRequest;

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
    private ExceptionRepo error;

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
