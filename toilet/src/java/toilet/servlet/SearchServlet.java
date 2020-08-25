package toilet.servlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.bean.SecurityRepo;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.HtmlMeta;
import toilet.bean.UtilBean;
import toilet.db.Article;
import toilet.tag.ArticleUrl;

@WebServlet(name = "SearchServlet", description = "Searches articles", urlPatterns = {"/search"})
public class SearchServlet extends ToiletServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        String searchTerm = request.getParameter("searchTerm");
        if (null == searchTerm || searchTerm.isEmpty()) {
            response.sendError(422, "Unprocessable Entity");
            return;
        }
        String etag = request.getAttribute(HttpHeaders.ETAG).toString();
        if (etag.equals(ifNoneMatch)) {
            request.setAttribute(Article.class.getCanonicalName(), null);
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        List<Article> results = arts.search(searchTerm);
        try {
            String suggestion = arts.searchSuggestion(searchTerm);
            request.setAttribute("searchSuggestion", suggestion);
            request.setAttribute("searchURL", imead.getValue(SecurityRepo.CANONICAL_URL) + "search?searchTerm=" + URLEncoder.encode(suggestion, "UTF-8"));
        } catch (NullPointerException n) {
        }
        if (results.isEmpty()) {
            showError(request, response, 42);
            return;
        } else if (1 == results.size()) {
            Article art = (Article) results.get(0);
            //String prefix = request.getServletContext().getContextPath();
            //          if (!prefix.endsWith("/")) {
            //            prefix += "/";
            //      }
            String url = ArticleUrl.getUrl("/", art, (Locale) request.getAttribute(Local.OVERRIDE_LOCALE_PARAM), null);
            request.getServletContext().getRequestDispatcher(url).forward(request, response);
            return;
        }
        request.setAttribute("articles", results);
        request.setAttribute("searchterm", searchTerm);
        for (Article art : (List<Article>) results) {
            if (null != art.getImageurl()) {
                HtmlMeta.addPropertyTag(request, "og:image", art.getImageurl());
                break;
            }
        }
        HtmlMeta.addPropertyTag(request, "og:site_name", imead.getLocal(UtilBean.SITE_TITLE, "en"));
        HtmlMeta.addPropertyTag(request, "og:type", "website");
        HtmlMeta.addPropertyTag(request, "og:description", "Search results");
        HtmlMeta.addNameTag(request, "description", "Search results");
        HtmlMeta.addNameTag(request, "robots", "noindex");

        request.getServletContext().getRequestDispatcher(IndexServlet.HOME_JSP).forward(request, response);
    }
}
