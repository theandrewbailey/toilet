package toilet.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.bean.GuardRepo;
import libWebsiteTools.HashUtil;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.imead.Local;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.tag.AbstractInput;
import libWebsiteTools.tag.HtmlCss;
import libWebsiteTools.tag.HtmlMeta;
import toilet.ArticleProcessor;
import toilet.UtilStatic;
import toilet.bean.UtilBean;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.db.Section;
import toilet.tag.ArticleUrl;

@WebServlet(name = "ArticleServlet", description = "Gets a single article from the DB with comments", urlPatterns = {"/article/*"})
public class ArticleServlet extends ToiletServlet {

    private static final String ARTICLE_JSP = "/WEB-INF/singleArticle.jsp";
    private static final String DEFAULT_NAME = "entry_defaultName";
    public static final String WORDS = "admin_magicwords";
    public static final String SPAM_WORDS = "site_spamwords";

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

        String properUrl = ArticleUrl.getUrl(imead.getValue(GuardRepo.CANONICAL_URL), art);
        String actual = request.getRequestURI();
        if (!properUrl.endsWith(actual)) {
            request.setAttribute(Article.class.getCanonicalName(), null);
            UtilStatic.permaMove(response, properUrl);
            return;
        }

        asyncRecentCategories(request, art.getSectionid().getName());
        boolean spamSuspected = (request.getSession(false) == null || request.getSession().isNew()) && request.getParameter("referer") == null;
        request.setAttribute("spamSuspected", spamSuspected);

        if (!spamSuspected) {
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
            etag = "\"" + etag + (spamSuspected ? "s" : "h") + "\"";
            response.setHeader("ETag", etag);
            if (etag.equals(ifNoneMatch)) {
                request.setAttribute(Article.class.getCanonicalName(), null);
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }
        response.setDateHeader("Date", spamSuspected ? art.getModified().getTime() - 10000 : art.getModified().getTime());
        //response.setHeader(HttpHeaders.CACHE_CONTROL, "public, max-age=100000, immutable");
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
            if (art.getComments()) {
                request.setAttribute("commentIframe", imead.getValue(GuardRepo.CANONICAL_URL) + "comments/" + art.getArticleid() + "?iframe");
            }
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
            HtmlMeta.addLink(request, "amphtml", ArticleUrl.getAmpUrl(imead.getValue(GuardRepo.CANONICAL_URL), art));
            request.getServletContext().getRequestDispatcher(ARTICLE_JSP).forward(request, response);
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
                if (!validator.reset(postName).matches()
                        || !validator.reset(rawin).matches()
                        || c.getPostedname().length() > 250 || c.getPostedhtml().length() > 64000) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                Integer id = cache.getArticleFromURI(request.getRequestURI()).getArticleid();
                HashMap<Comment, Integer> comments = new HashMap<>();
                comments.put(c, id);
                entry.addComments(comments);
                util.resetCommentFeed();
                request.getSession().setAttribute("LastPostedName", postName);
                cache.clearEtags();
                doGet(request, response);
                break;
            case "article":     // created or edited article
                Article art = updateArticleFromPage(request);
                String sect = AbstractInput.getParameter(request, "section");
                if (AdminPost.CREATE_NEW_GROUP.equals(sect)) {
                    sect = AbstractInput.getParameter(request, "newsection");
                }
                if ("Preview".equals(request.getParameter("action"))) {
                    art.setSectionid(new Section(0, sect));
                    AdminPost.displayArticleEdit(request, response, art);
                    return;
                } else if (!imead.verifyArgon2(AbstractInput.getParameter(request, "words"), WORDS)) {
                    request.setAttribute("mess", imead.getLocal(CoronerServlet.CORONER_PREFIX + "500", Local.resolveLocales(request)));
                    art.setSectionid(new Section(0, sect));
                    AdminPost.displayArticleEdit(request, response, art);
                    return;
                } else if (!validator.reset(art.getArticletitle()).matches()
                        || !validator.reset(art.getDescription()).matches()
                        || !validator.reset(art.getPostedname()).matches()
                        || !validator.reset(art.getPostedmarkdown()).matches()
                        || !validator.reset(sect).matches()) {
                    request.setAttribute("mess", imead.getLocal("page_patternMismatch", Local.resolveLocales(request)));
                    AdminPost.displayArticleEdit(request, response, art);
                    return;
                }
                HashMap<Article, String> articles = new HashMap<>();
                articles.put(art, sect);
                art = entry.addArticles(articles);
                util.resetArticleFeed();
                cache.clearEtags();
                response.sendRedirect(ArticleUrl.getUrl(imead.getValue(GuardRepo.CANONICAL_URL), art));
                break;
            default:
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                break;
        }
    }

    private Article updateArticleFromPage(HttpServletRequest req) {
        Article art = (Article) req.getSession().getAttribute(AdminPost.LAST_ARTICLE_EDITED);
        boolean isNewArticle = null == art.getArticleid();
        if (isNewArticle) {
            int nextID = entry.countArticles(null).intValue();
            art.setArticleid(++nextID);
        }
        art.setArticletitle(AbstractInput.getParameter(req, "articletitle"));
        art.setDescription(AbstractInput.getParameter(req, "description"));
        art.setPostedname(AbstractInput.getParameter(req, "postedname") == null || AbstractInput.getParameter(req, "postedname").isEmpty()
                ? imead.getValue(DEFAULT_NAME)
                : AbstractInput.getParameter(req, "postedname"));
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
        try {
            return new ArticleProcessor(ArticleProcessor.convert(art), imead, file).call();
        } finally {
            if (isNewArticle) {
                art.setArticleid(null);
            }
        }
    }
}
