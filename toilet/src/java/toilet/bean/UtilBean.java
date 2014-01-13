package toilet.bean;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import libOdyssey.bean.GuardHolder;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.iFeedBucket;
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
    public static final String IMEAD_LOCAL_NAME = "java:global/toilet/IMEADHolder";
    public static final String PERSISTENCE = "toiletPU";
    public static final String THISURL = "thisURL";
    public static final String SITE_TITLE = "page_title";
    public static final String TAGLINE = "page_tagline";
    public static final String COPYRIGHT = "rss_copyright";
    public static final String LANGUAGE = "rss_language";
    public static final String MASTER = "rss_master";
    private static final Logger log = Logger.getLogger(UtilBean.class.getName());
    @PersistenceUnit(name = UtilBean.PERSISTENCE)
    private EntityManagerFactory toiletPU;
    @EJB
    private iFeedBucket src;
    @EJB
    private IMEADHolder imead;
    @EJB
    private BackupDaemon backup;
    @EJB
    private GuardHolder guard;
    @EJB
    private StateCache cache;

    @PostConstruct
    public void init() {
        log.entering(UtilBean.class.getName(), "init");

        try {
            resetArticleFeed();
            resetCommentFeed();
        } catch (NullPointerException ex) {
        }

        log.exiting(UtilBean.class.getName(), "init");
    }

    /**
     * for all the other times that the app needs resetting, but doesn't need to be run at start up.
     */
    public void resetEverything(){
        log.entering(UtilBean.class.getName(), "resetEverything");
        toiletPU.getCache().evictAll();
        imead.populateCache();
        cache.reset();
        init();
        backup.backup();
        guard.refresh();
        try{
            // mbe not such a good idea...
            src.getFeed("Spruce.rss").preAdd();
        }catch(NullPointerException n){}
        log.exiting(UtilBean.class.getName(), "resetEverything");
    }

    public synchronized void resetArticleFeed() {
        src.getFeed(ArticleRss.NAME).preAdd();
    }

    public synchronized void resetCommentFeed() {
        src.getFeed(CommentRss.NAME).preAdd();
    }

    @PreDestroy
    private void destroy() {
        log.info("UtilBean destroyed");
    }

    /**
     * counts slashes, and strips off the last element in the given URI
     * 
     * TODO: redo with regex
     *
     * @param URI
     * @return 
     * @throws RuntimeException
     */
    public String getIdFromURI(String URI) {
        if ("/".equals(URI)) {
            return imead.getValue(EntryRepo.DEFAULT_CATEGORY);
        }
        int x = 1;
        String[] parts = URI.split("/");
        if ("toilet".equals(parts[x])) {
            x++;
        }
        if (parts.length == x) {
            return imead.getValue(EntryRepo.DEFAULT_CATEGORY);
        } else if ("index".equals(parts[x])) {
            x++;
            if (parts.length == x) {
                return imead.getValue(EntryRepo.DEFAULT_CATEGORY);
            } else {
                try {
                    return URLDecoder.decode(parts[x], "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    // not gonna happen
                    log.log(Level.SEVERE, JVMNotSupportedError.UTF8_UNSUPPORTED, ex);
                    throw new JVMNotSupportedError(ex);
                }
            }
        } else if ("article".equals(parts[x])) {
            return parts[++x];
        } else {
            throw new RuntimeException();
        }
    }
}
