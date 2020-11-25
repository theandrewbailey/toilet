package libWebsiteTools.file;

import java.util.concurrent.Callable;
import libWebsiteTools.AllBeanAccess;

/**
 *
 * @author alpha
 */
public abstract class FileCompressorJob implements Callable<Boolean>, Comparable<FileCompressorJob> {

    /**
     * use this to synchronize access to database entities
     */
    public static final String POTATO = "POTATO";
    final Fileupload file;
    final AllBeanAccess beans;

    public FileCompressorJob(AllBeanAccess beans, Fileupload file) {
        this.beans = beans;
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
