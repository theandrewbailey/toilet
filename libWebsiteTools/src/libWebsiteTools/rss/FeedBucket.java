package libWebsiteTools.rss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * The feed bucket, the source from which all RSS feeds come. Does what the
 * label reads: stores your feeds when you're not using them. nothing too fancy
 * here, doesn't manipulate space-time or anything like that, sorry
 *
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class FeedBucket {

    public static final String LOCAL_NAME = "java:module/FeedManager";
    public static final String TIME_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    private static final Logger LOG = Logger.getLogger(FeedBucket.class.getName());
    private final Map<String, iFeed> feeds = Collections.synchronizedMap(new HashMap<>());

    /**
     * returns an XML factory good to use.
     *
     * @param indent
     * @return
     * @throws RuntimeException if the transformer cannot be created
     */
    public static Transformer getTransformer(boolean indent) {
        TransformerFactory xFormFact = TransformerFactory.newInstance();
        xFormFact.setAttribute("indent-number", 4);
        try {
            Transformer trans = xFormFact.newTransformer();
            if (indent) {
                trans.setOutputProperty(OutputKeys.INDENT, "yes");
            }
            return trans;
        } catch (TransformerConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @PostConstruct
    private void init() {
        LOG.log(Level.INFO, "FeedBucket in service");
    }

    /**
     * adds a feed to be available from the servlet
     *
     * @param name how the feed is accessed in the URL ("peanutButter" will be
     * accessible through /rss/peanutButter)
     * @param feed feed object to add
     */
    public void addFeed(String name, iFeed feed) {
        if (feeds.containsKey(name)) {
            throw new IllegalStateException(String.format("Feed already exists, name: {0} class: {1}", new Object[]{name, feed.getClass().getName()}));
        }
        synchronized (feed) {
            feeds.put(name.toLowerCase(), feed.preAdd());
            feed.postAdd();
        }
        LOG.log(Level.INFO, "Feed added, name: {0} class: {1}", new Object[]{name, feed.getClass().getName()});
    }

    /**
     * uses @Feed to extract the desired URL for the feed, and uses it to call
     * addFeed(String, iFeed)
     *
     * @param feed feed object to add
     * @see libRssServlet.Feed
     */
    public void addFeed(iFeed feed) {
        addFeed(feed.getClass().getAnnotation(Feed.class) != null
                ? feed.getClass().getAnnotation(Feed.class).value()
                : feed.getClass().getSimpleName(), feed);
    }

    /**
     * adds a RssFeed by class name must have @Feed on the class, as
     * addFeed(iFeed) is called
     *
     * @param className fully qualified name of class
     */
    public void addFeed(String className) {
        try {
            addFeed((iFeed) Class.forName(className).newInstance());
        } catch (InstantiationException ex) {
            LOG.log(Level.SEVERE, "Feed " + className + " is not instantiatable", ex);
        } catch (IllegalAccessException ex) {
            LOG.log(Level.SEVERE, "Feed " + className + " has an inaccessable constructor", ex);
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.SEVERE, "Feed " + className + " has invalid constructor arguments", ex);
            throw ex;
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.SEVERE, "Feed " + className + " was not found", ex);
        } catch (IllegalStateException ex) {
            LOG.log(Level.SEVERE, "Feed " + className + " already exists", ex);
            throw ex;
        } catch (ClassCastException ex) {
            LOG.log(Level.SEVERE, "Class " + className + " does not implement iFeed", ex);
            throw ex;
        }
    }

    /**
     * returns a feed object by name
     *
     * @param feed name
     * @return iFeed
     */
    public iFeed getFeed(String feed) {
        return feeds.get(feed.toLowerCase());
    }

    /**
     * graciously removes a feed by name
     *
     * @param name name of feed to remove
     */
    public void removeFeed(String name) {
        if (!feeds.containsKey(name)) {
            throw new IllegalStateException(String.format("Feed doesn't exist, name: {0}", new Object[]{name}));
        }
        iFeed feed = getFeed(name);
        synchronized (feed) {
            feed.preRemove();
            feeds.remove(name).postRemove();
        }
        LOG.log(Level.INFO, "Feed removed: {0}", name);
    }

    /**
     * graciously removes a feed by class uses @Feed to determine which to
     * remove
     *
     * @param feed instance to remove
     */
    public void removeFeed(iFeed feed) {
        removeFeed(feed.getClass().getAnnotation(Feed.class).value());
    }

    @PreDestroy
    @Lock(LockType.WRITE)
    void destroy() {
        Collection<String> set = new ArrayList<>(feeds.keySet());
        for (String el : set) {
            removeFeed(el);
        }
        LOG.log(Level.INFO, "FeedBucket destroyed");
    }
}
