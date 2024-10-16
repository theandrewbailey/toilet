package toilet.servlet;

import java.io.IOException;
import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import libWebsiteTools.BaseServlet;
import libWebsiteTools.imead.Local;
import toilet.bean.ToiletBeanAccess;

/**
 *
 * @author alpha
 */
public abstract class ToiletServlet extends BaseServlet {

    public static final String ERROR_PREFIX = "page_error_";
    public static final String ERROR_MESSAGE_PARAM = "ERROR_MESSAGE";
    public static final String ERROR_JSP = "/WEB-INF/error.jsp";
    public static final String ERROR_IFRAME_JSP = "/WEB-INF/errorIframe.jsp";
    public static final String SITE_TITLE = "page_title";
    public static final String TAGLINE = "page_tagline";
    @EJB
    protected ToiletBeanAccess allBeans;

    protected void showError(HttpServletRequest req, HttpServletResponse res, String errorMessage) {
        req.setAttribute(ERROR_MESSAGE_PARAM, errorMessage);
        try {
            getServletContext().getRequestDispatcher((null == req.getParameter("iframe") ? ERROR_JSP : ERROR_IFRAME_JSP) + "?error=" + req.getAttribute("title")).forward(req, res);
        } catch (IllegalStateException | ServletException | IOException ix) {
        }
    }

    protected void showError(HttpServletRequest req, HttpServletResponse res, Integer errorCode) {
        req.setAttribute("title", "ERROR " + errorCode);
        ToiletBeanAccess beans = allBeans.getInstance(req);
        showError(req, res, beans.getImead().getLocal(ERROR_PREFIX + errorCode, Local.resolveLocales(beans.getImead(), req)));
    }
}
