package libWebsiteTools.turbo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author alpha
 */
public class GzipOutputStream extends GZIPOutputStream {

    public GzipOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    public GzipOutputStream(OutputStream out, int level) throws IOException {
        super(out);
        def.setLevel(level);
    }
}
