package toilet.servlet;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.OdysseyFilter;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;

@WebServlet(name = "CoronerServlet", description = "Error page stuff", urlPatterns = {"/coroner", "/coroner/*"})
public class CoronerServlet extends HttpServlet {

    @EJB
    private IMEADHolder imead;
    public static final String ERROR_JSP = "/WEB-INF/Error.jsp";
    public static final String CORONER_PREFIX = "coroner_";
    private final String[] vars = {"javax.servlet.error.status_code",
        "javax.servlet.error.exception_type", "javax.servlet.error.message",
        "javax.servlet.error.exception", "javax.servlet.error.request_uri"};

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (null == request.getSession(false) || request.getSession().isNew()) { // trying to hack me? F U
            OdysseyFilter.kill(request, response);
            return;
        }

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

        if (null != messages[1]) {
            String ex = messages[1].toString();
            if (ex.contains("DatabaseException")) {
                messages[0] = "13";
            } else if (ex.contains("ClassCastException")) {
                messages[0] = "45";
            }
        }
        setJSPAttrs(request, response, messages[0].toString());
    }

    private void setJSPAttrs(HttpServletRequest request, HttpServletResponse response, String ErrCodeStr) throws ServletException, IOException {
        String errorMessage = imead.getLocal(CORONER_PREFIX + ErrCodeStr, Local.resolveLocales(request));
        if (null == errorMessage) {
            errorMessage = imead.getLocal(CORONER_PREFIX + "404", Local.resolveLocales(request));
        }
        request.setAttribute("title", "ERROR " + ErrCodeStr);
        request.setAttribute("mess", errorMessage);
        getServletContext().getRequestDispatcher(ERROR_JSP).forward(request, response);
    }
}
