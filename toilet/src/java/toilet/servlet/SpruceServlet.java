package toilet.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import toilet.bean.SpruceGenerator;

@WebServlet(name = "SpruceServlet", urlPatterns = {"/spruce", "/spruce/*"})
public class SpruceServlet extends ToiletServlet {

    private static final String SPRUCE_JSP = "/WEB-INF/spruce.jsp";
    @EJB
    private SpruceGenerator spruce;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] parts = request.getRequestURI().split("/spruce/", 2);
        if (parts.length == 1 || parts[1] == null || parts[1].isEmpty()) {
            request.getServletContext().getRequestDispatcher(SPRUCE_JSP).forward(request, response);
        } else {
            try {
                short number = Short.parseShort(parts[1]);
                response.setContentType("text/plain");
                PrintWriter pw = response.getWriter();
                for (int x = 0; x < number; x++) {
                    pw.println(spruce.getAddSentence());
                }
                pw.flush();
            } catch (IOException | NumberFormatException x) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }
}
