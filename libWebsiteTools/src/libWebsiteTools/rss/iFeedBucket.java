package libWebsiteTools.rss;

/**
 * does what it says: interface to store feeds when you're not using them
 * nothing too fancy here, doesn't manipulate space-time or anything like that,
 * sorry
 *
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 * @see libWebsiteTools.rss.FeedBucket
 */
public interface iFeedBucket {

    /**
     * adds a feed to be available from the servlet
     *
     * @param name how the feed is accessed in the URL ("peanutButter" will be
     * accessible through /rss/peanutButter)
     * @param feed feed object to add
     */
    public void addFeed(String name, iFeed feed);

    /**
     * uses @Feed to extract the desired URL for the feed, and uses it to call
     * addFeed(String, iFeed)
     *
     * @param feed feed object to add
     * @see libRssServlet.Feed
     */
    public void addFeed(iFeed feed);

    /**
     * adds a RssFeed by class name must have @Feed on the class, as
     * addFeed(iFeed) is called
     *
     * @param className fully qualified name of class
     */
    public void addFeed(String className);

    /**
     * returns a feed object by name
     *
     * @param feed name
     * @return iFeed
     */
    public iFeed getFeed(String feed);

    /**
     * graciously removes a feed by name
     *
     * @param name name of feed to remove
     */
    public void removeFeed(String name);

    /**
     * graciously removes a feed by class uses @Feed to determine which to
     * remove
     *
     * @param feed instance to remove
     */
    public void removeFeed(iFeed feed);
}
