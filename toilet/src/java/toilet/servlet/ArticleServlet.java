package toilet.servlet;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.bean.SecurityRepo;
import libWebsiteTools.imead.Local;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.tag.AbstractInput;
import libWebsiteTools.tag.HtmlMeta;
import libWebsiteTools.tag.HtmlTime;
import toilet.ArticleProcessor;
import toilet.UtilStatic;
import toilet.bean.ArticleRepo;
import toilet.bean.UtilBean;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.db.Section;
import toilet.tag.ArticleUrl;

@WebServlet(name = "ArticleServlet", description = "Gets a single article from the DB with comments", urlPatterns = {"/article/*"})
public class ArticleServlet extends ToiletServlet {

    private static final Pattern ARTICLE_TERM = Pattern.compile("(.+?)(?=(?: \\d.*)|(?:[:,] .*)|(?: \\(\\d+\\))|$)");
    private static final String ARTICLE_JSP = "/WEB-INF/singleArticle.jsp";
    private static final String DEFAULT_NAME = "entry_defaultName";
    public static final String SPAM_WORDS = "entry_spamwords";

    @Override
    protected long getLastModified(HttpServletRequest request) {
        try {
            Article art = cache.getArticleFromURI(request.getRequestURI());
            request.setAttribute(Article.class.getCanonicalName(), art);
            return art.getModified().getTime();
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
        String properUrl = ArticleUrl.getUrl(imead.getValue(SecurityRepo.CANONICAL_URL), art, null, null);
        String actual = request.getRequestURI();
        if (!properUrl.endsWith(actual) && null == request.getAttribute("searchSuggestion")) {
            request.setAttribute(Article.class.getCanonicalName(), null);
            UtilStatic.permaMove(response, properUrl);
            return;
        }
        response.setDateHeader(HttpHeaders.DATE, art.getModified().getTime());
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

            request.setAttribute("seeAlsoTerm", getArticleSuggestionTerm(art));
            request.setAttribute("seeAlso", getArticleSuggestions(arts, art));

            if (art.getComments()) {
                request.setAttribute("commentForm", getcommentFormUrl(art, (Locale) request.getAttribute(Local.OVERRIDE_LOCALE_PARAM)));
                SimpleDateFormat timeFormat = new SimpleDateFormat(imead.getLocal(HtmlTime.SITE_DATEFORMAT_LONG, Local.resolveLocales(request, imead)));
                String footer = MessageFormat.format(imead.getLocal("page_articleFooter", Local.resolveLocales(request, imead)),
                        new Object[]{timeFormat.format(art.getPosted()), art.getSectionid().getName()})
                        + (1 == art.getCommentCollection().size() ? "1 comment." : art.getCommentCollection().size() + " comments.");
                request.setAttribute("commentFormTitle", footer);
            }
            HtmlMeta.addNameTag(request, "description", art.getDescription());
            HtmlMeta.addNameTag(request, "author", art.getPostedname());
            HtmlMeta.addPropertyTag(request, "og:title", art.getArticletitle());
            HtmlMeta.addPropertyTag(request, "og:url", ArticleUrl.getUrl(imead.getValue(SecurityRepo.CANONICAL_URL), art, (Locale) request.getAttribute(Local.OVERRIDE_LOCALE_PARAM), null));
            if (null != art.getImageurl()) {
                HtmlMeta.addPropertyTag(request, "og:image", art.getImageurl());
            }
            if (null != art.getDescription()) {
                HtmlMeta.addPropertyTag(request, "og:description", art.getDescription());
            }
            HtmlMeta.addPropertyTag(request, "og:site_name", imead.getLocal(UtilBean.SITE_TITLE, "en"));
            HtmlMeta.addPropertyTag(request, "og:type", "article");
            HtmlMeta.addPropertyTag(request, "og:article:published_time", htmlFormat.format(art.getPosted()));
            HtmlMeta.addPropertyTag(request, "og:article:modified_time", htmlFormat.format(art.getModified()));
            HtmlMeta.addPropertyTag(request, "og:article:author", art.getPostedname());
            HtmlMeta.addPropertyTag(request, "og:article:section", art.getSectionid().getName());
            HtmlMeta.addLink(request, "canonical", ArticleUrl.getUrl(imead.getValue(SecurityRepo.CANONICAL_URL), art, (Locale) request.getAttribute(Local.OVERRIDE_LOCALE_PARAM), null));
            HtmlMeta.addLink(request, "amphtml", ArticleUrl.getAmpUrl(imead.getValue(SecurityRepo.CANONICAL_URL), art, (Locale) request.getAttribute(Local.OVERRIDE_LOCALE_PARAM)));
            request.getServletContext().getRequestDispatcher(ARTICLE_JSP).forward(request, response);
        }
    }

