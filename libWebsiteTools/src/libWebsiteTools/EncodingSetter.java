package libWebsiteTools;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebListener;

/**
 *
 * @author alphavm
 */
@WebListener("Sets the request encoding to UTF-8.")
@WebFilter(description = "Sets the response encoding to UTF-8.", filterName = "Encoding Setter", dispatcherTypes = {DispatcherType.REQUEST}, urlPatterns = {"/*"})
public class EncodingSetter implements ServletRequestListener, Filter {
    private static final String DEFAULT_REQUEST_ENCODING = "UTF-8";
    private static final String DEFAULT_RESPONSE_ENCODING = "UTF-8";

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        try {
            sre.getServletRequest().setCharacterEncoding(DEFAULT_REQUEST_ENCODING);
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        res.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
    }
    
}
