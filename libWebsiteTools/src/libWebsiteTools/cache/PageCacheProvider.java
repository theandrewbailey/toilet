package libWebsiteTools.cache;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.cache.CacheManager;
import javax.cache.configuration.OptionalFeature;
import javax.cache.spi.CachingProvider;

/**
 * TODO: use JSR-107 via META-INF.services/javax.cache.spi.CachingProvider
 * @author alpha
 */
public class PageCacheProvider implements CachingProvider {

    public static final String DEFAULT_URI = "http://theandrewbailey.com/libWebsiteTools/cache/PageCacheProvider";
    private final Map<URI, CacheManager> caches = Collections.synchronizedMap(new HashMap<>());

    public PageCacheProvider() {
    }

    /**
     * Checks all caches for expired pages.
     */
    public void sweep() {
        for (CacheManager m : caches.values()) {
            try {
                m.unwrap(PageCaches.class).sweep();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader cl, Properties prprts) {
        if (!caches.containsKey(uri)) {
            CacheManager out = new PageCaches(this, uri, prprts);
            caches.put(uri, out);
            return out;
        }
        return caches.get(uri);
    }

    @Override
    public CacheManager getCacheManager(URI uri, ClassLoader cl) {
        return getCacheManager(uri, cl, getDefaultProperties());
    }

    @Override
    public CacheManager getCacheManager() {
        try {
            return getCacheManager(new URI(DEFAULT_URI), getDefaultClassLoader(), getDefaultProperties());
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public ClassLoader getDefaultClassLoader() {
        return PageCacheProvider.class.getClassLoader();
    }

    @Override
    public URI getDefaultURI() {
        try {
            return new URI(DEFAULT_URI);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public synchronized void close() {
        for (CacheManager c : caches.values()) {
            c.close();
        }
        caches.clear();
    }

    @Override
    public synchronized void close(ClassLoader cl) {
        for (CacheManager c : caches.values()) {
            c.close();
        }
        caches.clear();
    }

    @Override
    public void close(URI uri, ClassLoader cl) {
        CacheManager cm = caches.remove(uri);
        cm.close();
    }

    @Override
    public boolean isSupported(OptionalFeature of) {
        return false;
    }

}
