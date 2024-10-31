package libWebsiteTools;

import jakarta.ejb.Local;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.servlet.http.HttpServletRequest;
import libWebsiteTools.turbo.CachedPage;
import libWebsiteTools.turbo.PageCache;
import libWebsiteTools.turbo.PageCacheProvider;
import libWebsiteTools.turbo.PageCaches;
import libWebsiteTools.file.FileRepository;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.sitemap.SiteMapper;
import libWebsiteTools.turbo.PerfStats;

/**
 * Easy way to ensure static functions have access to requisite bean classes.
 *
 * @author alpha
 */
@Local
public interface AllBeanAccess {

    public AllBeanAccess getInstance(HttpServletRequest req);

    public ManagedExecutorService getExec();

    public SecurityRepo getError();

    public FileRepository getFile();

    public IMEADHolder getImead();

    public default String getImeadValue(String key) {
        return getImead().getValue(key);
    }

    public FeedBucket getFeeds();

    public SiteMapper getMapper();

    public PageCacheProvider getPageCacheProvider();

    public default PageCache getGlobalCache() {
        return (PageCache) getPageCacheProvider().getCacheManager().<String, CachedPage>getCache(PageCaches.DEFAULT_URI);
    }

    public PerfStats getPerfStats();

    public void reset();
}
