package toilet.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author alpha
 */
@WebServlet(name = "RobotsTxtServlet", description = "returns the robots.txt file", urlPatterns = {"/robots.txt"})
public class RobotsTxtServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getServletContext().getRequestDispatcher("/content/robots.txt").forward(req, resp);
    }
}
