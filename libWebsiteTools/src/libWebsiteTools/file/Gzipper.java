package libWebsiteTools.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alpha
 */
public class Gzipper extends FileCompressorJob {

    private final int SIZE_DIFFERENCE = "Accept-Encoding: gzip\n".length();
    private final String COMMAND_KEY = "file_gzipCommand";
    private final static Logger LOG = Logger.getLogger(Gzipper.class.getName());

    public Gzipper(Fileupload file) {
        super(file);
    }

    @Override
    public Boolean call() throws Exception {
        if (null != file.getGzipdata()) {
            LOG.log(Level.FINEST, "File {0} already gzipped.", file.getFilename());
            return false;
        }
        byte[] compressedData = null;
        File tempfile = null;
        String command = imead.getValue(COMMAND_KEY);
        if (null == command) {
            LOG.finest("Gzip command not set.");
            return false;
        }
        try {
            tempfile = File.createTempFile("libWebsiteTools", file.getFilename().replace("/", ""));
            try (OutputStream tempout = new FileOutputStream(tempfile)) {
                tempout.write(file.getFiledata());
            }
            command += tempfile.getAbsolutePath();
            compressedData = FileUtil.runProcess(command, null, file.getFiledata().length * 2);
            if (compressedData.length + SIZE_DIFFERENCE > file.getFiledata().length) {
                return false;
            }
        } catch (IOException | RuntimeException ex) {
            LOG.log(Level.SEVERE, "Problem while running command: " + command, ex);
            return false;
        } finally {
            if (null != tempfile) {
                tempfile.delete();
            }
        }
        if (null != compressedData) {
            synchronized (FileCompressorJob.POTATO) {
                Fileupload activeFile = fileRepo.get(file.getFilename());
                activeFile.setGzipdata(compressedData);
                fileRepo.upsert(Arrays.asList(activeFile));
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Gzipper " + file.getFilename();
    }
}
