package toilet.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import toilet.UtilStatic;

/**
 *
 * @author alpha
 */
@WebServlet(name = "WellKnownServlet", description = "Servlet for well known files", urlPatterns = {"/robots.txt", "/favicon.ico"})
public class WellKnownServlet extends ToiletServlet {

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String URL = request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
        switch (URL) {
            case "/robots.txt":
                request.getServletContext().getRequestDispatcher("/content/robots.txt").forward(request, response);
                break;
            case "/favicon.ico":
                UtilStatic.permaMove(response, imead.getValue("page_favicon"));
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                break;
        }
    }
}
