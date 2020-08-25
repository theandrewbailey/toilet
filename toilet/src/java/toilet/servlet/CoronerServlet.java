package toilet.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.imead.Local;

@WebServlet(name = "CoronerServlet", description = "Error page stuff", urlPatterns = {"/coroner", "/coroner/*"})
public class CoronerServlet extends ToiletServlet {

    private final String[] vars = {"javax.servlet.error.status_code",
        "javax.servlet.error.exception_type", "javax.servlet.error.message",
        "javax.servlet.error.exception", "javax.servlet.error.request_uri"};

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        /*if (!request.getRequestURL().toString().equals(request.getAttribute(OdysseyFilter.ORIGINAL_REQUEST_URL))
                && (null == request.getSession(false) || request.getSession().isNew())) { // trying to hack me? F U
            OdysseyFilter.kill(request, response);
            return;
        }*/
        String errorCode;
        String[] split = request.getRequestURI().split("coroner/");
        if (2 == split.length) {
            errorCode = split[1];
        } else {
            Object[] messages = new Object[5];
            for (int x = 0; x < messages.length; x++) {
                messages[x] = request.getAttribute(vars[x]);
            }
            errorCode = null != messages[0] ? messages[0].toString() : "501";
        }
        String errorMessage = imead.getLocal(ERROR_PREFIX + errorCode, Local.resolveLocales(request, imead));
        if (null == errorMessage) {
            errorMessage = imead.getLocal(ERROR_PREFIX + "404", Local.resolveLocales(request, imead));
        }
        request.setAttribute("title", "ERROR " + errorCode);
        showError(request, response, errorMessage);
    }
}
