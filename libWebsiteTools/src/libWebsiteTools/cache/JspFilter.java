package libWebsiteTools.cache;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.BaseAllBeanAccess;
import libWebsiteTools.file.BaseFileServlet;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.HtmlMeta;
import libWebsiteTools.tag.HtmlTime;

/**
 *
 * @author alpha
 */
@WebFilter(description = "Adds security headers and potentially adds to cache.", filterName = "JspFilter", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD}, urlPatterns = {"*.jsp"})
public class JspFilter extends BaseAllBeanAccess implements Filter {

    public static final String CONTENT_SECURITY_POLICY = "security_csp";
    public static final String FEATURE_POLICY = "security_features";
    public static final String REFERRER_POLICY = "security_referrer";
    public static final String PRIMARY_LOCALE_PARAM = "$_LIBIMEAD_PRIMARY_LOCALE";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = ((HttpServletRequest) request);
        HttpServletResponse res = (HttpServletResponse) response;
        Locale primaryLocale = Local.resolveLocales(getImead(), req).get(0);
        request.setAttribute(PRIMARY_LOCALE_PARAM, primaryLocale);
        HtmlMeta.addNameTag(req, "viewport", getImeadValue("site_viewport"));
        HtmlMeta.addLink(req, "shortcut icon", getImeadValue("site_favicon"));
        try {
            String icon = getImeadValue("site_appleTouchIcon");
            if (null != icon) {
                HtmlMeta.addLink(req, "apple-touch-icon", getFile().getFileMetadata(Arrays.asList(icon)).get(0).getUrl());
            }
        } catch (Exception x) {
        }
        Object csp = request.getAttribute(CONTENT_SECURITY_POLICY);
        res.setHeader("Accept-Ranges", "none");
        res.addHeader(HttpHeaders.CONTENT_LANGUAGE, primaryLocale.toLanguageTag());
        res.addHeader("Content-Security-Policy", null == csp ? getImeadValue(CONTENT_SECURITY_POLICY) : csp.toString());
        res.addHeader("Feature-Policy", getImeadValue(FEATURE_POLICY));
        res.addHeader("Referrer-Policy", getImeadValue(REFERRER_POLICY));
        // unnecessary for modern browsers
        //res.addHeader("X-Frame-Options", "SAMEORIGIN");
        //res.addHeader("X-Xss-Protection", "1; mode=block");
        if (null == request.getAttribute(HtmlTime.FORMAT_VAR)) {
            request.setAttribute(HtmlTime.FORMAT_VAR, getImead().getLocal(HtmlTime.SITE_DATEFORMAT_LONG, Local.resolveLocales(getImead(), (HttpServletRequest) request)));
        }
        PageCache cache = getGlobalCache().getCache(req, res);
        if (null != cache) {
            CachedPage page = capturePage(chain, req, res);
            cache.put(PageCache.getLookup(getImead(), req), page);
        } else {
            compressBody(chain, req, res);
        }
        res.flushBuffer();
    }

    private CachedPage capturePage(FilterChain chain, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        if ("gzip".equals(getCompression(req))) {
            res.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
        }
        String etag = res.getHeader(HttpHeaders.ETAG);
        if (null == etag) {
            etag = PageCache.getETag(getImead(), req);
            res.setHeader(HttpHeaders.ETAG, etag);
        }
        ServletOutputWrapper<ServletOutputWrapper.ByteArrayOutput> wrap = new ServletOutputWrapper<>(ServletOutputWrapper.ByteArrayOutput.class, res);
        compressBody(chain, req, wrap);
        wrap.flushBuffer();
        byte[] responseBytes = wrap.getOutputStream().getWriter().toByteArray();
        ServletOutputStream out = res.getOutputStream();
        out.write(responseBytes);
        try {
            out.flush();
        } catch (IOException ix) {
        }
        return new CachedPage(res, responseBytes, PageCache.getLookup(getImead(), req));
    }

    private void compressBody(FilterChain chain, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        if ("gzip".equals(getCompression(req))) {
            try (ServletOutputWrapper<ServletOutputWrapper.GZIPOutput> gzWrap = new ServletOutputWrapper<>(ServletOutputWrapper.GZIPOutput.class, (HttpServletResponse) res)) {
                chain.doFilter(req, gzWrap);
                gzWrap.flushBuffer();
            }
        } else {
            chain.doFilter(req, res);
        }
    }

    /**
     * Checks if client supports gzip compression
     *
     * @param req
     * @return "gzip" or null
     */
    public static String getCompression(HttpServletRequest req) {
        String encoding = req.getHeader(HttpHeaders.ACCEPT_ENCODING);
        if (null != encoding) {
            if (BaseFileServlet.GZIP_PATTERN.matcher(encoding).find()) {
                return "gzip";
                //} else if (BR_PATTERN.matcher(encoding).find()) {
                //  return "br";
            }
        }
        return "none";
    }

    @Override
    public void destroy() {
    }
}
