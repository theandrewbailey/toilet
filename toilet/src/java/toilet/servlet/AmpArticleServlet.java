package toilet.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Base64;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.JspFilter;
import libOdyssey.bean.GuardRepo;
import libWebsiteTools.HashUtil;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.HtmlMeta;
import toilet.UtilStatic;
import toilet.bean.UtilBean;
import toilet.db.Article;
import toilet.tag.ArticleUrl;

@WebServlet(name = "AmpArticleServlet", description = "Gets a single article from the DB and serve in AMP form", urlPatterns = {"/amp/*"})
public class AmpArticleServlet extends ArticleServlet {

    private static final String ARTICLE_JSP = "/WEB-INF/ampArticle.jsp";

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

        String properUrl = ArticleUrl.getAmpUrl(imead.getValue(GuardRepo.CANONICAL_URL), art);
        String actual = request.getRequestURI();
        if (!properUrl.endsWith(actual)) {
            request.setAttribute(Article.class.getCanonicalName(), null);
            UtilStatic.permaMove(response, properUrl);
            return;
        }
        asyncRecentCategories(request, art.getSectionid().getName());
        request.setAttribute(JspFilter.CONTENT_SECURITY_POLICY, "default-src https: data: 'unsafe-inline'; object-src 'none'; frame-ancestors 'self'");
        String ifNoneMatch = request.getHeader("If-None-Match");
        String etag = cache.getEtag(request.getRequestURI());
        if (null == etag) {
            try {
                MessageDigest md = HashUtil.getSHA256();
                md.update(request.getSession().getId().getBytes("UTF-8"));
                md.update(art.getEtag().getBytes("UTF-8"));
                try {
                    md.update(imead.getLocal("page_cssamp", Local.resolveLocales(request)).getBytes("UTF-8"));
                } catch (UnsupportedEncodingException enc) {
                    throw new JVMNotSupportedError(enc);
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
            SimpleDateFormat htmlFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            request.setAttribute("art", art);
            request.setAttribute("title", art.getArticletitle());
            request.setAttribute("articleCategory", art.getSectionid().getName());
            request.setAttribute("canonical", ArticleUrl.getUrl(imead.getValue(GuardRepo.CANONICAL_URL), art));
            request.setAttribute("css", new String(file.getFile(imead.getLocal("page_cssamp", Local.resolveLocales(request))).getFiledata(), "UTF-8"));
            HtmlMeta.addTag(request, "description", art.getDescription());
            HtmlMeta.addTag(request, "author", art.getPostedname());
            HtmlMeta.addProperty(request, "og:title", art.getArticletitle());
            HtmlMeta.addProperty(request, "og:url", ArticleUrl.getUrl(imead.getValue(GuardRepo.CANONICAL_URL), art));
            if (null != art.getImageurl()) {
                HtmlMeta.addProperty(request, "og:image", art.getImageurl());
            }
            if (null != art.getDescription()) {
                HtmlMeta.addProperty(request, "og:description", art.getDescription());
            }
            HtmlMeta.addProperty(request, "og:site_name", imead.getLocal(UtilBean.SITE_TITLE, "en"));
            HtmlMeta.addProperty(request, "og:type", "article");
            HtmlMeta.addProperty(request, "og:article:published_time", htmlFormat.format(art.getPosted()));
            HtmlMeta.addProperty(request, "og:article:modified_time", htmlFormat.format(art.getModified()));
            HtmlMeta.addProperty(request, "og:article:author", art.getPostedname());
            HtmlMeta.addProperty(request, "og:article:section", art.getSectionid().getName());
            HtmlMeta.addLink(request, "canonical", ArticleUrl.getUrl(imead.getValue(GuardRepo.CANONICAL_URL), art));
            request.getServletContext().getRequestDispatcher(ARTICLE_JSP).forward(request, response);
        }
    }
}
