package toilet.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.sitemap.ChangeFreq;
import libWebsiteTools.sitemap.SiteMaster;
import libWebsiteTools.sitemap.UrlMap;
import toilet.IndexFetcher;
import toilet.db.Article;
import toilet.tag.ArticleUrl;

/**
 *
 * @author alpha
 */
@Startup
@Singleton
@LocalBean
public class ArticleStateCache implements Iterable<UrlMap> {

    private static final String CATEGORY_QUERY = "SELECT distinct s.name FROM Section s WHERE size(s.articleCollection) > 0";
    private List<String> categories;
    private List<UrlMap> urlMap;
    @PersistenceUnit(name = UtilBean.PERSISTENCE)
    private EntityManagerFactory toiletPU;
    @EJB
    private SiteMaster siteMaster;
    @EJB
    private IMEADHolder imead;
    @EJB
    private EntryRepo entry;

    @PostConstruct
    private void init(){
        siteMaster.addSource(this);
        reset();
    }

    public synchronized void reset(){
        categories = new ArrayList<String>(0);
        urlMap = new ArrayList<UrlMap>(0);
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

    @Override
    public synchronized Iterator<UrlMap> iterator() {
        if (urlMap.isEmpty()) {
            String baseUrl = imead.getValue(UtilBean.THISURL);
            List<Article> entries = entry.getArticleArchive(null);
            List<String> cates = new ArrayList<String>(getArticleCategories());
            cates.add("");
            urlMap = new ArrayList<UrlMap>(entries.size() + cates.size() + 10);

            for (Article e : entries) {
                urlMap.add(new UrlMap(ArticleUrl.getUrl(baseUrl, e), e.getModified(), ChangeFreq.weekly, "1.0"));
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
                    urlMap.add(new UrlMap(baseUrl + "index/" + c + x, null, ChangeFreq.weekly, "0.5"));
                }
            }
        }
        return urlMap.iterator();
    }
}
