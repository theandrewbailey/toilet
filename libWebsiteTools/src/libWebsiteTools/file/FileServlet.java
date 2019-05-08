package libWebsiteTools.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.persistence.NoResultException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import libOdyssey.OdysseyFilter;
import libOdyssey.ResponseTag;
import libOdyssey.bean.ExceptionRepo;
import libOdyssey.bean.GuardRepo;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;

public abstract class FileServlet extends HttpServlet {

    // 0b1111111111111111111111100000000 == 0x7fffff00 == 2,147,483,392 ms == 24 days, 20 hours 31 minutes 23.392 seconds exactly
    public static final long MAX_AGE_MILLISECONDS = 0b1111111111111111111111100000000;
    public static final String MAX_AGE_SECONDS = Long.toString(MAX_AGE_MILLISECONDS / 1000);
    public static final long YEAR_SECONDS = 31535000;
    public static final Pattern GZIP_PATTERN = Pattern.compile("(?:.*? )?gzip(?:,.*)?");
    public static final Pattern BR_PATTERN = Pattern.compile("(?:.*? )?br(?:,.*)?");
    private static final String UNAUTHORIZED_CONTENT_REQUEST = "content_unauthorized";
    // [ origin, timestamp for immutable requests (guaranteed null if not immutable), file path, query string ]
    private static final Pattern FILE_URL = Pattern.compile("^(.*?)/content(?:Immutable/([^/]+))?/([^\\?]+)\\??(.*)?");
    @EJB
    protected FileRepo file;
    @EJB
    protected IMEADHolder imead;
    @EJB
    protected ExceptionRepo error;
    @EJB
    protected GuardRepo guard;
    @Resource
    protected ManagedExecutorService exec;

    abstract protected String getBaseURL();

