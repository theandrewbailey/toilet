package toilet;

import toilet.bean.ToiletBeanAccess;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.annotation.WebListener;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.sitemap.AbstractPageSource;
import libWebsiteTools.sitemap.ChangeFreq;
import libWebsiteTools.sitemap.UrlMap;
import toilet.bean.SpruceGenerator;
import toilet.db.Article;
import toilet.db.Section;
import toilet.tag.ArticleUrl;

/**
 *
 * @author alpha
 */
@WebListener("Gives out the site map.")
public class SitemapProvider extends AbstractPageSource {

    @EJB
    private ToiletBeanAccess beans;
    @EJB
    private SpruceGenerator spruce;

    @Override
    public Iterator<UrlMap> iterator() {
        if (urlMap == null || urlMap.isEmpty()) {
            List<Article> entries = new ArrayList<>(beans.getArts().getAll(null));
            Collections.reverse(entries);
            List<Section> sects = beans.getSects().getAll(null);
            urlMap = new ArrayList<>(entries.size() + sects.size() + 10);
            urlMap.add(new UrlMap(beans.getImeadValue(SecurityRepo.BASE_URL), null, ChangeFreq.daily, "0.7"));
            if (spruce.shouldBeReady()) {
                urlMap.add(new UrlMap(beans.getImeadValue(SecurityRepo.BASE_URL) + "spruce", null, ChangeFreq.always, "0.1"));
            }
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
                urlMap.add(new UrlMap(ArticleUrl.getUrl(beans.getImeadValue(SecurityRepo.BASE_URL), e, null), e.getModified(), freq, String.format("%.1f", difference)));
            }
            for (Section s : sects) {
                String name = s.getName();
                IndexFetcher f = new IndexFetcher(beans, "/index/" + name);
                if (!name.isEmpty()) {
                    name = name + "/";
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
                    urlMap.add(new UrlMap(beans.getImeadValue(SecurityRepo.BASE_URL) + "index/" + name + x, null, ChangeFreq.weekly, String.format("%.1f", difference)));
                }
            }
        }
        return urlMap.iterator();
    }
}
