package toilet.servlet;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.cache.CachedPage;
import libWebsiteTools.file.FileUtil;
import libWebsiteTools.imead.Local;
import libWebsiteTools.security.CertPath;
import libWebsiteTools.security.CertUtil;
import libWebsiteTools.security.GuardFilter;
import toilet.UtilStatic;

/**
 *
 * @author alpha
 */
@WebServlet(name = "AdminHealth", urlPatterns = {"/adminHealth"})
public class AdminHealth extends ToiletServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
                        return new String(FileUtil.runProcess(command, null, 1000)).trim();
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
            ArrayList<CachedPage> pages = new ArrayList<>(beans.getGlobalCache().getAll(null).values());
            Collections.sort(pages, (page, other) -> {
                return other.getHits() - page.getHits(); // reverse it, too
            });
            SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy h:mm a z");
            try {
                for (CachedPage page : pages) {
                    Integer hits = page.getHits();
                    String key = page.getLookup() + "\nExpires: " + formatter.format(page.getExpires()) + "\nHits: " + hits;
                    cached.add(UtilStatic.htmlFormat(key, false, false, true));
                }
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
            return cached;
        }));
        Map<X509Certificate, LinkedHashMap> certInfo = new HashMap<>();
        try {
            CertUtil certUtil = (CertUtil) request.getServletContext().getAttribute(CertUtil.CERTIFICATE_CHAIN);
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
        request.setAttribute("certInfo", certInfo);
        request.setAttribute("locales", Local.resolveLocales(beans.getImead(), request));
        request.getRequestDispatcher(AdminLoginServlet.ADMIN_HEALTH).forward(request, response);
    }
}
