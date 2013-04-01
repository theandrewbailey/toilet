package libWebsiteTools.rss;

import java.lang.reflect.InvocationTargetException;
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
 * the feed bucket, the source from which all RSS feeds come
 * 
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class FeedBucket implements iFeedBucket {

    public static final String LOCAL_NAME = "java:module/FeedManager";
    public static final String TIME_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    private final Map<String, iFeed> feeds = Collections.synchronizedMap(new HashMap<String, iFeed>());
    private static final Logger log = Logger.getLogger(FeedBucket.class.getName());

    /**
     * returns an XML factory good to use.
     * @param indent
     * @return 
     * @throws RuntimeException if the transformer cannot be created
     */
    public static Transformer getTransformer(boolean indent) {
        TransformerFactory xFormFact = TransformerFactory.newInstance();
        xFormFact.setAttribute("indent-number", new Integer(4));
        try {
            Transformer trans=xFormFact.newTransformer();
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
        log.log(Level.INFO, "FeedBucket in service");
    }

    @Override
    public void addFeed(String name, iFeed feed) {
        synchronized (feed) {
            feed.preAdd();
            feeds.put(name, feed);
            feed.postAdd();
        }
        log.log(Level.INFO, "Feed added, name:{0} class:{1}", new Object[]{name, feed.getClass().getName()});
    }

    @Override
    public void addFeed(iFeed feed) {
        if (feed.getClass().getAnnotation(Feed.class) == null) {
            // TODO: instead of annotation, use class name
            log.log(Level.SEVERE, "@Feed not found on {0}", feed.getClass().getName());
            throw new NullPointerException("@Feed not found on " + feed.getClass().getName());
        }
        addFeed(feed.getClass().getAnnotation(Feed.class).value(), feed);
    }

    @Override
    public void addFeed(String className) {
        try {
            addFeed((iFeed) Class.forName(className).getConstructors()[0].newInstance());
        } catch (InstantiationException ex) {
            log.log(Level.SEVERE, "Feed " + className + " is not instantiatable", ex);
        } catch (IllegalAccessException ex) {
            log.log(Level.SEVERE, "Feed " + className + " has an inaccessable constructor", ex);
        } catch (IllegalArgumentException ex) {
            log.log(Level.SEVERE, "Feed " + className + " has invalid constructor arguments", ex);
        } catch (InvocationTargetException ex) {
            log.log(Level.SEVERE, "Feed " + className + " constructor not invoked", ex);
        } catch (ClassNotFoundException ex) {
            log.log(Level.SEVERE, "Feed " + className + " was not found", ex);
        } catch (ClassCastException ex) {
            log.log(Level.SEVERE, "Class " + className + " does not implement iFeed", ex);
        }
    }

    @Override
    public iFeed getFeed(String feed) {
        return feeds.get(feed);
    }

    @Override
    public void removeFeed(String name) {
        iFeed feed = getFeed(name);
        if (feed == null) {
            return;
        }
        synchronized (feed) {
            feed.preRemove();
            feeds.remove(name);
            feed.postRemove();
        }
        log.log(Level.INFO, "Feed removed: {0}", name);
    }

    @Override
    public void removeFeed(iFeed feed) {
        removeFeed(feed.getClass().getAnnotation(Feed.class).value());
    }

    @PreDestroy
    @Lock(LockType.WRITE)
    void destroy() {
        Collection<String> set = new ArrayList<String>(feeds.keySet());
        for (String el : set) {
            removeFeed(el);
        }
        log.log(Level.INFO, "FeedBucket destroyed");
    }
}
