package toilet.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.HashUtil;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.bean.GuardRepo;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.HtmlCss;
import libWebsiteTools.tag.HtmlMeta;
import toilet.FirstTimeDetector;
import toilet.UtilStatic;
import toilet.bean.StateCache;
import toilet.db.Article;

@WebServlet(name = "IndexServlet", description = "Gets all the posts of a single group, defaults to Home", urlPatterns = {"/index", "/index/*"})
public class IndexServlet extends ToiletServlet {

    public static final String HOME_JSP = "/WEB-INF/category.jsp";

    @Override
    protected long getLastModified(HttpServletRequest request) {
        return getLastModified(request, cache, imead, file);
    }

    public static long getLastModified(HttpServletRequest request, StateCache cache, IMEADHolder imead, FileRepo file) {
        Date latest = UtilStatic.EPOCH;
        StateCache.IndexFetcher f = cache.getIndexFetcher(request.getRequestURI());
        request.setAttribute(StateCache.IndexFetcher.class.getCanonicalName(), f);
        for (Article a : f.getArticles()) {
            if (a.getModified().after(latest)) {
                latest = a.getModified();
            }
        }
        return latest.getTime();
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StateCache.IndexFetcher f = (StateCache.IndexFetcher) request.getAttribute(StateCache.IndexFetcher.class.getCanonicalName());
        if (null == f) {
            f = cache.getIndexFetcher(request.getRequestURI());
        }
        Collection<Article> lEntry = f.getArticles();
        if (lEntry.isEmpty()) {
            if (HttpMethod.HEAD.equals(request.getMethod())) {
                request.setAttribute(StateCache.IndexFetcher.class.getCanonicalName(), null);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            return;
        }
        asyncRecentCategories(request, f.getSection());
        String etagTag = Local.getLocaleString(request) + request.getRequestURI();
        String etag = cache.getEtag(etagTag);
        if (null == etag) {
            try {
                MessageDigest md = HashUtil.getSHA256();
                for (String cat : cache.getArticleCategories()) {
                    md.update(cat.getBytes("UTF-8"));
                }
                for (Article a : lEntry) {
                    md.update(a.getEtag().getBytes("UTF-8"));
                }
                try {
                    md.update(imead.getLocal(HtmlCss.PAGE_CSS_KEY, Local.resolveLocales(request)).getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                }
                etag = "\"" + Base64.getEncoder().encodeToString(md.digest()) + "\"";
                cache.setEtag(etagTag, etag);
            } catch (UnsupportedEncodingException enc) {
                throw new JVMNotSupportedError(enc);
            }
        }
        response.setHeader(HttpHeaders.ETAG, etag);
        if (etag.equals(request.getHeader("If-None-Match"))) {
            request.setAttribute(StateCache.IndexFetcher.class.getCanonicalName(), null);
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        request.setAttribute(StateCache.IndexFetcher.class.getCanonicalName(), f);
        //response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=100000, immutable");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (FirstTimeDetector.isFirstTime(imead)) {
            request.getRequestDispatcher("adminImead").forward(request, response);
        } else {
            asyncFiles(request);
            doHead(request, response);
            StateCache.IndexFetcher f = (StateCache.IndexFetcher) request.getAttribute(StateCache.IndexFetcher.class.getCanonicalName());
            if (null != f && !response.isCommitted()) {
                Collection<Article> lEntry = f.getArticles();
                request.setAttribute("curGroup", f.getSection());
                request.setAttribute("title", f.getSection());
                request.setAttribute("articles", lEntry);
                request.setAttribute("articleCategory", f.getSection());
                request.setAttribute("index", true);
                HtmlMeta.addTag(request, "description", f.getDescription());

                // dont bother if there is only 1 page total
                if (f.getCount() > 1) {
                    request.setAttribute("pagen_first", f.getFirst());
                    request.setAttribute("pagen_last", f.getLast());
                    request.setAttribute("pagen_link", f.getLink());
                    request.setAttribute("pagen_current", f.getPage());
                    request.setAttribute("pagen_count", f.getCount());
                } else if (null == f.getSection() && 0 == f.getCount()) {
                    String message = MessageFormat.format(imead.getLocal("page_noposts", Local.resolveLocales(request)), new Object[]{imead.getValue(GuardRepo.CANONICAL_URL) + "adminLogin"});
                    request.setAttribute("ERROR_MESSAGE", message);
                    request.getServletContext().getRequestDispatcher(CoronerServlet.ERROR_JSP).forward(request, response);
                    return;
                }
                request.getServletContext().getRequestDispatcher(HOME_JSP).forward(request, response);
            }
        }
    }
}
