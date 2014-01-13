package libOdyssey;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
import libOdyssey.bean.GuardHolder;
import libOdyssey.bean.GuardRepo;
import libOdyssey.bean.SessionBean;

@WebFilter(description = "Web analytics request logger and DoS preventer (maybe)", filterName = "OdysseyFilter", dispatcherTypes = {DispatcherType.REQUEST}, urlPatterns = {"/*"})
public class OdysseyFilter implements Filter {

    @EJB
    private GuardHolder guardholder;
    @EJB
    private GuardRepo guardrepo;
    @EJB
    private ExceptionRepo error;
    public static final String KILLED_REQUEST = "$_LIBODYSSEY_KILLED_REQUEST";
    public static final String HANDLED_ERROR = "$_LIBODYSSEY_HANDLED_ERROR";
    private static final Logger log = Logger.getLogger(OdysseyFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // validate request
        HttpServletRequest req = (HttpServletRequest) request;
        if (guardholder.isEnableGuard()) {
            if (guardrepo.inHoneypot(request.getRemoteAddr())) {
                killInHoney(request, response);
                error.add((HttpServletRequest)request, null, "IP already in honeypot", null);
                return;
            }
            String sn= guardholder.getHostValue().contains(":") ? request.getServerName() + ":"+request.getServerPort() : request.getServerName();
            if (!guardholder.getHostValue().equals(sn)) {
                kill(request, response);
                return;
            }
            if ("TRACE".equalsIgnoreCase(req.getMethod()) || 
                    "OPTIONS".equalsIgnoreCase(req.getMethod()) || 
                    "CONNECT".equalsIgnoreCase(req.getMethod()) || 
                    "HEAD".equalsIgnoreCase(req.getMethod())) {
                killInHoney(request, response);
                error.add((HttpServletRequest)request, null, "IP added to honeypot: Illegal method", null);
                return;
            }
            String userAgent = req.getHeader("User-Agent");
            if (userAgent != null) {
                for (Pattern u : guardholder.getDenyUAs()) {
                    if (u.matcher(userAgent).matches()) {
                        killInHoney(request, response);
                        error.add((HttpServletRequest)request, null, "IP added to honeypot: Illegal User-Agent", null);
                        return;
                    }
                }
            }
            String uri=req.getRequestURI().trim();
            for (Pattern h : guardholder.getHoneyList()) {
                if (h.matcher(uri).matches()) {
                    killInHoney(request, response);
                    error.add((HttpServletRequest)request, null, "IP added to honeypot: Illegal URL", null);
                    return;
                }
            }
//            if (req.getSession().isNew()) {
//                int[] sps = guardholder.getSps();
//                int[] es = guardholder.getEs();
//                if (guardrepo.sessionsPerSecond(request.getRemoteAddr(), sps[0], sps[1])
//                        || guardrepo.emptySessionCheck(request.getRemoteAddr(), es[0], es[1], es[2])) {
//                    kill(request, response);
//                    return;
//                }
//            }
        }

        // process request
        NoServerHeader res=new NoServerHeader((HttpServletResponse)response);
        if (guardholder.isHandleErrors()) {
            try {
                chain.doFilter(req, res);
                if (res.getStatus()>=400 && req.getAttribute(HANDLED_ERROR) == null) {
                    error.add(req, null, null, null);
                }
            } catch (Exception x) {
                error.add(req, null, null, x);
            }
        }
        else {
            chain.doFilter(request, response);
        }

        // log request
        if (req.getAttribute(OdysseyFilter.KILLED_REQUEST) != null) {
            return;
        }
        if (null == req.getAttribute(ResponseTag.RENDER_TIME_PARAM)) {
            request.setAttribute(ResponseTag.RENDER_TIME_PARAM, new Date().getTime() - ((Date) request.getAttribute(RequestTime.TIME_PARAM)).getTime());
        }
        try{
            SessionBean sbean = getSessionBean(req);
            if (sbean != null) {
                sbean.logRequest(req, res);
            }
        } catch (Exception e) {
            error.add(req, "Session bean error", null, e);
        }
    }

    public void killInHoney(ServletRequest request, ServletResponse response) throws IOException {
        kill(request, response);
        guardrepo.putInHoneypot(request.getRemoteAddr());
    }

    public static void kill(ServletRequest request, ServletResponse response) throws IOException {
        request.setAttribute(KILLED_REQUEST, KILLED_REQUEST);
        request.getInputStream().close();
        response.getOutputStream().close();
    }

    public static SessionBean getSessionBean(HttpServletRequest req) {
        HttpSession sess = req.getSession();
        SessionBean sbean = (SessionBean) sess.getAttribute(SessionBean.SESSION_BEAN);
        if (sbean == null) {
            try {
                InitialContext context =new InitialContext();
                sbean = (SessionBean) context.lookup(SessionBean.LOCAL_NAME);
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
}
