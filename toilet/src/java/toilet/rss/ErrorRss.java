package toilet.rss;

import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import libOdyssey.bean.ExceptionRepo;
import libOdyssey.db.Exceptionevent;
import libWebsiteTools.rss.Feed;
import libWebsiteTools.rss.entity.AbstractRssFeed;
import libWebsiteTools.rss.entity.RssChannel;
import libWebsiteTools.rss.entity.RssItem;
import libWebsiteTools.rss.iFeedBucket;
import org.w3c.dom.Document;
import toilet.servlet.AdminServlet;

/**
 *
 * @author alpha
 */
@Feed(ErrorRss.NAME)
@WebListener
public class ErrorRss extends AbstractRssFeed {

    @EJB
    private iFeedBucket src;
    @EJB
    private ExceptionRepo exr;
    public static final String NAME = "logger.rss";
    private static final Logger log = Logger.getLogger(ExceptionRepo.class.getName());

    @PostConstruct
    public void init() {
        src.removeFeed(this);
        src.addFeed(this);
    }

    @Override
    public Document preWrite(HttpServletRequest req) {
        if (AdminServlet.LOG.equals(req.getSession().getAttribute("login"))) {
            log.fine("Exception RSS feed requested");
            log.fine("Generating error RSS feed");
            RssChannel badRequests = new RssChannel("running log", req.getRequestURL().toString(), "404s, etc.");
            badRequests.setLimit(1000);
            List<Exceptionevent> exceptions = exr.getAll();
            for (Exceptionevent e : exceptions) {
                RssItem ri = new RssItem(e.getDescription());
                ri.setTitle(e.getTitle());
                ri.setPubDate(e.getAtime());
                badRequests.addItem(ri);
            }
            return super.refreshFeed(badRequests);
        }
        log.fine("Error RSS feed invalid authentication");
        throw new RuntimeException();
    }
}
