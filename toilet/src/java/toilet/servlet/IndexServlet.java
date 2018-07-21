package toilet.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.HashUtil;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.HtmlMeta;
import toilet.UtilStatic;
import toilet.bean.StateCache;
import toilet.db.Article;

@WebServlet(name = "IndexServlet", description = "Gets all the posts of a single group, defaults to Home", urlPatterns = {"/index", "/index/*"})
public class IndexServlet extends HttpServlet {

    public static final String HOME_JSP = "/WEB-INF/category.jsp";
    @EJB
    private StateCache cache;
    @EJB
    private IMEADHolder imead;
    @EJB
    private FileRepo file;

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
        for (String css : imead.getLocal("page_css", Local.resolveLocales(request)).split("\n")) {
            try {
                Fileupload fu = file.getFileMetadata(FileRepo.getFilename(css));
                if (fu.getAtime().after(latest)) {
                    latest = fu.getAtime();
                }
            } catch (Exception e) {
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

        List<Article> lEntry = f.getArticles();
        if (lEntry.isEmpty()) {
            request.setAttribute(StateCache.IndexFetcher.class.getCanonicalName(), null);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String etag = cache.getEtag(request.getRequestURI());
        if (null == etag) {
            try {
                MessageDigest md = HashUtil.getSHA256();
                for (String cat : cache.getArticleCategories()) {
                    md.update(cat.getBytes("UTF-8"));
                }
                for (Article a : lEntry) {
                    md.update(a.getEtag().getBytes("UTF-8"));
                }
                for (String css : imead.getLocal("page_css", Local.resolveLocales(request)).split("\n")) {
                    try {
                        Fileupload fu = file.getFileMetadata(FileRepo.getFilename(css));
                        md.update(fu.getEtag().getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                    }
                }
                etag = "\"" + HashUtil.getBase64(md.digest()) + "\"";
                cache.setEtag(request.getRequestURI(), etag);
            } catch (UnsupportedEncodingException enc) {
                throw new JVMNotSupportedError(enc);
            }
        }
        response.setHeader("ETag", etag);
        if (etag.equals(request.getHeader("If-None-Match"))) {
            request.setAttribute(StateCache.IndexFetcher.class.getCanonicalName(), null);
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        request.setAttribute(StateCache.IndexFetcher.class.getCanonicalName(), f);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHead(request, response);
        StateCache.IndexFetcher f = (StateCache.IndexFetcher) request.getAttribute(StateCache.IndexFetcher.class.getCanonicalName());
        if (null != f) {
            List<Article> lEntry = f.getArticles();
            request.setAttribute("curGroup", f.getGroup());
            request.setAttribute("title", f.getGroup());
            request.setAttribute("articles", lEntry);
            request.setAttribute("articleCategory", f.getGroup());
            request.setAttribute("index", true);

            // dont bother if there is only 1 page total
            if (f.getCount() > 1) {
                request.setAttribute("pagen_first", f.getFirst());
                request.setAttribute("pagen_last", f.getLast());
                request.setAttribute("pagen_link", f.getLink());
                request.setAttribute("pagen_current", f.getPage());
                request.setAttribute("pagen_count", f.getCount());
            }

            HtmlMeta.addTag(request, "description", f.getDescription());
            request.getServletContext().getRequestDispatcher(HOME_JSP).forward(request, response);
        }
    }
}
