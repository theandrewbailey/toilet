package toilet.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.file.FileServlet;

@WebServlet(name = "ContentServlet", description = "Handles uploading files, and serves files through inherited class", urlPatterns = {"/content", "/content/*", "/contentImmutable/*"})
@MultipartConfig(maxRequestSize = 1024 * 1024 * 1024) // 1 gigabyte
public class ContentServlet extends FileServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String login = request.getSession().getAttribute("login").toString();
        if (!login.equals(AdminLoginServlet.CONTENT)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        super.doPost(request, response);
        AdminContent.showFileList(request, response, file.getFileMetadata(null));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html;charset=UTF-8");
        super.doGet(request, response);
        if (HttpServletResponse.SC_OK != response.getStatus()) {
            response.sendError(response.getStatus());
        }
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html;charset=UTF-8");
        super.doHead(request, response);
    }

    @Override
    protected String getBaseURL() {
        return imead.getValue(libOdyssey.bean.GuardRepo.CANONICAL_URL) + "content/";
    }
}
