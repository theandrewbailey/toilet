package toilet.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.cache.CachedPage;
import libWebsiteTools.cache.PageCache;
import libWebsiteTools.cache.PageCaches;
import libWebsiteTools.imead.Local;
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
        response.setDateHeader(HttpHeaders.DATE, spamSuspected ? art.getModified().getTime() - 10000 : art.getModified().getTime());
        request.setAttribute("spamSuspected", spamSuspected);
        response.setHeader(HttpHeaders.CACHE_CONTROL, spamSuspected ? "no-cache" : "private, must-revalidate, max-age=600");
        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        try {
            MessageDigest md = HashUtil.getSHA256();
            md.update(request.getRequestURI().getBytes("UTF-8"));
            md.update(Local.getLocaleString(request, imead).getBytes("UTF-8"));
            md.update(imead.getLocalizedHash().getBytes("UTF-8"));
            md.update(request.getSession().getId().getBytes("UTF-8"));
            md.update(art.getEtag().getBytes("UTF-8"));
            md.update(spamSuspected ? "s".getBytes("UTF-8") : "h".getBytes("UTF-8"));
            String etag = "\"" + Base64.getEncoder().encodeToString(md.digest()) + "\"";
            response.setHeader(HttpHeaders.ETAG, etag);
            request.setAttribute(HttpHeaders.ETAG, etag);
            if (etag.equals(ifNoneMatch)) {
                request.setAttribute(Article.class.getCanonicalName(), null);
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            }
        } catch (UnsupportedEncodingException enc) {
            throw new JVMNotSupportedError(enc);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHead(request, response);
        HtmlMeta.addNameTag(request, "robots", "noindex");
        Article art = (Article) request.getAttribute(Article.class.getCanonicalName());
        if (null != art) {
            request.setAttribute("art", art);
            request.setAttribute("title", art.getArticletitle());
            request.setAttribute("articleCategory", art.getSectionid().getName());
            request.setAttribute("commentForm", request.getAttribute(SecurityRepo.BASE_URL).toString() + "comments/" + art.getArticleid() + (null == request.getParameter("iframe") ? "" : "?iframe"));
            request.getServletContext().getRequestDispatcher(null == request.getParameter("iframe") ? COMMENT_JSP : IFRAME_JSP).forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Matcher validator = AbstractInput.GENERAL_VALIDATION.matcher("");
        switch (AbstractInput.getParameter(request, "submit-type")) {
            case "comment":     // submitted comment
                String postName = AbstractInput.getParameter(request, "name");
                String postText = AbstractInput.getParameter(request, "text");
                if (null == postText || postText.isEmpty()
                        || null == postName || postName.isEmpty()) {
                    response.sendError(422, "Unprocessable Entity");
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
                // prevent posting from old page
                String postRequest = AbstractInput.getParameter(request, "original-request-time");
                if (null != postRequest) {
                    Date postRequestDate = new Date(Long.valueOf(postRequest) + 100);
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
                newComment.setArticleid(art);
                comms.upsert(Arrays.asList(newComment));
                request.getSession().setAttribute("LastPostedName", postName);
                exec.submit(() -> {
                    PageCache global = (PageCache) pageCacheProvider.getCacheManager().<String, CachedPage>getCache(PageCaches.DEFAULT_URI);
                    global.removeAll(global.searchLookups("/" + art.getArticleid().toString() + "/"));
                });
                doGet(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                break;
        }
    }
}
