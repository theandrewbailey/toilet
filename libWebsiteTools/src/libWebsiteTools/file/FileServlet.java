package libWebsiteTools.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import libOdyssey.OdysseyFilter;
import libOdyssey.RequestTime;
import libOdyssey.ResponseTag;
import libOdyssey.bean.ExceptionRepo;
import libOdyssey.bean.GuardHolder;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;

public abstract class FileServlet extends HttpServlet {

    // 0b1111111111111111111111100000000 == 0x7fffff00 == 2,147,483,392 ms == 24 days, 20 hours 31 minutes 23.392 seconds exactly
    public static final long MAX_AGE_MILLISECONDS = 0b1111111111111111111111100000000;
    public static final String IMMUTABLE_PARAM = "immutable";
    public static final Pattern GZIP_PATTERN = Pattern.compile("(?:.*? )?gzip(?:,.*)?");
    public static final Pattern BR_PATTERN = Pattern.compile("(?:.*? )?br(?:,.*)?");
    public static final String MAX_AGE_SECONDS = Long.toString(MAX_AGE_MILLISECONDS / 1000);
    private static final String UNAUTHORIZED_CONTENT_REQUEST = "content_unauthorized";
    @EJB
    private FileRepo file;
    @EJB
    private IMEADHolder imead;
    @EJB
    private ExceptionRepo error;
    @EJB
    private GuardHolder guard;
    @Resource
    private ManagedExecutorService exec;

    abstract protected String getBaseURL();

    @Override
    protected long getLastModified(HttpServletRequest request) {
        Fileupload c;
        try {
            c = file.getFile(FileRepo.getFilename(request.getRequestURL()));
            c.getAtime();
            request.setAttribute(FileServlet.class.getCanonicalName(), c);
        } catch (Exception ex) {
            return -1;
        }
        return c.getAtime().getTime() / 1000 * 1000;
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Fileupload c = (Fileupload) request.getAttribute(FileServlet.class.getCanonicalName());
        if (null == c) {
            try {
                String name = FileRepo.getFilename(request.getRequestURL());
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
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
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
        if (etag.equals(request.getHeader(HttpHeaders.IF_NONE_MATCH))) {
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        response.setHeader(HttpHeaders.ETAG, etag);
        if (null != request.getHeader("Referer")) {
            Matcher originMatcher = GuardHolder.ORIGIN_PATTERN.matcher(request.getHeader("Referer"));
            if (GuardHolder.matchesAny(request.getHeader("Referer"), guard.getDomains()) && originMatcher.matches()) {
                response.setHeader("Access-Control-Allow-Origin", originMatcher.group(1));
            } else {
                response.setHeader("Access-Control-Allow-Origin", guard.getCanonicalOrigin());
            }
        } else {
            response.setHeader("Access-Control-Allow-Origin", guard.getCanonicalOrigin());
        }
        if (null != request.getParameter(IMMUTABLE_PARAM) && null != FileRepo.getImmutable(request.getRequestURL())) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=" + MAX_AGE_SECONDS + ", immutable");
        } else {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=" + MAX_AGE_SECONDS);
        }
        response.setDateHeader(HttpHeaders.LAST_MODIFIED, c.getAtime().getTime());
        response.setDateHeader(HttpHeaders.EXPIRES, new Date().getTime() + MAX_AGE_MILLISECONDS);
        response.setContentType(c.getMimetype());
        if (!c.getMimetype().startsWith("text")) {
            response.setCharacterEncoding(null);
        }
        String encoding = request.getHeader(HttpHeaders.ACCEPT_ENCODING);
        if (null != encoding && GZIP_PATTERN.matcher(encoding).find() && null != c.getGzipdata()) {
            response.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
            response.setHeader(HttpHeaders.VARY, HttpHeaders.ETAG + ", " + HttpHeaders.CONTENT_ENCODING + ", " + HttpHeaders.ACCEPT_ENCODING);
        }
        if (null != encoding && BR_PATTERN.matcher(encoding).find() && null != c.getBrdata()) {
            if ("gzip".equals(response.getHeader(HttpHeaders.CONTENT_ENCODING)) && c.getGzipdata().length < c.getBrdata().length) {
                // don't do anything; gzip is smaller
            } else {
                response.setHeader(HttpHeaders.CONTENT_ENCODING, "br");
                response.setHeader(HttpHeaders.VARY, HttpHeaders.ETAG + ", " + HttpHeaders.CONTENT_ENCODING + ", " + HttpHeaders.ACCEPT_ENCODING);
            }
        }
        if (null == response.getHeader(HttpHeaders.CONTENT_ENCODING)) {
            response.setHeader(HttpHeaders.VARY, HttpHeaders.ETAG + ", " + HttpHeaders.ACCEPT_ENCODING);
            response.setContentLength(c.getFiledata().length);
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
            request.setAttribute(ResponseTag.RENDER_TIME_PARAM, new Date().getTime() - ((Date) request.getAttribute(RequestTime.TIME_PARAM)).getTime());
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
            return GuardHolder.matchesAny(referrer, guard.getAcceptableDomains());
        }
        return true;
    }
}
