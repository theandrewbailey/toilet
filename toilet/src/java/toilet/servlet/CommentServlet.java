package toilet.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import libOdyssey.bean.GuardHolder;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.tag.AbstractInput;
import libWebsiteTools.tag.HtmlMeta;
import libWebsiteTools.tag.RequestToken;
import libWebsiteTools.token.RequestTokenBucket;
import toilet.UtilStatic;
import toilet.bean.EntryRepo;
import toilet.bean.StateCache;
import toilet.bean.UtilBean;
import toilet.db.Article;
import toilet.db.Comment;

/**
 *
 * @author alpha
 */
@WebServlet(name = "CommentServlet", description = "Display article comments", urlPatterns = {"/comments/*"})
public class CommentServlet extends HttpServlet {

    private static final String COMMENT_JSP = "/WEB-INF/comments.jsp";
    private static final String IFRAME_JSP = "/WEB-INF/commentsIframe.jsp";
    @EJB
    protected EntryRepo entry;
    @EJB
    protected StateCache cache;
    @EJB
    protected IMEADHolder imead;
    @EJB
    protected UtilBean util;

    @Override
    protected long getLastModified(HttpServletRequest request) {
        boolean spamSuspected = (request.getSession(false) == null || request.getSession().isNew()) && request.getParameter("referer") == null;
        try {
            Article art = cache.getEntry(request.getRequestURI());
            request.setAttribute(Article.class.getCanonicalName(), art);
            return spamSuspected ? art.getModified().getTime() - 10000 : art.getModified().getTime();
        } catch (RuntimeException ex) {

        }
        return 0L;
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Article art = (Article) request.getAttribute(Article.class.getCanonicalName());
        if (null == art) {
            try {
                art = cache.getEntry(request.getRequestURI());
                request.setAttribute(Article.class.getCanonicalName(), art);
            } catch (RuntimeException ex) {
                request.getServletContext().getRequestDispatcher("/coroner/30").forward(request, response);
                return;
            }
        }
        if (null == art) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        boolean spamSuspected = (request.getSession(false) == null || request.getSession().isNew()) && request.getParameter("referer") == null;
        request.setAttribute("spamSuspected", spamSuspected);

        if (!spamSuspected) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "private, must-revalidate, max-age=600");
            String ifNoneMatch = request.getHeader("If-None-Match");
            String etag = cache.getEtag(request.getRequestURI());
            etag = "\"" + etag + (spamSuspected ? "s" : "h") + "\"";
            response.setHeader("ETag", etag);
            if (etag.equals(ifNoneMatch)) {
                request.setAttribute(Article.class.getCanonicalName(), null);
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        } else {
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        }
        response.setDateHeader("Date", spamSuspected ? art.getModified().getTime() - 10000 : art.getModified().getTime());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHead(request, response);
        HtmlMeta.addTag(request, "robots", "noindex");
        Article art = (Article) request.getAttribute(Article.class.getCanonicalName());
        if (null != art) {
            request.setAttribute("art", art);
            request.setAttribute("title", art.getArticletitle());
            request.setAttribute("articleCategory", art.getSectionid().getName());
            request.setAttribute("commentIframe", imead.getValue(GuardHolder.CANONICAL_URL) + "comments/" + art.getArticleid() + (null == request.getParameter("iframe") ? "" : "?iframe"));
            request.getServletContext().getRequestDispatcher(null == request.getParameter("iframe") ? COMMENT_JSP : IFRAME_JSP).forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Matcher validator = UtilStatic.GENERAL_VALIDATION.matcher("");
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
                String[] spamwords = imead.getValue(ArticleServlet.SPAM_WORDS).split("\n");
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
                if (!validator.reset(postName).matches()
                        || !validator.reset(rawin).matches()
                        || c.getPostedname().length() > 250 || c.getPostedhtml().length() > 64000) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                Integer id = cache.getEntry(request.getRequestURI()).getArticleid();
                HashMap<Comment, Integer> comments = new HashMap<>();
                comments.put(c, id);
                entry.addComments(comments);
                util.resetCommentFeed();
                request.getSession().setAttribute("LastPostedName", postName);
                cache.clearEtags();
                doGet(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                break;
        }
    }
}
