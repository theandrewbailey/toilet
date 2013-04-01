package toilet.servlet;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.imead.IMEADHolder;

@WebServlet(name = "FrontDoorServlet", description = "Determines if to forward as normal, or redirect the user", urlPatterns = {"/"})
public class FrontDoorServlet extends HttpServlet {

    private static final String REDIRECT = "page_redirect";
    @EJB
    private IMEADHolder imead;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String redir = imead.getValue(REDIRECT);
        if (redir == null || redir.isEmpty()) {
            request.getServletContext().getRequestDispatcher("/index/1").forward(request, response);
        } else {
            response.sendRedirect(redir);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
