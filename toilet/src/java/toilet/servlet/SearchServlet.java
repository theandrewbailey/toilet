package toilet.servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.tag.HtmlMeta;
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
        asyncFiles(request);
        String searchTerm = request.getParameter("searchTerm");
        if (null == searchTerm || searchTerm.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        asyncRecentCategories(request);
        List results = entry.search(searchTerm);
        if (results.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (1 == results.size()) {
            request.getServletContext().getRequestDispatcher(ArticleUrl.getUrl("/", (Article) results.get(0))).forward(request, response);
            return;
        }
        request.setAttribute("articles", results);
        request.setAttribute("searchterm", searchTerm);
        HtmlMeta.addTag(request, "robots", "noindex");
        //response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=100000, immutable");
        request.getServletContext().getRequestDispatcher(IndexServlet.HOME_JSP).forward(request, response);
    }
}
