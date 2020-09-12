package toilet.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.cache.JspFilter;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.HtmlMeta;
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
        String properUrl = ArticleUrl.getAmpUrl(imead.getValue(SecurityRepo.BASE_URL), art, (Locale) request.getAttribute(Local.OVERRIDE_LOCALE_PARAM));
        String actual = request.getRequestURI();
        if (!properUrl.endsWith(actual)) {
            request.setAttribute(Article.class.getCanonicalName(), null);
            ToiletServlet.permaMove(response, properUrl);
            return;
        }
        response.setDateHeader(HttpHeaders.DATE, art.getModified().getTime());
        request.setAttribute(JspFilter.CONTENT_SECURITY_POLICY, "default-src https: data: 'unsafe-inline'; object-src 'none'; frame-ancestors 'self'");
        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        String etag = request.getAttribute(HttpHeaders.ETAG).toString();
        if (etag.equals(ifNoneMatch)) {
            request.setAttribute(Article.class.getCanonicalName(), null);
            response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doHead(request, response);
        Article art = (Article) request.getAttribute(Article.class.getCanonicalName());
        if (null != art && !response.isCommitted()) {
            SimpleDateFormat htmlFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            request.setAttribute("art", art);
            request.setAttribute("title", art.getArticletitle());
            request.setAttribute("articleCategory", art.getSectionid().getName());
            request.setAttribute("canonical", ArticleUrl.getUrl(imead.getValue(SecurityRepo.BASE_URL), art, null, null));
            request.setAttribute("css", new String(file.get(imead.getLocal("site_cssamp", Local.resolveLocales(request, imead))).getFiledata(), "UTF-8"));
            HtmlMeta.addNameTag(request, "description", art.getDescription());
            HtmlMeta.addNameTag(request, "author", art.getPostedname());
            HtmlMeta.addPropertyTag(request, "og:title", art.getArticletitle());
            HtmlMeta.addPropertyTag(request, "og:url", ArticleUrl.getUrl(imead.getValue(SecurityRepo.BASE_URL), art, (Locale) request.getAttribute(Local.OVERRIDE_LOCALE_PARAM), null));
            if (null != art.getImageurl()) {
                HtmlMeta.addPropertyTag(request, "og:image", art.getImageurl());
            }
            if (null != art.getDescription()) {
                HtmlMeta.addPropertyTag(request, "og:description", art.getDescription());
            }
            HtmlMeta.addPropertyTag(request, "og:site_name", imead.getLocal(ToiletServlet.SITE_TITLE, "en"));
            HtmlMeta.addPropertyTag(request, "og:type", "article");
            HtmlMeta.addPropertyTag(request, "og:article:published_time", htmlFormat.format(art.getPosted()));
            HtmlMeta.addPropertyTag(request, "og:article:modified_time", htmlFormat.format(art.getModified()));
            HtmlMeta.addPropertyTag(request, "og:article:author", art.getPostedname());
            HtmlMeta.addPropertyTag(request, "og:article:section", art.getSectionid().getName());
            HtmlMeta.addLink(request, "canonical", ArticleUrl.getUrl(imead.getValue(SecurityRepo.BASE_URL), art, null, null));
            request.getServletContext().getRequestDispatcher(ARTICLE_JSP).forward(request, response);
        }
    }
}
