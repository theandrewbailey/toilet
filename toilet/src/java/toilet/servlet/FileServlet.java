package toilet.servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.file.BaseFileServlet;
import libWebsiteTools.file.Fileupload;

@WebServlet(name = "FileServlet", description = "Handles uploading files, and serves files through inherited class", urlPatterns = {"/file", "/file/*", "/fileImmutable/*"})
@MultipartConfig(maxRequestSize = 1024 * 1024 * 1024) // 1 gigabyte
public class FileServlet extends BaseFileServlet {

    public static final String DEFAULT_TYPE = "text/html;charset=UTF-8";

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AdminLoginServlet.FILES.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        super.doPost(request, response);
        try {
            Fileupload uploaded = ((List<Fileupload>) request.getAttribute("uploadedfiles")).get(0);
            String[] split = AdminFile.splitDirectoryAndName(uploaded.getFilename());
            request.setAttribute("opened_dir", split[0]);
        } catch (RuntimeException r) {
        }
        AdminFile.showFileList(request, response, file.getFileMetadata(null));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType(DEFAULT_TYPE);
        super.doGet(request, response);
        if (HttpServletResponse.SC_OK != response.getStatus()) {
            String accept = request.getHeader(HttpHeaders.ACCEPT);
            if (null != accept && accept.contains("text/html")) {
                response.setContentType(DEFAULT_TYPE);
                response.sendError(response.getStatus());
            }
        }
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType(DEFAULT_TYPE);
        super.doHead(request, response);
    }
}
