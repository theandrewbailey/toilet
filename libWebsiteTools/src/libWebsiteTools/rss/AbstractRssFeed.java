package libWebsiteTools.rss;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;

/**
 * extend this class to easily create your own feeds, and use @Feed
 *
 * to have a feed automatically add to the feed manager, register this as a
 * servlet listener, like by adding @WebListener to your classes.
 *
 * @author alpha
 */
public abstract class AbstractRssFeed implements iFeed, ServletContextListener {

    public static final String COPYRIGHT = "rss_copyright";
    public static final String LANGUAGE = "rss_language";
    public static final String MASTER = "rss_master";
    @EJB
    protected FeedBucket feeds;
    private static final Logger LOG = Logger.getLogger(AbstractRssFeed.class.getName());

    @Override
    public String getName() {
        return this.getClass().getSimpleName() + ".rss";
    }

    /**
     * RSS types are the only ones implemented at this time.
     *
     * @return MimeType.RSS
     */
    @Override
    public MimeType getMimeType() {
        return MimeType.RSS;
    }

    @Override
    public long getLastModified(HttpServletRequest req) {
        return -1;
    }

    @Override
    public Document preWrite(HttpServletRequest req, HttpServletResponse res) {
        return null;
    }

    @Override
    public iFeed postWrite(HttpServletRequest req) {
        return this;
    }

    @Override
    public iFeed preAdd() {
        return this;
    }

    @Override
    public iFeed postAdd() {
        return this;
    }

    @Override
    public iFeed doHead(HttpServletRequest req, HttpServletResponse res) {
        return this;
    }

    @Override
    public iFeed preRemove() {
        return this;
    }

    @Override
    public iFeed postRemove() {
        return this;
    }

    /**
     * automatically adds this feed to the feed manager
     *
     * unless explicitly called, this will only happen when this is a listener
     *
     * @param e ServletContextEvent (can be null)
     */
    @Override
    public void contextInitialized(ServletContextEvent e) {
        try {
            feeds.upsert(Arrays.asList(this));
        } catch (Exception r) {
            LOG.log(Level.WARNING, "Tried to add feed {0} but couldn't", this.getClass().getCanonicalName());
        }
    }

    /**
     * attempts to remove this feed
     *
     * unless explicitly called, this will only happen when this is a listener
     *
     * @param e ServletContextEvent (can be null)
     */
    @Override
    public void contextDestroyed(ServletContextEvent e) {
        try {
            feeds.delete(this.getName());
        } catch (EJBException x) {
        }
    }
}
