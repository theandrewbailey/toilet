package toilet.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.OdysseyFilter;
import libWebsiteTools.imead.Local;

@WebServlet(name = "CoronerServlet", description = "Error page stuff", urlPatterns = {"/coroner", "/coroner/*"})
public class CoronerServlet extends ToiletServlet {

    public static final String ERROR_JSP = "/WEB-INF/Error.jsp";
    public static final String ERROR_IFRAME_JSP = "/WEB-INF/errorIframe.jsp";
    public static final String ERROR_PREFIX = "error_";
    private final String[] vars = {"javax.servlet.error.status_code",
        "javax.servlet.error.exception_type", "javax.servlet.error.message",
        "javax.servlet.error.exception", "javax.servlet.error.request_uri"};

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!request.getRequestURL().toString().equals(request.getAttribute(OdysseyFilter.ORIGINAL_REQUEST_URL))
                && (null == request.getSession(false) || request.getSession().isNew())) { // trying to hack me? F U
            OdysseyFilter.kill(request, response);
            return;
        }

        asyncFiles(request);
        String[] split = request.getRequestURI().split("coroner/");
        if (2 == split.length) {
            setJSPAttrs(request, response, split[1]);
            return;
        }

        Object[] messages = new Object[5];
        for (int x = 0; x < messages.length; x++) {
            messages[x] = request.getAttribute(vars[x]);
        }

        if (null == messages[0]) {
            messages[0] = "501";
        }
        setJSPAttrs(request, response, messages[0].toString());
    }

    private void setJSPAttrs(HttpServletRequest request, HttpServletResponse response, String ErrCodeStr) throws ServletException, IOException {
        asyncRecentCategories(request);
        String errorMessage = imead.getLocal(ERROR_PREFIX + ErrCodeStr, Local.resolveLocales(request));
        if (null == errorMessage) {
            errorMessage = imead.getLocal(ERROR_PREFIX + "404", Local.resolveLocales(request));
        }
        request.setAttribute("title", "ERROR " + ErrCodeStr);
        request.setAttribute("ERROR_MESSAGE", errorMessage);
        try {
            getServletContext().getRequestDispatcher(null == request.getParameter("iframe") ? ERROR_JSP : ERROR_IFRAME_JSP).forward(request, response);
        } catch (IllegalStateException is) {
        }
    }
}
