package toilet.servlet;

import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.tag.HtmlMeta;
import toilet.bean.EntryRepo;

@WebServlet(name = "SearchServlet", description = "Searches articles", urlPatterns = {"/search"})
public class SearchServlet extends HttpServlet {

    @EJB
    private EntryRepo entry;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchTerm = request.getParameter("searchTerm");
        if (null == searchTerm || searchTerm.isEmpty()) {
            request.getServletContext().getRequestDispatcher("/coroner/41").forward(request, response);
            return;
        }
        List results = entry.search(searchTerm);
        if (results.isEmpty()) {
            request.getServletContext().getRequestDispatcher("/coroner/42").forward(request, response);
            return;
        }
        request.setAttribute("articles", results);
        request.setAttribute("searchterm", searchTerm);
        HtmlMeta.addTag(request, "robots", "noindex");
        request.getServletContext().getRequestDispatcher(IndexServlet.HOME_JSP).forward(request, response);
    }
}
