package libWebsiteTools.cache;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import libWebsiteTools.AllBeanAccess;
import libWebsiteTools.imead.Local;
import libWebsiteTools.imead.LocalizedStringNotFoundException;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.tag.HtmlMeta;
import libWebsiteTools.tag.HtmlTime;

/**
 *
 * @author alpha
 */
@WebFilter(description = "Adds security headers and potentially adds to cache.", filterName = "JspFilter", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD}, urlPatterns = {"*.jsp"})
public class JspFilter implements Filter {

    public static final String CONTENT_SECURITY_POLICY = "site_security_csp";
    public static final String FEATURE_POLICY = "site_security_features";
    public static final String PERMISSIONS_POLICY = "site_security_permissions";
    public static final String REFERRER_POLICY = "site_security_referrer";
    public static final String PRIMARY_LOCALE_PARAM = "$_LIBIMEAD_PRIMARY_LOCALE";
    public static final String VARY_HEADER = String.join(", ", new String[]{
        HttpHeaders.ACCEPT_ENCODING, HttpHeaders.ACCEPT_LANGUAGE});

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = ((HttpServletRequest) request);
        HttpServletResponse res = (HttpServletResponse) response;
        AllBeanAccess beans = (AllBeanAccess) req.getAttribute(AllBeanAccess.class.getCanonicalName());
        Locale primaryLocale = Local.resolveLocales(beans.getImead(), req).get(0);
        if (Locale.ROOT.equals(primaryLocale)) {
            primaryLocale = Locale.getDefault();
        }
        request.setAttribute(PRIMARY_LOCALE_PARAM, primaryLocale);
        HtmlMeta.addNameTag(req, "viewport", beans.getImeadValue("site_viewport"));
        HtmlMeta.addLink(req, "shortcut icon", beans.getImeadValue("site_favicon"));
        try {
            String icon = beans.getImeadValue("site_appleTouchIcon");
            if (null != icon) {
                HtmlMeta.addLink(req, "apple-touch-icon", beans.getFile().getFileMetadata(Arrays.asList(icon)).get(0).getUrl());
            }
        } catch (Exception x) {
        }
        try {
            String theme = beans.getImeadValue("site_themeColor");
            if (null != theme) {
                HtmlMeta.addNameTag(req, "theme-color", theme);
            }
        } catch (Exception x) {
        }
        Object csp = request.getAttribute(CONTENT_SECURITY_POLICY);
        res.setHeader("Accept-Ranges", "none");
        res.setHeader(HttpHeaders.VARY, VARY_HEADER);
        res.setHeader(HttpHeaders.CONTENT_LANGUAGE, primaryLocale.toLanguageTag());
        res.setHeader("Content-Security-Policy", null == csp ? beans.getImeadValue(CONTENT_SECURITY_POLICY) : csp.toString());
        if (null != beans.getImeadValue(FEATURE_POLICY)) {
            res.setHeader("Feature-Policy", beans.getImeadValue(FEATURE_POLICY));
        }
        if (null != beans.getImeadValue(PERMISSIONS_POLICY)) {
            res.setHeader("Permissions-Policy", beans.getImeadValue(PERMISSIONS_POLICY));
        }
        if (null != beans.getImeadValue(REFERRER_POLICY)) {
            res.setHeader("Referrer-Policy", beans.getImeadValue(REFERRER_POLICY));
        }
        // unnecessary for modern browsers
        //res.setHeader("X-Frame-Options", "SAMEORIGIN");
        //res.setHeader("X-Xss-Protection", "1; mode=block");
        if (null == request.getAttribute(HtmlTime.FORMAT_VAR)) {
            try {
                request.setAttribute(HtmlTime.FORMAT_VAR, beans.getImead().getLocal(HtmlTime.SITE_DATEFORMAT_LONG, Local.resolveLocales(beans.getImead(), (HttpServletRequest) request)));
            } catch (LocalizedStringNotFoundException lx) {
                request.setAttribute(HtmlTime.FORMAT_VAR, FeedBucket.TIME_FORMAT);
            }
        }
        PageCache cache = beans.getGlobalCache().getCache(req, res);
        if (null != cache) {
            CachedPage page = capturePage(chain, req, res);
            cache.put(PageCache.getLookup(beans.getImead(), req), page);
        } else {
            compressBody(chain, req, res);
        }
        res.flushBuffer();
    }

    private CachedPage capturePage(FilterChain chain, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        AllBeanAccess beans = (AllBeanAccess) req.getAttribute(AllBeanAccess.class.getCanonicalName());
        if (CompressedOutput.Gzip.TYPE.equals(getCompression(req))) {
            res.setHeader(HttpHeaders.CONTENT_ENCODING, CompressedOutput.Gzip.TYPE);
//        } else if (CompressedOutput.Zstd.TYPE.equals(getCompression(req))) {
//            res.setHeader(HttpHeaders.CONTENT_ENCODING, CompressedOutput.Zstd.TYPE);
        }
        String etag = res.getHeader(HttpHeaders.ETAG);
        if (null == etag) {
            etag = PageCache.getETag(beans.getImead(), req);
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
        return new CachedPage(res, responseBytes, PageCache.getLookup(beans.getImead(), req));
    }

    private void compressBody(FilterChain chain, HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        switch (getCompression(req)) {
            case CompressedOutput.Gzip.TYPE:
                try (ServletOutputWrapper<CompressedOutput.Gzip> gzWrap = new ServletOutputWrapper<>(CompressedOutput.Gzip.class, (HttpServletResponse) res)) {
                    chain.doFilter(req, gzWrap);
                    gzWrap.flushBuffer();
                }
                break;
//            case CompressedOutput.Zstd.TYPE:
//                try (ServletOutputWrapper<CompressedOutput.Zstd> zWrap = new ServletOutputWrapper<>(CompressedOutput.Zstd.class, (HttpServletResponse) res)) {
//                    chain.doFilter(req, zWrap);
//                    zWrap.flushBuffer();
//                }
//                break;
            default:
                chain.doFilter(req, res);
                break;
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
            //if (CompressedOutput.Zstd.PATTERN.matcher(encoding).find()) { return CompressedOutput.Zstd.TYPE;
            //} else 
                if (CompressedOutput.Gzip.PATTERN.matcher(encoding).find()) {
                return CompressedOutput.Gzip.TYPE;
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
