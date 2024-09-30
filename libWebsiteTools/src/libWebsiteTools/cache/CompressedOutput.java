package libWebsiteTools.cache;

//import io.airlift.compress.zstd.ZstdOutputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author alpha
 */
public abstract class CompressedOutput extends ServletOutputStream {

    public static class Gzip extends CompressedOutput {

        public static final String TYPE = "gzip";
        public static final Pattern PATTERN = Pattern.compile("(?:.*? )?gzip(?:,.*)?");

        public Gzip(HttpServletResponse res) throws IOException {
            super(res);
            res.setHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
            this.output = res.getOutputStream();
            compressedStream = new GZIPOutputStream(output);
        }
    }

//    public static class Zstd extends CompressedOutput {
//
//        public static final String TYPE = "zstd";
//        public static final Pattern PATTERN = Pattern.compile("(?:.*? )?zstd(?:,.*)?");
//
//        public Zstd(HttpServletResponse res) throws IOException {
//            super(res);
//            res.setHeader(HttpHeaders.CONTENT_ENCODING, "zstd");
//            this.output = res.getOutputStream();
//            compressedStream = new ZstdOutputStream(output);
//        }
//    }

    OutputStream compressedStream;
    final AtomicBoolean open = new AtomicBoolean(true);
    ServletOutputStream output;

    public CompressedOutput(HttpServletResponse res) throws IOException {
    }

    @Override
    public void close() throws IOException {
        if (open.compareAndSet(true, false)) {
            compressedStream.close();
        }
    }

    @Override
    public void flush() throws IOException {
        compressedStream.flush();
        output.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (!open.get()) {
            throw new IOException("Stream closed!");
        }
        compressedStream.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        if (!open.get()) {
            throw new IOException("Stream closed!");
        }
        compressedStream.write(b);
    }

    @Override
    public boolean isReady() {
        return output.isReady() && open.get();
    }

    @Override
    public void setWriteListener(WriteListener wl) {
        output.setWriteListener(wl);
    }

}
