package toilet.servlet;

import com.lambdaworks.crypto.SCryptUtil;
import java.io.IOException;
import java.util.zip.ZipInputStream;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.bean.ExceptionRepo;
import libWebsiteTools.imead.IMEADHolder;
import toilet.bean.BackupDaemon;
import toilet.bean.UtilBean;
import static toilet.servlet.ArticleServlet.WORDS;

@WebServlet(name = "ImportServlet", description = "Inserts articles, comments, and files via zip file upload", urlPatterns = {"/import"})
@MultipartConfig(maxRequestSize = 1024 * 1024 * 1000) // 1000 megabytes
public class ImportServlet extends HttpServlet {

    @EJB
    private BackupDaemon backup;
    @EJB
    private ExceptionRepo error;
    @EJB
    private IMEADHolder imead;

    @Override
    public void init() {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AdminServlet.IMPORT.equals(request.getSession().getAttribute("login"))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        response.setHeader("Content-Disposition", "attachment;filename=" + backup.getZipName());
        response.setContentType("application/zip");
        backup.generateZip(response.getOutputStream());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AdminServlet.IMPORT.equals(request.getSession().getAttribute("login"))
                || !SCryptUtil.check(request.getParameter("words"), imead.getValue(WORDS))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try{
            ZipInputStream zip = new ZipInputStream(request.getPart("zip").getInputStream());
            backup.restoreFromZip(zip);
        } catch (Exception ex) {
            error.add(request, "Restore from zip failed", null, ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        response.sendRedirect(imead.getValue(UtilBean.THISURL));
    }
}
