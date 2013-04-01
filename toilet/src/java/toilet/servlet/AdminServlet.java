package toilet.servlet;

import com.lambdaworks.crypto.SCryptUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.bean.ExceptionRepo;
import libWebsiteTools.imead.IMEADHolder;
import toilet.bean.ArticleStateCache;
import toilet.bean.UtilBean;
import toilet.rss.ErrorRss;

@WebServlet(name = "AdminServlet", description = "Populates the various admin view JSPs", urlPatterns = {"/admin"})
public class AdminServlet extends HttpServlet {

    public static final String ADDENTRY = "admin_addEntry";
    public static final String ANALYZE = "admin_anal";
    public static final String CONTENT = "admin_content";
    public static final String IMPORT = "admin_import";
    public static final String LOG = "admin_log";
    public static final String POSTS = "admin_posts";
    public static final String RESET = "admin_reset";
    public static final String SESSIONS = "admin_sessions";
    public static final String MAN_ADD_ENTRY = "WEB-INF/manAddEntry.jsp";
    public static final String MAN_ANAL = "WEB-INF/manAnal.jsp";
    public static final String MAN_CONTENT = "WEB-INF/manContent.jsp";
    public static final String MAN_IMPORT = "WEB-INF/manImport.jsp";
    public static final String MAN_DAY_SELECT = "WEB-INF/manDaySel.jsp";
    public static final String MAN_ENTRIES = "WEB-INF/manEntry.jsp";
    public static final String MAN_SESSIONS = "WEB-INF/manSession.jsp";
    @EJB
    private UtilBean util;
    @EJB
    private ExceptionRepo error;
    @EJB
    private IMEADHolder imead;
    @EJB
    private ArticleStateCache cache;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String answer = request.getParameter("answer");
        if (SCryptUtil.check(answer, imead.getValue(IMPORT))) {
            request.getSession().setAttribute("login", IMPORT);
            request.getRequestDispatcher(MAN_IMPORT).forward(request, response);

        } else if (SCryptUtil.check(answer, imead.getValue(LOG))) {
            request.getSession().setAttribute("login", LOG);
            response.sendRedirect(imead.getValue(UtilBean.THISURL).toString() + "rss/" + ErrorRss.NAME);

        } else if (SCryptUtil.check(answer, imead.getValue(POSTS))) {
            request.getSession().setAttribute("login", AdminServlet.POSTS);
            request.getRequestDispatcher("/adminPost").forward(request, response);

        } else if (SCryptUtil.check(answer, imead.getValue(ADDENTRY))) {
            request.getSession().setAttribute("article", ADDENTRY);
            List<String> groups = new ArrayList<String>(cache.getArticleCategories());
            request.setAttribute("entryType", "entry");
            request.setAttribute("title", "Add Entry");
            request.setAttribute("groups", groups);
            request.getRequestDispatcher(MAN_ADD_ENTRY).forward(request, response);

        } else if (SCryptUtil.check(answer, imead.getValue(CONTENT))) {
            AdminContent.showFileList(request, response);

        } else if (SCryptUtil.check(answer, imead.getValue(RESET))) {
            util.resetEverything();
            request.getSession().invalidate();
            request.getSession(true);
            response.sendRedirect(imead.getValue(UtilBean.THISURL).toString());

        } else if (SCryptUtil.check(answer, imead.getValue(SESSIONS))
                || (SESSIONS.equals(request.getSession().getAttribute("login"))
                && SESSIONS.equals(answer))) {
            // TODO: analytics
            request.getRequestDispatcher(MAN_DAY_SELECT).forward(request, response);

        } else if (SCryptUtil.check(answer, imead.getValue(ANALYZE))
                || (ANALYZE.equals(request.getSession().getAttribute("login"))
                && ANALYZE.equals(answer))) {
            // TODO: analytics
            request.getRequestDispatcher(MAN_ANAL).forward(request, response);

        } else {
            request.getSession().setAttribute("login", null);
            error.add(request, null, "Tried to access restricted area.", null);
            response.sendRedirect("index");
        }
    }
}
