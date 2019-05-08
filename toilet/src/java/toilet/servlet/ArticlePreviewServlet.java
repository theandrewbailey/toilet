package toilet.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.HashUtil;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.HtmlCss;
import toilet.db.Article;

/**
 *
 * @author alpha
 */
@WebServlet(name = "ArticlePreviewServlet", description = "Gets a single article from the DB with comments", urlPatterns = {"/articlePreview/*"})
public class ArticlePreviewServlet extends ToiletServlet {

    private static final String ARTICLE_JSP = "/WEB-INF/articleSummary.jsp";

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
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        if (null == art) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String ifNoneMatch = request.getHeader("If-None-Match");
        String etag = cache.getEtag(request.getRequestURI());
        if (null == etag) {
            try {
                MessageDigest md = HashUtil.getSHA256();
                md.update(request.getSession().getId().getBytes("UTF-8"));
                md.update(art.getEtag().getBytes("UTF-8"));
                try {
                    md.update(imead.getLocal(HtmlCss.PAGE_CSS_KEY, Local.resolveLocales(request)).getBytes("UTF-8"));
                } catch (UnsupportedEncodingException enc) {
                }
                etag = Base64.getEncoder().encodeToString(md.digest());
                cache.setEtag(request.getRequestURI(), etag);
            } catch (UnsupportedEncodingException enc) {
                throw new JVMNotSupportedError(enc);
            }
        }
        etag = "\"" + etag + "\"";
        response.setHeader("ETag", etag);
        if (etag.equals(ifNoneMatch)) {
            request.setAttribute(Article.class.getCanonicalName(), null);
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        response.setDateHeader("Date", art.getModified().getTime());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        asyncFiles(request);
        doHead(request, response);
        Article art = (Article) request.getAttribute(Article.class.getCanonicalName());
        if (null != art && !response.isCommitted()) {
            request.setAttribute("art", art);
            request.setAttribute("title", art.getArticletitle());
            request.getServletContext().getRequestDispatcher(ARTICLE_JSP).forward(request, response);
        }
    }

}
