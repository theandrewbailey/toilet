package libWebsiteTools.file;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import libWebsiteTools.HashUtil;
import libWebsiteTools.tag.AbstractInput;

/**
 *
 * @author alpha
 */
public class FileUtil {
    public static Fileupload getFileFromRequest(HttpServletRequest req, String fieldname) throws IOException, ServletException {
        Part filepart = AbstractInput.getPart(req, fieldname);
        byte[] tehFile = new byte[(int) (filepart.getSize())];
        if (0==tehFile.length){
            throw new FileNotFoundException();
        }
        String fileName = filepart.getHeader("content-disposition").split("filename=\"")[1];
        String dir = getParam(req, "directory");
        dir = dir == null ? "" : dir;
        fileName = dir + fileName.substring(0, fileName.length() - 1);

        DataInputStream dis = new DataInputStream(filepart.getInputStream());
        dis.readFully(tehFile);
        dis.close();

        Fileupload file = new Fileupload();
        file.setAtime(new Date());
        file.setEtag(HashUtil.getHashAsBase64(tehFile));
        file.setFiledata(tehFile);
        file.setFilename(fileName);
        file.setMimetype(filepart.getContentType());
        return file;
    }


    /**
     * retrieves a string from a multipart upload request
     *
     * @param req
     * @param param
     * @return value | null
     */
    public static String getParam(HttpServletRequest req, String param) {
        try {
            return new BufferedReader(new InputStreamReader(AbstractInput.getPart(req, param).getInputStream())).readLine();
        } catch (IOException | ServletException e) {
            return null;
        }
    }
}
