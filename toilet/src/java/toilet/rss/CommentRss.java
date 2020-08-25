package toilet.rss;

import java.io.StringWriter;
import java.util.ArrayList;
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
import libWebsiteTools.bean.SecurityRepo;
import libWebsiteTools.HashUtil;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.RssChannel;
import libWebsiteTools.rss.RssItem;
import libWebsiteTools.rss.RssServlet;
import libWebsiteTools.rss.SimpleRssFeed;
import libWebsiteTools.rss.iDynamicFeed;
import libWebsiteTools.rss.iFeed;
import org.w3c.dom.Document;
import toilet.UtilStatic;
import toilet.bean.ArticleRepo;
import toilet.bean.CommentRepo;
import toilet.bean.UtilBean;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.tag.ArticleUrl;

@WebListener("The RSS feed for comments, autoadded")
public class CommentRss extends SimpleRssFeed implements iDynamicFeed {

    public static final String NAME = "Comments.rss";
    private static final String COMMENT_COUNT = "rss_commentCount";
    private static final Logger LOG = Logger.getLogger(CommentRss.class.getName());
    private static final Pattern NAME_PATTERN = Pattern.compile("Comments(.*?)\\.rss");
    @EJB
    private ArticleRepo arts;
    @EJB
    private CommentRepo comms;
    @EJB
    private IMEADHolder imead;

    public CommentRss() {
    }

    public Document createFeed(Integer numEntries, Object articleId) {
        LOG.entering("CommentRss", "createFeed");
        // if instantiated manually
        if (comms == null && imead == null) {
            arts = UtilStatic.getBean(ArticleRepo.LOCAL_NAME, ArticleRepo.class);
            comms = UtilStatic.getBean(CommentRepo.LOCAL_NAME, CommentRepo.class);
            imead = UtilStatic.getBean(UtilBean.IMEAD_LOCAL_NAME, IMEADHolder.class);
        }

        RssChannel entries = new RssChannel(imead.getLocal(UtilBean.SITE_TITLE, "en") + " - Comments", imead.getValue(SecurityRepo.CANONICAL_URL), imead.getLocal(UtilBean.TAGLINE, "en"));
        entries.setWebMaster(imead.getValue(UtilBean.MASTER));
        entries.setManagingEditor(entries.getWebMaster());
        entries.setLanguage(imead.getValue(UtilBean.LANGUAGE));

        Collection<Comment> lComments;
        if (null == articleId) {
            lComments = comms.getAll(numEntries);
        } else {
            Article art = arts.get(Integer.parseInt(articleId.toString()));
            List<Comment> temp = new ArrayList<>(art.getCommentCollection());
            Collections.reverse(temp);
            lComments = temp;
            entries.setTitle(art.getArticletitle()+" - Comments");
            entries.setDescription("from " + imead.getLocal(UtilBean.SITE_TITLE, "en"));
        }
        for (Comment c : lComments) {
            RssItem i = new RssItem(c.getPostedhtml());
            entries.addItem(i);
            i.addCategory(c.getArticleid().getSectionid().getName(), imead.getValue(SecurityRepo.CANONICAL_URL) + "index/group=" + c.getArticleid().getSectionid().getName());
            i.setLink(ArticleUrl.getUrl(imead.getValue(SecurityRepo.CANONICAL_URL), c.getArticleid(), null, "comments"));
            i.setGuid(i.getLink());
            i.setPubDate(c.getPosted());
            i.setTitle(c.getArticleid().getArticletitle());
            i.setAuthor(c.getPostedname());
            if (c.getArticleid().getComments()) {
                i.setComments(ArticleUrl.getUrl(imead.getValue(SecurityRepo.CANONICAL_URL), c.getArticleid(), null, "comments"));
            }
        }
        LOG.exiting("CommentRss", "createFeed");
        return refreshFeed(entries);
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
        Article art = (Article) req.getAttribute("art");
        HashMap<String, String> output = new HashMap<>();
        output.put(getName(), "Comments");
        if (null != art) {
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
            createFeed(Integer.valueOf(imead.getValue(COMMENT_COUNT)), null);
        } catch (NumberFormatException n) {
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
                Document XML = createFeed(Integer.valueOf(imead.getValue(COMMENT_COUNT)), group);
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
