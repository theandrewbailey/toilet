package toilet.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.file.Filemetadata;
import libWebsiteTools.tag.AbstractInput;


/**
 *
 * @author alpha
 */
@WebServlet(name = "adminContent", description = "Performs admin duties on uploads", urlPatterns = {"/adminContent"})
public class AdminContent extends ToiletServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        asyncFiles(request);
        String del = request.getParameter("delete");
        String login = request.getSession().getAttribute("login").toString();
        String answer = AbstractInput.getParameter(request, "answer");
        if (answer != null && imead.verifyArgon2(answer, AdminLoginServlet.CONTENT)) {
            showFileList(request, response, file.getFileMetadata(null));
        } else if (login.equals(AdminLoginServlet.CONTENT) && del != null) {      // delete upload
            file.deleteFile(del);
            showFileList(request, response, file.getFileMetadata(null));
        }
    }

    public static void showFileList(HttpServletRequest request, HttpServletResponse response, List<Filemetadata> uploads) throws ServletException, IOException {
        request.getSession().setAttribute("login", AdminLoginServlet.CONTENT);
        LinkedHashMap<String, List<Filemetadata>> content = new LinkedHashMap<>(uploads.size() * 2);
        LinkedHashMap<String, String> directories = new LinkedHashMap<>();

        // root "directory" first
        content.put("", new ArrayList<>());
        directories.put("", "");

        for (Filemetadata f : uploads) {
            String[] parts = f.getFilename().split("/", 2);
            String dir = parts.length == 2 ? parts[0] + "/" : "";
            String name = parts.length == 2 ? parts[1] : parts[0];
            List<Filemetadata> temp = content.get(dir);
            if (temp == null) {
                temp = new ArrayList<>();
                content.put(dir, temp);
                directories.put(dir, dir);
            }
            f.setFilename(name);
            temp.add(f);
        }

        request.setAttribute("content", content);
        request.setAttribute("directories", directories);
        request.getRequestDispatcher(AdminLoginServlet.MAN_CONTENT).forward(request, response);
    }
}
