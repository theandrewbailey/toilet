package toilet.servlet;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import libOdyssey.RequestTime;
import libOdyssey.ResponseTag;
import libOdyssey.bean.GuardHolder;
import libWebsiteTools.imead.IMEADHolder;
import toilet.UtilStatic;
import toilet.bean.FileRepo;
import toilet.db.Fileupload;

@WebServlet(name = "ContentServlet", description = "Grabs a byte array from the DB and spits it out on the response and also handles posting files", urlPatterns = {"/content", "/content/*"})
@MultipartConfig(maxRequestSize = 1024 * 1024 * 1024) // 1 gigabyte
public class ContentServlet extends HttpServlet {

    @EJB
    private FileRepo file;
    @EJB
    private IMEADHolder imead;

    @Override
    protected long getLastModified(HttpServletRequest request) {
        Fileupload c;
        try {
            c = file.getFile(file.getFilename(request));
            c.getUploaded();
        } catch (Exception ex) {
            return -1;
        }
        return c.getUploaded().getTime() / 1000 * 1000;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Fileupload c;
        try {
            String name = file.getFilename(request);
            c = file.getFile(name);
            c.getUploaded();
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String ifNoneMatch = request.getHeader("If-None-Match");
        String etag = "\"" + c.getEtag() + "\"";
        if (etag.equals(ifNoneMatch)) {
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        response.setHeader("ETag", etag);
        response.setHeader("Access-Control-Allow-Origin", imead.getValue(GuardHolder.HOST));
        response.setContentType(c.getMimetype());
        response.setDateHeader("Last-Modified", c.getUploaded().getTime());
        response.getOutputStream().write(c.getBinarydata());

        request.setAttribute(ResponseTag.RENDER_TIME_PARAM, new Date().getTime() - ((Date) request.getAttribute(RequestTime.TIME_PARAM)).getTime());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String login = request.getSession().getAttribute("login").toString();
        if (!login.equals(AdminServlet.CONTENT)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        try {
            file.addFile(getFileFromRequest(request, "filedata"));
        } catch (IOException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        AdminContent.showFileList(request, response);
    }

    public static Fileupload getFileFromRequest(HttpServletRequest req, String fieldname) throws IOException, ServletException {
        Part filepart = req.getPart(fieldname);
        byte[] tehFile = new byte[(int) (filepart.getSize())];
        String fileName = filepart.getHeader("content-disposition").split("filename=\"")[1];
        String dir = UtilStatic.getParam(req, "directory");
        dir = dir == null ? "" : dir;
        fileName = dir + fileName.substring(0, fileName.length() - 1);

        DataInputStream dis = new DataInputStream(filepart.getInputStream());
        dis.readFully(tehFile);
        dis.close();

        return new Fileupload(null, tehFile, UtilStatic.getHash(tehFile), fileName, filepart.getContentType(), new Date());
    }
}
