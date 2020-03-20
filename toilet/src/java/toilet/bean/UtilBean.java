package toilet.bean;

import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import libWebsiteTools.bean.GuardRepo;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.FeedBucket;
import toilet.rss.ArticleRss;
import toilet.rss.CommentRss;

/**
 *
 * @author alpha
 */
@Stateless
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class UtilBean {

    public static final String LOCAL_NAME = "java:module/UtilBean";
    public static final String IMEAD_LOCAL_NAME = "java:module/IMEADHolder";
    public static final String PERSISTENCE = "toiletPU";
    public static final String SITE_TITLE = "page_title";
    public static final String TAGLINE = "page_tagline";
    public static final String COPYRIGHT = "rss_copyright";
    public static final String LANGUAGE = "rss_language";
    public static final String MASTER = "rss_master";
    private static final Logger LOG = Logger.getLogger(UtilBean.class.getName());
    @PersistenceUnit
    private EntityManagerFactory toiletPU;
    @EJB
    private FeedBucket src;
    @EJB
    private IMEADHolder imead;
    @EJB
    private BackupDaemon backup;
    @EJB
    private GuardRepo guard;
    @EJB
    private StateCache cache;
    @Resource
    private ManagedExecutorService exec;

    @PostConstruct
    public void init() {
        LOG.entering(UtilBean.class.getName(), "init");
        resetArticleFeed();
        resetCommentFeed();
        LOG.exiting(UtilBean.class.getName(), "init");
    }

    /**
     * for all the other times that the app needs resetting, but doesn't need to
     * be run at start up.
     */
    public void resetEverything() {
        LOG.entering(UtilBean.class.getName(), "resetEverything");
        toiletPU.getCache().evictAll();
        imead.evict();
        cache.reset();
        init();
        guard.refresh();
        exec.submit(() -> {
            {
                backup.backup();
                src.get(SpruceGenerator.SPRUCE_FEED_NAME).preAdd();
                return true;
            }
        });
        LOG.exiting(UtilBean.class.getName(), "resetEverything");
    }

    public synchronized void resetArticleFeed() {
        exec.submit(() -> {
            {
                src.get(ArticleRss.NAME).preAdd();
                return true;
            }
        });
    }

    public synchronized void resetCommentFeed() {
        exec.submit(() -> {
            {
                src.get(CommentRss.NAME).preAdd();
                return true;
            }
        });
    }

    @PreDestroy
    public void destroy() {
        LOG.info("UtilBean destroyed");
    }
}
