package toilet.bean;

import toilet.bean.database.SectionDatabase;
import toilet.bean.database.CommentDatabase;
import toilet.bean.database.PostgresArticleDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.ConcurrencyManagement;
import jakarta.ejb.ConcurrencyManagementType;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import jakarta.ws.rs.core.HttpHeaders;
import libWebsiteTools.cache.PageCacheProvider;
import libWebsiteTools.db.Repository;
import libWebsiteTools.file.FileRepository;
import libWebsiteTools.imead.IMEADDatabase;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.postgres.PostgresFileDatabase;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.security.GuardFilter;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.sitemap.SiteMapper;
import toilet.AllBeanAccess;
import toilet.SitemapProvider;
import toilet.db.Comment;
import toilet.db.Section;
import toilet.rss.ArticleRss;
import toilet.rss.CommentRss;
import toilet.rss.ErrorRss;
import toilet.servlet.AdminLoginServlet;

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

    public static final String DEFAULT_DATASOURCE = "java/toilet/default";
    @Resource
    private ManagedExecutorService exec;
    @PersistenceUnit
    private EntityManagerFactory toiletPU;
    private PageCacheProvider pageCacheProvider;
    private SecurityRepo error;
    private FileRepository file;
    private IMEADHolder imead;
    private FeedBucket feeds;
    private ArticleRepository arts;
    private Repository<Comment> comms;
    private Repository<Section> sects;
    private BackupDaemon backup;
