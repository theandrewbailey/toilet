package libWebsiteTools;

import java.io.IOException;
import java.io.Writer;

/**
 * A writer object that goes absolutely nowhere. Useful for JSP tags that are not supposed to have output.
 * @author alpha
 */
public class NullWriter extends Writer {

    @Override
    public void write(char[] chars, int i, int i1) throws IOException {
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }
}
