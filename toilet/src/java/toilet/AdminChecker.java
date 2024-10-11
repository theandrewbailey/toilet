package toilet;

import jakarta.ejb.EJB;
import toilet.bean.ToiletBeanAccess;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import libWebsiteTools.tag.AbstractInput;
import toilet.rss.ErrorRss;
import toilet.servlet.AdminLoginServlet;

/**
 *
 * @author alpha
 */
@WebFilter(filterName = "AdminChecker", description = "makes sure that you are logged in to do admin duties",
        dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE},
        urlPatterns = {"/adminLogin", "/adminContent", "/adminArticle", "/adminSession", "/adminHealth", "/adminImead", "/adminImport", "/adminExport", "/rss/" + ErrorRss.NAME})
public class AdminChecker implements Filter {

    @EJB
    protected ToiletBeanAccess allBeans;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        res.setHeader(HttpHeaders.CACHE_CONTROL, "private, no-store");
        res.setDateHeader(HttpHeaders.EXPIRES, OffsetDateTime.now().toInstant().toEpochMilli());
        if (DispatcherType.REQUEST.equals(req.getDispatcherType())
                && null != req.getHeader(HttpHeaders.AUTHORIZATION)
                && (null == req.getSession(false)
                || !req.getHeader(HttpHeaders.AUTHORIZATION).equals(req.getSession().getAttribute(HttpHeaders.AUTHORIZATION)))) {
            try {
                String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION).substring(6);
                String decoded = new String(Base64.getDecoder().decode(authHeader), "UTF-8");
                String[] parts = decoded.split(":");
                if (1 < parts.length) {
                    req.getSession().setAttribute(AdminLoginServlet.PERMISSION, AdminLoginServlet.getScope(allBeans, parts[1]));
                    req.getSession().setAttribute(HttpHeaders.AUTHORIZATION, req.getHeader(HttpHeaders.AUTHORIZATION));
                }
            } catch (InterruptedException | ExecutionException ex) {
            }
        }
        if ((req.getRequestURI().endsWith("/adminLogin") && "GET".equalsIgnoreCase(req.getMethod()))
                || allBeans.isFirstTime()) {
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
