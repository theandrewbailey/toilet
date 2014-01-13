package toilet.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.iFeedBucket;
import libWebsiteTools.sitemap.ChangeFreq;
import libWebsiteTools.sitemap.SiteMaster;
import libWebsiteTools.sitemap.UrlMap;
import toilet.IndexFetcher;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.db.Section;
import toilet.tag.ArticleUrl;

/**
 *
 * @author alpha
 */
@Startup
@Singleton
@LocalBean
public class StateCache implements Iterable<UrlMap> {

    public static final String LOCAL_NAME = "java:module/StateCache";
    private static final String CATEGORY_QUERY = "SELECT a.sectionid.name FROM Article a GROUP BY a.sectionid.name ORDER BY COUNT(a) DESC";
    private static final String ACCEPTABLE_CONTENT_DOMAINS = "site_acceptableContentDomains";
    private volatile List<String> categories;
    private final AtomicReference<List<UrlMap>> urlMap = new AtomicReference<>();
    private volatile List<Pattern> acceptableDomains;
    @PersistenceUnit//(name = UtilBean.PERSISTENCE)
    private EntityManagerFactory toiletPU;
    @EJB
    private SiteMaster siteMaster;
    @EJB
    private IMEADHolder imead;
    @EJB
    private EntryRepo entry;
    @EJB
    private iFeedBucket src;

    @PostConstruct
    private void init(){
        siteMaster.addSource(this);
        reset();
    }

    public synchronized void reset() {
        toiletPU.getCache().evict(Article.class);
        toiletPU.getCache().evict(Comment.class);
        toiletPU.getCache().evict(Section.class);
        List<String> cateTemp = toiletPU.createEntityManager().createQuery(CATEGORY_QUERY, String.class).getResultList();
        cateTemp.remove(imead.getValue(EntryRepo.DEFAULT_CATEGORY));
        categories = Collections.unmodifiableList(cateTemp);
        urlMap.set(null);

        List<Pattern> tempdomains = new ArrayList<>();
        String domains = imead.getValue(ACCEPTABLE_CONTENT_DOMAINS);
        for (String ua : domains.split("\n")) {
            tempdomains.add(Pattern.compile(ua));
        }
        acceptableDomains = tempdomains;
    }

    /**
     * @return list of all groups (categories)
     */
    public synchronized List<String> getArticleCategories() {
        if (categories.isEmpty()) {
            categories = toiletPU.createEntityManager().createQuery(CATEGORY_QUERY, String.class).getResultList();
            categories.remove(imead.getValue(EntryRepo.DEFAULT_CATEGORY));
            categories = Collections.unmodifiableList(categories);
        }
        return categories;
    }

    /**
     * @return the acceptableDomains to send content to (like google and feedly)
     */
    public List<Pattern> getAcceptableDomains() {
        return acceptableDomains;
    }

    @Override
    public Iterator<UrlMap> iterator() {
        if (urlMap.get() == null || urlMap.get().isEmpty()) {
            synchronized (urlMap) {
                if (urlMap.get() != null && !urlMap.get().isEmpty()) {
                    return urlMap.get().iterator();
                }
                String baseUrl = imead.getValue(UtilBean.THISURL);
                List<Article> entries = entry.getArticleArchive(null);
                Collections.reverse(entries);
                List<String> cates = new ArrayList<>(getArticleCategories());
                cates.add("");
                List<UrlMap> tempUrlMap = new ArrayList<>(entries.size() + cates.size() + 10);
                urlMap.set(tempUrlMap);

                int maxArticleID = 1;
                if (entries != null) {
                    maxArticleID = entries.get(0).getArticleid();
                }
                for (Article e : entries) {
                    float difference = maxArticleID - e.getArticleid();
                    difference = 1f - (difference / 50f);
                    if (difference < 0.1f) {
                        difference = 0.1f;
                    }
                    ChangeFreq freq = ChangeFreq.weekly;
                    if (!e.getComments()) {
                        freq = ChangeFreq.never;
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
                    tempUrlMap.add(new UrlMap(ArticleUrl.getUrl(baseUrl, e), e.getModified(), freq, String.format("%.1f", difference)));
                }
                for (String c : cates) {
                    IndexFetcher f = new IndexFetcher("/index/" + c);
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
                        tempUrlMap.add(new UrlMap(baseUrl + "index/" + c + x, null, ChangeFreq.weekly, String.format("%.1f", difference)));
                    }
                }
            }
        }
        return urlMap.get().iterator();
    }
}
