package libWebsiteTools;

import java.io.IOException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
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
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.bean.ExceptionRepo;
import libWebsiteTools.bean.GuardRepo;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.AbstractInput;

@WebFilter(description = "Web analytics request logger and DoS preventer (maybe)", filterName = "OdysseyFilter", dispatcherTypes = {DispatcherType.REQUEST}, urlPatterns = {"/*"})
public class OdysseyFilter implements Filter {

    public static final String ORIGINAL_REQUEST_URL = "$_LIBWEBSITETOOLS_ODYSSEY_REQUEST_URL";
    public static final String KILLED_REQUEST = "$_LIBWEBSITETOOLS_KILLED_REQUEST";
    public static final String HANDLED_ERROR = "$_LIBWEBSITETOOLS_HANDLED_ERROR";
    public static final String ORIGINAL_DOMAIN = "$_LIBWEBSITETOOLS_ORIGINAL_DOMAIN";
    public static final String TIME_PARAM = "$_LIBWEBSITETOOLS_REQUEST_START_TIME";
    public static final String CERTIFICATE_NAME = "security_certificate_name";
    private static final String DEFAULT_REQUEST_ENCODING = "UTF-8";
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
            if (null != imead.getValue(CERTIFICATE_NAME)) {
                CertUtil certs = new CertUtil();
                List<CertPath<X509Certificate>> serverCertificateChain = certs.getServerCertificateChain(imead.getValue(CERTIFICATE_NAME));
                subject = serverCertificateChain.get(0).getCertificates().get(0);
                if (!CertUtil.isValid(subject)) {
                    throw new RuntimeException(String.format("Your server's certificate has expired.\n%s", new Object[]{subject.getSubjectX500Principal().toString()}));
                }
                certDate = CertUtil.getEarliestExperation(serverCertificateChain);
                publicKeyPin = CertUtil.getPublicKeyPins(serverCertificateChain);
                filterConfig.getServletContext().setAttribute(CertUtil.class.getCanonicalName(), certs);
            } else {
                throw new RuntimeException("No certificate name set.");
            }
        } catch (RuntimeException ex) {
            certDate = null;
            LOG.log(Level.WARNING, "High security not available: {0}", ex.getMessage());
            error.add(null, "High security not available: " + ex.getMessage(), null, ex);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Date now = new Date();
        HttpServletRequest req = (HttpServletRequest) request;
        req.setCharacterEncoding(DEFAULT_REQUEST_ENCODING);
        HttpServletResponse res = (HttpServletResponse) response;
        response.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
        // kill suspicious requests
        if (null != guardrepo.getHoneypotFirstBlockTime()) {
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
            }
            if (!"GET".equalsIgnoreCase(req.getMethod())
                    && !"POST".equalsIgnoreCase(req.getMethod())
                    && !"HEAD".equalsIgnoreCase(req.getMethod())
                    && !"OPTIONS".equalsIgnoreCase(req.getMethod())) {
                killInHoney(request, response);
                error.add((HttpServletRequest) request, null, "IP added to honeypot: Illegal method", null);
                return;
            }
        }
        // set variables
        AbstractInput.getTokenURL(req);
        req.setAttribute(TIME_PARAM, now);
        res.setDateHeader(HttpHeaders.DATE, now.getTime());
        if (req.isSecure() && null != subject) {
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
        if (null == res.getHeader(HttpHeaders.CACHE_CONTROL)) {
            res.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=200000");
            res.setDateHeader(HttpHeaders.EXPIRES, now.getTime() + 200000000);
        }

        try {
            if (null != req.getParameter("lang")) {
                Local.resetLocales(req);
                String langParam = req.getParameter("lang");
                if (langParam.isEmpty()) {
                    req.getSession().removeAttribute(Local.OVERRIDE_LOCALE_PARAM);
                } else {
                    Locale selected = Locale.forLanguageTag(req.getParameter("lang"));
                    if (null != selected) {
                        req.getSession().setAttribute(Local.OVERRIDE_LOCALE_PARAM, selected);
                    }
                }
            }
            chain.doFilter(req, res);
            if (res.getStatus() >= 400 && req.getAttribute(HANDLED_ERROR) == null) {
                error.add(req, null, null, null);
            }
        } catch (IOException | ServletException x) {
            LOG.log(Level.SEVERE, "Exception caught in OdysseyFilter", x);
            error.add(req, null, null, x);
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

    public static Date getRequestTime(HttpServletRequest req) {
        return (Date) req.getAttribute(TIME_PARAM);
    }

    @Override
    public void destroy() {
    }
}
