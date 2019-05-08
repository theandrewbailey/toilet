package libWebsiteTools.file;

import java.util.concurrent.Callable;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import libWebsiteTools.imead.IMEADHolder;

/**
 *
 * @author alpha
 */
public abstract class FileCompressorJob implements Callable<Boolean>, Comparable<FileCompressorJob> {

    public static final String POTATO = "POTATO";
    final Fileupload file;
    final FileRepo fileRepo = getBean(FileRepo.LOCAL_NAME, FileRepo.class);
    final IMEADHolder imead = getBean(IMEADHolder.LOCAL_NAME, IMEADHolder.class);

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name, Class<T> type) {
        try {
            return (T) new InitialContext().lookup(name);
        } catch (NamingException n) {
            throw new RuntimeException(n);
        }
    }

    public FileCompressorJob(Fileupload file) {
        this.file = file;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this.getClass().equals(obj.getClass()) && this.toString().equals(obj.toString());
    }

    @Override
    public int compareTo(FileCompressorJob t) {
        // return file.getFiledata().length - t.file.getFiledata().length;
        return t.file.getFiledata().length - file.getFiledata().length; // for queue to work as intended, biggest files should be first
    }

}
