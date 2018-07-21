package libOdyssey;

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
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.imead.IMEADHolder;

/**
 *
 * @author alpha
 */
@WebFilter(description = "Adds the Content-Security-Policy HTTP header", filterName = "ContentSecurityPolicyFilter", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD}, urlPatterns = {"*.jsp"})
public class ContentSecurityPolicyFilter implements Filter {

    public static final String CONTENT_SECURITY_POLICY = "libOdyssey_content_security_policy";
    @EJB
    private IMEADHolder imead;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletResponse res = (HttpServletResponse) response;
            Object csp = request.getAttribute(CONTENT_SECURITY_POLICY);
            res.addHeader("Content-Security-Policy", null == csp ? imead.getValue(CONTENT_SECURITY_POLICY) : csp.toString());
        } catch (Exception ex) {
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
