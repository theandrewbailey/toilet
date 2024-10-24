package toilet.servlet;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import libWebsiteTools.cache.CachedPage;
import libWebsiteTools.file.FileUtil;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.imead.Local;
import libWebsiteTools.security.CertPath;
import libWebsiteTools.security.CertUtil;
import libWebsiteTools.security.GuardFilter;
import libWebsiteTools.security.RequestTimer;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.tag.AbstractInput;
import toilet.UtilStatic;
import toilet.bean.ToiletBeanAccess;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.rss.ErrorRss;

/**
 *
 * @author alpha
 */
@WebServlet(name = "AdminHealth", description = "Show some vital stats about the server and blog", urlPatterns = {"/adminHealth"})
public class AdminHealthServlet extends AdminServlet {

    public static final String HEALTH_COMMANDS = "site_healthCommands";
    public static final String ADMIN_HEALTH = "/WEB-INF/adminHealth.jsp";

    @Override
    public AdminServletPermission getRequiredPermission(HttpServletRequest req) {
        return AdminServletPermission.HEALTH;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ToiletBeanAccess beans = allBeans.getInstance(request);
        String action = AbstractInput.getParameter(request, "action");
        if ("reload".equals(action)) {
            beans.reset();
            beans.getExec().submit(beans.getBackup());
            request.getSession().invalidate();
            request.getSession(true);
            response.sendRedirect(request.getAttribute(SecurityRepo.BASE_URL).toString());
        } else if ("error".equals(action)) {
            response.sendRedirect(beans.getImeadValue(SecurityRepo.BASE_URL) + "rss/" + ErrorRss.NAME);
        } else {
            doGet(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ToiletBeanAccess beans = allBeans.getInstance(request);
        request.setAttribute("processes", beans.getExec().submit(() -> {
            LinkedHashMap<String, Future< String>> processes = new LinkedHashMap<>();
            for (String command : beans.getImeadValue(HEALTH_COMMANDS).split("\n")) {
                processes.put(command, beans.getExec().submit(() -> {
                    Instant start = Instant.now();
                    try {
                        return new String(FileUtil.runProcess(command.trim(), null, 1000));
                    } catch (IOException | RuntimeException t) {
                        return t.getLocalizedMessage();
                    } finally {
                        RequestTimer.addTiming(request, "command;desc=\""+command.trim()+"\"", Duration.between(start, Instant.now()));
                    }
                }));
            }
            return processes;
        }));
        request.setAttribute("articles", beans.getExec().submit(() -> {
            Instant start = Instant.now();
            List<Article> arts = beans.getArts().getAll(null);
            RequestTimer.addTiming(request, "articleQuery", Duration.between(start, Instant.now()));
            return arts;
        }));
        request.setAttribute("comments", beans.getExec().submit(() -> {
            Instant start = Instant.now();
            List<Comment> comms = beans.getComms().getAll(null);
            RequestTimer.addTiming(request, "commentQuery", Duration.between(start, Instant.now()));
            return comms;
        }));
        request.setAttribute("files", beans.getExec().submit(() -> {
            Instant start = Instant.now();
            List<Fileupload> files = beans.getFile().getFileMetadata(null);
            RequestTimer.addTiming(request, "fileQuery", Duration.between(start, Instant.now()));
            return files;
        }));
        request.setAttribute("cached", beans.getExec().submit(() -> {
            Instant start = Instant.now();
            ArrayList<String> cached = new ArrayList<>();
            cached.add(UtilStatic.htmlFormat("Total hits: " + beans.getGlobalCache().getTotalHits(), false, false, true));
            ArrayList<CachedPage> pages = new ArrayList<>(beans.getGlobalCache().getAll(null).values());
            Collections.sort(pages, (page, other) -> {
                int difference = other.getHits() - page.getHits();
                if (0 == difference) {
                    long time = page.getCreated().toEpochSecond() - other.getCreated().toEpochSecond();
                    return time < 0 ? 1 : time > 0 ? -1 : 0;
                } else {
                    return difference; // reverse it, too
                }
            });
            try {
                for (CachedPage page : pages) {
                    Integer hits = page.getHits();
                    String key = page.getLookup() + "\nExpires: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(page.getExpires()) + "\nHits: " + hits;
                    cached.add(UtilStatic.htmlFormat(key, false, false, true));
                }
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
            RequestTimer.addTiming(request, "cachedQuery", Duration.between(start, Instant.now()));
            return cached;
        }));
        Map<X509Certificate, LinkedHashMap> certInfo = new HashMap<>();
        if (null != beans.getImeadValue(GuardFilter.CERTIFICATE_NAME)) {
            try {
                CertUtil certUtil = beans.getError().getCerts();
                List<CertPath<X509Certificate>> certPaths = certUtil.getServerCertificateChain(beans.getImeadValue(GuardFilter.CERTIFICATE_NAME));
                for (CertPath<X509Certificate> path : certPaths) {
                    for (X509Certificate x509 : path.getCertificates()) {
                        LinkedHashMap<String, String> cert = CertUtil.formatCert(x509);
                        OffsetDateTime localNow = RequestTimer.getStartTime(request);
                        Long days = (x509.getNotAfter().getTime() - localNow.toInstant().toEpochMilli()) / 86400000;
                        if (null != cert) {
                            cert.put("daysUntilExpiration", days.toString());
                            certInfo.put(x509, cert);
                        }
                    }
                }
                request.setAttribute("certPaths", certPaths);
            } catch (RuntimeException x) {
                beans.getError().logException(request, "Certificate error", "building certificate chain", x);
            }
        }
        request.setAttribute("certInfo", certInfo);
        request.setAttribute("locales", Local.resolveLocales(beans.getImead(), request));
        request.getRequestDispatcher(ADMIN_HEALTH).forward(request, response);
    }
}
