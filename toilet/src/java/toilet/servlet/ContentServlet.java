package toilet.servlet;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.file.FileServlet;
import libWebsiteTools.imead.IMEADHolder;

@WebServlet(name = "ContentServlet", description = "Handles uploading files, and serves files through inherited class", urlPatterns = {"/content", "/content/*", "/contentImmutable/*"})
@MultipartConfig(maxRequestSize = 1024 * 1024 * 1024) // 1 gigabyte
public class ContentServlet extends FileServlet {

    @EJB
    private IMEADHolder imead;
    @EJB
    private FileRepo file;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String login = request.getSession().getAttribute("login").toString();
        if (!login.equals(AdminLoginServlet.CONTENT)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        super.doPost(request, response);
        AdminContent.showFileList(request, response, file.getUploadArchive());
    }

    @Override
    protected String getBaseURL() {
        return imead.getValue(libOdyssey.bean.GuardHolder.CANONICAL_URL) + "content/";
    }
}
