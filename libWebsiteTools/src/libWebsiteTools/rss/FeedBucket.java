package libWebsiteTools.rss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import libWebsiteTools.db.Repository;

/**
 * The feed bucket, the source from which all RSS feeds come. Does what the
 * label reads: stores your feeds when you're not using them. nothing too fancy
 * here, doesn't manipulate space-time or anything like that, sorry
 *
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 */
@Singleton
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class FeedBucket implements Repository<iFeed> {

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
     * uses @Feed to extract the desired URL for the feed, and uses it to call
     * addFeed(String, iFeed)
     *
     * @param entities feed objects to add
     * @see libRssServlet.Feed
     */
    @Override
    public List<iFeed> upsert(Collection<iFeed> entities) {
        for (iFeed feed : entities) {
            if (feeds.containsKey(feed.getName())) {
                continue;
            }
            synchronized (feed) {
                feed = feed.preAdd();
                if (null == feed) {
                    throw new NullPointerException("A feed can't be added, nor served.");
                }
                feeds.put(feed.getName(), feed);
                feed.postAdd();
            }
            LOG.log(Level.INFO, "Feed added, name: {0} class: {1}", new Object[]{feed.getName(), feed.getClass().getName()});
        }
        return new ArrayList<>(entities);
    }

    /**
     * returns a feed object by name
     *
     * @param feed name
     * @return iFeed
     */
    @Override
    public iFeed get(Object name) {
        return feeds.get(name.toString());
    }

    @Override
    public List<iFeed> getAll(Integer limit) {
        return new ArrayList<>(feeds.values());
    }

    @Override
    public void processArchive(Consumer<iFeed> operation, Boolean transaction) {
        feeds.values().stream().forEachOrdered(operation);
    }

    @Override
    public void evict() {
    }

    @Override
    public Long count() {
        return new Long(feeds.size());
    }

    /**
     * graciously removes a feed by name
     *
     * @param name name of feed to remove
     */
    @Override
    public iFeed delete(Object name) {
        if (!feeds.containsKey(name.toString())) {
            throw new IllegalStateException(String.format("Feed doesn't exist, name: %1s", name));
        }
        iFeed feed = get(name);
        synchronized (feed) {
            feed.preRemove();
            feeds.remove(name.toString()).postRemove();
        }
        LOG.log(Level.INFO, "Feed removed: {0}", name);
        return feed;
    }

    @PreDestroy
    @Lock(LockType.WRITE)
    void destroy() {
        for (String el : new ArrayList<>(feeds.keySet())) {
            try {
                delete(el);
            } catch (Exception x) {
            }
        }
        LOG.log(Level.INFO, "FeedBucket destroyed");
    }
}
