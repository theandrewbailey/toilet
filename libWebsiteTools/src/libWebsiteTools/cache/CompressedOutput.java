package libWebsiteTools.cache;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;
import com.github.luben.zstd.ZstdOutputStream;
import com.github.luben.zstd.util.Native;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
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

    static {
        Brotli4jLoader.ensureAvailability();
        Native.load();
    }

    public static class Zstd extends CompressedOutput {

        public static final String TYPE = "zstd";
        public static final Pattern PATTERN = Pattern.compile("(?:.*? )?zstd(?:,.*)?");

        public Zstd(HttpServletResponse res) throws IOException {
            super(res);
            res.setHeader(HttpHeaders.CONTENT_ENCODING, TYPE);
            this.output = res.getOutputStream();
            compressedStream = new ZstdOutputStream(output, 5);
        }
    }

    public static class Brotli extends CompressedOutput {

        public static final String TYPE = "br";
        public static final Pattern PATTERN = Pattern.compile("(?:.*? )?br(?:,.*)?");

        public Brotli(HttpServletResponse res) throws IOException {
            super(res);
            res.setHeader(HttpHeaders.CONTENT_ENCODING, TYPE);
            Encoder.Parameters params = new Encoder.Parameters().setMode(res.getContentType().contains("text/") ? Encoder.Mode.TEXT : Encoder.Mode.GENERIC);
            this.output = res.getOutputStream();
            compressedStream = new BrotliOutputStream(output, params);
        }
    }

    public static class Gzip extends CompressedOutput {

        public static final String TYPE = "gzip";
        public static final Pattern PATTERN = Pattern.compile("(?:.*? )?gzip(?:,.*)?");

        public Gzip(HttpServletResponse res) throws IOException {
            super(res);
            res.setHeader(HttpHeaders.CONTENT_ENCODING, TYPE);
            this.output = res.getOutputStream();
            compressedStream = new GZIPOutputStream(output);
        }
    }

    public static class None extends CompressedOutput {

        public None(HttpServletResponse res) throws IOException {
            super(res);
            this.output = res.getOutputStream();
            compressedStream = this.output;
        }
    }

    public static ServletOutputWrapper getCompressedOutput(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String encoding = req.getHeader(HttpHeaders.ACCEPT_ENCODING);
        if (null != encoding) {
            if (Native.isLoaded() && CompressedOutput.Zstd.PATTERN.matcher(encoding).find()) {
                return new ServletOutputWrapper<>(Zstd.class, res);
            } else if (Brotli4jLoader.isAvailable() && CompressedOutput.Brotli.PATTERN.matcher(encoding).find()) {
                return new ServletOutputWrapper<>(Brotli.class, res);
            } else if (CompressedOutput.Gzip.PATTERN.matcher(encoding).find()) {
                return new ServletOutputWrapper<>(Gzip.class, res);
            }
        }
        return new ServletOutputWrapper<>(None.class, res);
    }

    public static String getBestCompression(HttpServletRequest req) {
        String encoding = req.getHeader(HttpHeaders.ACCEPT_ENCODING);
        if (null != encoding) {
            if (Native.isLoaded() && CompressedOutput.Zstd.PATTERN.matcher(encoding).find()) {
                return Zstd.TYPE;
            } else if (Brotli4jLoader.isAvailable() && CompressedOutput.Brotli.PATTERN.matcher(encoding).find()) {
                return Brotli.TYPE;
            } else if (CompressedOutput.Gzip.PATTERN.matcher(encoding).find()) {
                return Gzip.TYPE;
            }
        }
        return "none";
    }

    protected OutputStream compressedStream;
    protected final AtomicBoolean open = new AtomicBoolean(true);
    protected ServletOutputStream output;

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
