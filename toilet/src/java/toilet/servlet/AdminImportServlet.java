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
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.tag.AbstractInput;
import toilet.bean.BackupDaemon;
import toilet.bean.ToiletBeanAccess;

@WebServlet(name = "AdminImportServlet", description = "Download the entire site as a zip. Insert articles, comments, and files via zip file upload", urlPatterns = {"/adminImport", "/adminExport"})
@MultipartConfig(maxRequestSize = 999999999)
public class AdminImportServlet extends ToiletServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AdminLoginServlet.IMPORT_EXPORT.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        ToiletBeanAccess beans = allBeans.getInstance(request);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + beans.getBackup().getZipName());
        response.setContentType("application/zip");
        beans.getBackup().createZip(response.getOutputStream(), Arrays.asList(BackupDaemon.BackupTypes.values()));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        ToiletBeanAccess beans = allBeans.getInstance(request);
        if (beans.isFirstTime()) {
            // this is OK
        } else if (!AdminLoginServlet.IMPORT_EXPORT.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        try {
            Part p = AbstractInput.getPart(request, "zip");
            InputStream i = p.getInputStream();
            ZipInputStream zip = new ZipInputStream(i);
            beans.getBackup().restoreFromZip(zip);
            OffsetDateTime start = GuardFilter.getRequestTime(request);
            Duration d = Duration.between(start, OffsetDateTime.now()).abs();
            log("Backup restored in " + d.toMillis() + " milliseconds.");
            request.getSession().invalidate();
            response.sendRedirect(request.getAttribute(SecurityRepo.BASE_URL).toString());
        } catch (IOException ex) {
            beans.getError().logException(request, "Restore from zip failed", null, ex);
            request.setAttribute(GuardFilter.HANDLED_ERROR, true);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (IllegalStateException sx) {
            request.getRequestDispatcher(AdminLoginServlet.ADMIN_IMPORT_EXPORT).forward(request, response);
        }
    }
}
