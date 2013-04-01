package libOdyssey;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import javax.servlet.http.HttpSession;
import libOdyssey.bean.ExceptionRepo;
import libOdyssey.bean.SessionBean;

/**
 *
 * @author alpha
 */
@WebFilter(description="Web analytics request logger", filterName = "libOdysseyAnalytics", dispatcherTypes = {DispatcherType.REQUEST}, urlPatterns = {"/*"})
public class RegisterHit implements Filter {

    private static final Logger log=Logger.getLogger(RegisterHit.class.getName());
    @EJB
    private ExceptionRepo error;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        req.getSession();
        chain.doFilter(request, response);
        if (req.getAttribute(Guard.KILLED_REQUEST) != null) {
            return;
        }
        if (null == req.getAttribute(ResponseTag.RENDER_TIME_PARAM)) {
            request.setAttribute(ResponseTag.RENDER_TIME_PARAM, new Date().getTime() - ((Date) request.getAttribute(RequestTime.TIME_PARAM)).getTime());
        }
        try{
            SessionBean sbean = getSessionBean(req);
            if (sbean != null) {
                sbean.logRequest(req, (HttpServletResponse) response);
            }
        } catch (Exception e) {
            error.add(req, "Session bean error", null, e);
        }
    }

    public static SessionBean getSessionBean(HttpServletRequest req) {
        HttpSession sess = req.getSession();
        SessionBean sbean = (SessionBean) sess.getAttribute(SessionBean.SESSION_BEAN);
        if (sbean == null) {
            try {
                sbean = (SessionBean) new InitialContext().lookup(SessionBean.LOCAL_NAME);
                sess.setAttribute(SessionBean.SESSION_BEAN, sbean);
            } catch (NamingException ex) {
                log.log(Level.SEVERE, "Session bean lookup name invalid: " + SessionBean.LOCAL_NAME, ex);
            }
        }
        return sbean;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}
