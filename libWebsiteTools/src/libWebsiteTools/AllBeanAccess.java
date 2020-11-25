package libWebsiteTools;

import javax.ejb.Local;
import javax.enterprise.concurrent.ManagedExecutorService;
import libWebsiteTools.cache.CachedPage;
import libWebsiteTools.cache.PageCache;
import libWebsiteTools.cache.PageCacheProvider;
import libWebsiteTools.cache.PageCaches;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.security.SecurityRepo;

/**
 * Easy way to ensure static functions have access to requisite bean classes.
 *
 * @author alpha
 */
@Local
public interface AllBeanAccess {

    public ManagedExecutorService getExec();

    public SecurityRepo getError();

    public FileRepo getFile();

    public IMEADHolder getImead();

    public default String getImeadValue(String key) {
        return getImead().getValue(key);
    }

    public FeedBucket getFeeds();

    public PageCacheProvider getPageCacheProvider();

    public default PageCache getGlobalCache() {
        return (PageCache) getPageCacheProvider().getCacheManager().<String, CachedPage>getCache(PageCaches.DEFAULT_URI);
    }

    public void reset();
}
