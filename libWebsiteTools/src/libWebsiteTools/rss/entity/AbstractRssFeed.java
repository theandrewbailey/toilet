package libWebsiteTools.rss.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import libWebsiteTools.rss.iFeed;
import libWebsiteTools.rss.iFeedBucket;
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
    protected iFeedBucket feeds;
    protected final Collection<RssChannel> channels = new ArrayList<>();

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
        } catch (Exception x) {
            throw new RuntimeException(x.getMessage(), x);
        }
        return XML;
    }

    /**
     * @param req
     * @return result of refreshFeed() (the no param override)
     */
    @Override
    public Document preWrite(HttpServletRequest req) {
        return refreshFeed();
    }

    @Override
    public void postWrite(HttpServletRequest req) {
    }

    @Override
    public void preAdd() {
    }

    @Override
    public void postAdd() {
    }

    @Override
    public void preRemove() {
    }

    @Override
    public void postRemove() {
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
        feeds.addFeed(this);
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
            feeds.removeFeed(this);
        } catch (EJBException x) {
        }
    }
}
