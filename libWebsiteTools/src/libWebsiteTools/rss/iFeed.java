package libWebsiteTools.rss;

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import org.w3c.dom.Document;

/**
 * interface for a RSS Feed
 *
 * to put feed into service, add to the RssFeedSource EJB, -OR-
 *
 * add a context-param element in the web.xml, like so:
 *
 * name: RSS.feeds
 * value: my.rss.feed;my.rss.secondFeed
 *
 * feeds are guaranteed to added and destroyed in the order declared
 *
 * AbstractRssFeed is a complete implementation (includes DOM), and recommend
 * usage of @Feed for your implementations
 *
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 * @see libWebsiteTools.rss.entity.AbstractRssFeed
 * @see libWebsiteTools.rss.Feed
 * @see libWebsiteTools.rss.RssServlet
 */
public interface iFeed extends Serializable {

    /**
     * preAdd will be called before this feed is made available
     */
    public void preAdd();

    /**
     * postAdd will be called after this feed is made available
     */
    public void postAdd();

    /**
     * preWrite will be called on every request for the feed
     * must return the XML to be sent back to user
     * @param req useful for getting the session object
     * @return XML document to preWrite to output stream
     */
    public Document preWrite(HttpServletRequest req);

    /**
     * postWrite will be called after writeToServlet
     * @param req useful for getting the session object
     */
    public void postWrite(HttpServletRequest req);

    /**
     * preRemove will be called before the feed is removed from service
     */
    public void preRemove();

    /**
     * postRemove will be called after the feed is removed from service
     */
    public void postRemove();
}
