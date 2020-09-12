package toilet.bean;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.cache.CachedPage;
import libWebsiteTools.cache.PageCache;
import libWebsiteTools.cache.PageCacheProvider;
import libWebsiteTools.cache.PageCaches;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.sitemap.ChangeFreq;
import libWebsiteTools.sitemap.SiteMaster;
import libWebsiteTools.sitemap.UrlMap;
import toilet.db.Article;
import toilet.rss.ArticleRss;
import toilet.servlet.ToiletServlet;
import toilet.tag.ArticleUrl;

/**
 *
 * @author alpha
 */
@Startup
@Singleton
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class StateCache implements Iterable<UrlMap> {

    public static final String LOCAL_NAME = "java:module/StateCache";
    public static final String POSTS_PER_PAGE = "pagenation_post_count";
    public static final String PAGES_AROUND_CURRENT = "pagenation_around_current";
    public static final Pattern INDEX_PATTERN = Pattern.compile(".*?/index(?:/(\\D*?))?(?:/([0-9]*)(?:\\?.*)?)?$");
    public static final Pattern ARTICLE_PATTERN = Pattern.compile(".*?/(?:(?:article)|(?:comments)|(?:amp))/([0-9]*)(?:/[\\w\\-\\.\\(\\)\\[\\]\\{\\}\\+,%_]*/?)?(?:\\?.*)?(?:#.*)?$");
    private volatile List<String> categories;
    private final AtomicReference<List<UrlMap>> urlMap = new AtomicReference<>();
    @PersistenceUnit
    private EntityManagerFactory toiletPU;
    @EJB
    private SecurityRepo guard;
    @EJB
    private FeedBucket feeds;
    @EJB
    private SiteMaster siteMaster;
    @EJB
    private IMEADHolder imead;
    @EJB
    private ArticleRepo arts;
    @Resource
    private ManagedExecutorService exec;
    @Inject
    private PageCacheProvider pageCacheProvider;
    private PageCache globalCache;

    /**
     * detect page numbers from URI
     *
     * @param URI
     * @return
     */
    public static String getPageNumber(String URI) {
        Matcher m = INDEX_PATTERN.matcher(URI);
        if (m.matches()) {
            return m.group(2);
        }
        m = ARTICLE_PATTERN.matcher(URI);
        if (m.matches()) {
            return m.group(2);
        }
        return "1";
    }

    /**
     * @param URI
     * @return
     * @throws RuntimeException
     */
    public static String getArticleIdFromURI(String URI) {
        Matcher m = ARTICLE_PATTERN.matcher(URI);
        if (m.matches()) {
            if (null != m.group(1)) {
                return m.group(1);
            }
        }
        throw new NumberFormatException("Can't parse article ID from " + URI);
    }

    public Article getArticleFromURI(String URI) {
        try {
            return arts.get(Integer.parseInt(StateCache.getArticleIdFromURI(URI)));
        } catch (NumberFormatException x) {
            return null;
        }
    }

    @PostConstruct
    private void init() {
        globalCache = (PageCache) pageCacheProvider.getCacheManager().<String, CachedPage>getCache(PageCaches.DEFAULT_URI);
        siteMaster.addSource(this);
        reset();
    }

    @SuppressWarnings("unchecked")
    public synchronized void reset() {
        toiletPU.getCache().evictAll();
        imead.evict();
        arts.evict();
        EntityManager em = toiletPU.createEntityManager();
        try {
            List<Object[]> sectionsByArticlesPosted = em.createNamedQuery("Section.byArticlesPosted").getResultList();
            double now = new Date().getTime();
            TreeMap<Double, String> popularity = new TreeMap<>();
            Long ameleration = (new Date().getTime() - ((Date) sectionsByArticlesPosted.get(0)[1]).getTime()) / 2;
            for (Object[] data : sectionsByArticlesPosted) {
                // score = average posts per year since category first started
                double score = ((Long) data[2]).doubleValue() / Math.pow((now - ((Date) data[1]).getTime() + ameleration) / 31556736.0, 1.8);
                popularity.put(score, data[0].toString());
            }
            ArrayList<String> cateTemp = new ArrayList<>(popularity.values());
            cateTemp.remove(imead.getValue(ArticleRepo.DEFAULT_CATEGORY));
            Collections.reverse(cateTemp);
            categories = Collections.unmodifiableList(cateTemp);
        } catch (ArrayIndexOutOfBoundsException ex) {
            categories = Collections.unmodifiableList(new ArrayList<>());
        } finally {
            em.close();
        }
        exec.submit(() -> {
            {
                globalCache.clear();
                urlMap.set(null);
                guard.evict();
                feeds.get(ArticleRss.NAME).preAdd();
                return true;
            }
        });
    }

    /**
     * @return list of all groups (categories)
     */
    public List<String> getArticleCategories() {
        return categories;
    }

    @Override
    public Iterator<UrlMap> iterator() {
        if (urlMap.get() == null || urlMap.get().isEmpty()) {
            synchronized (urlMap) {
                if (urlMap.get() != null && !urlMap.get().isEmpty()) {
                    return urlMap.get().iterator();
                }
                List<Article> entries = new ArrayList<>(arts.getAll(null));
                Collections.reverse(entries);
                List<String> cates = new ArrayList<>(getArticleCategories());
                cates.add("");
                List<UrlMap> tempUrlMap = new ArrayList<>(entries.size() + cates.size() + 10);
                urlMap.set(tempUrlMap);

                int maxArticleID = 0 < entries.size() ? entries.get(entries.size() - 1).getArticleid() : 1;
                for (Article e : entries) {
                    float difference = maxArticleID - e.getArticleid();
                    difference = 1f - (difference / 50f);
                    if (difference < 0.1f) {
                        difference = 0.1f;
                    }
                    ChangeFreq freq = ChangeFreq.weekly;
                    if (!e.getComments()) {
                        freq = ChangeFreq.never;
                        difference = 0.1f;
                    } else {
                        GregorianCalendar date = new GregorianCalendar();
                        date.add(GregorianCalendar.MONTH, -1);
                        if (date.getTimeInMillis() > e.getPosted().getTime()) {
                            freq = ChangeFreq.monthly;
                        }
                        date.add(GregorianCalendar.MONTH, -5);
                        if (date.getTimeInMillis() > e.getPosted().getTime()) {
                            freq = ChangeFreq.yearly;
                        }
                    }
                    tempUrlMap.add(new UrlMap(ArticleUrl.getUrl(imead.getValue(SecurityRepo.BASE_URL), e, null, null), e.getModified(), freq, String.format("%.1f", difference)));
                }
                for (String c : cates) {
                    IndexFetcher f = new IndexFetcher(this, "/index/" + c);
                    if (!c.isEmpty()) {
                        c = c + "/";
                    }
                    int countTo = f.getCount();
                    if (f.getLast() > countTo) {
                        countTo = f.getLast();
                    }
                    for (int x = 1; x <= countTo; x++) {
                        float difference = 0.5f - (x / 10f);
                        if (difference < 0.1f) {
                            difference = 0.1f;
                        }
                        tempUrlMap.add(new UrlMap(imead.getValue(SecurityRepo.BASE_URL) + "index/" + c + x, null, ChangeFreq.weekly, String.format("%.1f", difference)));
                    }
                }
            }
        }
        return urlMap.get().iterator();
    }

    public IndexFetcher getIndexFetcher(String URI) {
        return new IndexFetcher(this, URI);
    }

    /**
     * @param URI
     * @return
     */
    private String getCategoryFromURI(String URI) {
        Matcher m = INDEX_PATTERN.matcher(URI.replace("%20", " "));
        if (m.matches()) {
            if (null != m.group(1) || null != m.group(2)) {
                try {
                    String cate = m.group(1);
                    return null == cate || cate.isEmpty() ? imead.getValue(ArticleRepo.DEFAULT_CATEGORY) : URLDecoder.decode(cate, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    // not gonna happen
                    throw new JVMNotSupportedError(ex);
                }
            }
        }
        if ("/".equals(URI) || URI.isEmpty() || "/index".equals(URI)) {
            return imead.getValue(ArticleRepo.DEFAULT_CATEGORY);
        }
        throw new IllegalArgumentException("Invalid category URL: " + URI);
    }

    public static class IndexFetcher implements Serializable {

        private int page = 1;
        private String section = null;
        private int count = 0;
        private int first = 1;
        private final int pagesAroundCurrent;
        private final String siteTitle;
        private final String siteTagline;
        @SuppressWarnings("unchecked")
        private List<Article> articles = Collections.EMPTY_LIST;

        private IndexFetcher(StateCache cache, String URI) {
            int ppp = Integer.parseInt(cache.imead.getValue(POSTS_PER_PAGE));
            pagesAroundCurrent = Integer.parseInt(cache.imead.getValue(PAGES_AROUND_CURRENT));
            try {
                String pagenum = getPageNumber(URI);
                if (null != pagenum) {
                    page = pagenum.isEmpty() ? 1 : Integer.valueOf(pagenum);
                }
                section = cache.getCategoryFromURI(URI);
            } catch (RuntimeException e) {
                siteTitle = null;
                siteTagline = null;
                return;
            }
            try {
                Integer.valueOf(section);
                section = null;
            } catch (NumberFormatException e) {
            }
            if (null != section && section.equals(cache.imead.getValue(ArticleRepo.DEFAULT_CATEGORY))) {
                section = null;
            }
            // get total of all, to display number of pages limit
            if (count == 0) {
                double counted = cache.arts.count(section);
                count = (int) Math.ceil(counted / ppp);
            }
            // wierd algoritim to determine how many pagination links to other pages on this page
            if (page + pagesAroundCurrent > count) {
                first = count - pagesAroundCurrent * 2;
            } else if (page - pagesAroundCurrent > 0) {
                first = page - pagesAroundCurrent;
            }
            if (first < 1) {
                first = 1;
            }
            siteTitle = cache.imead.getLocal(ToiletServlet.SITE_TITLE, "en");
            siteTagline = cache.imead.getLocal(ToiletServlet.TAGLINE, "en");
            articles = cache.arts.getSection(section, page, ppp);
        }

        public List<Article> getArticles() {
            return articles;
        }

        public int getCount() {
            return count;
        }

        public int getFirst() {
            return first;
        }

        public int getLast() {
            return Math.min(first + pagesAroundCurrent * 2, count);
        }

        public String getDescription() {
            StringBuilder d = new StringBuilder(70).append(siteTitle);
            if (null == section && 1 != page) {
                d.append(", all categories, page ").append(page);
            } else if (null != section) {
                d.append(", ").append(section).append(" category, page ").append(page);
            } else {
                d.append(", ").append(siteTagline);
            }
            return d.toString();
        }

        public boolean isValid() {
            return section != null && page != 0;
        }

        public String getSection() {
            return section;
        }

        public int getPage() {
            return page;
        }
    }
}
