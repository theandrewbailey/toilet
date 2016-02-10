package toilet.servlet;

import java.io.IOException;
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
import libWebsiteTools.tag.HtmlMeta;
import toilet.bean.StateCache;
import toilet.db.Article;

@WebServlet(name = "IndexServlet", description = "Gets all the posts of a single group, defaults to Home", urlPatterns = {"/index", "/index/*"})
public class IndexServlet extends HttpServlet {

    public static final String HOME_JSP = "/WEB-INF/home.jsp";
    private static final Date EPOCH = new Date(0);
    @EJB
    private StateCache cache;

    @Override
    public void init() throws ServletException {
    }

    @Override
    protected long getLastModified(HttpServletRequest request) {
        Date latest = EPOCH;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StateCache.IndexFetcher f = (StateCache.IndexFetcher) request.getAttribute(StateCache.IndexFetcher.class.getCanonicalName());
        f = null == f ? cache.getIndexFetcher(request.getRequestURI()) : f;

        List<Article> lEntry = f.getArticles();
        if (lEntry.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String ifNoneMatch = request.getHeader("If-None-Match");
        MessageDigest md = HashUtil.getSHA256();
        for (String cat : cache.getArticleCategories()) {
            md.update(cat.getBytes());
        }
        for (Article a : lEntry) {
            md.update(a.getEtag().getBytes());
        }
        String etag = "\"" + HashUtil.getBase64(md.digest()) + "\"";
        if (etag.equals(ifNoneMatch)) {
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        response.setHeader("ETag", etag);
        request.setAttribute("curGroup", f.getGroup());
        request.setAttribute("title", f.getGroup());

        // dont bother if there is only 1 page total
        if (f.getCount() > 1) {
            request.setAttribute("pagen_first", f.getFirst());
            request.setAttribute("pagen_last", f.getLast());
            request.setAttribute("pagen_link", f.getLink());
            request.setAttribute("pagen_current", f.getPage());
            request.setAttribute("pagen_count", f.getCount());
        }

        request.setAttribute("index", true);
        request.setAttribute("articles", lEntry);
        request.setAttribute("articleCategory", f.getGroup());
        request.setAttribute("singleArticle", false);
        HtmlMeta.addTag(request, "description", f.getDescription());
        request.getServletContext().getRequestDispatcher(HOME_JSP).forward(request, response);
    }
}