    public static String getNameFromURL(CharSequence URL) {
        try {
            Matcher m = FILE_URL.matcher(URLDecoder.decode(URL.toString(), "UTF-8"));
            if (!m.matches() || m.groupCount() < 3) {
                throw new NoResultException("Unable to get content filename from " + URL);
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

    public static String getImmutableURL(String canonicalURL, Filemetadata f) {
        if (null == canonicalURL || "".equals(canonicalURL)) {
            throw new IllegalArgumentException("canonical URL empty!");
        } else if (null == f) {
            throw new IllegalArgumentException("no file metadata!");
        } else if (null == f.getFilename() || "".equals(f.getFilename())) {
            throw new IllegalArgumentException("empty filename!");
        } else if (null == f.getAtime()) {
            throw new IllegalArgumentException("no modified time for "+f.getFilename());
        }
        return new StringBuilder(300).append(canonicalURL).append("contentImmutable/")
                .append(Base64.getUrlEncoder().encodeToString(BigInteger.valueOf(f.getAtime().getTime()).toByteArray()))
                .append("/").append(f.getFilename()).toString();
    }

    @Override
    protected long getLastModified(HttpServletRequest request) {
        Fileupload c;
        try {
            c = file.getFile(getNameFromURL(request.getRequestURL()));
            c.getAtime();
            request.setAttribute(FileServlet.class.getCanonicalName(), c);
        } catch (Exception ex) {
            return -1;
        }
        return c.getAtime().getTime() / 1000 * 1000;
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Matcher originMatcher = GuardRepo.ORIGIN_PATTERN.matcher(request.getHeader("Origin"));
        if (null == request.getHeader("Origin")
                || (null == request.getHeader("Access-Control-Request-Method")
                && null == request.getHeader("Access-Control-Request-Headers"))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (GuardRepo.matchesAny(request.getHeader("Origin"), guard.getDomains()) && originMatcher.matches()) {
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
        Fileupload c = (Fileupload) request.getAttribute(FileServlet.class.getCanonicalName());
        if (null == c) {
            try {
                String name = getNameFromURL(request.getRequestURL());
                c = file.getFile(name);
                if (null == c) {
                    throw new FileNotFoundException(name);
                }
                request.setAttribute(FileServlet.class.getCanonicalName(), c);
            } catch (FileNotFoundException ex) {
                response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=" + MAX_AGE_SECONDS);
                response.setDateHeader(HttpHeaders.EXPIRES, new Date().getTime() + MAX_AGE_MILLISECONDS);
                if (HttpMethod.HEAD.equals(request.getMethod()) && fromApprovedDomain(request)) {
                    request.setAttribute(OdysseyFilter.HANDLED_ERROR, true);
                }
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        if (c.getMimetype().startsWith("image") || c.getMimetype().startsWith("audio") || c.getMimetype().startsWith("video")) {
            if (!fromApprovedDomain(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                error.add(request, null, imead.getLocal(UNAUTHORIZED_CONTENT_REQUEST, Local.resolveLocales(request)), null);
                request.setAttribute(OdysseyFilter.HANDLED_ERROR, true);
                return;
            }
        }

        String etag = "\"" + c.getEtag() + "\"";
        response.setHeader(HttpHeaders.ETAG, etag);
        if (null != request.getHeader("Referer")) {
            Matcher originMatcher = GuardRepo.ORIGIN_PATTERN.matcher(request.getHeader("Referer"));
            if (GuardRepo.matchesAny(request.getHeader("Referer"), guard.getDomains()) && originMatcher.matches()) {
                response.setHeader("Access-Control-Allow-Origin", originMatcher.group(1));
            } else {
                response.setHeader("Access-Control-Allow-Origin", guard.getCanonicalOrigin());
            }
        } else {
            response.setHeader("Access-Control-Allow-Origin", guard.getCanonicalOrigin());
        }
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
        String encoding = request.getHeader(HttpHeaders.ACCEPT_ENCODING);
        response.setHeader(HttpHeaders.VARY, HttpHeaders.ETAG + ", " + HttpHeaders.CONTENT_ENCODING + ", " + HttpHeaders.ACCEPT_ENCODING);
        response.setContentLength(c.getFilemetadata().getDatasize());
        if (null != encoding && GZIP_PATTERN.matcher(encoding).find() && null != c.getGzipdata()) {
            response.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
        }
        if (null != encoding && BR_PATTERN.matcher(encoding).find() && null != c.getBrdata()) {
            if ("gzip".equals(response.getHeader(HttpHeaders.CONTENT_ENCODING)) && c.getGzipdata().length < c.getBrdata().length) {
                // don't do anything; gzip is smaller
                response.setContentLength(c.getFilemetadata().getGzipsize());
            } else {
                response.setHeader(HttpHeaders.CONTENT_ENCODING, "br");
                response.setContentLength(c.getFilemetadata().getBrsize());
            }
        }
        if (etag.equals(request.getHeader(HttpHeaders.IF_NONE_MATCH))) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHead(request, response);
        Fileupload c = (Fileupload) request.getAttribute(FileServlet.class.getCanonicalName());
        if (null != c) {
            try {
                switch (response.getHeader(HttpHeaders.CONTENT_ENCODING)) {
                    case "br":
                        response.getOutputStream().write(c.getBrdata());
                        break;
                    case "gzip":
                        response.getOutputStream().write(c.getGzipdata());
                        break;
                    default:
                        response.getOutputStream().write(c.getFiledata());
                        break;
                }
            } catch (NullPointerException n) {
                response.getOutputStream().write(c.getFiledata());
            }
            request.setAttribute(ResponseTag.RENDER_TIME_PARAM, new Date().getTime() - ((Date) request.getAttribute(OdysseyFilter.TIME_PARAM)).getTime());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<Fileupload> uploadedfiles = FileUtil.getFilesFromRequest(request, "filedata");
            for (Fileupload uploadedfile : uploadedfiles) {
                if (null != file.getFile(uploadedfile.getFilename())) {
                    request.setAttribute("error", "File exists: " + uploadedfile.getFilename());
                    return;
                }
                uploadedfile.setUrl(getBaseURL() + uploadedfile.getFilename());
            }
            file.addFiles(uploadedfiles);
            request.setAttribute("uploadedfiles", uploadedfiles);
            for (Fileupload fileupload : uploadedfiles) {
                exec.submit(new Brotlier(fileupload));
                exec.submit(new Gzipper(fileupload));
            }
        } catch (FileNotFoundException fx) {
            request.setAttribute("error", "File not sent");
        } catch (EJBException | IOException | ServletException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean fromApprovedDomain(HttpServletRequest req) {
        String referrer = req.getHeader("referer");
        if (null != referrer) {
            return GuardRepo.matchesAny(referrer, guard.getAcceptableDomains());
        }
        return true;
    }
}
