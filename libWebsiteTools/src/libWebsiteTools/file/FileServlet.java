package libWebsiteTools.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.OdysseyFilter;
import libOdyssey.RequestTime;
import libOdyssey.ResponseTag;
import libOdyssey.bean.ExceptionRepo;
import libOdyssey.bean.GuardHolder;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;

public abstract class FileServlet extends HttpServlet {

    // 0b111111111111111111111100000000 == 0x3fffff00 == 1,073,741,568 ms == 12 days, 10 hours, 15 minutes, 41.57 seconds exactly
    public static final long MAX_AGE_MILLISECONDS = 0b111111111111111111111100000000;
    private static final String MAX_AGE_SECONDS = Long.toString(MAX_AGE_MILLISECONDS / 1000);
    private static final String UNAUTHORIZED_CONTENT_REQUEST = "content_unauthorized";
    private static final String FILE_ATTRIBUTE = "$_LIBWEBSITETOOLS_FILEUPLOAD";
    @EJB
    private FileRepo file;
    @EJB
    private IMEADHolder imead;
    @EJB
    private ExceptionRepo error;
    @EJB
    private GuardHolder guard;

    @Override
    protected long getLastModified(HttpServletRequest request) {
        Fileupload c;
        try {
            c = file.getFile(file.getFilename(request.getRequestURL()));
            c.getAtime();
            request.setAttribute(FILE_ATTRIBUTE, c);
        } catch (Exception ex) {
            return -1;
        }
        return c.getAtime().getTime() / 1000 * 1000;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Fileupload c = (Fileupload) request.getAttribute(FILE_ATTRIBUTE);
        if (null == c) {
            try {
                String name = file.getFilename(request.getRequestURL());
                c = file.getFile(name);
                if (null == c) {
                    throw new FileNotFoundException(name);
                }
            } catch (Exception ex) {
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

        String ifNoneMatch = request.getHeader("If-None-Match");
        String etag = "\"" + c.getEtag() + "\"";
        if (etag.equals(ifNoneMatch)) {
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        String canonical = imead.getValue(GuardHolder.CANONICAL_URL);
        Matcher matcher = GuardHolder.ORIGIN_PATTERN.matcher(canonical);
        matcher.find();
        response.setHeader("Access-Control-Allow-Origin", matcher.group(2));
        response.setHeader("Cache-Control", "public, max-age=" + MAX_AGE_SECONDS);
        response.setHeader("ETag", etag);
        response.setDateHeader("Last-Modified", c.getAtime().getTime());
        response.setDateHeader("Expires", new Date().getTime() + MAX_AGE_MILLISECONDS);
        response.setHeader("Vary", "ETag");
        response.setContentType(c.getMimetype());
        response.setContentLength(c.getFiledata().length);
        response.getOutputStream().write(c.getFiledata());

        request.setAttribute(ResponseTag.RENDER_TIME_PARAM, new Date().getTime() - ((Date) request.getAttribute(RequestTime.TIME_PARAM)).getTime());
    }

    @Override
    @SuppressWarnings("null")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Fileupload uploadedfile = null;
        try {
            uploadedfile = FileUtil.getFileFromRequest(request, "filedata");
            file.addFiles(Arrays.asList(uploadedfile));
        } catch (FileNotFoundException fx) {
            request.setAttribute("error", "File not sent");
        } catch (EJBException ex) {
            request.setAttribute("error", "File exists: " + uploadedfile.getFilename());
        } catch (IOException | ServletException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        request.setAttribute("uploadedfile", uploadedfile);
    }

    private boolean fromApprovedDomain(HttpServletRequest req) {
        String referrer = req.getHeader("referer");
        if (null != referrer) {
            return GuardHolder.matchesAny(referrer, guard.getAcceptableDomains());
        }
        return true;
    }
}
