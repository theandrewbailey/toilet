package libWebsiteTools.file;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.tag.AbstractInput;

/**
 *
 * @author alpha
 */
public class FileUtil {

    private final static Logger LOG = Logger.getLogger(FileUtil.class.getName());

    public static List<Fileupload> getFilesFromRequest(HttpServletRequest req, String fieldname) throws IOException, ServletException {
        List<Part> fileparts = AbstractInput.getParts(req, fieldname);
        List<Fileupload> files = new ArrayList<>(fileparts.size());
        for (Part filepart : fileparts) {
            byte[] tehFile = new byte[(int) (filepart.getSize())];
            if (0 == tehFile.length) {
                throw new FileNotFoundException();
            }
            String fileName = filepart.getHeader("content-disposition").split("filename=\"")[1];
            String dir = getParam(req, "directory");
            dir = dir == null ? "" : dir;
            fileName = dir + fileName.substring(0, fileName.length() - 1);
            try (DataInputStream dis = new DataInputStream(filepart.getInputStream())) {
                dis.readFully(tehFile);
            }
            Fileupload file = new Fileupload();
            file.setAtime(new Date());
            file.setEtag(HashUtil.getSHA256Hash(tehFile));
            file.setFiledata(tehFile);
            file.setFilename(fileName);
            if (file.getFilename().endsWith(".js")) {
                file.setMimetype("text/javascript");
            } else if (file.getFilename().endsWith(".css")) {
                file.setMimetype("text/css");
            } else {
                file.setMimetype(filepart.getContentType());
            }
            files.add(file);
        }
        return files;
    }

    public static byte[] runProcess(String command, byte[] stdin, int expectedOutputSize) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(expectedOutputSize);
        Process encoder = Runtime.getRuntime().exec(command);
        if (null != stdin) {
            try (OutputStream out = encoder.getOutputStream()) {
                out.write(stdin);
            }
        }
        LOG.log(Level.INFO, "running command: {0}", command);
        while (true) {
            try {
                try (InputStream input = encoder.getInputStream()) {
                    byte content[] = new byte[65536];
                    int readCount = 0;
                    while (-1 != (readCount = input.read(content))) {
                        output.write(content, 0, readCount);
                    }
                }
                int exitcode = encoder.waitFor();
                if (exitcode == 0) {
                } else {
                    LOG.log(Level.WARNING, "Command exited with {0}:\n{1}", new Object[]{exitcode, new String(output.toByteArray())});
                }
                break;
            } catch (InterruptedException | IOException ix) {
                throw new RuntimeException("Problem while running command: " + command, ix);
            }
        }
        encoder.destroy();
        return output.toByteArray();
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

    /**
     * useful for getting file contents out of a zip
     *
     * @param in
     * @return
     * @throws IOException
     */
    public static byte[] getByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[65536];
        int read;
        while ((read = in.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        return baos.toByteArray();
    }
}
