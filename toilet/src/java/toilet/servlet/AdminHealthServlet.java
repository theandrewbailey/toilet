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
import jakarta.ws.rs.core.HttpHeaders;
import libWebsiteTools.cache.CachedPage;
import libWebsiteTools.file.FileUtil;
import libWebsiteTools.imead.Local;
import libWebsiteTools.security.CertPath;
import libWebsiteTools.security.CertUtil;
import libWebsiteTools.security.GuardFilter;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.tag.AbstractInput;
import toilet.UtilStatic;
import toilet.bean.ToiletBeanAccess;
import toilet.rss.ErrorRss;

/**
 *
 * @author alpha
 */
@WebServlet(name = "AdminHealth", urlPatterns = {"/adminHealth"})
public class AdminHealthServlet extends ToiletServlet {

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
        if (!AdminLoginServlet.HEALTH.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        request.setAttribute("processes", beans.getExec().submit(() -> {
            LinkedHashMap<String, Future< String>> processes = new LinkedHashMap<>();
            for (String command : beans.getImeadValue(AdminLoginServlet.HEALTH_COMMANDS).split("\n")) {
                processes.put(command, beans.getExec().submit(() -> {
                    try {
                        return new String(FileUtil.runProcess(command, null, 1000));
                    } catch (IOException | RuntimeException t) {
                        return t.getLocalizedMessage();
                    }
                }));
            }
            return processes;
        }));
        request.setAttribute("articles", beans.getExec().submit(() -> {
            return beans.getArts().getAll(null);
        }));
        request.setAttribute("comments", beans.getExec().submit(() -> {
            return beans.getComms().getAll(null);
        }));
        request.setAttribute("files", beans.getExec().submit(() -> {
            return beans.getFile().getFileMetadata(null);
        }));
        request.setAttribute("cached", beans.getExec().submit(() -> {
            ArrayList<String> cached = new ArrayList<>();
            cached.add(UtilStatic.htmlFormat("Total pages: " + beans.getGlobalCache().getTotalPages() + "\nTotal hits: " + beans.getGlobalCache().getTotalHits(), false, false, true));
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
                        if (null != cert) {
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
        request.getRequestDispatcher(AdminLoginServlet.ADMIN_HEALTH).forward(request, response);
    }
}
