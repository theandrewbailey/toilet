package libWebsiteTools.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.persistence.NoResultException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.AllBeanAccess;
import libWebsiteTools.security.GuardFilter;
import libWebsiteTools.tag.ResponseTag;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.cache.CachedContent;
import libWebsiteTools.cache.PageCache;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;

public abstract class BaseFileServlet extends HttpServlet {

    // 0b1111111111111111111111100000000 == 0x7fffff00 == 2,147,483,392 ms == 24 days, 20 hours 31 minutes 23.392 seconds exactly
    public static final long MAX_AGE_MILLISECONDS = 0b1111111111111111111111100000000;
    public static final String MAX_AGE_SECONDS = Long.toString(MAX_AGE_MILLISECONDS / 1000);
    public static final long YEAR_SECONDS = 31535000;
    public static final Pattern GZIP_PATTERN = Pattern.compile("(?:.*? )?gzip(?:,.*)?");
    public static final Pattern BR_PATTERN = Pattern.compile("(?:.*? )?br(?:,.*)?");
    private static final String CROSS_SITE_REQUEST = "error_cross_site_request";
    // [ origin, timestamp for immutable requests (guaranteed null if not immutable), file path, query string ]
    private static final Pattern FILE_URL = Pattern.compile("^(.*?)file(?:Immutable/([^/]+))?/([^\\?]+)\\??(.*)?");
    @EJB
    protected AllBeanAccess beans;

    public static String getNameFromURL(CharSequence URL) {
        try {
            Matcher m = FILE_URL.matcher(URLDecoder.decode(URL.toString(), "UTF-8"));
            if (!m.matches() || m.groupCount() < 3) {
                throw new NoResultException("Unable to get filename from " + URL);
            }
            return m.group(3);
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        }
    }

    public static boolean isImmutableURL(CharSequence URL) {
        try {
            Matcher m = FILE_URL.matcher(URLDecoder.decode(URL.toString(), "UTF-8"));
            return m.matches() && m.groupCount() > 1 && null != m.group(2);
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        }
    }

    public static String getImmutableURL(String baseURL, Filemetadata f) {
        if (null == baseURL) {
            throw new IllegalArgumentException("base URL empty!");
        } else if (null == f) {
            throw new IllegalArgumentException("no file metadata!");
        } else if (null == f.getFilename() || "".equals(f.getFilename())) {
            throw new IllegalArgumentException("empty filename!");
        } else if (null == f.getAtime()) {
            throw new IllegalArgumentException("no modified time for " + f.getFilename());
        }
        return new StringBuilder(300).append(baseURL).append("fileImmutable/")
                .append(Base64.getUrlEncoder().encodeToString(BigInteger.valueOf(f.getAtime().getTime()).toByteArray()))
                .append("/").append(f.getFilename()).toString();
    }

    public static String getImmutableURL(String baseURL, Fileupload f) {
        return getImmutableURL(baseURL, new Filemetadata(f.getFilename(), f.getAtime()));
    }

    public static List<String> getCompression(HttpServletRequest req) {
        List<String> output = new ArrayList<>();
        String encoding = req.getHeader(HttpHeaders.ACCEPT_ENCODING);
        if (null != encoding) {
            if (BR_PATTERN.matcher(encoding).find()) {
                output.add("br");
            }
            if (GZIP_PATTERN.matcher(encoding).find()) {
                output.add("gzip");
            }
        }
        return output;
    }

