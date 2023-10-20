package toilet.servlet;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import toilet.bean.ToiletBeanAccess;

/**
 *
 * @author alpha
 */
@WebServlet(name = "WellKnownServlet", description = "Servlet for well known files", urlPatterns = {"/robots.txt", "/favicon.ico", "/browserconfig.xml", "/.well-known/*"})
public class WellKnownServlet extends ToiletServlet {

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String URL = request.getRequestURI().replaceFirst(request.getServletContext().getContextPath(), "");
        ToiletBeanAccess beans = allBeans.getInstance(request);
        switch (URL) {
            case "/favicon.ico":
                ToiletServlet.permaMove(response, beans.getImeadValue("site_favicon"));
                break;
            default:
                if (URL.contains("/.well-known/")) {
                    request.getServletContext().getRequestDispatcher(URL.replaceFirst("/.well-known/", "/content/")).forward(request, response);
                } else if (null != beans.getFile().get(URL.substring(1))) {
                    String next = "/file" + URL;
                    request.getServletContext().getRequestDispatcher(next).forward(request, response);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
                break;
        }
    }
}
