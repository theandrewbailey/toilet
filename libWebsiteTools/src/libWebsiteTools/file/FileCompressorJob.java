package libWebsiteTools.file;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;
import com.github.luben.zstd.ZstdOutputStream;
import com.github.luben.zstd.util.Native;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import libWebsiteTools.AllBeanAccess;

/**
 *
 * @author alpha
 */
public abstract class FileCompressorJob implements Callable<Boolean>, Comparable<FileCompressorJob> {

    static {
        Brotli4jLoader.ensureAvailability();
        Native.load();
    }

    public static class Brotlier extends FileCompressorJob {

        public Brotlier(AllBeanAccess beans, Fileupload file) {
            super(beans, file);
        }

        @Override
        public Fileupload setResult(Fileupload file, byte[] compressedData) {
            file.setBrdata(compressedData);
            return file;
        }

        @Override
        public OutputStream getOutputStream(ByteArrayOutputStream collector) throws IOException {
            Encoder.Parameters params = new Encoder.Parameters().setWindow(24).setQuality(11)
                    .setMode(file.getMimetype().contains("text/") ? Encoder.Mode.TEXT : Encoder.Mode.GENERIC);
            return new BrotliOutputStream(collector, params);
        }

        @Override
        public boolean shouldCompress() {
            return null == file.getBrdata();
        }
    }

    public static class Gzipper extends FileCompressorJob {

        public Gzipper(AllBeanAccess beans, Fileupload file) {
            super(beans, file);
        }

        @Override
        public Fileupload setResult(Fileupload file, byte[] compressedData) {
            file.setGzipdata(compressedData);
            return file;
        }

        @Override
        public OutputStream getOutputStream(ByteArrayOutputStream collector) throws IOException {
            return new GZIPOutputStream(collector);
        }

        @Override
        public boolean shouldCompress() {
            return null == file.getGzipdata();

        }
    }

    public static class Zstdder extends FileCompressorJob {

        public Zstdder(AllBeanAccess beans, Fileupload file) {
            super(beans, file);
        }

        @Override
        public OutputStream getOutputStream(ByteArrayOutputStream collector) throws IOException {
            return new ZstdOutputStream(collector, 19);
        }

        @Override
        public Fileupload setResult(Fileupload file, byte[] compressedData) {
            file.setZstddata(compressedData);
            return file;
        }

        @Override
        public boolean shouldCompress() {
            return null == file.getZstddata();
        }
    }

    public static List<Future> startAllJobs(AllBeanAccess beans, Fileupload file) {
        return List.of(
                beans.getExec().submit(new Gzipper(beans, file)),
                beans.getExec().submit(new Brotlier(beans, file)),
                beans.getExec().submit(new Zstdder(beans, file)));
    }
    private final static Logger LOG = Logger.getLogger(FileCompressorJob.class
            .getName());
    final Fileupload file;
    final AllBeanAccess beans;

    public FileCompressorJob(AllBeanAccess beans, Fileupload file) {
        this.beans = beans;
        this.file = file;
    }

    public abstract OutputStream getOutputStream(ByteArrayOutputStream collector) throws IOException;

    public abstract Fileupload setResult(Fileupload file, byte[] compressedData);

    public abstract boolean shouldCompress();

    @Override
    public Boolean call() {
        if (!shouldCompress()) {
            LOG.log(Level.FINEST, "File {0} already compressed.", file.getFilename());
            return false;
        }
        byte[] compressedData = encode();
        synchronized (file) {
            if (null != compressedData && compressedData.length < file.getFiledata().length) {
                Fileupload activeFile = beans.getFile().get(file.getFilename());
                beans.getFile().upsert(Arrays.asList(setResult(activeFile, compressedData)));
                beans.reset();
                return true;
            }
        }
        return false;
    }

    public byte[] encode() {
        ByteArrayOutputStream collector = new ByteArrayOutputStream(file.getFiledata().length * 2);
        try (OutputStream out = getOutputStream(collector)) {
            out.write(file.getFiledata());

        } catch (IOException ex) {
            Logger.getLogger(FileCompressorJob.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }
        return collector.toByteArray();
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
        return t.file.getFiledata().length - file.getFiledata().length;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " " + file.getFilename();
    }

}
