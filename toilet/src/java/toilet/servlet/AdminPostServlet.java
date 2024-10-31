package toilet.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import java.time.Duration;
import java.time.Instant;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.file.BaseFileServlet;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.turbo.RequestTimer;
import libWebsiteTools.tag.AbstractInput;
import toilet.ArticleProcessor;
import toilet.bean.ToiletBeanAccess;
import toilet.bean.BackupDaemon;
import toilet.db.Article;

/**
 *
 * @author alpha
 */
@WebServlet(name = "adminPost", description = "Administer articles (and sometimes comments)", urlPatterns = {"/adminPost"})
public class AdminPostServlet extends AdminServlet {

    public static final String ADMIN_EDIT_POSTS = "/WEB-INF/adminEditPosts.jsp";

    @Override
    public AdminServletPermission getRequiredPermission(HttpServletRequest req) {
        return AdminServletPermission.EDIT_POSTS;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ToiletBeanAccess beans = allBeans.getInstance(request);
        Instant start = Instant.now();
        if (request.getParameter("deletecomment") != null) {      // delete comment
            beans.getComms().delete(Integer.parseInt(request.getParameter("deletecomment")));
            beans.getArts().evict();
            beans.getGlobalCache().clear();
            RequestTimer.addTiming(request, "save", Duration.between(start, Instant.now()));
            showList(request, response, beans.getArts().getAll(null));
        } else if (request.getParameter("disablecomments") != null) {
            List<Article> articles = new ArrayList<>();
            for (String id : request.getParameterValues(AbstractInput.getIncomingHash(request, "selectedArticle"))) {
                Article art = beans.getArts().get(Integer.parseInt(id));
                art.setComments(Boolean.FALSE);
                articles.add(art);
            }
            beans.getArts().upsert(articles);
            beans.getArts().evict();
            beans.getGlobalCache().clear();
            RequestTimer.addTiming(request, "save", Duration.between(start, Instant.now()));
            response.setHeader(RequestTimer.SERVER_TIMING, RequestTimer.getTimingHeader(request, Boolean.FALSE));
            response.sendRedirect(request.getAttribute(SecurityRepo.BASE_URL).toString());
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
                    beans.getArts().evict();
                    beans.getGlobalCache().clear();
                });
            } catch (NullPointerException n) {
            }
            beans.getGlobalCache().clear();
            RequestTimer.addTiming(request, "rewrite", Duration.between(start, Instant.now()));
            response.setHeader(RequestTimer.SERVER_TIMING, RequestTimer.getTimingHeader(request, Boolean.FALSE));
            response.sendRedirect(request.getAttribute(SecurityRepo.BASE_URL).toString());
        } else {
            List<Article> articles = beans.getArts().getAll(null);
            RequestTimer.addTiming(request, "query", Duration.between(start, Instant.now()));
            showList(request, response, articles);
        }
    }

    public static void showList(HttpServletRequest request, HttpServletResponse response, Collection<Article> articles) throws ServletException, IOException {
        request.setAttribute("title", "Posts");
        request.setAttribute("articles", articles);
        request.getRequestDispatcher(ADMIN_EDIT_POSTS).forward(request, response);
    }

}
