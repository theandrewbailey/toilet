package libWebsiteTools.cache;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

/**
 *
 * @author alpha
 */
public final class PageCaches implements CacheManager {

    public static final String DEFAULT_URI = "http://theandrewbailey.com/libWebsiteTools/cache/PageCaches";
    private boolean closed = false;
    private final URI uri;
    private final Properties props;
    private final HashMap<String, PageCache> caches = new HashMap<>();
    private final PageCacheProvider provider;

    public PageCaches(PageCacheProvider provider, URI uri, Properties props) {
        this.provider = provider;
        this.uri = uri;
        this.props = props;
        createCache(DEFAULT_URI, new MutableConfiguration<String, CachedPage>().setTypes(String.class, CachedPage.class));
    }

    /**
     * Checks all caches for expired pages.
     */
    public void sweep() {
        OffsetDateTime now = OffsetDateTime.now();
        for (PageCache cache : caches.values()) {
            for (String entry : new ArrayList<>(cache.getAll(null).keySet())) {
                CachedPage page = cache.get(entry);
                // hasn't been used more than once per hour? drop it
                Duration d = Duration.between(page.getCreated(), now).abs();
                if (null != page
                        && Math.max(Double.valueOf(page.getHits()), 1.0) / d.toHours() < 1.0) {
                    cache.remove(entry);
                }
            }
        }
    }

    @Override
    public CachingProvider getCachingProvider() {
        return provider;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public ClassLoader getClassLoader() {
        return PageCaches.class.getClassLoader();
    }

    @Override
    public Properties getProperties() {
        return props;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(String name, C c) throws IllegalArgumentException {
        if (!String.class.equals(c.getKeyType()) || !CachedPage.class.equals(c.getValueType())) {
            throw new IllegalArgumentException("This cache only supports <String,libWebsiteTools.cache.CachedPage>.");
        }
        PageCache output = new PageCache(this, name, DEFAULT_URI.equals(name) ? 100 : 100);
        caches.put(name, output);
        return (Cache<K, V>) output;
    }

    @Override
    public <K, V> Cache<K, V> getCache(String name, Class<K> k, Class<V> v) {
        throw new UnsupportedOperationException("This cache only supports <String,libWebsiteTools.cache.CachedPage>.");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> Cache<K, V> getCache(String name) {
        return (Cache<K, V>) caches.get(name);
    }

    @Override
    public Iterable<String> getCacheNames() {
        return new ArrayList<>(caches.keySet());
    }

    @Override
    public void destroyCache(String name) {
        try (PageCache cache = caches.remove(name)) {
            cache.clear();
        }
    }

    @Override
    public void enableManagement(String name, boolean bln) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void enableStatistics(String name, boolean bln) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void close() {
        for (String name : getCacheNames()) {
            destroyCache(name);
        }
        closed = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public <T> T unwrap(Class<T> type) {
        try {
            return type.cast(this);
        } catch (ClassCastException c) {
            throw new IllegalArgumentException(c);
        }
    }

}
