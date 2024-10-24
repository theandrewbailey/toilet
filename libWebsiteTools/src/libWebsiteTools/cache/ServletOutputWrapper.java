package libWebsiteTools.cache;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.security.RequestTimer;

/**
 *
 * @author alpha
 * @param <ALT> underlying stream type, see subclasses
 */
public class ServletOutputWrapper<ALT extends ServletOutputStream> extends HttpServletResponseWrapper implements Closeable {

    private final Class<ALT> type;
    private ALT outputStream;
    private PrintWriter printWriter;

    public ServletOutputWrapper(Class<ALT> type, HttpServletResponse res) throws IOException {
        super(res);
        this.type = type;
    }

    @Override
    public void close() throws IOException {
        if (printWriter != null) {
            printWriter.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
    }

    @Override
    public void addHeader(String name, String value) {
        ((HttpServletResponse) getResponse()).addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        ((HttpServletResponse) getResponse()).setHeader(name, value);
    }

    @Override
    public void flushBuffer() throws IOException {
        if (printWriter != null) {
            printWriter.flush();
        }
        if (outputStream != null) {
            outputStream.flush();
        }
    }

    @Override
    public ALT getOutputStream() throws IOException {
        if (outputStream == null) {
            try {
                outputStream = type.getConstructor(HttpServletResponse.class).newInstance(getResponse());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
                throw new RuntimeException(ex);
            }
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (printWriter == null && outputStream != null) {
            throw new IllegalStateException("OutputStream already defined. Use that instead.");
        }
        if (printWriter == null) {
            printWriter = new PrintWriter(new OutputStreamWriter(getOutputStream(), getResponse().getCharacterEncoding()));
        }
        return printWriter;
    }

    @Override
    public void setContentLength(int len) {
    }

    public static class ByteArrayOutput extends ServletOutputStream {

        private final ByteArrayOutputStream writer = new ByteArrayOutputStream(50000);

        public ByteArrayOutput(HttpServletResponse res) {
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            writer.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            writer.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }

        @Override
        public String toString() {
            try {
                // huge ass-umption
                return writer.toString("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new JVMNotSupportedError(ex);
            }
        }

        /**
         * @return the writer
         */
        public ByteArrayOutputStream getWriter() {
            return writer;
        }
    }
}
