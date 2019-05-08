package libWebsiteTools.imead;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author alpha
 */
public class IMEADEdit extends HttpServlet {

    @EJB
    private IMEADHolder imead;

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHead(request, response);
        Map<Locale, Properties> properties = imead.getProperties();
        List<Locale> unusedLocales = Arrays.asList(Locale.getAvailableLocales());
        unusedLocales.removeAll(properties.keySet());
        request.setAttribute("properties", properties);
        request.setAttribute("unusedLocales", unusedLocales);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
}
