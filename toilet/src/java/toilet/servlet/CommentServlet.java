package toilet.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import libOdyssey.bean.GuardRepo;
import libWebsiteTools.tag.AbstractInput;
import libWebsiteTools.tag.HtmlMeta;
import toilet.UtilStatic;
import toilet.db.Article;
import toilet.db.Comment;

/**
 *
 * @author alpha
 */
@WebServlet(name = "CommentServlet", description = "Display article comments", urlPatterns = {"/comments/*"})
public class CommentServlet extends ToiletServlet {

    private static final String COMMENT_JSP = "/WEB-INF/comments.jsp";
    private static final String IFRAME_JSP = "/WEB-INF/commentsIframe.jsp";

    @Override
    protected long getLastModified(HttpServletRequest request) {
        boolean spamSuspected = (request.getSession(false) == null || request.getSession().isNew()) && request.getParameter("referer") == null;
        try {
            Article art = cache.getArticleFromURI(request.getRequestURI());
            request.setAttribute(Article.class.getCanonicalName(), art);
            return spamSuspected ? art.getModified().getTime() - 10000 : art.getModified().getTime();
        } catch (RuntimeException ex) {

        }
        return 0L;
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Article art = (Article) request.getAttribute(Article.class.getCanonicalName());
        if (null == art) {
            try {
                art = cache.getArticleFromURI(request.getRequestURI());
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
        asyncFiles(request);
        doHead(request, response);
        HtmlMeta.addTag(request, "robots", "noindex");
        Article art = (Article) request.getAttribute(Article.class.getCanonicalName());
        if (null != art) {
            request.setAttribute("art", art);
            request.setAttribute("title", art.getArticletitle());
            request.setAttribute("articleCategory", art.getSectionid().getName());
            request.setAttribute("commentIframe", imead.getValue(GuardRepo.CANONICAL_URL) + "comments/" + art.getArticleid() + (null == request.getParameter("iframe") ? "" : "?iframe"));
            request.getServletContext().getRequestDispatcher(null == request.getParameter("iframe") ? COMMENT_JSP : IFRAME_JSP).forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Matcher validator = UtilStatic.GENERAL_VALIDATION.matcher("");
        switch (AbstractInput.getParameter(request, "submit-type")) {
            case "comment":     // submitted comment
                String postName = AbstractInput.getParameter(request, "name");
                String postText = AbstractInput.getParameter(request, "text");
                if (null == postText || postText.isEmpty()
                        || null == postName || postName.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                postName = postName.trim();
                postText = UtilStatic.removeSpaces(postText);
                String referred = request.getHeader("Referer");
                if (request.getSession().isNew() || referred == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                String[] spamwords = imead.getValue(ArticleServlet.SPAM_WORDS).split("\n");
                for (String ua : spamwords) {
                    if (Pattern.matches(ua, postText.toLowerCase())) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                }

                Comment newComment = new Comment();
                newComment.setPostedhtml(UtilStatic.htmlFormat(postText, false, true));
                newComment.setPostedname(UtilStatic.htmlFormat(postName, false, false));
                if (!validator.reset(postName).matches()
                        || !validator.reset(postText).matches()
                        || newComment.getPostedname().length() > 250 || newComment.getPostedhtml().length() > 64000) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                Article art = cache.getArticleFromURI(request.getRequestURI());
                String postRequest = AbstractInput.getParameter(request, "original-request-time");
                if (null != postRequest) {
                    Date postRequestDate = new Date(Long.valueOf(postRequest));
                    for (Comment existingComment : art.getCommentCollection()) {
                        if ((existingComment.getPostedname().equals(newComment.getPostedname())) && existingComment.getPosted().after(postRequestDate)
                                || existingComment.getPostedhtml().equals(newComment.getPostedhtml())) {
                            response.setStatus(HttpServletResponse.SC_CONFLICT);
                            request.getSession().setAttribute("LastPostedName", postName);
                            request.setAttribute("commentText", postText);
                            doGet(request, response);
                            return;
                        }
                    }
                }

                HashMap<Comment, Integer> comments = new HashMap<>();
                comments.put(newComment, art.getArticleid());
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
