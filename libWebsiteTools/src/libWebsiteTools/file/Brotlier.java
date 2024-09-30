package libWebsiteTools.file;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import libWebsiteTools.AllBeanAccess;
import libWebsiteTools.cache.PageCache;

/**
 *
 * @author alpha
 */
public class Brotlier extends FileCompressorJob {

    private final int SIZE_DIFFERENCE = "Accept-Encoding: br\n".length();
    private final String COMMAND_KEY = "site_brCommand";
    private final static Logger LOG = Logger.getLogger(Brotlier.class.getName());

    public Brotlier(AllBeanAccess beans, Fileupload file) {
        super(beans, file);
    }

    @Override
    public Boolean call() throws Exception {
        if (null != file.getBrdata()) {
            LOG.log(Level.FINEST, "File {0} already brotli'd.", file.getFilename());
            return false;
        }
        String command = beans.getImeadValue(COMMAND_KEY);
        if (null == command || command.isEmpty()) {
            LOG.finest("Brotli command not set.");
            return false;
        }
        try {
            byte[] compressedData = FileUtil.runProcess(command, file.getFiledata(), file.getFiledata().length * 2);
            if (null != compressedData && compressedData.length + SIZE_DIFFERENCE < file.getFiledata().length) {
                synchronized (FileCompressorJob.POTATO) {
                    Fileupload activeFile = beans.getFile().get(file.getFilename());
                    activeFile.setBrdata(compressedData);
                    beans.getFile().upsert(Arrays.asList(activeFile));
                    PageCache global = beans.getGlobalCache();
                    global.removeAll(global.searchLookups(activeFile.getFilename()));
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
