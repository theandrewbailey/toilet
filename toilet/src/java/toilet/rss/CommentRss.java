package toilet.rss;

import java.util.List;
import javax.ejb.EJB;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.Feed;
import libWebsiteTools.rss.entity.AbstractRssFeed;
import libWebsiteTools.rss.entity.RssChannel;
import libWebsiteTools.rss.entity.RssItem;
import org.w3c.dom.Document;
import toilet.UtilStatic;
import toilet.bean.EntryRepo;
import toilet.bean.UtilBean;
import toilet.db.Comment;
import toilet.tag.ArticleUrl;

@WebListener("The RSS feed for comments, autoadded")
@Feed(CommentRss.NAME)
public class CommentRss extends AbstractRssFeed {

    public static final String NAME = "Comments.rss";
    private static final String COMMENT_COUNT = "rss_commentCount";
    @EJB
    private EntryRepo entry;
    @EJB
    private IMEADHolder imead;
    private Document XML;

    public CommentRss() {
    }

    public Document generateFeed(Integer numEntries) {
        // if instantiated manually
        if (entry == null && imead == null) {
            entry = UtilStatic.getBean(EntryRepo.LOCAL_NAME, EntryRepo.class);
            imead = UtilStatic.getBean(UtilBean.IMEAD_LOCAL_NAME, IMEADHolder.class);
        }

        RssChannel entries = new RssChannel(imead.getValue(UtilBean.SITE_TITLE) + " - Comments", imead.getValue(UtilBean.THISURL), imead.getValue(UtilBean.TAGLINE));
        entries.setWebMaster(imead.getValue(UtilBean.MASTER));
        entries.setManagingEditor(entries.getWebMaster());
        entries.setLanguage(imead.getValue(UtilBean.LANGUAGE));

        List<Comment> lComments = entry.getCommentArchive(numEntries);

        for (Comment c : lComments) {
            RssItem i = new RssItem(c.getPostedhtml());
            i.addCategory(c.getArticleid().getSectionid().getName(), imead.getValue(UtilBean.THISURL) + "index/group=" + c.getArticleid().getSectionid().getName());
            i.setLink(ArticleUrl.getUrl(imead.getValue(UtilBean.THISURL), c.getArticleid()) + "#" + c.getCommentid());
            i.setGuid(i.getLink());
            i.setPubDate(c.getPosted());
            i.setTitle("Re: " + c.getArticleid().getArticletitle());
            i.setAuthor(c.getPostedname());
            if (c.getArticleid().getComments()) {
                i.setComments(ArticleUrl.getUrl(imead.getValue(UtilBean.THISURL), c.getArticleid()) + "#comments");
            }
            entries.addItem(i);
        }
        return refreshFeed(entries);
    }

    @Override
    public synchronized void preAdd() {
        XML = generateFeed(Integer.valueOf(imead.getValue(COMMENT_COUNT)));
    }

    @Override
    public Document preWrite(HttpServletRequest req) {
        return XML;
    }
}
