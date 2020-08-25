package toilet.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.HashUtil;
import libWebsiteTools.bean.SecurityRepo;
import libWebsiteTools.file.FileServlet;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.tag.AbstractInput;
import toilet.ArticleProcessor;
import toilet.UtilStatic;
import toilet.bean.StateCache;
import toilet.bean.ArticleRepo;
import toilet.db.Article;
import toilet.rss.CommentRss;

/**
 *
 * @author alpha
 */
@WebServlet(name = "adminArticle", description = "Administer articles (and sometimes comments)", urlPatterns = {"/adminArticle"})
public class AdminArticle extends ToiletServlet {

    public static final String LAST_ARTICLE_EDITED = "art";
    public static final String CREATE_NEW_GROUP = "$_CREATE_NEW_GROUP";
    @EJB
    private FeedBucket src;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String answer = request.getParameter("answer");
        if (answer != null && HashUtil.verifyArgon2Hash(imead.getValue(AdminLoginServlet.EDITPOSTS), answer)) {
            showList(request, response, arts.getAll(null));
        } else if (AdminLoginServlet.EDITPOSTS.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            if (request.getParameter("deletecomment") != null) {      // delete comment
                comms.delete(Integer.parseInt(request.getParameter("deletecomment")));
                src.get(CommentRss.NAME).preAdd();
                showList(request, response, arts.getAll(null));
                return;
            } else if (request.getParameter("editarticle") != null) {      // set up to edit article
                Article art = arts.get(Integer.parseInt(request.getParameter("editarticle")));
                displayArticleEdit(request, response, art);
                return;
            } else if (request.getParameter("disablecomments") != null) {
                List<Article> articles = new ArrayList<>();
                for (String id : request.getParameterValues(AbstractInput.getIncomingHash(request, "selectedArticle"))) {
                    Article art = arts.get(Integer.parseInt(id));
                    art.setComments(Boolean.FALSE);
                    articles.add(art);
                }
                arts.upsert(articles);
                util.resetEverything();
                response.sendRedirect(imead.getValue(SecurityRepo.CANONICAL_URL));
                exec.submit(() -> {
                    util.resetArticleFeed();
                    arts.refreshSearch();
                });
                return;
            } else if (request.getParameter("rewrite") != null) {
                file.processArchive((fileupload) -> {
                    fileupload.setUrl(FileServlet.getImmutableURL(imead.getValue(SecurityRepo.CANONICAL_URL), fileupload));
                }, true);
                Queue<Future<Article>> articleTasks = new ConcurrentLinkedQueue<>();
                for (String id : request.getParameterValues(AbstractInput.getIncomingHash(request, "selectedArticle"))) {
                    Article art = arts.get(Integer.parseInt(id));
                    art.setPostedhtml(null);
                    art.setPostedamp(null);
                    articleTasks.add(exec.submit(new ArticleProcessor(art, imead, file)));
                }
                List<Article> articles = new ArrayList<>(articleTasks.size());
                for (Future<Article> f : articleTasks) {
                    while (true) {
                        try {
                            if (articles.isEmpty() || null != f.get(1L, TimeUnit.MILLISECONDS)) {
                                articles.add(f.get());
                                break;
                            }
                        } catch (TimeoutException t) {
                            arts.upsert(articles);
                            articles.clear();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                arts.upsert(articles);
                util.resetEverything();
                response.sendRedirect(imead.getValue(SecurityRepo.CANONICAL_URL));
                exec.submit(() -> {
                    util.resetArticleFeed();
                    arts.refreshSearch();
                });
                return;
            }
            request.getRequestDispatcher("adminLogin?answer=" + imead.getValue(AdminLoginServlet.EDITPOSTS)).forward(request, response);
        }
    }

    public static void showList(HttpServletRequest request, HttpServletResponse response, Collection<Article> articles) throws ServletException, IOException {
        request.getSession().setAttribute(AdminLoginServlet.PERMISSION, AdminLoginServlet.EDITPOSTS);
        request.setAttribute("title", "Posts");
        request.setAttribute("articles", articles);
        request.getRequestDispatcher(AdminLoginServlet.ADMIN_EDIT_POSTS).forward(request, response);
    }

    public static void displayArticleEdit(HttpServletRequest request, HttpServletResponse response, Article art) throws ServletException, IOException {
        if (art.getCommentCollection() == null) {
            art.setCommentCollection(new ArrayList<>());
        }
        LinkedHashSet<String> groups = new LinkedHashSet<>();
        String defaultGroup = UtilStatic.getBean(IMEADHolder.LOCAL_NAME, IMEADHolder.class).getValue(ArticleRepo.DEFAULT_CATEGORY);
        groups.addAll(UtilStatic.getBean(StateCache.LOCAL_NAME, StateCache.class).getArticleCategories());
        request.setAttribute("groups", groups);
        if (null == art.getSectionid()) {
            groups.add(defaultGroup);
        } else if (!groups.add(art.getSectionid().getName())) {
            groups.add(art.getSectionid().getName());
        }
        request.setAttribute(LAST_ARTICLE_EDITED, art);
        request.getSession().setAttribute(LAST_ARTICLE_EDITED, art);
        request.getRequestDispatcher(AdminLoginServlet.ADMIN_ADD_ARTICLE).forward(request, response);
    }
}
