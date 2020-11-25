package toilet;

import toilet.bean.ToiletBeanAccess;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.ExecutionException;
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
import toilet.rss.ErrorRss;
import toilet.servlet.AdminLoginServlet;

/**
 *
 * @author alpha
 */
@WebFilter(filterName = "AdminChecker", description = "makes sure that you are logged in to do admin duties",
        dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE},
        urlPatterns = {"/adminLogin", "/adminContent", "/adminArticle", "/adminSession", "/adminHealth", "/adminImead", "/adminImport", "/rss/" + ErrorRss.NAME})
public class AdminChecker extends ToiletBeanAccess implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        res.setHeader(HttpHeaders.CACHE_CONTROL, "private, no-store");
        res.setDateHeader(HttpHeaders.EXPIRES, new Date().getTime() + 1000);
        if (DispatcherType.REQUEST.equals(req.getDispatcherType())
                && null != req.getHeader(HttpHeaders.AUTHORIZATION)
                && (null == req.getSession(false)
                || !req.getHeader(HttpHeaders.AUTHORIZATION).equals(req.getSession().getAttribute(HttpHeaders.AUTHORIZATION)))) {
            try {
                String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION).substring(6);
                String decoded = new String(Base64.getDecoder().decode(authHeader), "UTF-8");
                String[] parts = decoded.split(":");
                if (1 < parts.length) {
                    req.getSession().setAttribute(AdminLoginServlet.PERMISSION, AdminLoginServlet.getScope(this, parts[1]));
                    req.getSession().setAttribute(HttpHeaders.AUTHORIZATION, req.getHeader(HttpHeaders.AUTHORIZATION));
                }
            } catch (InterruptedException | ExecutionException ex) {
            }
        }
        if ((req.getRequestURI().endsWith("/adminLogin") && "GET".equalsIgnoreCase(req.getMethod()))
                || FirstTimeDetector.FIRST_TIME_SETUP.equals(request.getServletContext().getAttribute(FirstTimeDetector.FIRST_TIME_SETUP))) {
            // the exception
        } else if (null != req.getSession(false) && req.getSession().getAttribute(AdminLoginServlet.PERMISSION) == null && AbstractInput.getParameter(req, "answer") == null) {
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic");
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
