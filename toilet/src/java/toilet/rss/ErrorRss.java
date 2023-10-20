package toilet.rss;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.db.Exceptionevent;
import libWebsiteTools.rss.RssChannel;
import libWebsiteTools.rss.RssItem;
import org.w3c.dom.Document;
import toilet.bean.ToiletBeanAccess;
import toilet.servlet.AdminLoginServlet;
import libWebsiteTools.rss.Feed;

/**
 *
 * @author alpha
 */
public class ErrorRss implements Feed {

    public static final String NAME = "logger.rss";
    private static final Logger LOG = Logger.getLogger(SecurityRepo.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Feed doHead(HttpServletRequest req, HttpServletResponse res) {
        res.setHeader(HttpHeaders.CACHE_CONTROL, "private, no-store");
        if (null != req.getSession(false) && AdminLoginServlet.ERROR_LOG.equals(req.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            return this;
        }
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return this;
    }

    @Override
    public Document preWrite(HttpServletRequest req, HttpServletResponse res) {
        if (null != req.getSession(false) && AdminLoginServlet.ERROR_LOG.equals(req.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            LOG.fine("Exception RSS feed requested");
            RssChannel badRequests = new RssChannel("running log", req.getRequestURL().toString(), "404s, etc.");
            badRequests.setLimit(1000);
            ToiletBeanAccess beans = (ToiletBeanAccess) req.getAttribute(libWebsiteTools.AllBeanAccess.class.getCanonicalName());
            List<Exceptionevent> exceptions = beans.getError().getAll(null);
            for (Exceptionevent e : exceptions) {
                RssItem ri = new RssItem(e.getDescription());
                ri.setTitle(e.getTitle());
                ri.setPubDate(e.getAtime());
                badRequests.addItem(ri);
            }
            return Feed.refreshFeed(Arrays.asList(badRequests));
        }
        LOG.fine("Error RSS feed invalid authentication");
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return null;
    }
}
