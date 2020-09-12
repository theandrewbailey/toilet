package libWebsiteTools.security;

import java.io.IOException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.cache.CachedPage;
import libWebsiteTools.cache.PageCache;
import libWebsiteTools.cache.PageCacheProvider;
import libWebsiteTools.cache.PageCaches;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.AbstractInput;

@WebFilter(description = "DoS preventer (maybe) and reverse proxy", filterName = "GuardFilter", dispatcherTypes = {DispatcherType.REQUEST}, urlPatterns = {"/*"})
public class GuardFilter implements Filter {

    public static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";
    public static final String KILLED_REQUEST = "$_LIBWEBSITETOOLS_KILLED_REQUEST";
    public static final String HANDLED_ERROR = "$_LIBWEBSITETOOLS_HANDLED_ERROR";
    public static final String ORIGINAL_DOMAIN = "$_LIBWEBSITETOOLS_ORIGINAL_DOMAIN";
    public static final String TIME_PARAM = "$_LIBWEBSITETOOLS_REQUEST_START_TIME";
    public static final String CERTIFICATE_NAME = "security_certificateName";
    public static final String DEFAULT_REQUEST_ENCODING = "UTF-8";
    public static final String DEFAULT_RESPONSE_ENCODING = "UTF-8";
    public static final String VARY_HEADER = String.join(", ", new String[]{
        HttpHeaders.ACCEPT_ENCODING, HttpHeaders.ACCEPT_LANGUAGE});
    private static final String VIA_HEADER = "site_via";
    private static final Logger LOG = Logger.getLogger(GuardFilter.class.getName());
    private static final Pattern LANG_URL_PATTERN = Pattern.compile("^/([A-Za-z\\-]+?)(?:(/.*?))?$");
    private X509Certificate subject;
    private Date certExpDate;
    @EJB
    private SecurityRepo guardrepo;
    @EJB
    private SecurityRepo error;
    @EJB
    private IMEADHolder imead;
    @Inject
    private PageCacheProvider pageCacheProvider;
    private PageCache globalCache;

