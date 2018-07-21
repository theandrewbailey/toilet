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
            file.setEtag(HashUtil.getHashAsBase64(tehFile));
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
        System.out.println("running command: " + command);
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
                    System.out.println(new String(output.toByteArray()));
                }
                break;
            } catch (InterruptedException | IOException ix) {
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
}
