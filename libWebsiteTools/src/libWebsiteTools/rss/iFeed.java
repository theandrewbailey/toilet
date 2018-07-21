package libWebsiteTools.rss;

import java.io.Serializable;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;

/**
 * interface for a RSS Feed.
 *
 * to put feed into service, add to the RssFeedSource EJB, -OR-
 *
 * add a context-param element in the web.xml, like so:
 *
 * name: RSS.feeds value: my.rss.feed;my.rss.secondFeed
 *
 * feeds are guaranteed to added and destroyed in the order declared.
 *
 * AbstractRssFeed is a mostly complete implementation (includes DOM), and
 * recommend usage of @Feed for your implementations.
 *
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 * @see libWebsiteTools.rss.entity.AbstractRssFeed
 * @see libWebsiteTools.rss.Feed
 * @see libWebsiteTools.rss.RssServlet
 */
public interface iFeed extends Serializable {

    /**
     * preAdd will be called before this feed is made available.
     */
    public void preAdd();

    /**
     * postAdd will be called after this feed is made available.
     */
    public void postAdd();

    /**
     * lastModified will return when the feed last changed.
     * @see HttpServlet
     * 
     * @return milliseconds since epoch
     */
    public long getLastModified();

    /**
     * HttpServlet.doHead(HttpServletRequest, HttpServletResponse) calls this.
     * Setting status to something other than 200 is respected.
     * 
     * @param req
     * @param res
     */
    public void doHead(HttpServletRequest req, HttpServletResponse res);

    /**
     * preWrite will be called on every request for the feed. it must return the
     * XML to send back to user, or set the HTTP response status to something
     * other than 200.
     * 
     * there is no external syncronized block around this call and postWrite. if
     * thread-safety is needed, you must provide it on your own.
     * 
     * @param req useful for getting the session object
     * @param res useful for setting headers
     * @return XML document to write to output stream
     */
    public Document preWrite(HttpServletRequest req, HttpServletResponse res);

    /**
     * postWrite is called after preWrite.
     * 
     * there is no external syncronized block around this call and postWrite. if
     * thread-safety is needed, you must provide it on your own.
     * 
     * @param req useful for getting the session object
     */
    public void postWrite(HttpServletRequest req);

    /**
     * preRemove will be called before the feed is removed from service.
     */
    public void preRemove();

    /**
     * postRemove will be called after the feed is removed from service.
     */
    public void postRemove();
}
