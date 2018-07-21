package toilet.servlet;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.bean.ExceptionRepo;
import libOdyssey.bean.GuardHolder;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.tag.AbstractInput;
import toilet.bean.EntryRepo;
import toilet.bean.StateCache;
import toilet.bean.UtilBean;
import toilet.db.Article;
import toilet.rss.ErrorRss;

@WebServlet(name = "AdminLoginServlet", description = "Populates admin view JSPs", urlPatterns = {"/adminLogin"})
public class AdminLoginServlet extends HttpServlet {

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
    private EntryRepo entry;
    @EJB
    private FileRepo file;
    @EJB
    private StateCache cache;
    @Resource
    private ManagedExecutorService exec;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String answer = AbstractInput.getParameter(request, "answer");
        try {
            if (null == answer || answer.length() < 15) {
                throw new IllegalArgumentException("Answer wasn't long enough!");
            }

            // this will take a while, so multithread
            Future<Boolean> isImport = exec.submit(new PasswordChecker(imead, answer, IMPORT));
            Future<Boolean> isLog = exec.submit(new PasswordChecker(imead, answer, LOG));
            Future<Boolean> isPosts = exec.submit(new PasswordChecker(imead, answer, POSTS));
            Future<Boolean> isAddEntry = exec.submit(new PasswordChecker(imead, answer, ADDENTRY));
            Future<Boolean> isContent = exec.submit(new PasswordChecker(imead, answer, CONTENT));
            Future<Boolean> isReset = exec.submit(new PasswordChecker(imead, answer, RESET));
            Future<Boolean> isSessions = exec.submit(new PasswordChecker(imead, answer, SESSIONS));
            Future<Boolean> isAnalyze = exec.submit(new PasswordChecker(imead, answer, ANALYZE));

            if (isImport.get()) {
                request.getSession().setAttribute("login", IMPORT);
                request.getRequestDispatcher(MAN_IMPORT).forward(request, response);

            } else if (isLog.get()) {
                request.getSession().setAttribute("login", LOG);
                response.sendRedirect(imead.getValue(GuardHolder.CANONICAL_URL) + "rss/" + ErrorRss.NAME);

            } else if (isPosts.get()) {
                AdminPost.showList(request, response, entry.getArticleArchive(null));

            } else if (isAddEntry.get()) {
                Article art = (Article) request.getSession().getAttribute(AdminPost.LAST_ARTICLE_EDITED);
                if (null == art) {
                    art = new Article();
                }
                AdminPost.displayArticleEdit(request, response, art);

            } else if (isContent.get()) {
                AdminContent.showFileList(request, response, file.getUploadArchive());

            } else if (isReset.get()) {
                util.resetEverything();
                request.getSession().invalidate();
                request.getSession(true);
                response.sendRedirect(imead.getValue(GuardHolder.CANONICAL_URL));

            } else if (isSessions.get()
                    || (SESSIONS.equals(request.getSession().getAttribute("login"))
                    && SESSIONS.equals(answer))) {
                // TODO: analytics
                request.getRequestDispatcher(MAN_DAY_SELECT).forward(request, response);

            } else if (isAnalyze.get()
                    || (ANALYZE.equals(request.getSession().getAttribute("login"))
                    && ANALYZE.equals(answer))) {
                // TODO: analytics
                request.setAttribute("etags", cache.getEtags());
                request.getRequestDispatcher(MAN_ANAL).forward(request, response);

            }
        } catch (InterruptedException | ExecutionException ex) {
            error.add(request, "Multithread Exception", "Something happened while verifying passwords", ex);
        } catch (IllegalArgumentException a) {
            request.getSession().setAttribute("login", null);
            error.add(request, null, "Tried to access restricted area.", null);
            response.sendRedirect("index");
        }
    }
}

class PasswordChecker implements Callable<Boolean> {

    private final IMEADHolder imead;
    private final String toVerify;
    private final String key;

    public PasswordChecker(IMEADHolder imead, String toVerify, String key) {
        this.imead = imead;
        this.toVerify = toVerify;
        this.key = key;
    }

    @Override
    public Boolean call() throws Exception {
        return imead.verifyArgon2(toVerify, key);
    }
}
