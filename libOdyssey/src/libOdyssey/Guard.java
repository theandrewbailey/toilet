package libOdyssey;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
import libOdyssey.bean.ExceptionRepo;
import libOdyssey.bean.GuardHolder;
import libOdyssey.bean.GuardRepo;
import org.apache.catalina.connector.ResponseFacade;

@WebFilter(description = "DoS preventer (maybe)", filterName = "libOdysseyGuard", dispatcherTypes = {DispatcherType.REQUEST}, urlPatterns = {"/*"})
public class Guard implements Filter {

    @EJB
    private GuardHolder guardholder;
    @EJB
    private GuardRepo guardrepo;
    @EJB
    private ExceptionRepo error;
    public static final String KILLED_REQUEST = "$_LIBODYSSEY_KILLED_REQUEST";
    private static final Logger log = Logger.getLogger(Guard.class.getName());
    private static final String DEFAULT_REQUEST_ENCODING = "UTF-8";
    private static final String DEFAULT_RESPONSE_ENCODING = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (DEFAULT_REQUEST_ENCODING.equals(request.getCharacterEncoding())) {
            request.setCharacterEncoding(DEFAULT_REQUEST_ENCODING);
        }
        if (DEFAULT_RESPONSE_ENCODING.equals(response.getCharacterEncoding())) {
            response.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
        }

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
            if (req.getSession().isNew()) {
                int[] sps = guardholder.getSps();
                int[] es = guardholder.getEs();
                if (guardrepo.sessionsPerSecond(request.getRemoteAddr(), sps[0], sps[1])
                        || guardrepo.emptySessionCheck(request.getRemoteAddr(), es[0], es[1], es[2])) {
                    kill(request, response);
                    return;
                }
            }
        }

        response=new NoServerHeader((HttpServletResponse)response);
        if (guardholder.isHandleErrors()) {
            try {
                chain.doFilter(request, response);
                if (response instanceof ResponseFacade && ((ResponseFacade) response).isError()) {
                    error.add(req, null, null, null);
                }
            } catch (Exception x) {
                error.add(req, null, null, x);
            }
        }
        else {
            chain.doFilter(request, response);
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

    @Override
    public void destroy() {
    }
}
