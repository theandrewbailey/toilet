package libWebsiteTools.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 *
 * @author alpha
 */
public class Gzipper extends FileCompressorJob {

    private final int SIZE_DIFFERENCE = "Accept-Encoding: gzip\n".length();
    private final String COMMAND_KEY = "file_gzipCommand";

    public Gzipper(Fileupload file) {
        super(file);
    }

    @Override
    public Boolean call() throws Exception {
        if (null != file.getGzipdata()) {
            return false;
        }
        byte[] compressedData = null;
        File tempfile = null;
        String command = imead.getValue(COMMAND_KEY);
        if (null == command) {
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
        } catch (IOException ex) {
            error.add(null, "Compression failed", command, ex);
            return false;
        } finally {
            if (null != tempfile) {
                tempfile.delete();
            }
        }
        if (null != compressedData) {
            synchronized (FileCompressorJob.POTATO) {
                Fileupload activeFile = fileRepo.getFile(file.getFilename());
                activeFile.setGzipdata(compressedData);
                fileRepo.upsertFiles(Arrays.asList(activeFile));
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Gzipper " + file.getFilename();
    }
}
