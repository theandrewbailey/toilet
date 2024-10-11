package toilet.servlet;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.file.BaseFileServlet;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.tag.AbstractInput;
import toilet.ArticleProcessor;
import toilet.bean.ToiletBeanAccess;
import toilet.bean.ArticleRepository;
import toilet.bean.BackupDaemon;
import toilet.db.Article;
import toilet.db.Section;

/**
 *
 * @author alpha
 */
@WebServlet(name = "adminArticle", description = "Administer articles (and sometimes comments)", urlPatterns = {"/adminArticle"})
public class AdminArticleServlet extends ToiletServlet {

    public static final String CREATE_NEW_GROUP = "$_CREATE_NEW_GROUP";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (AdminLoginServlet.EDIT_POSTS.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            ToiletBeanAccess beans = allBeans.getInstance(request);
            Article art = new Article();
            AdminArticleServlet.displayArticleEdit(beans, request, response, art);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ToiletBeanAccess beans = allBeans.getInstance(request);
        String answer = request.getParameter("answer");
        if (answer != null && HashUtil.verifyArgon2Hash(beans.getImeadValue(AdminLoginServlet.EDIT_POSTS), answer)) {
            showList(request, response, beans.getArts().getAll(null));
        } else if (AdminLoginServlet.EDIT_POSTS.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            if (request.getParameter("deletecomment") != null) {      // delete comment
                beans.getComms().delete(Integer.parseInt(request.getParameter("deletecomment")));
                beans.reset();
                showList(request, response, beans.getArts().getAll(null));
                return;
            } else if (request.getParameter("editarticle") != null) {      // set up to edit article
                Article art = beans.getArts().get(Integer.parseInt(request.getParameter("editarticle")));
                displayArticleEdit(beans, request, response, art);
                return;
            } else if (request.getParameter("disablecomments") != null) {
                List<Article> articles = new ArrayList<>();
                for (String id : request.getParameterValues(AbstractInput.getIncomingHash(request, "selectedArticle"))) {
                    Article art = beans.getArts().get(Integer.parseInt(id));
                    art.setComments(Boolean.FALSE);
                    articles.add(art);
                }
                beans.getArts().upsert(articles);
                beans.reset();
                response.sendRedirect(request.getAttribute(SecurityRepo.BASE_URL).toString());
                return;
            } else if (request.getParameter("rewrite") != null) {
                List<Fileupload> files = Collections.synchronizedList(new ArrayList<>(BackupDaemon.PROCESSING_CHUNK_SIZE * 2));
                beans.getFile().processArchive((file) -> {
                    String url = BaseFileServlet.getImmutableURL(beans.getImeadValue(SecurityRepo.BASE_URL), file);
                    if (!url.equals(file.getUrl())) {
                        file.setUrl(url);
                        files.add(file);
                    }
                    synchronized (files) {
                        if (files.size() > BackupDaemon.PROCESSING_CHUNK_SIZE) {
                            final List<Fileupload> fileChunk = new ArrayList<>(files);
                            beans.getFile().upsert(fileChunk);
                            files.clear();
                        }
                    }
                }, false);
                if (!files.isEmpty()) {
                    beans.getFile().upsert(files);
                }
                try {
                    Queue<Future<Article>> articleTasks = new ConcurrentLinkedQueue<>();
                    for (String id : request.getParameterValues(AbstractInput.getIncomingHash(request, "selectedArticle"))) {
                        Article art = beans.getArts().get(Integer.parseInt(id));
                        art.setPostedhtml(null);
                        art.setImageurl(null);
                        articleTasks.add(beans.getExec().submit(new ArticleProcessor(beans, art)));
                    }
                    beans.getExec().submit(() -> {
                        List<Article> articles = new ArrayList<>(articleTasks.size());
                        for (Future<Article> f : articleTasks) {
                            while (true) {
                                try {
                                    if (articles.isEmpty() || null != f.get(1L, TimeUnit.MILLISECONDS)) {
                                        articles.add(f.get());
                                        break;
                                    }
                                } catch (TimeoutException t) {
                                    beans.getArts().upsert(articles);
                                    articles.clear();
                                } catch (InterruptedException | ExecutionException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        beans.getArts().upsert(articles);
                        beans.reset();
                    });
                } catch (NullPointerException n) {
                }
                beans.getGlobalCache().clear();
                response.sendRedirect(request.getAttribute(SecurityRepo.BASE_URL).toString());
                return;
            }
            request.getRequestDispatcher("adminLogin?answer=" + beans.getImeadValue(AdminLoginServlet.EDIT_POSTS)).forward(request, response);
        }
    }

    public static void showList(HttpServletRequest request, HttpServletResponse response, Collection<Article> articles) throws ServletException, IOException {
        request.getSession().setAttribute(AdminLoginServlet.PERMISSION, AdminLoginServlet.EDIT_POSTS);
        request.setAttribute("title", "Posts");
        request.setAttribute("articles", articles);
        request.getRequestDispatcher(AdminLoginServlet.ADMIN_EDIT_POSTS).forward(request, response);
    }

    public static void displayArticleEdit(ToiletBeanAccess beans, HttpServletRequest request, HttpServletResponse response, Article art) throws ServletException, IOException {
        if (art.getCommentCollection() == null) {
            art.setCommentCollection(new ArrayList<>());
        }
        LinkedHashSet<String> groups = new LinkedHashSet<>();
        String defaultGroup = beans.getImeadValue(ArticleRepository.DEFAULT_CATEGORY);
        for (Section sect : beans.getSects().getAll(null)) {
            groups.add(sect.getName());
        }
        request.setAttribute("groups", groups);
        if (null == art.getSectionid()) {
            groups.add(defaultGroup);
        } else if (!groups.add(art.getSectionid().getName())) {
            groups.add(art.getSectionid().getName());
        }
        request.setAttribute(Article.class.getSimpleName(), art);
        request.setAttribute("defaultSearchTerm", ArticleRepository.getArticleSuggestionTerm(art));
        request.getSession().setAttribute(Article.class.getSimpleName(), art);
        Collection<Article> seeAlso = ArticleServlet.getArticleSuggestions(beans.getArts(), art);
        request.setAttribute("seeAlso", seeAlso);
        request.setAttribute("seeAlsoTerm", null != art.getSuggestion() ? art.getSuggestion() : ArticleRepository.getArticleSuggestionTerm(art));
        String formattedDate = null != art.getPosted() ? DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(art.getPosted()) : "";
        request.setAttribute("formattedDate", formattedDate);
        request.getRequestDispatcher(AdminLoginServlet.ADMIN_ADD_ARTICLE).forward(request, response);
    }
}
