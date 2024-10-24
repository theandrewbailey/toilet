package toilet.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.zip.ZipInputStream;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Arrays;
import libWebsiteTools.security.GuardFilter;
import libWebsiteTools.security.RequestTimer;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.tag.AbstractInput;
import toilet.bean.BackupDaemon;
import toilet.bean.ToiletBeanAccess;

@WebServlet(name = "AdminImportServlet", description = "Download the entire site as a zip. Insert articles, comments, and files via zip file upload", urlPatterns = {"/adminImport", "/adminExport"})
@MultipartConfig(maxRequestSize = 999999999)
public class AdminImportServlet extends AdminServlet {

    public static final String ADMIN_IMPORT_EXPORT = "/WEB-INF/adminImportExport.jsp";

    @Override
    public AdminServletPermission getRequiredPermission(HttpServletRequest req) {
        return AdminServletPermission.IMPORT_EXPORT;
    }

    @Override
    public boolean isAuthorized(HttpServletRequest req) {
        return isAuthorized(req, AdminServletPermission.IMPORT_EXPORT) || allBeans.getInstance(req).isFirstTime(req);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ToiletBeanAccess beans = allBeans.getInstance(request);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + beans.getBackup().getZipName());
        response.setContentType("application/zip");
        beans.getBackup().createZip(response.getOutputStream(), Arrays.asList(BackupDaemon.BackupTypes.values()));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ToiletBeanAccess beans = allBeans.getInstance(request);
        try {
            Part p = AbstractInput.getPart(request, "zip");
            InputStream i = p.getInputStream();
            ZipInputStream zip = new ZipInputStream(i);
            beans.getBackup().restoreFromZip(zip);
            request.getSession().invalidate();
            response.setHeader(RequestTimer.SERVER_TIMING, RequestTimer.getTimingHeader(request, Boolean.FALSE));
            response.sendRedirect(request.getAttribute(SecurityRepo.BASE_URL).toString());
        } catch (IOException ex) {
            beans.getError().logException(request, "Restore from zip failed", null, ex);
            request.setAttribute(GuardFilter.HANDLED_ERROR, true);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (IllegalStateException sx) {
            request.getRequestDispatcher(ADMIN_IMPORT_EXPORT).forward(request, response);
        }
    }
}
