package libOdyssey;

import java.io.IOException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
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
import javax.ws.rs.core.HttpHeaders;
import libOdyssey.bean.ExceptionRepo;
import libOdyssey.bean.GuardRepo;
import libOdyssey.bean.SessionBean;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.AbstractInput;
import libWebsiteTools.tag.HtmlTime;

@WebFilter(description = "Web analytics request logger and DoS preventer (maybe)", filterName = "OdysseyFilter", dispatcherTypes = {DispatcherType.REQUEST}, urlPatterns = {"/*"})
public class OdysseyFilter implements Filter {

    public static final String KILLED_REQUEST = "$_LIBODYSSEY_KILLED_REQUEST";
    public static final String HANDLED_ERROR = "$_LIBODYSSEY_HANDLED_ERROR";
    public static final String ORIGINAL_DOMAIN = "$_LIBODYSSEY_ORIGINAL_DOMAIN";
    public static final String TIME_PARAM = "$_LIBODYSSEY_REQUEST_START_TIME";
    public static final String CERTIFICATE_NAME = "libOdyssey_certificate_name";
    //private static final String DEFAULT_REQUEST_ENCODING = "UTF-8";
    private static final Logger LOG = Logger.getLogger(OdysseyFilter.class.getName());
    private static final String DEFAULT_RESPONSE_ENCODING = "UTF-8";
    private String publicKeyPin;
    private X509Certificate subject;
    private Date certDate;
    @EJB
    private GuardRepo guardrepo;
    @EJB
    private ExceptionRepo error;
    @EJB
    private IMEADHolder imead;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            CertUtil certs = new CertUtil();
            filterConfig.getServletContext().setAttribute(CertUtil.class.getCanonicalName(), certs);
            List<CertPath<X509Certificate>> serverCertificateChain = certs.getServerCertificateChain(imead.getValue(CERTIFICATE_NAME));
            CertPath<X509Certificate> path = serverCertificateChain.get(0);
            subject = path.getCertificates().get(0);
            LinkedHashSet<String> publicKeyPins = new LinkedHashSet<>(serverCertificateChain.size());
            if (!CertUtil.isValid(subject)) {
                throw new RuntimeException("Your server's certificate has expired.");
            }
            certDate = subject.getNotAfter();
            for (X509Certificate cert : serverCertificateChain.get(0).getCertificates()) {
                if (CertUtil.isValid(cert)) {
                    publicKeyPins.add(String.format("pin-sha256=\"%s\"", new Object[]{CertUtil.getCertificatePinSHA256(cert)}));
                    if (cert.getNotAfter().before(certDate)) {
                        certDate = cert.getNotAfter();
                    }
                } else {
                    throw new RuntimeException(String.format("A certificate in your trust chain is not valid!\n%s", new Object[]{cert.getSubjectX500Principal().toString()}));
                }
            }
            if (!publicKeyPins.isEmpty()) {
                publicKeyPins.add("max-age=");
                publicKeyPin = String.join("; ", publicKeyPins);
            }
        } catch (RuntimeException ex) {
            certDate = null;
            LOG.log(Level.WARNING, "High security not available", ex);
            error.add(null, "High security not available", null, ex);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Date now = new Date();
        HttpServletRequest req = (HttpServletRequest) request;
        // kill suspicious requests
        if (guardrepo.isEnableGuard() && !("127.0.0.1".equals(req.getRemoteAddr()) || "::0".equals(req.getRemoteAddr()))) {
            if (null == req.getSession(false) || req.getSession().isNew()) {
                if (guardrepo.inHoneypot(request.getRemoteAddr())) {
                    killInHoney(request, response);
                    error.add((HttpServletRequest) request, null, "IP already in honeypot", null);
                    return;
                }
                String userAgent = req.getHeader("User-Agent");
                if (userAgent != null && GuardRepo.matchesAny(userAgent, guardrepo.getDenyUAs())) {
                    killInHoney(request, response);
                    error.add((HttpServletRequest) request, null, "IP added to honeypot: Illegal User-Agent", null);
                    return;
                }
                if (GuardRepo.matchesAny(req.getRequestURL(), guardrepo.getHoneyList())) {
                    killInHoney(request, response);
                    error.add((HttpServletRequest) request, null, "IP added to honeypot: Illegal URL", null);
                    return;
                }
                if (!GuardRepo.matchesAny(req.getRequestURL(), guardrepo.getDomains())) {
                    kill(request, response);
                    return;
                }
            }
            if (!"GET".equalsIgnoreCase(req.getMethod())
                    && !"POST".equalsIgnoreCase(req.getMethod())
                    && !"HEAD".equalsIgnoreCase(req.getMethod())
                    && !"OPTIONS".equalsIgnoreCase(req.getMethod())) {
                killInHoney(request, response);
                error.add((HttpServletRequest) request, null, "IP added to honeypot: Illegal method", null);
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

        // check CSRF token
//        if (!checkRequestToken(req)) {
//            kill(request, response);
//            return;
//        }
        AbstractInput.getTokenURL(req);

        // set variables
        //NoServerHeader res = new NoServerHeader((HttpServletResponse) response);
        HttpServletResponse res = (HttpServletResponse) response;
        req.setAttribute(TIME_PARAM, now);
        res.setDateHeader(HttpHeaders.DATE, now.getTime());
        if (req.isSecure() && null != subject) {
            res.addHeader("X-Frame-Options", "SAMEORIGIN");
            res.addHeader("X-Xss-Protection", "1; mode=block");
            res.addHeader("X-Content-Type-Options", "nosniff");
            try {
                subject.checkValidity();
                if (now.before(certDate)) {
                    long difference = certDate.getTime() - now.getTime();
                    difference /= 1000;
                    res.setHeader("Strict-Transport-Security", "max-age=" + difference + "; includeSubDomains");
                    if (null != publicKeyPin) {
                        res.setHeader("Public-Key-Pins", publicKeyPin + difference + "; includeSubDomains");
                    }
                }
            } catch (CertificateExpiredException | CertificateNotYetValidException | RuntimeException ex) {
                error.add(req, "High security misconfigured", null, ex);
                subject = null;
                certDate = null;
            }
        }
        response.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
        if (null == req.getAttribute(HtmlTime.FORMAT_VAR)) {
            req.setAttribute(HtmlTime.FORMAT_VAR, imead.getLocal("page_dateFormat", Local.resolveLocales(req)));
        }
        if (null == res.getHeader(HttpHeaders.CACHE_CONTROL)) {
            res.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=200000");
            res.setDateHeader(HttpHeaders.EXPIRES, now.getTime() + 200000000);
        }

        // move along
        if (guardrepo.isHandleErrors()) {
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
        res.flushBuffer();
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

    public static Date getRequestTime(HttpServletRequest req) {
        return (Date) req.getAttribute(TIME_PARAM);
    }
//
//    /**
//     * check for the security token on the request.
//     *
//     * @param req
//     * @return true if everything passed
//     * @throws SecurityTokenInvalidException if request is fraudulent
//     */
//    private boolean checkRequestToken(HttpServletRequest req) {
//        if ("POST".equalsIgnoreCase(req.getMethod()) && req.getServletContext().getAttribute(DISABLE_TOKEN_CHECKING) == null) {
//            if (req.getSession(false) == null) {
//                throw new RequestTokenInvalidException();
//            }
//            if (req.getSession().getAttribute(DISABLE_TOKEN_CHECKING) != null) {
//                return true;
//            }
//            String tokenHash = RequestToken.getHash(req);
//            if (tokenHash == null) {
//                throw new RequestTokenInvalidException();
//            }
//            RequestTokenBucket bucket = RequestTokenBucket.getRequestTokenBucket(req);
//            req.setAttribute(RequestToken.ID_NAME, req.getParameter(tokenHash));
//            /*if (req.getServletContext().getAttribute(DISABLE_REFERRER_CHECKING) == null && 
//                    req.getSession().getAttribute(DISABLE_REFERRER_CHECKING) == null) {
//                if (!bucket.claimToken(req.getParameter(tokenHash), req.getHeader("referer"))){
//                    throw new RequestTokenInvalidException();
//                }
//            }
//            else {*/
//            if (!bucket.claimToken(req.getParameter(tokenHash))) {
//                throw new RequestTokenInvalidException();
//            }
//            //}
//        }
//        return true;
//    }

    @Override
    public void destroy() {
    }
}
