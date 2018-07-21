package libOdyssey;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import libOdyssey.bean.ExceptionRepo;
import libOdyssey.bean.GuardHolder;
import libOdyssey.bean.GuardRepo;
import libOdyssey.bean.SessionBean;
import libWebsiteTools.imead.IMEADHolder;

@WebFilter(description = "Web analytics request logger and DoS preventer (maybe)", filterName = "OdysseyFilter", dispatcherTypes = {DispatcherType.REQUEST}, urlPatterns = {"/*"})
public class OdysseyFilter implements Filter {

    public static final String KILLED_REQUEST = "$_LIBODYSSEY_KILLED_REQUEST";
    public static final String HANDLED_ERROR = "$_LIBODYSSEY_HANDLED_ERROR";
    public static final String ORIGINAL_URL = "$_LIBODYSSEY_ORIGINAL_URL";
    public static final String ORIGINAL_DOMAIN = "$_LIBODYSSEY_ORIGINAL_DOMAIN";
    private static final Logger LOG = Logger.getLogger(OdysseyFilter.class.getName());
    private static final String CERTIFICATE_NAME = "libOdyssey_certificate_name";
    @EJB
    private GuardHolder guardholder;
    @EJB
    private GuardRepo guardrepo;
    @EJB
    private ExceptionRepo error;
    @EJB
    private IMEADHolder imead;
    private X509Certificate cert;
    private Date certDate;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (null != imead.getValue(CERTIFICATE_NAME)) {
            try {
                KeyStore jks = KeyStore.getInstance("JKS");
                char[] pass = null;
                if (null != System.getProperty("javax.net.ssl.keyStorePassword", null)) {
                    pass = System.getProperty("javax.net.ssl.keyStorePassword").toCharArray();
                }
                jks.load(new FileInputStream(System.getProperty("javax.net.ssl.keyStore")), pass);
                cert = (X509Certificate) jks.getCertificate(imead.getValue(CERTIFICATE_NAME));
                cert.checkValidity();
                certDate = cert.getNotAfter();
            } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
                error.add(null, "High security not available", null, ex);
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // validate request
        HttpServletRequest req = (HttpServletRequest) request;
        if (guardholder.isEnableGuard()) {
            if (guardrepo.inHoneypot(request.getRemoteAddr())) {
                killInHoney(request, response);
                error.add((HttpServletRequest) request, null, "IP already in honeypot", null);
                return;
            }
            if ("TRACE".equalsIgnoreCase(req.getMethod())
                    || "OPTIONS".equalsIgnoreCase(req.getMethod())
                    || "CONNECT".equalsIgnoreCase(req.getMethod())) {
                killInHoney(request, response);
                error.add((HttpServletRequest) request, null, "IP added to honeypot: Illegal method", null);
                return;
            }
            String userAgent = req.getHeader("User-Agent");
            if (userAgent != null && GuardHolder.matchesAny(userAgent, guardholder.getDenyUAs())) {
                killInHoney(request, response);
                error.add((HttpServletRequest) request, null, "IP added to honeypot: Illegal User-Agent", null);
                return;
            }
            if (GuardHolder.matchesAny(req.getRequestURL(), guardholder.getHoneyList())) {
                killInHoney(request, response);
                error.add((HttpServletRequest) request, null, "IP added to honeypot: Illegal URL", null);
                return;
            }
            if (!GuardHolder.matchesAny(req.getRequestURL(), guardholder.getDomains())) {
                kill(request, response);
                return;
            }
//            if (req.getSession().isNew()) {
//                int[] sps = guardholder.getSps();
//                int[] es = guardholder.getEs();
//                if (guardrepo.sessionsPerSecond(request.getRemoteAddr(), sps[0], sps[1])
//                        || guardrepo.emptySessionCheck(request.getRemoteAddr(), es[0], es[1], es[2])) {
//                    kill(request, response);
//                    return;
//                }
//            }
        }

        // process request
        //NoServerHeader res = new NoServerHeader((HttpServletResponse) response);
        HttpServletResponse res = (HttpServletResponse) response;
        if (req.isSecure() && null != cert) {
            res.addHeader("X-Frame-Options", "SAMEORIGIN");
            res.addHeader("X-Xss-Protection", "1; mode=block");
            res.addHeader("X-Content-Type-Options", "nosniff");
            try {
                cert.checkValidity();
                Date now = RequestTime.getRequestTime(req);
                if (now.before(certDate)) {
                    long difference = certDate.getTime() - now.getTime();
                    difference /= 1000;
                    res.setHeader("Strict-Transport-Security", "max-age=" + difference + "; includeSubDomains");
                }
            } catch (CertificateExpiredException | CertificateNotYetValidException ex) {
                error.add(req, "High security misconfigured", null, ex);
                cert = null;
                certDate = null;
            }
        }
        if (guardholder.isHandleErrors()) {
            try {
                chain.doFilter(req, res);
                if (res.getStatus() >= 400 && req.getAttribute(HANDLED_ERROR) == null) {
                    error.add(req, null, null, null);
                }
            } catch (IOException | ServletException x) {
                LOG.log(Level.SEVERE, "Exception caught in OdysseyFilter", x);
                error.add(req, null, null, x);
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    public void killInHoney(ServletRequest request, ServletResponse response) throws IOException {
        kill(request, response);
        guardrepo.putInHoneypot(request.getRemoteAddr());
    }

    public static void kill(ServletRequest request, ServletResponse response) throws IOException {
        request.setAttribute(KILLED_REQUEST, KILLED_REQUEST);
        request.getInputStream().close();
        response.getOutputStream().close();
    }

    public static SessionBean getSessionBean(HttpServletRequest req) {
        HttpSession sess = req.getSession();
        SessionBean sbean = (SessionBean) sess.getAttribute(SessionBean.SESSION_BEAN);
        if (sbean == null) {
            try {
                InitialContext context = new InitialContext();
                sbean = (SessionBean) context.lookup(SessionBean.LOCAL_NAME);
                sess.setAttribute(SessionBean.SESSION_BEAN, sbean);
            } catch (NamingException ex) {
                LOG.log(Level.SEVERE, "Session bean lookup name invalid: " + SessionBean.LOCAL_NAME, ex);
            }
        }
        return sbean;
    }

    @Override
    public void destroy() {
    }
}
