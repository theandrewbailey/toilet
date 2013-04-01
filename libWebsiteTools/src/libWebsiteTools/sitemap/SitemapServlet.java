package libWebsiteTools.sitemap;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@WebServlet(name = "Sitemap", description = "Provides a sitemap xml", urlPatterns = {"/sitemap.xml"})
public class SitemapServlet extends HttpServlet {

    @EJB
    private SiteMaster master;
    private final TransformerFactory xFormFact = TransformerFactory.newInstance();
    private static final Logger log = Logger.getLogger(SitemapServlet.class.getName());

    @Override
    public void init() throws ServletException {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DOMSource DOMsrc = new DOMSource(master.getSiteMap());
        response.setContentType("application/xml");
        StreamResult str = new StreamResult(response.getWriter());
        try {
            Transformer xForm;
            synchronized (xFormFact) {
                xForm = xFormFact.newTransformer();
            }
            xForm.transform(DOMsrc, str);
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Sitemap encountered error during request", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
