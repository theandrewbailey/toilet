package toilet.rss;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.AbstractRssFeed;
import libWebsiteTools.rss.RssChannel;
import libWebsiteTools.rss.RssServlet;
import libWebsiteTools.rss.SimpleRssFeed;
import libWebsiteTools.rss.iDynamicFeed;
import libWebsiteTools.rss.iFeed;
import org.w3c.dom.Document;
import toilet.UtilStatic;
import toilet.bean.ArticleRepo;
import toilet.bean.StateCache;
import toilet.db.Article;
import toilet.servlet.ToiletServlet;
import toilet.tag.ArticleUrl;
import toilet.tag.Categorizer;

@WebListener("The RSS feed for articles, autoadded")
public class ArticleRss extends AbstractRssFeed implements iDynamicFeed {

    public static final String NAME = "Articles.rss";
    private static final String ARTICLE_COUNT = "rss_articleCount";
    private static final Logger LOG = Logger.getLogger(Article.class.getName());
    private static final Pattern NAME_PATTERN = Pattern.compile("(.*?)Articles\\.rss");
    private Map<String, String> URLs = new LinkedHashMap<>();
    @EJB
    private ArticleRepo arts;
    @EJB
    private StateCache cache;
    @EJB
    private IMEADHolder imead;

    public ArticleRss() {
    }

    public Document createFeed(Integer numEntries, String category) {
        LOG.entering("ArticleRss", "createFeed");
        // if instantiated manually
        if (arts == null && imead == null) {
            arts = UtilStatic.getBean(ArticleRepo.LOCAL_NAME, ArticleRepo.class);
            imead = UtilStatic.getBean(IMEADHolder.LOCAL_NAME, IMEADHolder.class);
        }
        RssChannel entries = new RssChannel(null == category
                ? imead.getLocal(ToiletServlet.SITE_TITLE, "en")
                : imead.getLocal(ToiletServlet.SITE_TITLE, "en") + " - " + category,
                imead.getValue(SecurityRepo.BASE_URL), imead.getLocal(ToiletServlet.TAGLINE, "en"));
        entries.setWebMaster(imead.getValue(AbstractRssFeed.MASTER));
        entries.setManagingEditor(entries.getWebMaster());
        entries.setLanguage(imead.getValue(AbstractRssFeed.LANGUAGE));
        entries.setCopyright(imead.getValue(AbstractRssFeed.COPYRIGHT));
        for (Article art : arts.getSection(category, 1, numEntries)) {
            String text = art.getPostedhtml();
            ToiletRssItem i = new ToiletRssItem(text);
            entries.addItem(i);
            i.setTitle(art.getArticletitle());
            i.setAuthor(entries.getWebMaster());
            i.setLink(ArticleUrl.getUrl(imead.getValue(SecurityRepo.BASE_URL), art, null, null));
            i.setGuid(i.getLink());
            i.setGuidPermaLink(true);
            i.setPubDate(art.getPosted());
            i.setMarkdownSource(art.getPostedmarkdown());
            i.setDescription(art.getPostedhtml());
            i.setMetadescription(art.getDescription());
            i.addCategory(art.getSectionid().getName(), Categorizer.getUrl(imead.getValue(SecurityRepo.BASE_URL), category, null, null));
            if (art.getComments()) {
                i.setComments(i.getLink() + "#comments");
            }
        }
        LOG.exiting("ArticleRss", "createFeed");
        return SimpleRssFeed.refreshFeed(Arrays.asList(entries));
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     *
     * @param req
     * @return (category)Articles.rss
     */
    @Override
    public Map<String, String> getFeedURLs(HttpServletRequest req) {
        return URLs;
    }

    /**
     *
     * @param name
     * @return if name matches (.*?)Articles\\.rss
     */
    @Override
    public boolean willHandle(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }

    @Override
    public iFeed preAdd() {
        try {
            createFeed(Integer.valueOf(imead.getValue(ARTICLE_COUNT)), null);
            Map<String, String> temp = new LinkedHashMap<>();
            temp.put(getName(), "All articles");
            for (String cat : cache.getArticleCategories()) {
                temp.put(cat + getName(), cat + " articles");
            }
            URLs = Collections.unmodifiableMap(temp);
        } catch (RuntimeException r) {
            LOG.log(Level.SEVERE, "Comment feed will not be available due to an invalid parameter.");
            return null;
        }
        return this;
    }

    @Override
    public iFeed doHead(HttpServletRequest req, HttpServletResponse res) {
        if (null == req.getAttribute(NAME)) {
            Object name = req.getAttribute(RssServlet.class.getSimpleName());
            Matcher regex = NAME_PATTERN.matcher(name.toString());
            regex.find();
            String category = (regex.group(1) != null && !regex.group(1).isEmpty())
                    ? regex.group(1) : null;
            try {
                Document XML = createFeed(Integer.valueOf(imead.getValue(ARTICLE_COUNT)), category);
                DOMSource DOMsrc = new DOMSource(XML);
                StringWriter holder = new StringWriter(100000);
                StreamResult str = new StreamResult(holder);
                Transformer trans = TransformerFactory.newInstance().newTransformer();
                trans.transform(DOMsrc, str);
                String etag = "\"" + HashUtil.getSHA256Hash(holder.toString()) + "\"";
                res.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=" + 10000);
                res.setDateHeader(HttpHeaders.EXPIRES, new Date().getTime() + 10000000);
                res.setHeader(HttpHeaders.ETAG, etag);
                req.setAttribute(HttpHeaders.ETAG, etag);
                req.setAttribute(NAME, XML);
                if (etag.equals(req.getHeader(HttpHeaders.IF_NONE_MATCH))) {
                    res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                }
            } catch (NumberFormatException n) {
                LOG.log(Level.SEVERE, "Article feed will not be available due to an invalid parameter.");
                return null;
            } catch (TransformerException ex) {
                LOG.log(Level.SEVERE, "Article feed will not be available due to an XML transformation error.", ex);
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return null;
            }
        }
        return this;
    }

    @Override
    public Document preWrite(HttpServletRequest req, HttpServletResponse res) {
        doHead(req, res);
        return HttpServletResponse.SC_OK == res.getStatus() ? (Document) req.getAttribute(NAME) : null;
    }
}
