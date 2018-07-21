package toilet;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import libOdyssey.bean.ExceptionRepo;

/**
 *
 * @author alpha
 */
@WebFilter(description = "Inserts Cache-Control header", filterName = "CacheControl", dispatcherTypes = {DispatcherType.REQUEST}, urlPatterns = {"/*"})
public class CacheController implements Filter {

    @EJB
    private ExceptionRepo error;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String URI = req.getRequestURI();
        if (URI.contains("/admin") || URI.endsWith("/riddle.jsp")) {
            res.setHeader(HttpHeaders.CACHE_CONTROL, "private, no-store");
        } else {
            res.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=200000");
        }
        try {
            chain.doFilter(request, response);
        } catch (RuntimeException x) {
            error.add(req, null, null, x);
            throw x;
        }
    }

    @Override
    public void destroy() {
    }
}
