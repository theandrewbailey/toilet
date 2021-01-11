package toilet.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.security.GuardFilter;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.tag.AbstractInput;
import toilet.AllBeanAccess;
import toilet.IndexFetcher;
import toilet.db.Article;
import toilet.rss.ErrorRss;

@WebServlet(name = "AdminLoginServlet", description = "Populates admin view JSPs", urlPatterns = {"/adminLogin", "/edit/*"})
public class AdminLoginServlet extends ToiletServlet {

    public static final String ADD_ARTICLE = "admin_addArticle";
    public static final String FILES = "admin_files";
    public static final String EDIT_POSTS = "admin_editPosts";
    public static final String ERROR_LOG = "admin_errorLog";
    public static final String HEALTH = "admin_health";
    public static final String IMEAD = "admin_imead";
    public static final String RELOAD = "admin_reload";
    public static final List<String> SCOPES = Arrays.asList(ADD_ARTICLE, FILES, EDIT_POSTS, ERROR_LOG, HEALTH, IMEAD, RELOAD);
    public static final String PERMISSION = "$_ADMIN_LOGIN_SERVLET_PERMISSION";
    public static final String HEALTH_COMMANDS = "file_healthCommands";
    public static final String ADMIN_ADD_ARTICLE = "/WEB-INF/adminAddArticle.jsp";
    public static final String ADMIN_CONTENT = "/WEB-INF/adminFile.jsp";
    public static final String ADMIN_IMPORT = "/WEB-INF/adminImport.jsp";
    public static final String ADMIN_HEALTH = "/WEB-INF/adminHealth.jsp";
    public static final String ADMIN_EDIT_POSTS = "/WEB-INF/adminEditPosts.jsp";
    public static final String ADMIN_LOGIN_PAGE = "/WEB-INF/adminLogin.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Article art = IndexFetcher.getArticleFromURI(beans, request.getRequestURI());
            request.getSession().setAttribute(Article.class.getSimpleName(), art);
        } catch (RuntimeException ex) {
        }
        request.getRequestDispatcher(ADMIN_LOGIN_PAGE).forward(request, response);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String answer = AbstractInput.getParameter(request, "answer");
        try {
            if (null == answer) {
                throw new IllegalArgumentException("No answer!");
            }
            switch (getScope(beans, answer)) {
                case ERROR_LOG:
                    request.getSession().setAttribute(PERMISSION, ERROR_LOG);
                    response.sendRedirect(beans.getImeadValue(SecurityRepo.BASE_URL) + "rss/" + ErrorRss.NAME);
                    return;
                case EDIT_POSTS:
                    request.getSession().setAttribute(AdminLoginServlet.PERMISSION, AdminLoginServlet.EDIT_POSTS);
                    AdminArticle.showList(request, response, beans.getArts().getAll(null));
                    return;
                case ADD_ARTICLE:
                    request.getSession().setAttribute(PERMISSION, ADD_ARTICLE);
                    Article art = (Article) request.getSession().getAttribute(Article.class.getSimpleName());
                    if (null == art) {
                        art = new Article();
                    }
                    AdminArticle.displayArticleEdit(beans, request, response, art);
                    return;
                case FILES:
                    request.getSession().setAttribute(AdminLoginServlet.PERMISSION, AdminLoginServlet.FILES);
                    AdminFile.showFileList(request, response, beans.getFile().getFileMetadata(null));
                    return;
                case RELOAD:
                    beans.reset();
                    beans.getExec().submit(beans.getBackup());
                    request.getSession().invalidate();
                    request.getSession(true);
                    response.sendRedirect(request.getAttribute(SecurityRepo.BASE_URL).toString());
                    return;
                case HEALTH:
                    request.getSession().setAttribute(PERMISSION, HEALTH);
                    request.getRequestDispatcher("/adminHealth").forward(request, response);
                    return;
                case IMEAD:
                    request.getSession().setAttribute(PERMISSION, IMEAD);
                    request.getRequestDispatcher("/adminImead").forward(request, response);
                    return;
                default:
                    beans.getError().logException(request, "Bad Login", "Tried to access restricted area. Login not recognized: " + answer, null);
                    request.getSession().setAttribute(PERMISSION, null);
                    request.setAttribute(GuardFilter.HANDLED_ERROR, true);
                    request.getRequestDispatcher("/").forward(request, response);
                    break;
            }
        } catch (InterruptedException | ExecutionException ex) {
            beans.getError().logException(request, "Multithread Exception", "Something happened while verifying passwords", ex);
            request.setAttribute(GuardFilter.HANDLED_ERROR, true);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public static String getScope(AllBeanAccess beans, String password) throws InterruptedException, ExecutionException {
        List<Future<String>> checkers = new ArrayList<>(SCOPES.size());
        for (String scope : SCOPES) {
            checkers.add(beans.getExec().submit(new PasswordChecker(beans.getImead(), password, scope)));
        }
        for (Future<String> test : checkers) {
            if (null != test.get()) {
                return test.get();
            }
        }
        return null;
    }
}

class PasswordChecker implements Callable<String> {

    private final IMEADHolder imead;
    private final String toVerify;
    private final String key;

    public PasswordChecker(IMEADHolder imead, String toVerify, String key) {
        this.imead = imead;
        this.toVerify = toVerify;
        this.key = key;
    }

    @Override
    public String call() throws Exception {
        try {
            return HashUtil.verifyArgon2Hash(imead.getValue(key), toVerify) ? key : null;
        } catch (Exception x) {
            return null;
        }
    }
}
