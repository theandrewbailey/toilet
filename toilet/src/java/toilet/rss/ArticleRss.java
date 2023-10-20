package toilet.rss;

import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import libWebsiteTools.imead.Local;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.rss.RssChannel;
import libWebsiteTools.rss.RssServlet;
import org.w3c.dom.Document;
import toilet.bean.ToiletBeanAccess;
import toilet.db.Article;
import toilet.db.Section;
import toilet.servlet.ToiletServlet;
import toilet.tag.ArticleUrl;
import toilet.tag.Categorizer;
import libWebsiteTools.rss.Feed;
import libWebsiteTools.rss.DynamicFeed;

public class ArticleRss implements DynamicFeed {

    public static final String NAME = "Articles.rss";
    public static final String ARTICLE_COUNT = "rss_articleCount";
    private static final Logger LOG = Logger.getLogger(Article.class.getName());
    private static final Pattern NAME_PATTERN = Pattern.compile("(.*?)Articles\\.rss");

    public ArticleRss() {
    }

    public Document createFeed(ToiletBeanAccess beans, Integer numEntries, String category) {
        LOG.entering("ArticleRss", "createFeed");
        RssChannel entries = new RssChannel(null == category
                ? beans.getImead().getValue(ToiletServlet.SITE_TITLE)
                : beans.getImead().getValue(ToiletServlet.SITE_TITLE) + " - " + category,
                beans.getImeadValue(SecurityRepo.BASE_URL), beans.getImead().getValue(ToiletServlet.TAGLINE));
        entries.setWebMaster(beans.getImeadValue(Feed.MASTER));
        entries.setManagingEditor(entries.getWebMaster());
        entries.setLanguage(beans.getImeadValue(Feed.LANGUAGE));
        entries.setCopyright(beans.getImeadValue(Feed.COPYRIGHT));
        for (Article art : beans.getArts().getBySection(category, 1, numEntries, null)) {
            String text = art.getPostedhtml();
            ToiletRssItem i = new ToiletRssItem(text);
            entries.addItem(i);
            i.setTitle(art.getArticletitle());
            i.setAuthor(entries.getWebMaster());
            i.setLink(ArticleUrl.getUrl(beans.getImeadValue(SecurityRepo.BASE_URL), art, null));
            i.setGuid(i.getLink());
            i.setGuidPermaLink(true);
            i.setPubDate(art.getPosted());
            i.setMarkdownSource(art.getPostedmarkdown());
            i.setSuggestion(art.getSuggestion());
            i.setDescription(art.getPostedhtml());
            i.setMetadescription(art.getDescription());
            i.setSummary(art.getSummary());
            i.setImageURL(art.getImageurl());
            i.addCategory(art.getSectionid().getName(), Categorizer.getUrl(beans.getImeadValue(SecurityRepo.BASE_URL), category, null));
            if (art.getComments()) {
                i.setComments(i.getLink() + "#comments");
            }
        }
        LOG.exiting("ArticleRss", "createFeed");
        return Feed.refreshFeed(Arrays.asList(entries));
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
        Map<String, String> urls = new LinkedHashMap<>();
        urls.put(getName(), "All articles");
        ToiletBeanAccess beans = (ToiletBeanAccess) req.getAttribute(libWebsiteTools.AllBeanAccess.class.getCanonicalName());
        for (Section cat : beans.getSects().getAll(null)) {
            urls.put(cat.getName() + getName(), cat.getName() + " articles");
        }
        return urls;
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
    public Feed preAdd() {
        return this;
    }

    @Override
    public Feed doHead(HttpServletRequest req, HttpServletResponse res) {
        if (null == req.getAttribute(NAME)) {
            ToiletBeanAccess beans = (ToiletBeanAccess) req.getAttribute(libWebsiteTools.AllBeanAccess.class.getCanonicalName());
            Object name = req.getAttribute(RssServlet.class.getSimpleName());
            Matcher regex = NAME_PATTERN.matcher(name.toString());
            regex.find();
            String category = (regex.group(1) != null && !regex.group(1).isEmpty())
                    ? regex.group(1) : null;
            try {
                Document XML = createFeed(beans, Integer.valueOf(beans.getImeadValue(ARTICLE_COUNT)), category);
                DOMSource DOMsrc = new DOMSource(XML);
                StringWriter holder = new StringWriter(100000);
                StreamResult str = new StreamResult(holder);
                Transformer trans = TransformerFactory.newInstance().newTransformer();
                trans.transform(DOMsrc, str);
                String etag = "\"" + HashUtil.getSHA256Hash(holder.toString()) + "\"";
                res.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=10000, s-maxage=100");
                res.setDateHeader(HttpHeaders.EXPIRES, OffsetDateTime.now().plusSeconds(10000).toInstant().toEpochMilli());
                res.setHeader(HttpHeaders.ETAG, etag);
                req.removeAttribute(Local.LOCALE_PARAM);
                req.setAttribute(Local.OVERRIDE_LOCALE_PARAM, Locale.forLanguageTag(beans.getImeadValue(Feed.LANGUAGE)));
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
