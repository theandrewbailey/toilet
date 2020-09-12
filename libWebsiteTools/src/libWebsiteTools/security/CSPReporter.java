package libWebsiteTools.security;

import java.io.IOException;
import java.util.Map;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.imead.IMEADHolder;

/**
 *
 * @author alpha
 */
@WebServlet(name = "CSP Reporter", description = "Receives and logs Content Security Policy reports", urlPatterns = {"/report"})
public class CSPReporter extends HttpServlet {

    @EJB
    protected SecurityRepo error;
    @EJB
    protected IMEADHolder imead;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        StringBuilder report = new StringBuilder(1000);
        JsonReader read = Json.createReader(request.getInputStream());
        JsonObject reportObject = read.readObject().getJsonObject("csp-report");
        for (Map.Entry<String, JsonValue> field : reportObject.entrySet()) {
            report.append(SecurityRepo.htmlFormat(field.getKey())).append(": ").append(SecurityRepo.htmlFormat(field.getValue().toString())).append("<br/>");
        }
        error.logException(null, "Content Security Policy violation", report.toString(), null);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