    @Override
    public void init(FilterConfig conf) throws ServletException {
        globalCache = (PageCache) pageCacheProvider.getCacheManager().<String, CachedPage>getCache(PageCaches.DEFAULT_URI);
        try {
            if (null != imead.getValue(CERTIFICATE_NAME)) {
                CertUtil certs = new CertUtil();
                List<CertPath<X509Certificate>> serverCertificateChain = certs.getServerCertificateChain(imead.getValue(CERTIFICATE_NAME));
                subject = serverCertificateChain.get(0).getCertificates().get(0);
                if (!CertUtil.isValid(subject)) {
                    throw new RuntimeException(String.format("Your server's certificate has expired.\n%s", new Object[]{subject.getSubjectX500Principal().toString()}));
                }
                certExpDate = CertUtil.getEarliestExperation(serverCertificateChain);
                conf.getServletContext().setAttribute(CertUtil.CERTIFICATE_CHAIN, certs);
            } else {
                throw new RuntimeException("No certificate name set.");
            }
        } catch (RuntimeException ex) {
            certExpDate = null;
            LOG.log(Level.WARNING, "High security not available: {0}", ex.getMessage());
            error.logException(null, "High security not available: " + ex.getMessage(), null, ex);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Date now = new Date();
        HttpServletRequest req = (HttpServletRequest) request;
        req.setCharacterEncoding(DEFAULT_REQUEST_ENCODING);
        HttpServletResponse res = (HttpServletResponse) response;
        response.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
        res.setStatus(HttpServletResponse.SC_OK);
        // kill suspicious requests
        if (null == req.getSession(false) || req.getSession().isNew()) {
            if (guardrepo.inHoneypot(request.getRemoteAddr())) {
                killInHoney(request, response);
                error.logException((HttpServletRequest) request, null, "IP already in honeypot", null);
                return;
            }
            String userAgent = req.getHeader("User-Agent");
            if (userAgent != null && IMEADHolder.matchesAny(userAgent, imead.getPatterns(SecurityRepo.DENIED_USER_AGENTS))) {
                killInHoney(request, response);
                error.logException((HttpServletRequest) request, null, "IP added to honeypot: Illegal User-Agent", null);
                return;
            }
            if (IMEADHolder.matchesAny(req.getRequestURL(), imead.getPatterns(SecurityRepo.HONEYPOTS))) {
                killInHoney(request, response);
                error.logException((HttpServletRequest) request, null, "IP added to honeypot: Illegal URL", null);
                return;
            }
        }
        if (!"GET".equalsIgnoreCase(req.getMethod())
                && !"POST".equalsIgnoreCase(req.getMethod())
                && !"HEAD".equalsIgnoreCase(req.getMethod())
                && !"OPTIONS".equalsIgnoreCase(req.getMethod())) {
            killInHoney(request, response);
            error.logException((HttpServletRequest) request, null, "IP added to honeypot: Illegal method", null);
            return;
        }
        // set variables and headers
        String forwardURL = null;
        AbstractInput.getTokenURL(req);
        req.setAttribute(TIME_PARAM, now);
        req.setAttribute(SecurityRepo.BASE_URL, imead.getValue(SecurityRepo.BASE_URL));
        res.setDateHeader(HttpHeaders.DATE, now.getTime());
        if (req.isSecure() && null != subject) {
            res.addHeader("X-Content-Type-Options", "nosniff");
            try {
                subject.checkValidity();
                if (now.before(certExpDate)) {
                    long difference = certExpDate.getTime() - now.getTime();
                    difference /= 1000;
                    res.setHeader(STRICT_TRANSPORT_SECURITY, "max-age=" + difference + "; includeSubDomains");
                }
            } catch (CertificateExpiredException | CertificateNotYetValidException | RuntimeException ex) {
                error.logException(req, "High security misconfigured", null, ex);
                subject = null;
                certExpDate = null;
            }
        }
        if (null == res.getHeader(HttpHeaders.CACHE_CONTROL)) {
            res.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=200000");
            res.setDateHeader(HttpHeaders.EXPIRES, now.getTime() + 200000000);
        }
        // set request language
        Matcher langMatcher = LANG_URL_PATTERN.matcher(req.getServletPath());
        if (langMatcher.find()) {
            Locale selected = Locale.forLanguageTag(langMatcher.group(1));
            if (null != selected && !Locale.ROOT.equals(selected) && imead.getLocales().contains(selected)) {
                req.setAttribute(Local.OVERRIDE_LOCALE_PARAM, selected);
                req.setAttribute(SecurityRepo.BASE_URL, imead.getValue(SecurityRepo.BASE_URL) + langMatcher.group(1) + "/");
                forwardURL = langMatcher.group(2);
                if (null == forwardURL) {
                    forwardURL = "/";
                }
                if (null != req.getQueryString()) {
                    forwardURL += "?" + req.getQueryString();
                }
            }
        }
        // carry on
        try {
            if (!reverseProxy(req, res)) {
                if (null != forwardURL) {
                    req.getServletContext().getRequestDispatcher(forwardURL).forward(request, response);
                } else {
                    chain.doFilter(req, res);
                }
            }
            if (res.getStatus() >= 400 && req.getAttribute(HANDLED_ERROR) == null) {
                error.logException(req, null, null, null);
            }
        } catch (IOException | ServletException x) {
            LOG.log(Level.SEVERE, "Exception caught in OdysseyFilter", x);
            error.logException(req, null, null, x);
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

    /**
     * Look up response from cache, and if found, write to response. Bypass with
     * "nocache" URL query parameter.
     *
     * @param req
     * @param res
     * @return if something was found and written to response
     * @throws IOException
     */
    private boolean reverseProxy(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (null != req.getParameter("nocache")) {
            res.setHeader(HttpHeaders.ETAG, PageCache.getETag(req, imead));
            return false;
        }
        String lookup = PageCache.getLookup(req, imead);
        res.addHeader(HttpHeaders.VARY, VARY_HEADER);
        String ifNoneMatch = req.getHeader(HttpHeaders.IF_NONE_MATCH);
        if (null == ifNoneMatch) {
            ifNoneMatch = "";
        }
        CachedPage page;
        switch (req.getMethod()) {
            case HttpMethod.GET:
                page = globalCache.get(lookup);
                if (null != page && page.isApplicable(req)) {
                    writeHeaders(res, page);
                    if (ifNoneMatch.equals(page.getHeader(HttpHeaders.ETAG)) || ifNoneMatch.equals(PageCache.getETag(req, imead))) {
                        res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                        return true;
                    }
                    ServletOutputStream out = res.getOutputStream();
                    out.write(page.getBody());
                    out.flush();
                    res.flushBuffer();
                    page.hit();
                    return true;
                }
                res.setHeader(HttpHeaders.ETAG, PageCache.getETag(req, imead));
                break;
            case HttpMethod.HEAD:
                page = globalCache.get(lookup);
                if (null != page && page.isApplicable(req)) {
                    if (ifNoneMatch.equals(PageCache.getETag(req, imead))) {
                        res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    }
                    writeHeaders(res, page);
                    page.hit();
                    return true;
                }
                res.setHeader(HttpHeaders.ETAG, PageCache.getETag(req, imead));
                break;
            default:
                break;
        }
        return false;
    }

    private void writeHeaders(HttpServletResponse res, CachedPage page) {
        res.setHeader("Via", imead.getValue(VIA_HEADER));
        res.setStatus(page.getStatus());
        if (null != page.getContentType()) {
            res.setContentType(page.getContentType());
        }
        for (Map.Entry<String, String> field : page.getHeaders().entrySet()) {
            res.setHeader(field.getKey(), field.getValue());
        }
    }

    @Override
    public void destroy() {
    }
}
