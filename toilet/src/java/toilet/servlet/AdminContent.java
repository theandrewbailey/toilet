package toilet.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.tag.AbstractInput;
import libWebsiteTools.tag.RequestToken;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.file.Fileupload;

/**
 *
 * @author alpha
 */
@WebServlet(name = "adminContent", description = "Performs admin duties on uploads", urlPatterns = {"/adminContent"})
public class AdminContent extends HttpServlet {

    @EJB
    private FileRepo file;
    @EJB
    private IMEADHolder imead;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String del = request.getParameter("delete");
        String login = request.getSession().getAttribute("login").toString();
        String answer = AbstractInput.getParameter(request, "answer");
        if (answer != null && imead.verifyArgon2(answer, AdminLoginServlet.CONTENT)) {
            showFileList(request, response, file.getUploadArchive());
        } else if (login.equals(AdminLoginServlet.CONTENT) && del!=null) {      // delete upload
            file.deleteFile(new Integer(del));
            showFileList(request, response, file.getUploadArchive());
        }
    }

    public static void showFileList(HttpServletRequest request, HttpServletResponse response, List<Fileupload> uploads) throws ServletException, IOException {
        request.getSession().setAttribute("login", AdminLoginServlet.CONTENT);
        LinkedHashMap<String, List<Fileupload>> content = new LinkedHashMap<>(uploads.size() * 2);
        LinkedHashMap<String, String> directories = new LinkedHashMap<>();

        // root "directory" first
        content.put("", new ArrayList<>());
        directories.put("", "");

        for (Fileupload f : uploads) {
            String[] parts = f.getFilename().split("/", 2);
            String dir = parts.length == 2 ? parts[0] + "/" : "";
            String name = parts.length == 2 ? parts[1] : parts[0];
            List<Fileupload> temp = content.get(dir);
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
        request.setAttribute(RequestToken.ID_NAME, null);
        request.getRequestDispatcher(AdminLoginServlet.MAN_CONTENT).forward(request, response);
    }
}
