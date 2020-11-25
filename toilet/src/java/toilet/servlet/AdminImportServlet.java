package toilet.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipInputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.security.GuardFilter;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.tag.AbstractInput;
import toilet.FirstTimeDetector;
import toilet.bean.BackupDaemon;

@WebServlet(name = "AdminImportServlet", description = "Inserts articles, comments, and files via zip file upload", urlPatterns = {"/adminImport"})
@MultipartConfig(maxRequestSize = 1024 * 1024 * 1000) // 1000 megabytes
public class AdminImportServlet extends ToiletServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AdminLoginServlet.EDIT_POSTS.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + beans.getBackup().getZipName());
        response.setContentType("application/zip");
        beans.getBackup().createZip(response.getOutputStream(), Arrays.asList(BackupDaemon.BackupTypes.values()));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (FirstTimeDetector.FIRST_TIME_SETUP.equals(getServletContext().getAttribute(FirstTimeDetector.FIRST_TIME_SETUP))) {
            // this is OK
        } else if (!AdminLoginServlet.EDIT_POSTS.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))
                || !HashUtil.verifyArgon2Hash(beans.getImeadValue(AdminLoginServlet.ADD_ARTICLE), AbstractInput.getParameter(request, "words"))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        try {
            beans.getExec().submit(() -> {
                try {
                    ZipInputStream zip = new ZipInputStream(AbstractInput.getPart(request, "zip").getInputStream());
                    beans.getBackup().restoreFromZip(zip);
                    if (!FirstTimeDetector.isFirstTime(beans)) {
                        request.getServletContext().removeAttribute(FirstTimeDetector.FIRST_TIME_SETUP);
                    }
                    Date start = (Date) request.getAttribute(GuardFilter.TIME_PARAM);
                    Long time = new Date().getTime() - start.getTime();
                    log("Backup restored in " + time + " milliseconds.");
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }).get();
        } catch (Exception ex) {
            beans.getError().logException(request, "Restore from zip failed", null, ex);
            request.setAttribute(GuardFilter.HANDLED_ERROR, true);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        response.sendRedirect(request.getAttribute(SecurityRepo.BASE_URL).toString());
    }
}
