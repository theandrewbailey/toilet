package toilet.servlet;

import com.lambdaworks.crypto.SCryptUtil;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.bean.GuardHolder;
import libWebsiteTools.Markdowner;
import libWebsiteTools.token.RequestTokenBucket;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.tag.AbstractInput;
import libWebsiteTools.tag.HtmlMeta;
import libWebsiteTools.tag.RequestToken;
import toilet.UtilStatic;
import toilet.bean.EntryRepo;
import toilet.bean.StateCache;
import toilet.bean.UtilBean;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.db.Section;
import toilet.tag.ArticleUrl;

@WebServlet(name = "ArticleServlet", description = "Gets a single article from the DB with comments", urlPatterns = {"/article/*"})
public class ArticleServlet extends HttpServlet {

    public static final String WORDS = "admin_magicwords";
    private static final String DEFAULT_NAME = "entry_defaultName";
    private static final String SPAM_WORDS = "site_spamwords";
    @EJB
    private EntryRepo entry;
    @EJB
    private IMEADHolder imead;
    @EJB
    private UtilBean util;
    @EJB
    private Markdowner markdown;

    @Override
    public void init() {
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

        String properUrl = ArticleUrl.getUrl(imead.getValue(GuardHolder.CANONICAL_URL), e);
        String actual = request.getRequestURL().toString();
        if (!actual.endsWith(properUrl)) {
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
        request.setAttribute("articles", new Article[]{e});
        request.setAttribute("title", e.getArticletitle());
        request.setAttribute("articleCategory", e.getSectionid().getName());
        request.setAttribute("singleArticle", true);
        HtmlMeta.addTag(request, "description", e.getDescription());
        HtmlMeta.addTag(request, "author", e.getPostedname());
        request.getServletContext().getRequestDispatcher(IndexServlet.HOME_JSP).forward(request, response);
    }

    protected Article getEntry(String URI) {
        try {
            Integer entryId = new Integer(StateCache.getArticleIdFromURI(URI));
            return entry.getArticle(entryId);
        } catch (Exception x) {
            return null;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        switch (AbstractInput.getParameter(request, "submit-type")) {
            case "comment":     // submitted comment
                if (AbstractInput.getParameter(request, "text") == null || AbstractInput.getParameter(request, "text").isEmpty()
                        || AbstractInput.getParameter(request, "name") == null || AbstractInput.getParameter(request, "name").isEmpty()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    RequestTokenBucket.getRequestTokenBucket(request).addToken(AbstractInput.getParameter(request, RequestToken.ID_NAME), request.getHeader("Referer"));
                    return;
                }
                String referred = request.getHeader("Referer");
                if (request.getSession().isNew() || referred == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                String rawin = AbstractInput.getParameter(request, "text");

                String totest = rawin.toLowerCase();
                String[] spamwords = imead.getValue(SPAM_WORDS).split("\n");
                for (String ua : spamwords) {
                    if (Pattern.matches(ua, totest)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                }

                Comment c = new Comment();
                c.setPostedhtml(UtilStatic.htmlFormat(UtilStatic.removeSpaces(rawin), false, true));
                String postName = AbstractInput.getParameter(request, "name");
                postName = postName.trim();
                c.setPostedname(UtilStatic.htmlFormat(postName, false, false));
                if (c.getPostedname().length() > 250 || c.getPostedhtml().length() > 64000) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                Integer id = getEntry(request.getRequestURI()).getArticleid();
                HashMap<Comment, Integer> comments = new HashMap<>();
                comments.put(c, id);
                entry.addComments(comments);
                util.resetCommentFeed();
                request.getSession().setAttribute("LastPostedName", postName);
                doGet(request, response);
                break;
            case "article":     // created or edited article
                Article art = updateArticleFromPage(request);
                String sect = request.getParameter("section");
                if (sect == null || sect.isEmpty()) {
                    sect = request.getParameter("newsection");
                }
                if (request.getParameter("action").equals("Preview")) {
                    art.setSectionid(new Section(0, sect));
                    AdminPost.displayArticleEdit(request, response, art);
                    return;
                } else if (!SCryptUtil.check(AbstractInput.getParameter(request, "words"), imead.getValue(WORDS))) {
                    request.setAttribute("mess", imead.getLocal(CoronerServlet.CORONER_PREFIX + "500", Local.resolveLocales(request)));
                    art.setSectionid(new Section(0, sect));
                    AdminPost.displayArticleEdit(request, response, art);
                    return;
                }
                HashMap<Article, String> articles = new HashMap<>();
                articles.put(art, sect);
                art = entry.addArticles(articles);
                util.resetArticleFeed();
                response.sendRedirect(ArticleUrl.getUrl(imead.getValue(GuardHolder.CANONICAL_URL), art));
                break;
            default:
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                break;
        }
    }

    private Article updateArticleFromPage(HttpServletRequest req) {
        Article art = (Article) req.getSession().getAttribute("art");
        art.setArticletitle(AbstractInput.getParameter(req, "articletitle"));
        art.setDescription(AbstractInput.getParameter(req, "description"));
        art.setPostedname(AbstractInput.getParameter(req, "postedname") == null || AbstractInput.getParameter(req, "postedname").isEmpty() ? imead.getValue(DEFAULT_NAME) : AbstractInput.getParameter(req, "postedname"));
        String date = AbstractInput.getParameter(req, "posted");
        if (date != null) {
            try {
                art.setPosted(new SimpleDateFormat(FeedBucket.TIME_FORMAT).parse(date));
            } catch (ParseException p) {
                art.setPosted(new Date());
            }
        }
        art.setComments(AbstractInput.getParameter(req, "comments") != null);
        art.setPostedmarkdown(AbstractInput.getParameter(req, "postedmarkdown"));
        art.setPostedhtml(markdown.getHtml(AbstractInput.getParameter(req, "postedmarkdown")));
        return art;
    }
}
