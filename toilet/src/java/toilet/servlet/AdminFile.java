package toilet.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.file.Filemetadata;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.tag.AbstractInput;

/**
 *
 * @author alpha
 */
@WebServlet(name = "adminFile", description = "Performs admin stuff on file uploads", urlPatterns = {"/adminFile"})
public class AdminFile extends ToiletServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String del = AbstractInput.getParameter(request, "action");
        String answer = AbstractInput.getParameter(request, "answer");
        if (answer != null && HashUtil.verifyArgon2Hash(imead.getValue(AdminLoginServlet.FILES), answer)) {
            showFileList(request, response, file.getFileMetadata(null));
        } else if (AdminLoginServlet.FILES.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION)) && del != null) {
            Fileupload deleted = file.delete(del.split("\\|")[1]);
            String[] split = splitDirectoryAndName(deleted.getFilename());
            request.setAttribute("opened_dir", split[0]);
            showFileList(request, response, file.getFileMetadata(null));
        }
    }

    public static void showFileList(HttpServletRequest request, HttpServletResponse response, List<Filemetadata> uploads) throws ServletException, IOException {
        request.getSession().setAttribute(AdminLoginServlet.PERMISSION, AdminLoginServlet.FILES);
        LinkedHashMap<String, List<Filemetadata>> content = new LinkedHashMap<>(uploads.size() * 2);
        List<String> directories = new ArrayList<>();

        // root "directory" first
        content.put("", new ArrayList<>());
        directories.add("");

        for (Filemetadata f : uploads) {
            String[] split = splitDirectoryAndName(f.getFilename());
            List<Filemetadata> temp = content.get(split[0]);
            if (temp == null) {
                temp = new ArrayList<>();
                content.put(split[0], temp);
                directories.add(split[0]);
            }
            f.setFilename(split[1]);
            temp.add(f);
        }

        request.setAttribute("content", content);
        request.setAttribute("directories", directories);
        if (null == request.getAttribute("opened_dir")) {
            request.setAttribute("opened_dir", "");
        }
        request.getRequestDispatcher(AdminLoginServlet.ADMIN_CONTENT).forward(request, response);
    }

    public static String[] splitDirectoryAndName(String filename) {
        String[] parts = filename.split("/", 2);
        String dir = parts.length == 2 ? parts[0] + "/" : "";
        String name = parts.length == 2 ? parts[1] : parts[0];
        return new String[]{dir, name};
    }
}
