package toilet.rss;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import libWebsiteTools.rss.AbstractRssFeed;
import libWebsiteTools.rss.RssChannel;
import libWebsiteTools.rss.RssItem;
import libWebsiteTools.rss.RssServlet;
import libWebsiteTools.rss.SimpleRssFeed;
import libWebsiteTools.rss.iDynamicFeed;
import libWebsiteTools.rss.iFeed;
import org.w3c.dom.Document;
import toilet.bean.ToiletBeanAccess;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.servlet.ToiletServlet;
import toilet.tag.ArticleUrl;
import toilet.tag.Categorizer;

@WebListener("The RSS feed for comments, autoadded")
public class CommentRss extends AbstractRssFeed implements iDynamicFeed {

    public static final String NAME = "Comments.rss";
    private static final String COMMENT_COUNT = "rss_commentCount";
    private static final Logger LOG = Logger.getLogger(CommentRss.class.getName());
    private static final Pattern NAME_PATTERN = Pattern.compile("Comments(.*?)\\.rss");
    @EJB
    private ToiletBeanAccess beans;

    public CommentRss() {
    }

    public CommentRss(ToiletBeanAccess beans) {
        this.beans = beans;
    }

    public Document createFeed(Object articleId) {
        LOG.entering("CommentRss", "createFeed");
        RssChannel entries;
        if (null == articleId) {
            entries = createChannel(beans.getComms().getAll(Integer.valueOf(beans.getImeadValue(COMMENT_COUNT))));
        } else {
            Article art = beans.getArts().get(Integer.parseInt(articleId.toString()));
            List<Comment> lComments = new ArrayList<>(art.getCommentCollection());
            Collections.reverse(lComments);
            entries = createChannel(lComments);
            entries.setTitle(art.getArticletitle() + " - Comments");
            entries.setDescription("from " + beans.getImead().getLocal(ToiletServlet.SITE_TITLE, "en"));
        }
        LOG.exiting("CommentRss", "createFeed");
        return SimpleRssFeed.refreshFeed(Arrays.asList(entries));
    }

    public RssChannel createChannel(Collection<Comment> lComments) {
        RssChannel entries = new RssChannel(beans.getImead().getLocal(ToiletServlet.SITE_TITLE, "en") + " - Comments", beans.getImeadValue(SecurityRepo.BASE_URL), beans.getImead().getLocal(ToiletServlet.TAGLINE, "en"));
        entries.setWebMaster(beans.getImeadValue(AbstractRssFeed.MASTER));
        entries.setManagingEditor(entries.getWebMaster());
        entries.setLanguage(beans.getImeadValue(AbstractRssFeed.LANGUAGE));
        for (Comment c : lComments) {
            RssItem i = new RssItem(c.getPostedhtml());
            entries.addItem(i);
            i.addCategory(c.getArticleid().getSectionid().getName(), Categorizer.getUrl(beans.getImeadValue(SecurityRepo.BASE_URL), c.getArticleid().getSectionid().getName(), null, null));
            i.setLink(ArticleUrl.getUrl(beans.getImeadValue(SecurityRepo.BASE_URL), c.getArticleid(), null, "comments"));
            i.setGuid(HashUtil.getSHA256Hash(c.getPostedname() + c.getPosted().getTime() + c.getPostedhtml()));
            i.setPubDate(c.getPosted());
            i.setTitle(c.getArticleid().getArticletitle());
            i.setAuthor(c.getPostedname());
            if (c.getArticleid().getComments()) {
                i.setComments(ArticleUrl.getUrl(beans.getImeadValue(SecurityRepo.BASE_URL), c.getArticleid(), null, "comments"));
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
    public iFeed preAdd() {
        try {
            createFeed(null);
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
            String group = regex.find() ? regex.group(1) : null;
            if (null != group && group.isEmpty()) {
                group = null;
            }
            try {
                Document XML = createFeed(group);
                DOMSource DOMsrc = new DOMSource(XML);
                StringWriter holder = new StringWriter(10000);
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
