package toilet.servlet;

import java.io.IOException;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.bean.ExceptionRepo;
import libWebsiteTools.cache.PageCacheProvider;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import toilet.bean.ArticleRepo;
import toilet.bean.CommentRepo;
import toilet.bean.StateCache;
import toilet.bean.UtilBean;

/**
 *
 * @author alpha
 */
public abstract class ToiletServlet extends HttpServlet {

    public static final String ERROR_PREFIX = "error_";
    public static final String ERROR_MESSAGE_PARAM = "ERROR_MESSAGE";
    public static final String ERROR_JSP = "/WEB-INF/error.jsp";
    public static final String ERROR_IFRAME_JSP = "/WEB-INF/errorIframe.jsp";
    @Resource
    protected ManagedExecutorService exec;
    @EJB
    protected ExceptionRepo error;
    @EJB
    protected FileRepo file;
    @EJB
    protected ArticleRepo arts;
    @EJB
    protected CommentRepo comms;
    @EJB
    protected StateCache cache;
    @EJB
    protected IMEADHolder imead;
    @EJB
    protected UtilBean util;
    @Inject
    protected PageCacheProvider pageCacheProvider;

    protected void showError(HttpServletRequest req, HttpServletResponse res, String errorMessage) {
        req.setAttribute(ERROR_MESSAGE_PARAM, errorMessage);
        try {
            getServletContext().getRequestDispatcher(null == req.getParameter("iframe") ? ERROR_JSP : ERROR_IFRAME_JSP).forward(req, res);
        } catch (IllegalStateException | ServletException | IOException ix) {
        }
    }

    protected void showError(HttpServletRequest req, HttpServletResponse res, Integer errorCode) {
        showError(req, res, imead.getLocal(ERROR_PREFIX + errorCode, Local.resolveLocales(req, imead)));
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
