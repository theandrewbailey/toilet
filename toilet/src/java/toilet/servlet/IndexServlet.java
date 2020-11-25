package toilet.servlet;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.HtmlMeta;
import toilet.FirstTimeDetector;
import toilet.IndexFetcher;
import toilet.db.Article;

@WebServlet(name = "IndexServlet", description = "Gets all the posts of a single group, defaults to Home", urlPatterns = {"/", "/index", "/index/*"})
public class IndexServlet extends ToiletServlet {

    public static final String HOME_JSP = "/WEB-INF/category.jsp";

    private IndexFetcher getIndexFetcher(HttpServletRequest req) {
        String URI = req.getRequestURI();
        if (URI.startsWith(getServletContext().getContextPath())) {
            URI = URI.substring(getServletContext().getContextPath().length());
        }
        return new IndexFetcher(beans, URI);
    }

    @Override
    protected long getLastModified(HttpServletRequest request) {
        Date latest = new Date(0);
        IndexFetcher f = getIndexFetcher(request);
        request.setAttribute(IndexFetcher.class.getCanonicalName(), f);
        for (Article a : f.getArticles()) {
            if (a.getModified().after(latest)) {
                latest = a.getModified();
            }
        }
        return latest.getTime();
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        IndexFetcher f = (IndexFetcher) request.getAttribute(IndexFetcher.class.getCanonicalName());
        if (null == f) {
            f = getIndexFetcher(request);
            request.setAttribute(IndexFetcher.class.getCanonicalName(), f);
        }
        Collection<Article> articles = f.getArticles();
        if (articles.isEmpty()) {
            if (HttpMethod.HEAD.equals(request.getMethod())) {
                request.setAttribute(IndexFetcher.class.getCanonicalName(), null);
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            return;
        }
        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        String etag = request.getAttribute(HttpHeaders.ETAG).toString();
        response.setHeader(HttpHeaders.ETAG, etag);
        if (etag.equals(ifNoneMatch)) {
            request.setAttribute(IndexFetcher.class.getCanonicalName(), null);
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (FirstTimeDetector.isFirstTime(beans)) {
            request.getRequestDispatcher("adminImead").forward(request, response);
            return;
        }
        doHead(request, response);
        IndexFetcher f = (IndexFetcher) request.getAttribute(IndexFetcher.class.getCanonicalName());
        if (null != f && !response.isCommitted()) {
            Collection<Article> articles = f.getArticles();
            // dont bother if there is only 1 page total
            if (f.getCount() > 1) {
                request.setAttribute("pagen_first", f.getFirst());
                request.setAttribute("pagen_last", f.getLast());
                request.setAttribute("pagen_current", f.getPage());
                request.setAttribute("pagen_count", f.getCount());
            } else if (null == f.getSection() && 0 == beans.getArts().count()) {
                String message = MessageFormat.format(beans.getImead().getLocal("page_noPosts", Local.resolveLocales(beans.getImead(), request)), new Object[]{request.getAttribute(SecurityRepo.BASE_URL).toString() + "adminLogin"});
                request.setAttribute(CoronerServlet.ERROR_MESSAGE_PARAM, message);
                request.getServletContext().getRequestDispatcher(CoronerServlet.ERROR_JSP).forward(request, response);
                return;
            } else if (HttpServletResponse.SC_NOT_FOUND == response.getStatus()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            request.setAttribute("curGroup", f.getSection());
            request.setAttribute("title", f.getSection());
            request.setAttribute("articles", articles);
            request.setAttribute("articleCategory", f.getSection());
            request.setAttribute("index", true);
            for (Article art : f.getArticles()) {
                if (null != art.getImageurl()) {
                    HtmlMeta.addPropertyTag(request, "og:image", art.getImageurl());
                    break;
                }
            }
            HtmlMeta.addPropertyTag(request, "og:description", f.getDescription());
            HtmlMeta.addPropertyTag(request, "og:site_name", beans.getImead().getLocal(ToiletServlet.SITE_TITLE, "en"));
            HtmlMeta.addPropertyTag(request, "og:type", "website");
            HtmlMeta.addNameTag(request, "description", f.getDescription());
            request.getServletContext().getRequestDispatcher(HOME_JSP).forward(request, response);
        }
    }
}
