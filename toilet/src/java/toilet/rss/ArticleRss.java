package toilet.rss;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.bean.GuardHolder;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.Feed;
import libWebsiteTools.rss.entity.AbstractRssFeed;
import libWebsiteTools.rss.entity.RssChannel;
import org.w3c.dom.Document;
import toilet.UtilStatic;
import toilet.bean.EntryRepo;
import toilet.bean.UtilBean;
import toilet.db.Article;
import toilet.tag.ArticleUrl;

@WebListener("The RSS feed for articles, autoadded")
@Feed(ArticleRss.NAME)
public class ArticleRss extends AbstractRssFeed {

    public static final String NAME = "Articles.rss";
    private static final String ARTICLE_COUNT = "rss_articleCount";
    @EJB
    private EntryRepo entry;
    @EJB
    private IMEADHolder imead;
    private Document XML;
    private Date lastUpdated = new Date(0);

    public ArticleRss() {
    }

    public Document generateFeed(Integer numEntries) {
        // if instantiated manually
        if (entry == null && imead == null) {
            entry = UtilStatic.getBean(EntryRepo.LOCAL_NAME, EntryRepo.class);
            imead = UtilStatic.getBean(UtilBean.IMEAD_LOCAL_NAME, IMEADHolder.class);
        }

        lastUpdated = new Date(0);
        RssChannel entries = new RssChannel(imead.getValue(UtilBean.SITE_TITLE), imead.getValue(GuardHolder.CANONICAL_URL), imead.getValue(UtilBean.TAGLINE));
        entries.setWebMaster(imead.getValue(UtilBean.MASTER));
        entries.setManagingEditor(entries.getWebMaster());
        entries.setLanguage(imead.getValue(UtilBean.LANGUAGE));
        entries.setCopyright(imead.getValue(UtilBean.COPYRIGHT));

        List<Article> lEntry = entry.getArticleArchive(numEntries);

        for (Article e : lEntry) {
            String text = e.getPostedhtml();
            MarkdownRssItem i = new MarkdownRssItem(text);
            entries.addItem(i);
            i.setTitle(e.getArticletitle());
            if (!e.getSectionid().getName().equals(imead.getValue(EntryRepo.DEFAULT_CATEGORY))) {
                i.addCategory(e.getSectionid().getName(), imead.getValue(GuardHolder.CANONICAL_URL) + "index/" + e.getSectionid().getName());
                String prefix = e.getSectionid().getName() + ": ";
                if (!e.getArticletitle().startsWith(prefix)) {
                    i.setTitle(prefix + e.getArticletitle());
                }
            }
            i.setAuthor(entries.getWebMaster());
            i.setLink(ArticleUrl.getUrl(imead.getValue(GuardHolder.CANONICAL_URL), e));
            try {
                i.setGuid(URLEncoder.encode(e.getDescription(), "UTF-8"));
                i.setGuidPermaLink(false);
            } catch (UnsupportedEncodingException enc) {
                throw new JVMNotSupportedError(enc);
            }
            i.setPubDate(e.getPosted());
            i.setMarkdownSource(e.getPostedmarkdown());
            i.setDescription(e.getPostedhtml());
            if (e.getComments()) {
                i.setComments(i.getLink() + "#comments");
            }
            if (i.getPubDate().after(lastUpdated)){
                lastUpdated = i.getPubDate();
            }
        }
        return refreshFeed(entries);
    }

    @Override
    public synchronized void preAdd() {
        XML = generateFeed(Integer.valueOf(imead.getValue(ARTICLE_COUNT)));
    }

    @Override
    public long getLastModified(){
        return lastUpdated.getTime();
    }

    @Override
    public Document preWrite(HttpServletRequest req, HttpServletResponse res) {
        res.setHeader("Cache-Control", "public, max-age="+10000);
        res.setDateHeader("Last-Modified", lastUpdated.getTime());
        res.setDateHeader("Expires", new Date().getTime()+10000000);
        return XML;
    }
}
