package toilet.servlet;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.CertPath;
import libWebsiteTools.CertUtil;
import libWebsiteTools.HashUtil;
import libWebsiteTools.GuardFilter;
import libWebsiteTools.bean.SecurityRepo;
import libWebsiteTools.cache.CachedPage;
import libWebsiteTools.cache.PageCache;
import libWebsiteTools.cache.PageCaches;
import libWebsiteTools.file.FileUtil;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.AbstractInput;
import toilet.UtilStatic;
import toilet.db.Article;
import toilet.rss.ErrorRss;

@WebServlet(name = "AdminLoginServlet", description = "Populates admin view JSPs", urlPatterns = {"/adminLogin"})
public class AdminLoginServlet extends ToiletServlet {

    public static final String ADDARTICLE = "admin_addArticle";
    public static final String CONTENT = "admin_content";
    public static final String EDITPOSTS = "admin_editPosts";
    public static final String HEALTH = "admin_health";
    public static final String IMEAD = "admin_imead";
    public static final String LOG = "admin_log";
    public static final String RELOAD = "admin_reload";
    public static final String PERMISSION = "LOGIN";
    public static final String HEALTH_COMMANDS = "site_health_commands";
    public static final String ADMIN_ADD_ARTICLE = "WEB-INF/adminAddArticle.jsp";
    public static final String ADMIN_CONTENT = "WEB-INF/adminContent.jsp";
    public static final String ADMIN_IMPORT = "WEB-INF/adminImport.jsp";
    public static final String ADMIN_HEALTH = "WEB-INF/adminHealth.jsp";
    public static final String ADMIN_EDIT_POSTS = "WEB-INF/adminEditPosts.jsp";
    public static final String ADMIN_LOGIN_PAGE = "WEB-INF/adminLogin.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

            // this will take a while, so multithread
            Future<Boolean> isAddArticle = exec.submit(new PasswordChecker(imead, answer, ADDARTICLE));
            Future<Boolean> isContent = exec.submit(new PasswordChecker(imead, answer, CONTENT));
            Future<Boolean> isHealth = exec.submit(new PasswordChecker(imead, answer, HEALTH));
            Future<Boolean> isImead = exec.submit(new PasswordChecker(imead, answer, IMEAD));
            Future<Boolean> isLog = exec.submit(new PasswordChecker(imead, answer, LOG));
            Future<Boolean> isEditPosts = exec.submit(new PasswordChecker(imead, answer, EDITPOSTS));
            Future<Boolean> isReload = exec.submit(new PasswordChecker(imead, answer, RELOAD));

            if (isLog.get()) {
                request.getSession().setAttribute(PERMISSION, LOG);
                response.sendRedirect(imead.getValue(SecurityRepo.CANONICAL_URL) + "rss/" + ErrorRss.NAME);
                return;
            } else if (isEditPosts.get()) {
                AdminArticle.showList(request, response, arts.getAll(null));
                return;
            } else if (isAddArticle.get()) {
                request.getSession().setAttribute(PERMISSION, ADDARTICLE);
                Article art = (Article) request.getSession().getAttribute(AdminArticle.LAST_ARTICLE_EDITED);
                if (null == art) {
                    art = new Article();
                }
                AdminArticle.displayArticleEdit(request, response, art);
                return;
            } else if (isContent.get()) {
                AdminContent.showFileList(request, response, file.getFileMetadata(null));
                return;
            } else if (isReload.get()) {
                util.resetEverything();
                request.getSession().invalidate();
                request.getSession(true);
                response.sendRedirect(imead.getValue(SecurityRepo.CANONICAL_URL));
                return;
            } else if (isHealth.get()) {
                request.getSession().setAttribute(PERMISSION, HEALTH);
                request.setAttribute("processes", exec.submit(() -> {
                    LinkedHashMap<String, String> processes = new LinkedHashMap<>();
                    for (String command : imead.getValue(HEALTH_COMMANDS).split("\n")) {
                        try {
                            processes.put(command, new String(FileUtil.runProcess(command, null, 1000)));
                        } catch (IOException | RuntimeException t) {
                            processes.put(command, t.getLocalizedMessage());
                        }
                    }
                    return processes;
                }));
                request.setAttribute("articles", exec.submit(() -> {
                    return arts.getAll(null);
                }));
                request.setAttribute("comments", exec.submit(() -> {
                    return comms.getAll(null);
                }));
                request.setAttribute("files", exec.submit(() -> {
                    return file.getFileMetadata(null);
                }));
                request.setAttribute("cached", exec.submit(() -> {
                    PageCache globalCache = (PageCache) pageCacheProvider.getCacheManager().<String, CachedPage>getCache(PageCaches.DEFAULT_URI);
                    ArrayList<String> cached = new ArrayList<>(100);
                    for (Map.Entry<String, CachedPage> page : globalCache.getAll(null).entrySet()) {
                        String key = page.getKey() + "\nExpires: " + page.getValue().getExpires().toString();
                        cached.add(UtilStatic.htmlFormat(key, false, false));
                    }
                    return cached;
                }));
                Map<X509Certificate, LinkedHashMap> certInfo = new HashMap<>();
                try {
                    CertUtil certUtil = (CertUtil) request.getServletContext().getAttribute(CertUtil.CERTIFICATE_CHAIN);
                    List<CertPath<X509Certificate>> certPaths = certUtil.getServerCertificateChain(imead.getValue(GuardFilter.CERTIFICATE_NAME));
                    for (CertPath<X509Certificate> path : certPaths) {
                        for (X509Certificate x509 : path.getCertificates()) {
                            LinkedHashMap<String, String> cert = CertUtil.formatCert(x509);
                            if (null != cert) {
                                certInfo.put(x509, cert);
                            }
                        }
                    }
                    request.setAttribute("certPaths", certPaths);
                } catch (RuntimeException x) {
                    error.add(request, "Certificate error", "building certificate chain", x);
                }
                request.setAttribute("certInfo", certInfo);
                request.setAttribute("locales", Local.resolveLocales(request, imead));
                request.getRequestDispatcher(ADMIN_HEALTH).forward(request, response);
                return;
            } else if (isImead.get()) {
                request.getSession().setAttribute(PERMISSION, IMEAD);
                request.getRequestDispatcher("adminImead").forward(request, response);
                //response.sendRedirect(imead.getValue(SecurityRepo.CANONICAL_URL) + "adminImead");
                return;
            }
        } catch (InterruptedException | ExecutionException ex) {
            error.add(request, "Multithread Exception", "Something happened while verifying passwords", ex);
            request.setAttribute(GuardFilter.HANDLED_ERROR, true);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        request.getSession().setAttribute(PERMISSION, null);
        error.add(request, null, "Tried to access restricted area.", null);
        request.setAttribute(GuardFilter.HANDLED_ERROR, true);
        request.getRequestDispatcher("/").forward(request, response);
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
        try {
            return HashUtil.verifyArgon2Hash(imead.getValue(key), toVerify);
        } catch (Exception x) {
            return false;
        }
    }
}
