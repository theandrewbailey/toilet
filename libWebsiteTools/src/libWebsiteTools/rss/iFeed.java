package libWebsiteTools.rss;

import java.io.Serializable;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.w3c.dom.Document;

/**
 * interface for a RSS Feed.
 *
 * to put feed into service, add to the FeedBucket EJB. feeds are guaranteed to
 * added and destroyed in the order declared.
 *
 * AbstractRssFeed is a mostly complete implementation (includes DOM), and
 * recommend usage of @Feed for your implementations.
 *
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 * @see libWebsiteTools.rss.entity.AbstractRssFeed
 * @see libWebsiteTools.rss.RssServlet
 */
public interface iFeed extends Serializable {

    public enum MimeType {
        RSS("application/rss+xml"), ATOM("application/atom+xml");
        public final String type;

        MimeType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }
    }

    /**
     *
     * @return what this feed should be called, for purposes of URLs
     */
    public String getName();

    /**
     *
     * @return MIME type of this feed
     */
    public MimeType getMimeType();

    /**
     * preAdd will be called before this feed is made available.
     *
     * @return this
     */
    public iFeed preAdd();

    /**
     * postAdd will be called after this feed is made available.
     *
     * @return this
     */
    public iFeed postAdd();

    /**
     * lastModified will return when the feed last changed.
     *
     * @param req
     * @return milliseconds since epoch
     * @see HttpServlet
     */
    public long getLastModified(HttpServletRequest req);

    /**
     * HttpServlet.doHead(HttpServletRequest, HttpServletResponse) calls this.
     * Setting status to something other than 200 is respected.
     *
     * @param req
     * @param res
     * @return this
     */
    public iFeed doHead(HttpServletRequest req, HttpServletResponse res);

    /**
     * preWrite will be called on every request for the feed. it must return the
     * XML to send back to user, or set the HTTP response status to something
     * other than 200.
     *
     * there is no external synchronized block around this call and postWrite.
     * if thread-safety is needed, you must provide it on your own.
     *
     * @param req useful for getting the session object
     * @param res useful for setting headers
     * @return XML document to write to output stream
     */
    public Document preWrite(HttpServletRequest req, HttpServletResponse res);

    /**
     * postWrite is called after preWrite.
     *
     * there is no external synchronized block around this call and postWrite.
     * if thread-safety is needed, you must provide it on your own.
     *
     * @param req useful for getting the session object
     * @return this
     */
    public iFeed postWrite(HttpServletRequest req);

    /**
     * preRemove will be called before the feed is removed from service.
     *
     * @return this
     */
    public iFeed preRemove();

    /**
     * postRemove will be called after the feed is removed from service.
     *
     * @return this
     */
    public iFeed postRemove();
}
