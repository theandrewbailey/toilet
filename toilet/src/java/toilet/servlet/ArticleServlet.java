package toilet.servlet;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.DateTimeException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.imead.Local;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.tag.AbstractInput;
import libWebsiteTools.tag.HtmlMeta;
import libWebsiteTools.tag.HtmlTime;
import toilet.ArticleProcessor;
import toilet.IndexFetcher;
import toilet.UtilStatic;
import toilet.bean.ArticleRepo;
import toilet.bean.ToiletBeanAccess;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.db.Section;
import toilet.tag.ArticleUrl;
import toilet.tag.Categorizer;

@WebServlet(name = "ArticleServlet", description = "Gets a single article from the DB with comments", urlPatterns = {"/article/*", "/amp/*"})
public class ArticleServlet extends ToiletServlet {

    private static final String ARTICLE_JSP = "/WEB-INF/singleArticle.jsp";
    private static final String DEFAULT_NAME = "entry_defaultName";
    public static final String SPAM_WORDS = "entry_spamwords";

    @Override
    protected long getLastModified(HttpServletRequest request) {
        try {
            ToiletBeanAccess beans = allBeans.getInstance(request);
            Article art = IndexFetcher.getArticleFromURI(beans, request.getRequestURI());
            request.setAttribute(Article.class.getCanonicalName(), art);
            return art.getModified().toInstant().toEpochMilli();
        } catch (RuntimeException ex) {
        }
        return 0L;
    }

    @Override
    @SuppressWarnings("UnnecessaryReturnStatement")
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Article art = (Article) request.getAttribute(Article.class.getCanonicalName());
        if (null == art) {
            try {
                ToiletBeanAccess beans = allBeans.getInstance(request);
                art = IndexFetcher.getArticleFromURI(beans, request.getRequestURI());
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
        String properUrl = ArticleUrl.getUrl(request.getAttribute(SecurityRepo.BASE_URL).toString(), art, null);
        String actual = request.getAttribute(AbstractInput.ORIGINAL_REQUEST_URL).toString();
        if (!actual.contains(properUrl) && null == request.getAttribute("searchSuggestion")) {
            request.setAttribute(Article.class.getCanonicalName(), null);
            ToiletServlet.permaMove(response, properUrl);
            return;
        }
        response.setDateHeader(HttpHeaders.DATE, art.getModified().toInstant().toEpochMilli());
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
            ToiletBeanAccess beans = allBeans.getInstance(request);
            Collection<Article> seeAlso = getArticleSuggestions(beans.getArts(), art);
            request.setAttribute("seeAlso", seeAlso);
            request.setAttribute("seeAlsoTerm", null != art.getSuggestion() ? art.getSuggestion() : ArticleRepo.getArticleSuggestionTerm(art));
            // keep track of articles referenced on the page, to help de-duplicate links and maximize unique articles linked to
            ArrayList<Article> pageArticles = new ArrayList<>(Arrays.asList(art));
            pageArticles.addAll(seeAlso);
            request.setAttribute("articles", pageArticles);
            request.setAttribute(Article.class.getSimpleName(), art);
            request.setAttribute("title", art.getArticletitle());
            request.setAttribute("articleCategory", art.getSectionid().getName());
            if (art.getComments()) {
                request.setAttribute("commentForm", getCommentFormUrl(art, (Locale) request.getAttribute(Local.OVERRIDE_LOCALE_PARAM)));
                String format = beans.getImead().getLocal(HtmlTime.SITE_DATEFORMAT_LONG, Local.resolveLocales(beans.getImead(), request));
                DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern(format);
                String postedDate = timeFormat.format(art.getPosted().toZonedDateTime());
                String footer = MessageFormat.format(beans.getImead().getLocal("page_articleFooter", Local.resolveLocales(beans.getImead(), request)),
                        new Object[]{postedDate, art.getSectionid().getName()})
                        + (1 == art.getCommentCollection().size() ? "1 comment." : art.getCommentCollection().size() + " comments.");
                request.setAttribute("commentFormTitle", footer);
            }
            HtmlMeta.addNameTag(request, "description", art.getDescription());
            HtmlMeta.addNameTag(request, "author", art.getPostedname());
            HtmlMeta.addPropertyTag(request, "og:title", art.getArticletitle());
            HtmlMeta.addPropertyTag(request, "og:url", ArticleUrl.getUrl(request.getAttribute(SecurityRepo.BASE_URL).toString(), art, null));
            if (null != art.getImageurl()) {
                HtmlMeta.addPropertyTag(request, "og:image", art.getImageurl());
            }
            if (null != art.getDescription()) {
                HtmlMeta.addPropertyTag(request, "og:description", art.getDescription());
            }
            HtmlMeta.addPropertyTag(request, "og:site_name", beans.getImead().getLocal(ToiletServlet.SITE_TITLE, Local.resolveLocales(beans.getImead(), request)));
            HtmlMeta.addPropertyTag(request, "og:type", "article");
            HtmlMeta.addPropertyTag(request, "og:article:published_time", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(art.getPosted().toZonedDateTime()));
            HtmlMeta.addPropertyTag(request, "og:article:modified_time", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(art.getModified().toZonedDateTime()));
            HtmlMeta.addPropertyTag(request, "og:article:author", art.getPostedname());
            HtmlMeta.addPropertyTag(request, "og:article:section", art.getSectionid().getName());
            HtmlMeta.addLink(request, "canonical", ArticleUrl.getUrl(request.getAttribute(SecurityRepo.BASE_URL).toString(), art, null));
            if (null != art.getImageurl()) {
                JsonArrayBuilder image = Json.createArrayBuilder();
                image.add(art.getImageurl());
                JsonObjectBuilder article = Json.createObjectBuilder().add("@context", "https://schema.org").add("@type", "Article").
                        add("headline", art.getArticletitle()).add("image", image).
                        add("datePublished", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(art.getPosted().toZonedDateTime())).
                        add("dateModified", DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(art.getModified().toZonedDateTime()));
                HtmlMeta.addLDJSON(request, article.build().toString());
            }
            JsonArrayBuilder itemList = Json.createArrayBuilder();
            itemList.add(HtmlMeta.getLDBreadcrumb(beans.getImead().getLocal("page_title", Local.resolveLocales(beans.getImead(), request)), 1, request.getAttribute(SecurityRepo.BASE_URL).toString()));
            itemList.add(HtmlMeta.getLDBreadcrumb(art.getSectionid().getName(), 2, Categorizer.getUrl(request.getAttribute(SecurityRepo.BASE_URL).toString(), art.getSectionid().getName(), null)));
            itemList.add(HtmlMeta.getLDBreadcrumb(art.getArticletitle(), 3, ArticleUrl.getUrl(beans.getImeadValue(SecurityRepo.BASE_URL), art, null)));
            JsonObjectBuilder breadcrumbs = Json.createObjectBuilder().add("@context", "https://schema.org").add("@type", "BreadcrumbList").add("itemListElement", itemList);
            HtmlMeta.addLDJSON(request, breadcrumbs.build().toString());
            request.getServletContext().getRequestDispatcher(ARTICLE_JSP).forward(request, response);
        }
    }

