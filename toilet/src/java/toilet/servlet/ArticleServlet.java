package toilet.servlet;

import com.lambdaworks.crypto.SCryptUtil;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.bean.Analyzer;
import libOdyssey.bean.GuardHolder;
import libOdyssey.bean.SessionBean;
import libOdyssey.db.Httpsession;
import libOdyssey.db.Pagerequest;
import libWebsiteTools.RequestTokenBucket;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.rss.iFeedBucket;
import libWebsiteTools.tag.RequestToken;
import toilet.UtilStatic;
import toilet.bean.EntryRepo;
import toilet.bean.UtilBean;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.rss.ArticleRss;
import toilet.rss.CommentRss;
import toilet.tag.ArticleUrl;

@WebServlet(name = "ArticleServlet", description = "Gets a single article from the DB with comments", urlPatterns = {"/article/*"})
public class ArticleServlet extends HttpServlet {

    public static final String WORDS = "admin_magicwords";
    public static final String EDITING = "entry_editing";
    private static final String DEFAULT_NAME = "entry_defaultName";
    private static final String HONEYPOTURL = "page_honeypot";
    private static final String COMMENT_DELAY = "page_commentPostDelay";
    private static final Logger log = Logger.getLogger(ArticleServlet.class.getName());
    @EJB
    private Analyzer anal;
    @EJB
    private EntryRepo entry;
    @EJB
    private iFeedBucket src;
    @EJB
    private IMEADHolder imead;
    @EJB
    private UtilBean util;
    @EJB
    private GuardHolder guard;
//    @EJB private SpruceBean spruce;

