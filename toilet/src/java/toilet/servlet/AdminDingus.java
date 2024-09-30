package toilet.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import libWebsiteTools.Markdowner;
import libWebsiteTools.tag.AbstractInput;
import toilet.ArticleProcessor;
import toilet.UtilStatic;
import toilet.db.Article;

/**
 *
 * @author alpha
 */
@WebServlet(name = "AdminDingus", description = "Shows a commonmark dingus", urlPatterns = {"/adminDingus"})
public class AdminDingus extends ToiletServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (AdminLoginServlet.DINGUS.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            String markdown = AbstractInput.getParameter(request, "postedmarkdown");
            if (null != markdown) {
                Article art = new Article();
                art.setPostedhtml(Markdowner.getHtml(markdown));
                art.setPostedmarkdown(markdown);
                new ArticleProcessor(allBeans.getInstance(request), art).call();
                request.setAttribute(Article.class.getSimpleName(), art);
                request.setAttribute("rawHtml", UtilStatic.htmlFormat(art.getPostedhtml(), false, false, true));
            }
            request.getRequestDispatcher(AdminLoginServlet.ADMIN_DINGUS).forward(request, response);
        }
    }
}
