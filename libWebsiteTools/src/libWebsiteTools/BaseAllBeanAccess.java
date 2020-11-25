package libWebsiteTools;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
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
public class BaseAllBeanAccess implements AllBeanAccess {

    @Resource
    private ManagedExecutorService exec;
    @EJB
    private SecurityRepo error;
    @EJB
    private FileRepo file;
    @EJB
    private IMEADHolder imead;
    @EJB
    private FeedBucket feeds;
    @Inject
    private PageCacheProvider pageCacheProvider;

    @Override
    public ManagedExecutorService getExec() {
        return exec;
    }

    @Override
    public SecurityRepo getError() {
        return error;
    }

    @Override
    public FileRepo getFile() {
        return file;
    }

    @Override
    public IMEADHolder getImead() {
        return imead;
    }

    @Override
    public FeedBucket getFeeds() {
        return feeds;
    }

    @Override
    public PageCacheProvider getPageCacheProvider() {
        return pageCacheProvider;
    }
    /**
     * called when big state changes occur that require temporary caches to be
     * emptied (new article, changed configuration, etc).
     */
    @Override
    public synchronized void reset() {
        imead.evict();
        file.evict();
        ((PageCache) pageCacheProvider.getCacheManager().<String, CachedPage>getCache(PageCaches.DEFAULT_URI)).clear();
    }

}
