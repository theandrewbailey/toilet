package libWebsiteTools.file;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import libWebsiteTools.CompressionAlgorithm;

/**
 * Because sometimes other compression methods have a smaller size for a random
 * file. Note: this class has a natural ordering that is inconsistent with
 * equals.
 *
 * @author alpha
 */
public abstract class CompressionSorter implements CompressionAlgorithm<HttpServletResponse, HttpServletRequest>, Comparable<CompressionAlgorithm> {

    public static CompressionSorter getInstance(Fileupload f, HttpServletRequest req) {
        LinkedList<CompressionSorter> algorithms = new LinkedList<>();
        ZstdFile z = new ZstdFile(f);
        if (z.shouldCompress(req)) {
            algorithms.push(z);
        }
        BrFile b = new BrFile(f);
        if (b.shouldCompress(req)) {
            algorithms.push(b);
        }
        GzipFile g = new GzipFile(f);
        if (g.shouldCompress(req)) {
            algorithms.push(g);
        }
        algorithms.push(new PlainFile(f));
        algorithms.sort(null);
        return algorithms.poll();
    }

    @Override
    public boolean shouldCompress(HttpServletRequest req) {
        if (null != getResult()) {
            String encoding = req.getHeader(HttpHeaders.ACCEPT_ENCODING);
            if (null != encoding) {
                return Arrays.asList(encoding.split(", ")).contains(getType());
            }
        }
        return false;
    }

    /**
     * Set response headers, return null
     *
     * @param res
     * @return null
     * @throws IOException
     */
    @Override
    public OutputStream getOutputStream(HttpServletResponse res) throws IOException {
        res.setHeader(HttpHeaders.CONTENT_ENCODING, getType());
        res.setContentLength(getResult().length);
        return null;
    }

    @Override
    public HttpServletResponse setResult(HttpServletResponse res, byte[] compressedData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int compareTo(CompressionAlgorithm t) {
        return this.getResult().length - t.getResult().length;
    }

    private static class ZstdFile extends CompressionSorter {

        private final Fileupload f;

        public ZstdFile(Fileupload file) {
            f = file;
        }

        @Override
        public String getType() {
            return "zstd";
        }

        @Override
        public byte[] getResult() {
            return f.getZstddata();
        }
    }

    private static class BrFile extends CompressionSorter {

        private final Fileupload f;

        public BrFile(Fileupload file) {
            f = file;
        }

        @Override
        public String getType() {
            return "br";
        }

        @Override
        public byte[] getResult() {
            return f.getBrdata();
        }
    }

    private static class GzipFile extends CompressionSorter {

        private final Fileupload f;

        public GzipFile(Fileupload file) {
            f = file;
        }

        @Override
        public String getType() {
            return "gzip";
        }

        @Override
        public byte[] getResult() {
            return f.getGzipdata();
        }
    }

    private static class PlainFile extends CompressionSorter {

        private final Fileupload f;

        public PlainFile(Fileupload file) {
            f = file;
        }

        @Override
        public OutputStream getOutputStream(HttpServletResponse res) throws IOException {
            res.setContentLength(getResult().length);
            return null;
        }

        @Override
        public boolean shouldCompress(HttpServletRequest req) {
            return true;
        }

        @Override
        public String getType() {
            return "plain";
        }

        @Override
        public byte[] getResult() {
            return f.getFiledata();
        }
    }
}
