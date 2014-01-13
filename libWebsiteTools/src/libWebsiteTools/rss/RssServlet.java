package libWebsiteTools.rss;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * praetor_alpha libRssServlet
 *
 * Feel free to use this in your websites, blogs, or anything else you can think
 * of. If you have improvements, please, do tell.
 *
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 */
@WebServlet(name = "RssServlet", description = "RSS host servlet, generates XML DOM for RSS feeds", urlPatterns = {"/rss/*"}, loadOnStartup = 1)
public class RssServlet extends HttpServlet {

    public static final String FEEDS = "RSS.feeds";
    public static final String INDENT = "RSS.indent";
    private static final Logger log = Logger.getLogger(RssServlet.class.getName());
    @EJB
    private iFeedBucket src;
    private final TransformerFactory xFormFact = TransformerFactory.newInstance();

    @Override
    public void init() throws ServletException {
        xFormFact.setAttribute("indent-number", new Integer(4));
        String feeds = getServletContext().getInitParameter(FEEDS);
        if (feeds != null && !feeds.isEmpty()) {
            for (String f : feeds.split(";")) {
                src.addFeed(f);
            }
        }
    }

    /**
     * outputs an RSS XML on the request, the URL determines which feed gets
     * written out /rss/peanutButter writes feed peanutButter to response
     *
     * @param request
     * @param response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String[] uri = request.getRequestURI().split("/rss/", 2);
        if (uri.length != 2) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String name = uri[1];
        iFeed feed = src.getFeed(name);
        Transformer trans;
        if (feed == null) {
            log.log(Level.FINE, "RSS feed {0} not found", name);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            synchronized (feed) {
                log.log(Level.FINE, "RSS feed {0} requested, servicing", name);
                DOMSource DOMsrc = new DOMSource(feed.preWrite(request));
                StreamResult str = new StreamResult(response.getWriter());
                response.setContentType(feed.getClass().getAnnotation(Feed.class).MIME());
                synchronized (xFormFact) {
                    trans = xFormFact.newTransformer();
                }
                if (Boolean.TRUE.toString().equals(getServletContext().getInitParameter(INDENT))) {
                    trans.setOutputProperty(OutputKeys.INDENT, "yes");
                }
                trans.transform(DOMsrc, str);
                feed.postWrite(request);
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error occured while retrieving RSS feed " + name, ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
