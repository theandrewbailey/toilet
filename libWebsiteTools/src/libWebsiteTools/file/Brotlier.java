package libWebsiteTools.file;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alpha
 */
public class Brotlier extends FileCompressorJob {

    private final int SIZE_DIFFERENCE = "Accept-Encoding: br\n".length();
    private final String COMMAND_KEY = "file_brCommand";
    private final static Logger LOG = Logger.getLogger(Brotlier.class.getName());

    public Brotlier(Fileupload file) {
        super(file);
    }

    @Override
    public Boolean call() throws Exception {
        if (null != file.getBrdata()) {
            LOG.log(Level.FINEST, "File {0} already brotli'd.", file.getFilename());
            return false;
        }
        String command = imead.getValue(COMMAND_KEY);
        try {
            if (null == command) {
                LOG.finest("Brotli command not set.");
                return false;
            }
            byte[] compressedData = FileUtil.runProcess(command, file.getFiledata(), file.getFiledata().length * 2);
            if (null != compressedData && compressedData.length + SIZE_DIFFERENCE < file.getFiledata().length) {
                synchronized (FileCompressorJob.POTATO) {
                    Fileupload activeFile = fileRepo.getFile(file.getFilename());
                    activeFile.setBrdata(compressedData);
                    fileRepo.upsertFiles(Arrays.asList(activeFile));
                }
            }
        } catch (IOException | RuntimeException ex) {
            LOG.log(Level.SEVERE, "Problem while running command: " + command, ex);
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Brotlier " + file.getFilename();
    }
}
