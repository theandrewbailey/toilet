package toilet.bean;

import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import libWebsiteTools.cache.PageCacheProvider;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.security.SecurityRepo;
import toilet.AllBeanAccess;

/**
 * Easy way to ensure static functions have access to requisite bean classes.
 *
 * @author alpha
 */
@Startup
@Singleton
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ToiletBeanAccess implements AllBeanAccess, libWebsiteTools.AllBeanAccess {

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
    @EJB
    private ArticleRepo arts;
    @EJB
    private CommentRepo comms;
    @EJB
    private SectionRepo sects;
    @EJB
    private BackupDaemon backup;

    /**
     * lookup an EJB. avoid using this, because it's not fast.
     * @param <T>
     * @param name
     * @param type
     * @return
     * @deprecated
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name, Class<T> type) {
        try {
            return (T) new InitialContext().lookup(name);
        } catch (NamingException n) {
            throw new RuntimeException("Attempted to look up invalid bean, name:" + name + " type:" + type.getName(), n);
        }
    }

    /**
     * called when big state changes occur that require temporary caches to be
     * emptied (new article, changed configuration, etc).
     */
    @Override
    public synchronized void reset() {
        imead.evict();
        arts.evict();
        sects.evict();
        file.evict();
        getGlobalCache().clear();
    }

    @Override
    public ArticleRepo getArts() {
        return arts;
    }

    @Override
    public CommentRepo getComms() {
        return comms;
    }

    @Override
    public SectionRepo getSects() {
        return sects;
    }

    @Override
    public BackupDaemon getBackup() {
        return backup;
    }

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
}
