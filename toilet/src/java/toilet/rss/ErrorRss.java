package toilet.rss;

import java.util.List;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.db.Exceptionevent;
import libWebsiteTools.rss.RssChannel;
import libWebsiteTools.rss.RssItem;
import libWebsiteTools.rss.SimpleRssFeed;
import libWebsiteTools.rss.iFeed;
import org.w3c.dom.Document;
import toilet.servlet.AdminLoginServlet;

/**
 *
 * @author alpha
 */
@WebListener
public class ErrorRss extends SimpleRssFeed {

    @EJB
    private SecurityRepo exr;
    public static final String NAME = "logger.rss";
    private static final Logger LOG = Logger.getLogger(SecurityRepo.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public iFeed doHead(HttpServletRequest req, HttpServletResponse res) {
        res.setHeader(HttpHeaders.CACHE_CONTROL, "private, must-revalidate, max-age=600");
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
            List<Exceptionevent> exceptions = exr.getAll(null);
            for (Exceptionevent e : exceptions) {
                RssItem ri = new RssItem(e.getDescription());
                ri.setTitle(e.getTitle());
                ri.setPubDate(e.getAtime());
                badRequests.addItem(ri);
            }
            return super.refreshFeed(badRequests);
        }
        LOG.fine("Error RSS feed invalid authentication");
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return null;
    }
}