//    private SpruceGenerator spruce;
    private SiteMapper mapper;
    private Boolean firstTime;
    private HashMap<String, ToiletBeanAccess> altHosts;

    public ToiletBeanAccess() {
    }

    /**
     *
     * @param req
     * @return appropriate version of beans based on the request's hostname
     */
    @Override
    public ToiletBeanAccess getInstance(HttpServletRequest req) {
        ToiletBeanAccess beans = (ToiletBeanAccess) req.getAttribute(libWebsiteTools.AllBeanAccess.class.getCanonicalName());
        if (null == beans) {
            String host = req.getHeader(HttpHeaders.HOST);
            //beans = altHosts.get(host);
            //if (null == beans) {
            beans = this;
            //}
            req.setAttribute(libWebsiteTools.AllBeanAccess.class.getCanonicalName(), beans);
        }
        return beans;
    }

    @PostConstruct
    private void init() {
        try {
            ArticleRss a = new ArticleRss();
            a.createFeed(this, Integer.valueOf(this.getImeadValue(ArticleRss.ARTICLE_COUNT)), null);
            getFeeds().upsert(Arrays.asList(a));
        } catch (RuntimeException ex) {
        }
        try {
            CommentRss c = new CommentRss();
            c.createFeed(this, null);
            getFeeds().upsert(Arrays.asList(c));
        } catch (Exception ex) {
        }
        //HashMap<String, DataSource> dataSources = traverseContext(null, "");
        getFeeds().upsert(Arrays.asList(new ErrorRss()));
//        getSpruce();
        // TODO: dyamically create entity managers for each java/toilet/* database pools
        /*altHosts = new HashMap<>(dataSources.size());
        for (Map.Entry<String, DataSource> pair : dataSources.entrySet()) {
            String jndiName = pair.getKey();
            if (!DEFAULT_DATASOURCE.equals(jndiName) && jndiName.startsWith("java/toilet/")) {
                HashMap<String, Object> props = new HashMap<>(toiletPU.getProperties());
                props.put("javax.persistence.nonJtaDataSource", jndiName);
                props.put("javax.persistence.transactionType", "RESOURCE_LOCAL");
                EntityManagerFactory emf = Persistence.createEntityManagerFactory(DEFAULT_DATASOURCE, props);
                ToiletBeanAccess newbeans = new ToiletBeanAccess().use(toiletPU).use(emf).init();
                newbeans.getSpruce();
                newbeans.getFeeds().upsert(getFeeds().getAll(Integer.SIZE));
                altHosts.put(jndiName.replaceFirst(jndiName, "java/toilet/"), newbeans);
            }
        }*/
    }

    private ToiletBeanAccess use(EntityManagerFactory fact) {
        toiletPU = fact;
        return this;
    }

    private ToiletBeanAccess use(ManagedExecutorService ex) {
        exec = ex;
        return this;
    }

    private HashMap<String, DataSource> traverseContext(Context ic, String lastSpace) {
        HashMap<String, DataSource> subNames = new HashMap<>();
        try {
            if (null == ic) {
                ic = new InitialContext();
            }
            ArrayList<NameClassPair> contextList = Collections.list(ic.list(""));
            for (NameClassPair nc : contextList) {
                if ("__SYSTEM".equals(nc.getName())) {
                    continue;
                }
                try {
                    Object o = ic.lookup(nc.getName());
                    if (o instanceof Context) {
                        subNames.putAll(traverseContext((Context) o, lastSpace + nc.getName() + "/"));
                    } else if (o instanceof DataSource && !nc.getName().endsWith("__pm")) {
                        DataSource ds = (DataSource) o;
                        subNames.put(lastSpace + nc.getName(), ds);
                    }
                } catch (NamingException ex) {
                    Logger.getLogger(ToiletBeanAccess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (NamingException ex) {
            Logger.getLogger(ToiletBeanAccess.class.getName()).log(Level.SEVERE, null, ex);
        }
        return subNames;
    }

    /**
     * lookup an EJB. avoid using this, because it's not fast.
     *
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
        getImead().evict();
        getArts().evict();
        getSects().evict();
        getFile().evict();
        getGlobalCache().clear();
        firstTime = null;
    }

    @Schedule(persistent = false, hour = "1")
    private void nightly() {
        getBackup().run();
        getError().evict();
    }

    @Schedule(persistent = false, minute = "*", hour = "*", dayOfWeek = "*", month = "*")
    private void sweep() {
        getPageCacheProvider().sweep();
    }

    @Override
    public ArticleRepository getArts() {
        if (null == arts) {
            arts = new PostgresArticleDatabase(toiletPU, getImead());
        }
        return arts;
    }

    @Override
    public Repository<Comment> getComms() {
        if (null == comms) {
            comms = new CommentDatabase(toiletPU);
        }
        return comms;
    }

    @Override
    public Repository<Section> getSects() {
        if (null == sects) {
            sects = new SectionDatabase(toiletPU, getImead());
        }
        return sects;
    }

    @Override
    public BackupDaemon getBackup() {
        if (null == backup) {
            backup = new BackupDaemon(this);
        }
        return backup;
    }

    @Override
    public ManagedExecutorService getExec() {
        return exec;
    }

    @Override
    public SecurityRepo getError() {
        if (null == error) {
            error = new SecurityRepo(toiletPU, getImead());
            try {
                error.getCerts().verifyCertificate(getImeadValue(GuardFilter.CERTIFICATE_NAME));
            } catch (RuntimeException rx) {
                error.logException(null, "High security not available: " + rx.getMessage(), null, rx);
            }
        }
        return error;
    }

    @Override
    public FileRepository getFile() {
        if (null == file) {
            file = new PostgresFileDatabase(toiletPU);
        }
        return file;
    }

    @Override
    public IMEADHolder getImead() {
        if (null == imead) {
            imead = new IMEADDatabase(toiletPU);
        }
        return imead;
    }

    @Override
    public FeedBucket getFeeds() {
        if (null == feeds) {
            feeds = new FeedBucket();
        }
        return feeds;
    }

    @Override
    public PageCacheProvider getPageCacheProvider() {
        if (null == pageCacheProvider) {
            pageCacheProvider = new PageCacheProvider();
        }
        return pageCacheProvider;
    }

//    @Override
//    public SpruceGenerator getSpruce() {
//        if (null == spruce) {
//            spruce = new SpruceGenerator(getImead(), getFile(), getExec());
//            if (spruce.shouldBeReady()) {
//                getFeeds().upsert(Arrays.asList(spruce));
//            }
//        }
//        return spruce;
//    }
    @Override
    public SiteMapper getMapper() {
        if (null == mapper) {
            mapper = new SiteMapper();
            mapper.addSource(new SitemapProvider(this));
        }
        return mapper;
    }

    public boolean isFirstTime() {
        return null != firstTime ? firstTime : (null == getImeadValue(AdminLoginServlet.IMEAD)
                || !HashUtil.ARGON2_ENCODING_PATTERN.matcher(getImeadValue(AdminLoginServlet.IMEAD)).matches());
    }
}
