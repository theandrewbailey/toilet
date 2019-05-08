package toilet.servlet;

import java.io.IOException;
import java.util.zip.ZipInputStream;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import libOdyssey.bean.GuardRepo;
import libWebsiteTools.tag.AbstractInput;
import toilet.bean.BackupDaemon;

@WebServlet(name = "ImportServlet", description = "Inserts articles, comments, and files via zip file upload", urlPatterns = {"/import"})
@MultipartConfig(maxRequestSize = 1024 * 1024 * 1000) // 1000 megabytes
public class ImportServlet extends ToiletServlet {

    @EJB
    private BackupDaemon backup;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Object seslog = request.getSession().getAttribute("login");
        if (!AdminLoginServlet.IMPORT.equals(seslog)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + backup.getZipName());
        response.setContentType("application/zip");
        backup.generateZip(response.getOutputStream());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AdminLoginServlet.IMPORT.equals(request.getSession().getAttribute("login"))
                || !imead.verifyArgon2(AbstractInput.getParameter(request, "words"), ArticleServlet.WORDS)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try{
            ZipInputStream zip = new ZipInputStream(AbstractInput.getPart(request, "zip").getInputStream());
            backup.restoreFromZip(zip);
        } catch (Exception ex) {
            error.add(request, "Restore from zip failed", null, ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        response.sendRedirect(imead.getValue(GuardRepo.CANONICAL_URL));
    }
}
