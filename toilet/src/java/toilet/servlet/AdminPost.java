package toilet.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.FeedBucket;
import toilet.UtilStatic;
import toilet.bean.StateCache;
import toilet.bean.EntryRepo;
import toilet.db.Article;
import toilet.db.Section;
import toilet.rss.CommentRss;
import static toilet.servlet.AdminLoginServlet.POSTS;

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
        String login = request.getSession().getAttribute("login").toString();
        String answer = request.getParameter("answer");
        if (answer != null && imead.verifyArgon2(answer, POSTS)) {
            showList(request, response, entry.getArticleArchive(null));
        } else if (login.equals(AdminLoginServlet.POSTS)) {
            if (request.getParameter("deletecomment") != null) {      // delete comment
                entry.deleteComment(Integer.parseInt(request.getParameter("deletecomment")));
                src.getFeed(CommentRss.NAME).preAdd();
                cache.clearEtags();
                showList(request, response, entry.getArticleArchive(null));
                return;
            } else if (request.getParameter("editarticle") != null) {      // set up to edit Entry
                Article art = entry.getArticle(Integer.parseInt(request.getParameter("editarticle")));
                displayArticleEdit(request, response, art);
                return;
            }
            request.getRequestDispatcher("adminLogin?answer=" + imead.getValue(AdminLoginServlet.POSTS)).forward(request, response);
        }
    }

    public static void showList(HttpServletRequest request, HttpServletResponse response, List<Article> articles) throws ServletException, IOException {
        request.getSession().setAttribute("login", AdminLoginServlet.POSTS);
        request.setAttribute("title", "Posts");
        request.setAttribute("articles", articles);
        request.getRequestDispatcher(AdminLoginServlet.MAN_ENTRIES).forward(request, response);
    }

    public static void displayArticleEdit(HttpServletRequest request, HttpServletResponse response, Article art) throws ServletException, IOException {
        if (art.getCommentCollection() == null) {
            art.setCommentCollection(new ArrayList<>());
        }
        LinkedHashMap<String, String> groups = new LinkedHashMap<>();
        String defaultGroup = UtilStatic.getBean(IMEADHolder.LOCAL_NAME, IMEADHolder.class).getValue(EntryRepo.DEFAULT_CATEGORY);
        groups.put(defaultGroup, defaultGroup);
        for (String group : UtilStatic.getBean(StateCache.LOCAL_NAME, StateCache.class).getArticleCategories()) {
            groups.put(group, group);
        }
        groups.put(CREATE_NEW_GROUP, "new group --&gt;");
        request.setAttribute("groups", groups);
        if (null == art.getSectionid()) {
            art.setSectionid(new Section(0, defaultGroup));
        } else if (!groups.containsKey(art.getSectionid().getName())) {
            groups.put(art.getSectionid().getName(), art.getSectionid().getName());
        }
        request.getSession().setAttribute(LAST_ARTICLE_EDITED, art);
        request.getRequestDispatcher(AdminLoginServlet.MAN_ADD_ENTRY).forward(request, response);
        request.getSession().setAttribute(LAST_ARTICLE_EDITED, art);
    }
}
