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
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.cache.PageCacheProvider;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import libWebsiteTools.rss.FeedBucket;
import toilet.bean.ArticleRepo;
import toilet.bean.BackupDaemon;
import toilet.bean.CommentRepo;
import toilet.bean.StateCache;

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
    @Resource
    protected ManagedExecutorService exec;
    @EJB
    protected SecurityRepo error;
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
    protected FeedBucket feeds;
    @EJB
    protected BackupDaemon backup;
    @Inject
    protected PageCacheProvider pageCacheProvider;

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
