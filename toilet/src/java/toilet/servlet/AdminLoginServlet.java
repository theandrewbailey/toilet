package toilet.servlet;

import java.io.IOException;
import java.security.cert.X509Certificate;
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
import libOdyssey.CertPath;
import libOdyssey.CertUtil;
import libOdyssey.OdysseyFilter;
import libOdyssey.bean.GuardRepo;
import libWebsiteTools.file.FileUtil;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.tag.AbstractInput;
import toilet.db.Article;
import toilet.rss.ErrorRss;

@WebServlet(name = "AdminLoginServlet", description = "Populates admin view JSPs", urlPatterns = {"/adminLogin"})
public class AdminLoginServlet extends ToiletServlet {

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
    public static final String MAN_HEALTH = "WEB-INF/manHealth.jsp";
    public static final String MAN_ENTRIES = "WEB-INF/manEntry.jsp";
    public static final String MAN_SESSIONS = "WEB-INF/manSession.jsp";
    public static final String RIDDLE = "WEB-INF/riddle.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        asyncFiles(request);
        request.getRequestDispatcher(RIDDLE).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        asyncFiles(request);
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
            Future<Boolean> isHealth = exec.submit(new PasswordChecker(imead, answer, SESSIONS));

            if (isImport.get()) {
                request.getSession().setAttribute("login", IMPORT);
                request.getRequestDispatcher(MAN_IMPORT).forward(request, response);
                return;
            } else if (isLog.get()) {
                request.getSession().setAttribute("login", LOG);
                response.sendRedirect(imead.getValue(GuardRepo.CANONICAL_URL) + "rss/" + ErrorRss.NAME);
                return;
            } else if (isPosts.get()) {
                AdminPost.showList(request, response, entry.getArticleArchive(null));
                return;
            } else if (isAddEntry.get()) {
                Article art = (Article) request.getSession().getAttribute(AdminPost.LAST_ARTICLE_EDITED);
                if (null == art) {
                    art = new Article();
                }
                AdminPost.displayArticleEdit(request, response, art);
                return;
            } else if (isContent.get()) {
                AdminContent.showFileList(request, response, file.getFileMetadata(null));
                return;
            } else if (isReset.get()) {
                util.resetEverything();
                request.getSession().invalidate();
                request.getSession(true);
                response.sendRedirect(imead.getValue(GuardRepo.CANONICAL_URL));
                return;
            } else if (isHealth.get()) {
                asyncFiles(request);
                request.setAttribute("uptime", exec.submit(() -> {
                    try {
                        return new String(FileUtil.runProcess("uptime", null, 1000));
                    } catch (IOException | RuntimeException t) {
                        return t.getLocalizedMessage();
                    }
                }));
                request.setAttribute("free", exec.submit(() -> {
                    try {
                        return new String(FileUtil.runProcess("free -m", null, 1000));
                    } catch (IOException | RuntimeException t) {
                        return t.getLocalizedMessage();
                    }
                }));
                request.setAttribute("disk", exec.submit(() -> {
                    try {
                        return new String(FileUtil.runProcess("df -hx tmpfs", null, 1000));
                    } catch (IOException | RuntimeException t) {
                        return t.getLocalizedMessage();
                    }
                }));
                request.setAttribute("articles", exec.submit(() -> {
                    return entry.getArticleArchive(null);
                }));
                request.setAttribute("comments", exec.submit(() -> {
                    return entry.getCommentArchive(null);
                }));
                request.setAttribute("files", exec.submit(() -> {
                    return file.getFileMetadata(null);
                }));
                Map<X509Certificate, LinkedHashMap> certInfo = new HashMap<>();
                try {
                    CertUtil certUtil = (CertUtil) request.getServletContext().getAttribute(CertUtil.class.getCanonicalName());
                    List<CertPath<X509Certificate>> certPaths = certUtil.getServerCertificateChain(imead.getValue(OdysseyFilter.CERTIFICATE_NAME));
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
                request.getRequestDispatcher(MAN_HEALTH).forward(request, response);
                return;
                /*
            } else if (isSessions.get()) {
                // TODO: analytics
                request.getRequestDispatcher(MAN_DAY_SELECT).forward(request, response);
                return;
            } else if (isAnalyze.get()
                    || (ANALYZE.equals(request.getSession().getAttribute("login"))
                    && ANALYZE.equals(answer))) {
                // TODO: analytics
                request.setAttribute("etags", cache.getEtags());
                request.getRequestDispatcher(MAN_ANAL).forward(request, response);
                return;
/**/
            }
        } catch (InterruptedException | ExecutionException ex) {
            error.add(request, "Multithread Exception", "Something happened while verifying passwords", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        request.getSession().setAttribute("login", null);
        error.add(request, null, "Tried to access restricted area.", null);
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
        return imead.verifyArgon2(toVerify, key);
    }
}
