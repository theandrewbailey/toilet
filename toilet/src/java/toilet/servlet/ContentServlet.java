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

@WebServlet(name = "ContentServlet", description = "Grabs a byte array from the DB and spits it out on the response and also handles posting files", urlPatterns = {"/content", "/content/*"})
@MultipartConfig(maxRequestSize = 1024 * 1024 * 1000) // 1000 megabytes
public class ContentServlet extends FileServlet {

    @EJB
    private FileRepo file;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String login = request.getSession().getAttribute("login").toString();
        if (!login.equals(AdminServlet.CONTENT)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        super.doPost(request, response);
        AdminContent.showFileList(request, response, file.getUploadArchive());
    }
}
