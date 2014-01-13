package toilet.servlet;

import com.lambdaworks.crypto.SCryptUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.iFeedBucket;
import libWebsiteTools.tag.RequestToken;
import toilet.UtilStatic;
import toilet.bean.StateCache;
import toilet.bean.EntryRepo;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.rss.CommentRss;
import static toilet.servlet.AdminServlet.POSTS;

/**
 *
 * @author alpha
 */
@WebServlet(name = "adminPost", description = "Performs admin duties on posts (articles/comments)", urlPatterns = {"/adminPost"})
public class AdminPost extends HttpServlet {

    @EJB
    private EntryRepo entry;
    @EJB
    private iFeedBucket src;
    @EJB
    private IMEADHolder imead;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String login = request.getSession().getAttribute("login").toString();
        String answer = request.getParameter("answer");
        if (answer != null && SCryptUtil.check(answer, imead.getValue(POSTS))) {
            showList(request, response);
        } else if (login.equals(AdminServlet.POSTS)) {
            if (request.getParameter("deletecomment")!=null) {      // delete comment
                entry.deleteComment(Integer.parseInt(request.getParameter("deletecomment")));
                src.getFeed(CommentRss.NAME).preAdd();
                showList(request, response);
                return;
            } else if (request.getParameter("editarticle")!=null) {      // set up to edit Entry
                Article art = entry.getEntry(Integer.parseInt(request.getParameter("editarticle")));
                displayArticleEdit(request, response, art);
                return;
            }
            request.getRequestDispatcher("admin?answer=" + imead.getValue(AdminServlet.POSTS)).forward(request, response);
        }
    }

    public static void showList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getSession().setAttribute("login", AdminServlet.POSTS);
        List<Article> articles = UtilStatic.getBean(EntryRepo.LOCAL_NAME, EntryRepo.class).getArticleArchive(null);
        Collections.reverse(articles);
        request.setAttribute("title", "Posts");
        request.setAttribute("articles", articles);
        request.setAttribute(RequestToken.ID_NAME, null);
        request.getRequestDispatcher(AdminServlet.MAN_ENTRIES).forward(request, response);
    }

    public static void displayArticleEdit(HttpServletRequest request, HttpServletResponse response, Article art) throws ServletException, IOException {
        if (art.getCommentCollection() == null) {
            art.setCommentCollection(new ArrayList<Comment>());
        }System.out.println("displaying article");
        List<String> groups = new ArrayList<>(UtilStatic.getBean(StateCache.LOCAL_NAME, StateCache.class).getArticleCategories());
        groups.add(UtilStatic.getBean(IMEADHolder.LOCAL_NAME, IMEADHolder.class).getValue(EntryRepo.DEFAULT_CATEGORY));
        request.setAttribute("groups", groups);
        request.getSession().setAttribute("art", art);
        request.setAttribute(RequestToken.ID_NAME, null);
        request.getRequestDispatcher(AdminServlet.MAN_ADD_ENTRY).forward(request, response);
    }
}
