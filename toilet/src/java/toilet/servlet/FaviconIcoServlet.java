package toilet.servlet;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.imead.IMEADHolder;
import toilet.UtilStatic;

/**
 *
 * @author alpha
 */
@WebServlet(name = "FaviconIcoServlet", description = "returns a favicon, in case the data: uri didn't work", urlPatterns = {"/favicon.ico"})
public class FaviconIcoServlet extends HttpServlet {

    @EJB private IMEADHolder imead;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        UtilStatic.permaMove(resp, imead.getValue("page_favicon"));
    }
}
