package toilet.servlet;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.imead.Local;
import toilet.bean.ToiletBeanAccess;

/**
 *
 * @author alpha
 */
public abstract class ToiletServlet extends HttpServlet {

    public static final String ERROR_PREFIX = "error_";
    public static final String ERROR_MESSAGE_PARAM = "ERROR_MESSAGE";
    public static final String ERROR_JSP = "/WEB-INF/error.jsp";
    public static final String ERROR_IFRAME_JSP = "/WEB-INF/errorIframe.jsp";
    public static final String SITE_TITLE = "page_title";
    public static final String TAGLINE = "page_tagline";
    @EJB
    protected ToiletBeanAccess beans;

    /**
     * tells the client to go to a new location. WHY is this not included in the
     * standard servlet API????
     *
     * @param res
     * @param newLocation
     */
    public static void permaMove(HttpServletResponse res, String newLocation) {
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader("Location", newLocation);
    }

    protected void showError(HttpServletRequest req, HttpServletResponse res, String errorMessage) {
        req.setAttribute(ERROR_MESSAGE_PARAM, errorMessage);
        try {
            getServletContext().getRequestDispatcher((null == req.getParameter("iframe") ? ERROR_JSP : ERROR_IFRAME_JSP) + "?error=" + req.getAttribute("title")).forward(req, res);
        } catch (IllegalStateException | ServletException | IOException ix) {
        }
    }

    protected void showError(HttpServletRequest req, HttpServletResponse res, Integer errorCode) {
        req.setAttribute("title", "ERROR " + errorCode);
        showError(req, res, beans.getImead().getLocal(ERROR_PREFIX + errorCode, Local.resolveLocales(beans.getImead(), req)));
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
