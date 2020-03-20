package toilet;

import java.io.IOException;
import java.util.Date;
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
import libWebsiteTools.tag.AbstractInput;
import toilet.servlet.AdminLoginServlet;

/**
 *
 * @author alpha
 */
@WebFilter(filterName = "AdminChecker", description = "makes sure that you are logged in to do admin duties",
        dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE}, urlPatterns = {"/adminLogin", "/adminContent", "/adminPost", "/adminSession", "/import"})
public class AdminChecker implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        res.setHeader(HttpHeaders.CACHE_CONTROL, "private, no-store");
        res.setDateHeader(HttpHeaders.EXPIRES, new Date().getTime() + 1000);
        if ((req.getRequestURI().endsWith("/adminLogin") && "GET".equalsIgnoreCase(req.getMethod()))
                || FirstTimeDetector.FIRST_TIME_SETUP.equals(request.getServletContext().getAttribute(FirstTimeDetector.FIRST_TIME_SETUP))) {
            // the exception
        } else if (req.getSession().getAttribute(AdminLoginServlet.PERMISSION) == null && AbstractInput.getParameter(req, "answer") == null) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
