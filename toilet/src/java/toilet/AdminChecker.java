package toilet;

import java.io.IOException;
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

/**
 *
 * @author alpha
 */
@WebFilter(filterName="AdminChecker", description="makes sure that you are logged in to do admin duties", 
        dispatcherTypes={DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE}, urlPatterns={"/admin", "/adminContent", "/adminPost", "/adminSession", "/import"})
public class AdminChecker implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (req.getParameter("answer") == null && req.getSession().getAttribute("login") == null) {
            HttpServletResponse res = (HttpServletResponse) response;
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
