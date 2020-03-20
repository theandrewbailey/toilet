package libWebsiteTools;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;
import javax.ejb.EJB;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.file.FileServlet;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.HtmlTime;

/**
 *
 * @author alpha
 */
@WebFilter(description = "", filterName = "JspFilter", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD}, urlPatterns = {"*.jsp", "/rss/*"})
public class JspFilter implements Filter {

    public static final String CONTENT_SECURITY_POLICY = "security_content_security_policy";
    public static final String PRIMARY_LOCALE_PARAM = "$_LIBIMEAD_PRIMARY_LOCALE";
    public static final String VARY_HEADER = String.join(", ", new String[]{
        HttpHeaders.ETAG, HttpHeaders.ACCEPT_ENCODING, HttpHeaders.CONTENT_ENCODING, HttpHeaders.ACCEPT_LANGUAGE, HttpHeaders.CONTENT_LANGUAGE});
    @EJB
    private IMEADHolder imead;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = ((HttpServletRequest) request);
        Locale primaryLocale = Local.resolveLocales(req).get(0);
        request.setAttribute(PRIMARY_LOCALE_PARAM, primaryLocale);
        Object csp = request.getAttribute(CONTENT_SECURITY_POLICY);
        HttpServletResponse res = (HttpServletResponse) response;
        res.addHeader(HttpHeaders.CONTENT_LANGUAGE, primaryLocale.toLanguageTag());
        res.addHeader(HttpHeaders.VARY, JspFilter.VARY_HEADER);
        res.addHeader("Content-Security-Policy", null == csp ? imead.getValue(CONTENT_SECURITY_POLICY) : csp.toString());
        res.addHeader("X-Frame-Options", "SAMEORIGIN");
        res.addHeader("X-Xss-Protection", "1; mode=block");
        if (null == request.getAttribute(HtmlTime.FORMAT_VAR)) {
            request.setAttribute(HtmlTime.FORMAT_VAR, imead.getLocal(HtmlTime.DATE_FORMAT_LONG, Local.resolveLocales((HttpServletRequest) request)));
        }

        String encoding = req.getHeader(HttpHeaders.ACCEPT_ENCODING);
        if (null != encoding && FileServlet.GZIP_PATTERN.matcher(encoding).find()) {
            GZIPHttpServletResponseWrapper gzRes = new GZIPHttpServletResponseWrapper((HttpServletResponse) response);
            chain.doFilter(request, gzRes);
            gzRes.flushBuffer();
            gzRes.finish();
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }

}

class GZIPHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private ServletResponseGZIPOutputStream gzipStream;
    private ServletOutputStream outputStream;
    private PrintWriter printWriter;

    public GZIPHttpServletResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);
        response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
    }

    public void finish() throws IOException {
        if (printWriter != null) {
            printWriter.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        if (gzipStream != null) {
            gzipStream.close();
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        if (printWriter != null) {
            printWriter.flush();
        }
        if (outputStream != null) {
            outputStream.flush();
        }
        super.flushBuffer();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (printWriter != null) {
            throw new IllegalStateException("printWriter already defined");
        }
        if (outputStream == null) {
            initGzip();
            outputStream = gzipStream;
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("printWriter already defined");
        }
        if (printWriter == null) {
            initGzip();
            printWriter = new PrintWriter(new OutputStreamWriter(gzipStream, getResponse().getCharacterEncoding()));
        }
        return printWriter;
    }

    @Override
    public void setContentLength(int len) {
    }

    private void initGzip() throws IOException {
        gzipStream = new ServletResponseGZIPOutputStream(getResponse().getOutputStream());
    }

}

class ServletResponseGZIPOutputStream extends ServletOutputStream {

    GZIPOutputStream gzipStream;
    final AtomicBoolean open = new AtomicBoolean(true);
    OutputStream output;

    public ServletResponseGZIPOutputStream(OutputStream output) throws IOException {
        this.output = output;
        gzipStream = new GZIPOutputStream(output);
    }

    @Override
    public void close() throws IOException {
        if (open.compareAndSet(true, false)) {
            gzipStream.close();
        }
    }

    @Override
    public void flush() throws IOException {
        gzipStream.flush();
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
        gzipStream.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        if (!open.get()) {
            throw new IOException("Stream closed!");
        }
        gzipStream.write(b);
    }

    @Override
    public boolean isReady() {
        return open.get();
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
    }

}
