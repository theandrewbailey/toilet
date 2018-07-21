package libWebsiteTools.file;

import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author alpha
 */
public class Brotlier extends FileCompressorJob {

    private final int SIZE_DIFFERENCE = "Accept-Encoding: br\n".length();
    private final String COMMAND_KEY = "file_brCommand";

    public Brotlier(Fileupload file) {
        super(file);
    }

    @Override
    public Boolean call() throws Exception {
        if (null != file.getBrdata()) {
            return false;
        }
        try {
            String command = imead.getValue(COMMAND_KEY);
            if (null == command) {
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
        }
        return true;
    }

    @Override
    public String toString() {
        return "Brotlier " + file.getFilename();
    }
}
