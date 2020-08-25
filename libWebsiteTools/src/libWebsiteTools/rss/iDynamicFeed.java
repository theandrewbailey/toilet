package libWebsiteTools.rss;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * this is meant for feeds that can serve multiple URLs, potentially depending
 * on the request or session. this interface is necessary for feeds to be
 * automatically linked to pages (see getFeedURLs(req)).
 *
 * @author alpha
 */
public interface iDynamicFeed extends iFeed {

    /**
     * this method is intended to be called on each request to list RSS feeds on
     * each page.
     *
     * @param req HttpServletRequest
     * @return mapping of URL to feed title
     */
    public Map<String, String> getFeedURLs(HttpServletRequest req);

    /**
     * determines if this feed instance can handle the given name. this will
     * only be called if no instance can be found using getName()
     *
     * @param name
     * @return can this feed handle the given request?
     */
    public boolean willHandle(String name);
}
