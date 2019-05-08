package toilet.rss;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import libOdyssey.bean.GuardRepo;
import libWebsiteTools.HashUtil;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.Feed;
import libWebsiteTools.rss.entity.AbstractRssFeed;
import libWebsiteTools.rss.entity.RssChannel;
import libWebsiteTools.rss.iFeed;
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
    private String etag = "";

    public ArticleRss() {
    }

    public Document generateFeed(Integer numEntries) {
        // if instantiated manually
        if (entry == null && imead == null) {
            entry = UtilStatic.getBean(EntryRepo.LOCAL_NAME, EntryRepo.class);
            imead = UtilStatic.getBean(UtilBean.IMEAD_LOCAL_NAME, IMEADHolder.class);
        }

        lastUpdated = new Date(0);
        RssChannel entries = new RssChannel(imead.getLocal(UtilBean.SITE_TITLE, "en"), imead.getValue(GuardRepo.CANONICAL_URL), imead.getLocal(UtilBean.TAGLINE, "en"));
        entries.setWebMaster(imead.getValue(UtilBean.MASTER));
        entries.setManagingEditor(entries.getWebMaster());
        entries.setLanguage(imead.getValue(UtilBean.LANGUAGE));
        entries.setCopyright(imead.getValue(UtilBean.COPYRIGHT));

        List<Article> lEntry = entry.getArticleArchive(numEntries);

        for (Article e : lEntry) {
            String text = e.getPostedhtml();
            ToiletRssItem i = new ToiletRssItem(text);
            entries.addItem(i);
            i.setTitle(e.getArticletitle());
            if (!e.getSectionid().getName().equals(imead.getValue(EntryRepo.DEFAULT_CATEGORY))) {
                i.addCategory(e.getSectionid().getName(), imead.getValue(GuardRepo.CANONICAL_URL) + "index/" + e.getSectionid().getName());
                String prefix = e.getSectionid().getName() + ": ";
                if (!e.getArticletitle().startsWith(prefix)) {
                    i.setTitle(prefix + e.getArticletitle());
                }
            }
            i.setAuthor(entries.getWebMaster());
            i.setLink(ArticleUrl.getUrl(imead.getValue(GuardRepo.CANONICAL_URL), e));
            i.setGuid(i.getLink());
            i.setGuidPermaLink(true);
            i.setPubDate(e.getPosted());
            i.setMarkdownSource(e.getPostedmarkdown());
            i.setDescription(e.getPostedhtml());
            i.setMetadescription(e.getDescription());
            if (e.getComments()) {
                i.setComments(i.getLink() + "#comments");
            }
            if (i.getPubDate().after(lastUpdated)) {
                lastUpdated = i.getPubDate();
            }
        }
        return refreshFeed(entries);
    }

    @Override
    public iFeed preAdd() {
        XML = generateFeed(Integer.valueOf(imead.getValue(ARTICLE_COUNT)));
        try {
            DOMSource DOMsrc = new DOMSource(XML);
            StringWriter holder = new StringWriter(20000);
            StreamResult str = new StreamResult(holder);
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            trans.transform(DOMsrc, str);
            etag = "\"" + HashUtil.getSHA256Hash(holder.toString()) + "\"";
        } catch (TransformerException ex) {
            Logger.getLogger(ArticleRss.class.getName()).log(Level.SEVERE, null, ex);
        }
        return this;
    }

    @Override
    public long getLastModified() {
        return lastUpdated.getTime();
    }

    @Override
    public iFeed doHead(HttpServletRequest req, HttpServletResponse res) {
        res.setHeader("Cache-Control", "public, max-age=" + 10000);
        res.setDateHeader("Last-Modified", lastUpdated.getTime());
        res.setDateHeader("Expires", new Date().getTime() + 10000000);
        res.setHeader("ETag", etag);
        if (etag.equals(req.getHeader("If-None-Match"))) {
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
        return this;
    }

    @Override
    public Document preWrite(HttpServletRequest req, HttpServletResponse res) {
        doHead(req, res);
        return HttpServletResponse.SC_OK == res.getStatus() ? XML : null;
    }
}
