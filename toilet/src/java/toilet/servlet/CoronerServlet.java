package toilet.servlet;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.Guard;
import libWebsiteTools.imead.IMEADHolder;

@WebServlet(name = "CoronerServlet", description = "Error page stuff", urlPatterns = {"/coroner", "/coroner/*"})
public class CoronerServlet extends HttpServlet {

    @EJB
    private IMEADHolder imead;
    public static final String ERROR_JSP = "/WEB-INF/Error.jsp";
    private static final String CORONER_PREFIX="coroner_";
    private String[] vars = {"javax.servlet.error.status_code",
        "javax.servlet.error.exception_type", "javax.servlet.error.message",
        "javax.servlet.error.exception", "javax.servlet.error.request_uri"};

    @Override
    public void init() throws ServletException {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getSession(false) == null || request.getSession().isNew()) { // trying to hack me? F U
            Guard.kill(request, response);
            return;
        }

        String[] split = request.getRequestURI().split("coroner/");
        if (split.length == 2) {
            setJSPAttrs(request, response, split[1]);
            return;
        }

        Object[] messages = new Object[5];
        for (int x = 0; x < messages.length; x++) {
            messages[x] = request.getAttribute(vars[x]);
        }
//        if ((Throwable)messages[3]!=null)
//            error.logError(request, (Throwable)messages[3]);

        if (messages[0] == null) {
            messages[0] = "501";
        }

        if (messages[1] != null) {
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
        if (getError(ErrCodeStr) == null) {
            ErrCodeStr = "404";
        }
        request.setAttribute("title", "ERROR " + ErrCodeStr);
        request.setAttribute("mess", getError(ErrCodeStr));
        getServletContext().getRequestDispatcher(ERROR_JSP).forward(request, response);
    }

    /**
     * @param id predefined error number or other
     * @return (user friendly) error message
     */
    public String getError(String id){
        return imead.getValue(CORONER_PREFIX+id);
    }
}
