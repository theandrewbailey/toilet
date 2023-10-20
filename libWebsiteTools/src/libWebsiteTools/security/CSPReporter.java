package libWebsiteTools.security;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import libWebsiteTools.AllBeanAccess;

/**
 *
 * @author alpha
 */
@WebServlet(name = "CSP Reporter", description = "Receives and logs Content Security Policy reports", urlPatterns = {"/report"})
public class CSPReporter extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        StringBuilder report = new StringBuilder(1000).append("IP: ").append(SecurityRepo.getIP(request)).append(SecurityRepo.NEWLINE);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headers = request.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                String header = headers.nextElement();
                report.append(headerName).append(": ").append(SecurityRepo.htmlFormat(header)).append(SecurityRepo.NEWLINE);
            }
        }
        report.append(SecurityRepo.NEWLINE).append(SecurityRepo.NEWLINE).append("csp-report:").append(SecurityRepo.NEWLINE);
        JsonReader read = Json.createReader(request.getInputStream());
        JsonObject reportObject = read.readObject().getJsonObject("csp-report");
        for (Map.Entry<String, JsonValue> field : reportObject.entrySet()) {
            report.append(SecurityRepo.htmlFormat(field.getKey())).append(": ").append(SecurityRepo.htmlFormat(field.getValue().toString())).append(SecurityRepo.NEWLINE);
        }
        AllBeanAccess beans = (AllBeanAccess) request.getAttribute(AllBeanAccess.class.getCanonicalName());
        beans.getError().logException(null, "Content Security Policy violation", report.toString(), null);
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