    @Override
    protected long getLastModified(HttpServletRequest request) {
        Fileupload c;
        try {
            c = beans.getFile().get(getNameFromURL(request.getRequestURL()));
            c.getAtime();
            request.setAttribute(BaseFileServlet.class.getCanonicalName(), c);
        } catch (Exception ex) {
            return -1;
        }
        return c.getAtime().getTime() / 1000 * 1000;
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Matcher originMatcher = SecurityRepo.ORIGIN_PATTERN.matcher(request.getHeader("Origin"));
        if (null == request.getHeader("Origin")
                || (null == request.getHeader("Access-Control-Request-Method")
                && null == request.getHeader("Access-Control-Request-Headers"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (IMEADHolder.matchesAny(request.getHeader("Origin"), beans.getImead().getPatterns(SecurityRepo.ALLOWED_ORIGINS)) && originMatcher.matches()) {
            response.setHeader("Access-Control-Allow-Origin", originMatcher.group(1));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        response.setHeader("Access-Control-Request-Method", "GET");
        if (null != request.getHeader("Access-Control-Request-Headers")) {
            response.setHeader("Access-Control-Request-Headers", request.getHeader("Access-Control-Request-Headers"));
        }
        response.setHeader("Access-Control-Max-Age", MAX_AGE_SECONDS);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Fileupload c = (Fileupload) request.getAttribute(BaseFileServlet.class.getCanonicalName());
        if (null == c) {
            try {
                String name = getNameFromURL(request.getRequestURL());
                c = beans.getFile().get(name);
                if (null == c) {
                    throw new FileNotFoundException(name);
                }
                request.setAttribute(BaseFileServlet.class.getCanonicalName(), c);
            } catch (FileNotFoundException ex) {
                response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=" + MAX_AGE_SECONDS);
                response.setDateHeader(HttpHeaders.EXPIRES, new Date().getTime() + MAX_AGE_MILLISECONDS);
                if (HttpMethod.HEAD.equals(request.getMethod()) && fromApprovedDomain(request, beans.getImead().getPatterns(SecurityRepo.ALLOWED_ORIGINS))) {
                    request.setAttribute(GuardFilter.HANDLED_ERROR, true);
                }
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        if (!isAuthorized(request, c.getMimetype(), beans.getImead().getPatterns(SecurityRepo.ALLOWED_ORIGINS))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            beans.getError().logException(request, null, beans.getImead().getLocal(CROSS_SITE_REQUEST, Local.resolveLocales(beans.getImead(), request)), null);
            request.setAttribute(GuardFilter.HANDLED_ERROR, true);
            return;
        }

//        if (null != request.getHeader("Referer")) {
//            Matcher originMatcher = SecurityRepo.ORIGIN_PATTERN.matcher(request.getHeader("Referer"));
//            if (null != imead.getPatterns(SecurityRepo.ACCEPTABLE_CONTENT_DOMAINS)
//                    && SecurityRepo.matchesAny(request.getHeader("Referer"), imead.getPatterns(SecurityRepo.ACCEPTABLE_CONTENT_DOMAINS)) && originMatcher.matches()) {
//                response.setHeader("Access-Control-Allow-Origin", originMatcher.group(1));
//            } else if (null != guard.getCanonicalOrigin()) {
//                response.setHeader("Access-Control-Allow-Origin", guard.getCanonicalOrigin());
//            }
//        } else {
        Matcher canonicalMatcher = SecurityRepo.ORIGIN_PATTERN.matcher(request.getAttribute(SecurityRepo.BASE_URL).toString());
        canonicalMatcher.matches();
        response.setHeader("Access-Control-Allow-Origin", canonicalMatcher.group(1));
//        }
        if (isImmutableURL(request.getRequestURL())) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=" + YEAR_SECONDS + ", immutable");
            response.setDateHeader(HttpHeaders.EXPIRES, new Date().getTime() + (YEAR_SECONDS * 1000));
        } else {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=" + MAX_AGE_SECONDS);
            response.setDateHeader(HttpHeaders.EXPIRES, new Date().getTime() + MAX_AGE_MILLISECONDS);
        }
        response.setDateHeader(HttpHeaders.LAST_MODIFIED, c.getAtime().getTime());
        response.setContentType(c.getMimetype());
        if (!c.getMimetype().startsWith("text")) {
            response.setCharacterEncoding(null);
        }
        String etag;
        List<String> compressions = getCompression(request);
        if (compressions.contains("br") && null != c.getBrdata()) {
            if ((compressions.contains("gzip") && null != c.getGzipdata()
                    && c.getGzipdata().length < c.getBrdata().length)) {
                response.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
                response.setContentLength(c.getGzipdata().length);
                etag = "\"" + c.getEtag() + "g\"";
            } else {
                response.setHeader(HttpHeaders.CONTENT_ENCODING, "br");
                response.setContentLength(c.getBrdata().length);
                etag = "\"" + c.getEtag() + "b\"";
            }
        } else if (compressions.contains("gzip") && null != c.getGzipdata()) {
            response.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
            response.setContentLength(c.getGzipdata().length);
            etag = "\"" + c.getEtag() + "g\"";
        } else {
            response.setContentLength(c.getFiledata().length);
            etag = "\"" + c.getEtag() + "\"";
        }
        response.setHeader(HttpHeaders.ETAG, etag);
        if (etag.equals(request.getHeader(HttpHeaders.IF_NONE_MATCH))) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
        response.setHeader("Accept-Ranges", "none");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHead(request, response);
        Fileupload c = (Fileupload) request.getAttribute(BaseFileServlet.class.getCanonicalName());
        byte[] responseBytes;
        if (HttpServletResponse.SC_OK == response.getStatus() && null != c) {
            try {
                switch (response.getHeader(HttpHeaders.CONTENT_ENCODING)) {
                    case "br":
                        responseBytes = c.getBrdata();
                        break;
                    case "gzip":
                        responseBytes = c.getGzipdata();
                        break;
                    default:
                        responseBytes = c.getFiledata();
                        break;
                }
            } catch (NullPointerException n) {
                responseBytes = c.getFiledata();
            }
            response.getOutputStream().write(responseBytes);
            PageCache global = beans.getGlobalCache().getCache(request, response);
            if (null != global) {
                global.put(PageCache.getLookup(beans.getImead(), request), new CachedContent(beans.getImead().getPatterns(SecurityRepo.ALLOWED_ORIGINS), response, responseBytes, PageCache.getLookup(beans.getImead(), request)));
            }
            request.setAttribute(ResponseTag.RENDER_TIME_PARAM, new Date().getTime() - ((Date) request.getAttribute(GuardFilter.TIME_PARAM)).getTime());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Fileupload> uploadedfiles = FileUtil.getFilesFromRequest(request, "filedata");
            for (Fileupload uploadedfile : uploadedfiles) {
                if (null != beans.getFile().get(uploadedfile.getFilename())) {
                    request.setAttribute("ERROR_MESSAGE", "File exists: " + uploadedfile.getFilename());
                    return;
                }
                uploadedfile.setUrl(getImmutableURL(beans.getImeadValue(SecurityRepo.BASE_URL), uploadedfile));
            }
            beans.getFile().upsert(uploadedfiles);
            PageCache global = beans.getGlobalCache();
            for (Fileupload uploadedfile : uploadedfiles) {
                if (null != global) {
                    global.removeAll(global.searchLookups(uploadedfile.getFilename()));
                }
                beans.getExec().submit(new Brotlier(beans, uploadedfile));
                beans.getExec().submit(new Gzipper(beans, uploadedfile));
            }
            request.setAttribute("uploadedfiles", uploadedfiles);
        } catch (FileNotFoundException fx) {
            request.setAttribute("ERROR_MESSAGE", "File not sent");
        } catch (EJBException | IOException | ServletException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public static boolean isAuthorized(HttpServletRequest req, String mimetype, List<Pattern> acceptableDomains) {
        if (null == mimetype) {
            throw new IllegalArgumentException("No mimetype.");
        }
        if (mimetype.startsWith("image") || mimetype.startsWith("audio") || mimetype.startsWith("video")) {
            try {
                if (!fromApprovedDomain(req, acceptableDomains)) {
                    return false;
                }
            } catch (NullPointerException n) {
                // lucky
            }
        }
        return true;
    }

    public static boolean fromApprovedDomain(HttpServletRequest req, List<Pattern> acceptableDomains) {
        String referrer = req.getHeader("referer");
        if (null != referrer) {
            return IMEADHolder.matchesAny(referrer, acceptableDomains);
        }
        return true;
    }
}