    @Override
    public void init() {
        // BIG RED BUTTON
//        if (entry.getAllEntries().size()<500){
//            Random r=new Random();
//            for (int x=0; x<500; x++){
//                Entry e=new Entry();
//                StringBuilder b=new StringBuilder(spruce.getAddSentence());
//                int sentences=r.nextInt(5);
//                for (int y=1; y<sentences+4; y++)
//                    b.append(" ").append(spruce.getAddSentence());
//                e.setEntrytext(b.toString());
//                e.setEntrytitle(spruce.getRandomWord());
//                e.setPostedname("Spruce");
//                e.setComments(true);
//                entry.addEntry(e, util.getValue(EntryBean.DEFAULT_CATEGORY), true);
//            }
//            List<Entry> entries=entry.getAllEntries();
//            for (int x=0; x<1000; x++){
//                Entrycomment c=new Entrycomment();
//                StringBuilder b=new StringBuilder(spruce.getAddSentence());
//                int sentences=r.nextInt(5);
//                for (int y=1; y<sentences; y++)
//                    b.append(" ").append(spruce.getAddSentence());
//                c.setPostedname("Spruce");
//                c.setEntrytext(spruce.getAddSentence());
//                int id=Math.abs(r.nextInt()%500);
//                System.out.println(id);
//                entry.addComment(entries.get(id).getEntryid(), c);
//            }
//        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Article e;
        try {
            e = getEntry(request.getRequestURI());
        } catch (RuntimeException ex) {
            response.sendError(30);
            return;
        }
        if (e == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String properUrl = ArticleUrl.getUrl(imead.getValue(UtilBean.THISURL), e);
        String actual = request.getRequestURL().toString();
        if (!actual.equals(properUrl)) {
            UtilStatic.permaMove(response, properUrl);
            return;
        }

        boolean spamSuspected = (request.getSession(false) == null || request.getSession().isNew()) && request.getParameter("referer") == null;
        request.setAttribute("spamSuspected", spamSuspected);

        String ifNoneMatch = request.getHeader("If-None-Match");
        String etag = "\"" + e.getEtag();
        etag += spamSuspected ? "\"" : request.getSession().getId() + "\"";

        if (etag.equals(ifNoneMatch)) {
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        response.setHeader("ETag", etag);

        e.setCommentCount(e.getCommentCollection().size());

        request.setAttribute("articles", new Article[]{e});
        request.setAttribute("title", e.getArticletitle());
        request.setAttribute("description", e.getDescription());
        request.setAttribute("author", e.getPostedname());
//        request.setAttribute("siteTitleHide", true);
        request.setAttribute("articleCategory", e.getSectionid().getName());
        request.getServletContext().getRequestDispatcher(IndexServlet.HOME_JSP).forward(request, response);
    }

    protected Article getEntry(String URI) {
        try {
            Integer entryId = new Integer(util.getIdFromURI(URI));
            return entry.getEntry(entryId);
        } catch (Exception x) {
            return null;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Article e;
        if (request.getParameter("submit-type").equals("comment")) {             // submitted comment
            if (request.getParameter("text") == null || request.getParameter("text").isEmpty()
                    || request.getParameter("name") == null || request.getParameter("name").isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                RequestTokenBucket.getRequestTokenBucket(request).addToken(request.getParameter(RequestToken.ID_NAME), request.getHeader("Referer"));
                return;
            }
            String referred = request.getHeader("Referer");
            if (request.getSession().isNew() || referred == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            boolean sufficentTime = false;
            Httpsession sess = anal.getSession(request.getSession().getId(), true);
            ArrayList<Pagerequest> pages = new ArrayList<Pagerequest>(sess.getPagerequestCollection());
            Collections.reverse(pages);
            referred = referred.substring(referred.indexOf(guard.getHostValue()) + guard.getHostValue().length());
            referred = SessionBean.getURL(request.getServletContext().getContextPath(), referred, null);
            for (Pagerequest p : pages) {
                try{
                    if (referred.equals(p.getCamefrompagerequestid().getPageid().getUrl())) {
                        sufficentTime = new Date().getTime() - p.getAtime().getTime() > Integer.valueOf(imead.getValue(COMMENT_DELAY));
                        break;
                    }
                } catch (NullPointerException n) {
                    // benefit of the doubt
                    sufficentTime = true;
                    log.info("Unable to retrieve when commenter requested original page, procedding to post comment.");
                    break;
                }
            }
            if (!sufficentTime) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                RequestTokenBucket.getRequestTokenBucket(request).addToken(request.getParameter(RequestToken.ID_NAME), request.getHeader("Referer"));
                return;
            }

            Comment c = new Comment();
            c.setPostedtext(UtilStatic.htmlFormat(UtilStatic.removeSpaces(request.getParameter("text")), false, true));
            String postName = request.getParameter("name");
            postName = postName.trim();
            c.setPostedname(UtilStatic.htmlFormat(postName, false, false));

            if (c.getPostedname().length() > 250 || c.getPostedtext().length() > 64000) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
//            if (sess.getCommentCollection().size() > 0) {
//                ArrayList<Comment> comments = new ArrayList<Comment>(sess.getCommentCollection());
//                if (new Date().getTime() - 10000 < comments.get(comments.size() - 1).getPosted().getTime()) {
//                    // reject
//                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
//                    return;
//                }
//            }
            Integer id = getEntry(request.getRequestURI()).getArticleid();

            entry.addComment(id, c);
            src.getFeed(CommentRss.NAME).preAdd();
            doGet(request, response);
        } else if (request.getParameter("submit-type").equals("edit")
                || request.getParameter("submit-type").equals("entry")) {          // edited article
            boolean adding = AdminServlet.ADDENTRY.equals(request.getSession().getAttribute("article"));
            boolean editing = EDITING.equals(request.getSession().getAttribute("article"));
            if (!(adding ^ editing)
                    || !SCryptUtil.check(request.getParameter("words"), imead.getValue(WORDS))) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            request.getSession().setAttribute("article", null);
            String date = request.getParameter("posted");

            e = new Article();
            e.setArticletitle(request.getParameter("name"));
            e.setDescription(request.getParameter("desc"));
            e.setPostedtext(UtilStatic.htmlFormat(request.getParameter("text"), true, true));
            e.setPostedname(imead.getValue(DEFAULT_NAME));
            if (date != null) {
                try {
                    e.setPosted(new SimpleDateFormat(FeedBucket.TIME_FORMAT).parse(date));
                } catch (ParseException p) {
                }
            }

            if (editing) {
                Article articleEditing = ((Article)request.getSession().getAttribute("art"));
                e.setArticleid(articleEditing.getArticleid());
            }
            if (request.getParameter("commentable") != null) {
                e.setComments(true);
            } else {
                e.setComments(false);
            }
            String sect = request.getParameter("Groupings");
            if (sect == null || sect.isEmpty()) {
                sect = request.getParameter("newGrouping");
            }
            if (sect == null || sect.isEmpty()) {
                sect = e.getSectionid().getName();
            }
            if (adding && (sect == null || sect.isEmpty())) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            e = entry.addEntry(e, sect);

//            util.updatePageTemplate();
            src.getFeed(ArticleRss.NAME).preAdd();
            response.sendRedirect(ArticleUrl.getUrl(imead.getValue(UtilBean.THISURL), e));
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
