package libWebsiteTools.rss;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * praetor_alpha libRssServlet
 *
 * Feel free to use this in your websites, blogs, or anything else you can think
 * of. If you have improvements, please, do tell.
 *
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 */
@WebServlet(name = "RssServlet", description = "RSS host servlet, writes XML DOM for RSS feeds", urlPatterns = {"/rss/*"}, loadOnStartup = 1)
public class RssServlet extends HttpServlet {

    public static final String INDENT = "RSS.indent";
    public static final Pattern RSS_NAME_REGEX = Pattern.compile("^.+?/rss/([^\\?]+?)(?:\\?.*)?$");
    private static final Logger LOG = Logger.getLogger(RssServlet.class.getName());
    private final TransformerFactory xFormFact = TransformerFactory.newInstance();
    @EJB
    private FeedBucket src;

    public static String getRssName(String URL) {
        Matcher matcher = RSS_NAME_REGEX.matcher(URL);
        return matcher.find() ? matcher.group(1) : null;
    }

    public iFeed getFeed(HttpServletRequest req) {
        iFeed feed = (iFeed) req.getAttribute(iFeed.class.getCanonicalName());
        if (null != feed) {
            return feed;
        }
        String name = getRssName(req.getRequestURL().toString());
        feed = src.get(name);
        if (feed == null) {
            LOG.log(Level.FINE, "RSS feed {0} not found", name);
            return null;
        }
        req.setAttribute(iFeed.class.getCanonicalName(), feed);
        return feed;
    }

    @Override
    protected long getLastModified(HttpServletRequest request) {
        iFeed feed = getFeed(request);
        if (feed == null) {
            LOG.log(Level.FINE, "RSS feed {0} not found", getRssName(request.getRequestURI()));
            return -1;
        }
        return feed.getLastModified();
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        iFeed feed = getFeed(request);
        if (feed == null) {
            LOG.log(Level.FINE, "RSS feed {0} not found", getRssName(request.getRequestURI()));
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        feed.doHead(request, response);
        if (HttpServletResponse.SC_OK != response.getStatus()) {
            response.sendError(response.getStatus());
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
        try {
            doHead(request, response);
            iFeed feed = getFeed(request);
            LOG.log(Level.FINE, "RSS feed {0} requested, servicing", getRssName(request.getRequestURI()));
            Document XML = feed.preWrite(request, response);
            if (HttpServletResponse.SC_OK != response.getStatus()) {
                response.sendError(response.getStatus());
                return;
            }
            DOMSource DOMsrc = new DOMSource(XML);
            StreamResult str = new StreamResult(response.getWriter());
            response.setContentType(feed.getClass().getAnnotation(Feed.class) != null
                    ? feed.getClass().getAnnotation(Feed.class).MIME()
                    : Feed.MIME_RSS);
            Transformer trans;
            synchronized (xFormFact) {
                trans = xFormFact.newTransformer();
            }
            trans.transform(DOMsrc, str);
            feed.postWrite(request);
        } catch (IOException | IllegalArgumentException | TransformerException ex) {
            LOG.log(Level.SEVERE, "Error occured while retrieving RSS feed " + getRssName(request.getRequestURI()), ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

}
