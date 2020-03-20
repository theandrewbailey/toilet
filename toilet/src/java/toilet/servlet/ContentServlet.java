package toilet.servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.file.FileServlet;
import libWebsiteTools.file.Fileupload;

@WebServlet(name = "ContentServlet", description = "Handles uploading files, and serves files through inherited class", urlPatterns = {"/content", "/content/*", "/contentImmutable/*"})
@MultipartConfig(maxRequestSize = 1024 * 1024 * 1024) // 1 gigabyte
public class ContentServlet extends FileServlet {

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AdminLoginServlet.CONTENT.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        super.doPost(request, response);
        try {
            Fileupload uploaded = ((List<Fileupload>) request.getAttribute("uploadedfiles")).get(0);
            String[] split = AdminContent.splitDirectoryAndName(uploaded.getFilename());
            request.setAttribute("opened_dir", split[0]);
        } catch (RuntimeException r) {
        }
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
}