    /**
     *
     * @param arts Article repository to get articles from
     * @param art get articles similar to these
     * @return up to 6 similar articles, or null if something exploded
     */
    @SuppressWarnings("unchecked")
    public static Collection<Article> getArticleSuggestions(ArticleRepo arts, Article art) {
        try {
            Collection<Article> seeAlso = new LinkedHashSet<>(arts.search(null != art.getSuggestion() ? art.getSuggestion() : ArticleRepo.getArticleSuggestionTerm(art)));
            if (7 > seeAlso.size()) {
                seeAlso.addAll(arts.getBySection(art.getSectionid().getName(), 1, 7, null));
            }
            if (7 > seeAlso.size()) {
                seeAlso.addAll(arts.getBySection(null, 1, 7, null));
            }
            seeAlso.remove(art);
            List<Article> temp = Arrays.asList(Arrays.copyOf(seeAlso.toArray(new Article[]{}), 6));
            seeAlso = new ArrayList(temp);
            seeAlso.removeAll(Collections.singleton(null));
            // sort articles without images last
            for (Article a : temp) {
                if (null == a) {
                    break;
                } else if (null == a.getImageurl()) {
                    seeAlso.remove(a);
                    seeAlso.add(a);
                }
            }
            if (!seeAlso.isEmpty()) {
                return seeAlso;
            }
        } catch (Exception x) {
        }
        return new ArrayList<>();
    }