    /**
     *
     * @param art Article to get an appropriate search term from
     * @return String suitable to pass to article search to retrieve similar
     * articles
     */
    public static String getArticleSuggestionTerm(Article art) {
        String term = art.getArticletitle();
        Matcher articleMatch = ARTICLE_TERM.matcher(term);
        if (articleMatch.find()) {
            term = articleMatch.group(1).trim();
        }
        return term.replaceAll(" ", "|");
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
            Collection<Article> seeAlso = new LinkedHashSet<>(arts.search(getArticleSuggestionTerm(art)));
            if (7 > seeAlso.size()) {
                seeAlso.addAll(arts.getSection(art.getSectionid().getName(), 1, 14));
            }
            seeAlso.remove(art);
            List<Article> temp = Arrays.asList(Arrays.copyOf(seeAlso.toArray(new Article[]{}), 6));
            seeAlso = new ArrayList(temp);
            seeAlso.removeAll(Collections.singleton(null));
            // sort articles without images last
            for (Article a : temp) {
                if (null == a.getImageurl()) {
                    seeAlso.remove(a);
                    seeAlso.add(a);
                }
            }
            if (!seeAlso.isEmpty()) {
                return seeAlso;
            }
        } catch (Exception x) {
        }
        return null;
    }

    public String getcommentFormUrl(Article art, Locale lang) {
        StringBuilder url = new StringBuilder(imead.getValue(SecurityRepo.CANONICAL_URL)).append("comments/").append(art.getArticleid()).append("?iframe");
        if (null != lang) {
            url.append("&lang=").append(lang.toLanguageTag());
        }
        return url.toString();
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
                c.setArticleid(new Article(id));
                comms.upsert(Arrays.asList(c));
                util.resetCommentFeed();
                request.getSession().setAttribute("LastPostedName", postName);
                doGet(request, response);
                break;
            case "article":     // created or edited article
                if (!AdminLoginServlet.ADDARTICLE.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))
                        && !AdminLoginServlet.EDITPOSTS.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    break;
                }
                Article art = updateArticleFromPage(request);
                if ("Preview".equals(request.getParameter("action"))) {
                    AdminArticle.displayArticleEdit(request, response, art);
                    return;
                } else if (!validator.reset(art.getArticletitle()).matches()
                        || !validator.reset(art.getDescription()).matches()
                        || !validator.reset(art.getPostedname()).matches()
                        || !validator.reset(art.getPostedmarkdown()).matches()
                        || !validator.reset(art.getSectionid().getName()).matches()) {
                    request.setAttribute(CoronerServlet.ERROR_MESSAGE_PARAM, imead.getLocal("page_patternMismatch", Local.resolveLocales(request, imead)));
                    AdminArticle.displayArticleEdit(request, response, art);
                    return;
                }
                art = arts.upsert(Arrays.asList(art)).get(0);
                response.sendRedirect(ArticleUrl.getUrl(imead.getValue(SecurityRepo.CANONICAL_URL), art, (Locale) request.getAttribute(Local.OVERRIDE_LOCALE_PARAM), null));
                request.getSession().removeAttribute(AdminArticle.LAST_ARTICLE_EDITED);
                exec.submit(() -> {
                    util.resetArticleFeed();
                    arts.refreshSearch();
                });
                break;
            default:
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                break;
        }
    }

    private Article updateArticleFromPage(HttpServletRequest req) {
        Article art = (Article) req.getSession().getAttribute(AdminArticle.LAST_ARTICLE_EDITED);
        boolean isNewArticle = null == art.getArticleid();
        if (isNewArticle) {
            int nextID = arts.count().intValue();
            art.setArticleid(++nextID);
        }
        art.setArticletitle(AbstractInput.getParameter(req, "articletitle"));
        art.setDescription(AbstractInput.getParameter(req, "description"));
        art.setSectionid(new Section(0, AbstractInput.getParameter(req, "section")));
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
