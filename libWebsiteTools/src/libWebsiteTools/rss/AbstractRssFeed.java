package libWebsiteTools.rss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * a complete implementation of an RSS feed (includes DOM)
 *
 * extend this class to quickly create your own feeds, and use @Feed
 *
 * to have a feed automatically add to the feed manager, register this as a
 * servlet listener, like by adding @WebListener to your classes.
 *
 * @author alpha
 * @see libWebsiteTools.rss.Feed
 */
public abstract class AbstractRssFeed implements iFeed, ServletContextListener {

    @EJB
    protected FeedBucket feeds;
    protected final Collection<RssChannel> channels = new ArrayList<>();
    private static final Logger LOG = Logger.getLogger(AbstractRssFeed.class.getName());

    /**
     *
     * @param chan
     */
    public void addChannel(RssChannel chan) {
        channels.add(chan);
    }

    /**
     * builds the entire DOM behind this RSS feed with all built-in channels,
     * controlled by addChannel and removeChannel
     *
     * @return XML document
     */
    public Document refreshFeed() {
        return refreshFeed(channels);
    }

    /**
     * rebuild the XML DOM behind this RSS feed, with the given channel
     *
     * @param channel
     * @return RSS XML output
     */
    public Document refreshFeed(RssChannel channel) {
        return refreshFeed(Arrays.asList(channel));
    }

    /**
     * rebuild the DOM behind this RSS feed, with the given channels
     *
     * @param channels
     * @return RSS XML output
     */
    public Document refreshFeed(Collection<RssChannel> channels) {
        Document XML = null;
        try {
            XML = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = XML.createElement("rss");
            XML.appendChild(root);
            root.setAttribute("version", "2.0");
            for (RssChannel chan : channels) {
                chan.publish(root);
            }
        } catch (ParserConfigurationException | DOMException x) {
            throw new RuntimeException(x.getMessage(), x);
        }
        return XML;
    }

    @Override
    public String getName() {
        return this.getClass().getAnnotation(Feed.class).value();
    }

    @Override
    public long getLastModified() {
        return -1;
    }

    /**
     * @param req
     * @return result of refreshFeed() (the no param override)
     */
    @Override
    public Document preWrite(HttpServletRequest req, HttpServletResponse res) {
        return refreshFeed();
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