    public String getCommentFormUrl(Article art, Locale lang) {
        @SuppressWarnings("ReplaceStringBufferByString")
        StringBuilder url = new StringBuilder("comments/").append(art.getArticleid()).append("?iframe");
        return url.toString();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Matcher validator = AbstractInput.DEFAULT_REGEXP.matcher("");
        ToiletBeanAccess beans = allBeans.getInstance(request);
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
                String[] spamwords = beans.getImeadValue(SPAM_WORDS).split("\n");
                for (String ua : spamwords) {
                    if (Pattern.matches(ua, totest)) {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                }
                Comment c = new Comment();
                c.setPostedhtml(UtilStatic.htmlFormat(UtilStatic.removeSpaces(rawin), false, true, true));
                String postName = AbstractInput.getParameter(request, "name");
                postName = postName.trim();
                c.setPostedname(UtilStatic.htmlFormat(postName, false, false, true));
                if (!validator.reset(postName).matches()
                        || !validator.reset(rawin).matches()
                        || c.getPostedname().length() > 250 || c.getPostedhtml().length() > 64000) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                Integer id = IndexFetcher.getArticleFromURI(beans, request.getRequestURI()).getArticleid();
                c.setArticleid(new Article(id));
                beans.getComms().upsert(Arrays.asList(c));
                request.getSession().setAttribute("LastPostedName", postName);
                doGet(request, response);
                break;
            case "article":     // created or edited article
                if (!AdminLoginServlet.ADD_ARTICLE.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))
                        && !AdminLoginServlet.EDIT_POSTS.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    break;
                }
                Article art = updateArticleFromPage(request);
                if ("Preview".equals(request.getParameter("action"))) {
                    AdminArticleServlet.displayArticleEdit(beans, request, response, art);
                    return;
                } else if (!validator.reset(art.getArticletitle()).matches()
                        || !validator.reset(art.getDescription()).matches()
                        || !validator.reset(art.getPostedname()).matches()
                        || !validator.reset(art.getPostedmarkdown()).matches()
                        || !validator.reset(art.getSectionid().getName()).matches()) {
                    request.setAttribute(CoronerServlet.ERROR_MESSAGE_PARAM, beans.getImead().getLocal("page_patternMismatch", Local.resolveLocales(beans.getImead(), request)));
                    AdminArticleServlet.displayArticleEdit(beans, request, response, art);
                    return;
                }
                art = beans.getArts().upsert(Arrays.asList(art)).get(0);
                beans.reset();
                response.sendRedirect(ArticleUrl.getUrl(request.getAttribute(SecurityRepo.BASE_URL).toString(), art, null));
                request.getSession().removeAttribute(Article.class.getSimpleName());
                beans.getExec().submit(() -> {
                    beans.getArts().refreshSearch();
                });
                break;
            default:
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                break;
        }
    }

    private Article updateArticleFromPage(HttpServletRequest req) {
        ToiletBeanAccess beans = allBeans.getInstance(req);
        Article art = (Article) req.getSession().getAttribute(Article.class.getSimpleName());
        boolean isNewArticle = null == art.getArticleid();
        if (isNewArticle) {
            int nextID = beans.getArts().count().intValue();
            art.setArticleid(++nextID);
            req.setAttribute("isNewArticle", true);
        } else {
            req.setAttribute("isNewArticle", false);
        }
        art.setArticletitle(AbstractInput.getParameter(req, "articletitle"));
        art.setDescription(AbstractInput.getParameter(req, "description"));
        art.setSectionid(new Section(0, AbstractInput.getParameter(req, "section")));
        art.setPostedname(AbstractInput.getParameter(req, "postedname") == null || AbstractInput.getParameter(req, "postedname").isEmpty()
                ? beans.getImeadValue(DEFAULT_NAME)
                : AbstractInput.getParameter(req, "postedname"));
        String date = AbstractInput.getParameter(req, "posted");
        if (date != null) {
            try {
                art.setPosted(FeedBucket.parseTimeFormat(DateTimeFormatter.ISO_OFFSET_DATE_TIME, date));
            } catch (DateTimeException p) {
                art.setPosted(OffsetDateTime.now());
            }
        }
        art.setComments(AbstractInput.getParameter(req, "comments") != null);
        art.setPostedmarkdown(AbstractInput.getParameter(req, "postedmarkdown"));
        String suggestion = AbstractInput.getParameter(req, "suggestion");
        if (null != suggestion && suggestion.length() > 0) {
            art.setSuggestion(suggestion);
        } else {
            art.setSuggestion(null);
        }
        try {
            art.setImageurl(null);
            return new ArticleProcessor(beans, ArticleProcessor.convert(art)).call();
        } finally {
            if (isNewArticle) {
                art.setArticleid(null);
            }
        }
    }
}
