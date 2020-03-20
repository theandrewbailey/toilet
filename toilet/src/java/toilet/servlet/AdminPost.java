package toilet.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.HashUtil;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.FeedBucket;
import toilet.UtilStatic;
import toilet.bean.StateCache;
import toilet.bean.ArticleRepo;
import toilet.db.Article;
import toilet.rss.CommentRss;

/**
 *
 * @author alpha
 */
@WebServlet(name = "adminPost", description = "Performs admin duties on posts (articles/comments)", urlPatterns = {"/adminPost"})
public class AdminPost extends ToiletServlet {

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
        if (answer != null && HashUtil.verifyArgon2Hash(imead.getValue(AdminLoginServlet.POSTS), answer)) {
            showList(request, response, arts.getAll(null));
        } else if (AdminLoginServlet.POSTS.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            if (request.getParameter("deletecomment") != null) {      // delete comment
                comms.delete(Integer.parseInt(request.getParameter("deletecomment")));
                src.get(CommentRss.NAME).preAdd();
                cache.clearEtags();
                showList(request, response, arts.getAll(null));
                return;
            } else if (request.getParameter("editarticle") != null) {      // set up to edit Entry
                Article art = arts.get(Integer.parseInt(request.getParameter("editarticle")));
                displayArticleEdit(request, response, art);
                return;
            }
            request.getRequestDispatcher("adminLogin?answer=" + imead.getValue(AdminLoginServlet.POSTS)).forward(request, response);
        }
    }

    public static void showList(HttpServletRequest request, HttpServletResponse response, Collection<Article> articles) throws ServletException, IOException {
        request.getSession().setAttribute(AdminLoginServlet.PERMISSION, AdminLoginServlet.POSTS);
        request.setAttribute("title", "Posts");
        request.setAttribute("articles", articles);
        request.getRequestDispatcher(AdminLoginServlet.MAN_ENTRIES).forward(request, response);
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
        request.getSession().setAttribute(LAST_ARTICLE_EDITED, art);
        request.getRequestDispatcher(AdminLoginServlet.MAN_ADD_ENTRY).forward(request, response);
        request.getSession().setAttribute(LAST_ARTICLE_EDITED, art);
    }
}
