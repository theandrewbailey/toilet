package toilet.rss;

import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import libWebsiteTools.rss.RssItem;
import libWebsiteTools.rss.RssServlet;
import org.w3c.dom.Document;
import toilet.bean.ToiletBeanAccess;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.servlet.ToiletServlet;
import toilet.tag.ArticleUrl;
import toilet.tag.Categorizer;
import libWebsiteTools.rss.Feed;
import libWebsiteTools.rss.DynamicFeed;

public class CommentRss implements DynamicFeed {

    public static final String NAME = "Comments.rss";
    private static final String COMMENT_COUNT = "rss_commentCount";
    private static final Logger LOG = Logger.getLogger(CommentRss.class.getName());
    private static final Pattern NAME_PATTERN = Pattern.compile("Comments(.*?)\\.rss");

    public CommentRss() {
    }

    public Document createFeed(ToiletBeanAccess beans, Object articleId) {
        LOG.entering("CommentRss", "createFeed");
        RssChannel entries;
        if (null == articleId) {
            entries = createChannel(beans, beans.getComms().getAll(Integer.valueOf(beans.getImeadValue(COMMENT_COUNT))));
        } else {
            Article art = beans.getArts().get(Integer.parseInt(articleId.toString()));
            List<Comment> lComments = new ArrayList<>(art.getCommentCollection());
            Collections.reverse(lComments);
            entries = createChannel(beans, lComments);
            entries.setTitle(art.getArticletitle() + " - Comments");
            entries.setDescription("from " + beans.getImead().getValue(ToiletServlet.SITE_TITLE));
        }
        LOG.exiting("CommentRss", "createFeed");
        return Feed.refreshFeed(Arrays.asList(entries));
    }

    public RssChannel createChannel(ToiletBeanAccess beans, Collection<Comment> lComments) {
        RssChannel entries = new RssChannel(beans.getImead().getValue(ToiletServlet.SITE_TITLE) + " - Comments", beans.getImeadValue(SecurityRepo.BASE_URL), beans.getImead().getValue(ToiletServlet.TAGLINE));
        entries.setWebMaster(beans.getImeadValue(Feed.MASTER));
        entries.setManagingEditor(entries.getWebMaster());
        entries.setLanguage(beans.getImeadValue(Feed.LANGUAGE));
        for (Comment c : lComments) {
            RssItem i = new RssItem(c.getPostedhtml());
            entries.addItem(i);
            i.addCategory(c.getArticleid().getSectionid().getName(), Categorizer.getUrl(beans.getImeadValue(SecurityRepo.BASE_URL), c.getArticleid().getSectionid().getName(), null));
            i.setLink(ArticleUrl.getUrl(beans.getImeadValue(SecurityRepo.BASE_URL), c.getArticleid(), "comments"));
            i.setGuid(HashUtil.getSHA256Hash(c.getPostedname() + c.getPosted().toInstant().toEpochMilli() + c.getPostedhtml()));
            i.setPubDate(c.getPosted());
            i.setTitle(c.getArticleid().getArticletitle());
            i.setAuthor(c.getPostedname());
            if (c.getArticleid().getComments()) {
                i.setComments(ArticleUrl.getUrl(beans.getImeadValue(SecurityRepo.BASE_URL), c.getArticleid(), "comments"));
            }
        }
        return entries;
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * will return Comments.rss and if on an article, Comments(articleID).rss
     *
     * @param req
     * @return
     */
    @Override
    public Map<String, String> getFeedURLs(HttpServletRequest req) {
        Article art = (Article) req.getAttribute(Article.class.getSimpleName());
        HashMap<String, String> output = new HashMap<>();
        output.put(getName(), "All Comments");
        if (null != art && null != art.getComments() && art.getComments()) {
            output.put("Comments" + art.getArticleid() + ".rss", art.getArticletitle() + " Comments");
        }
        return output;
    }

    /**
     *
     * @param name
     * @return if name matches Comments(.*?)//.rss
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
            String group = regex.find() ? regex.group(1) : null;
            if (null != group && group.isEmpty()) {
                group = null;
            }
            try {
                Document XML = createFeed(beans, group);
                DOMSource DOMsrc = new DOMSource(XML);
                StringWriter holder = new StringWriter(10000);
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
            } catch (TransformerException ex) {
                LOG.log(Level.SEVERE, "Coment feed will not be available due to an XML transformation error.", ex);
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
