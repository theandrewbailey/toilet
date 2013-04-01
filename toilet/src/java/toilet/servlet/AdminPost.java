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
import toilet.UtilStatic;
import toilet.bean.ArticleStateCache;
import toilet.bean.EntryRepo;
import toilet.db.Article;
import toilet.rss.ArticleRss;
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
    private ArticleStateCache cache;
    @EJB
    private iFeedBucket src;
    @EJB
    private IMEADHolder imead;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String del = request.getParameterNames().nextElement().toString();
        String login = request.getSession().getAttribute("login").toString();
        String answer = request.getParameter("answer");
        if (answer != null && SCryptUtil.check(answer, imead.getValue(POSTS))) {
            List<Article> articles = entry.getArticleArchive(null);
            Collections.reverse(articles);
            request.setAttribute("title", "Posts");
            request.setAttribute("arts", articles);
            request.getRequestDispatcher(AdminServlet.MAN_ENTRIES).forward(request, response);
        } else if (login.equals(AdminServlet.POSTS)) {
//            if (del.startsWith("a")){           // delete Entry
//                entry.deleteEntry(Integer.parseInt(del.substring(1)));
//                util.updatePageTemplate();
//                src.getFeed(ArticleRss.NAME).preAdd();
//            }
//            else 
            if (del.startsWith("c")) {      // delete comment
                entry.deleteComment(Integer.parseInt(del.substring(1)));
                src.getFeed(CommentRss.NAME).preAdd();
            } else if (del.startsWith("e")) {      // set up to edit Entry
                Article e = entry.getEntry(Integer.parseInt(del.substring(1)));
                List<String> groups = new ArrayList<String>(cache.getArticleCategories());
                groups.add(imead.getValue(EntryRepo.DEFAULT_CATEGORY));
                request.setAttribute("entryType", "edit");
                request.getSession().setAttribute("art", e);

//                request.setAttribute("artTitle", e.getArticletitle());
                request.setAttribute("text", UtilStatic.htmlUnformat(e.getPostedtext(), true));
//                request.setAttribute("commentable", e.getComments());
//                request.setAttribute("idee", e.getEntryid());
                request.setAttribute("groups", groups);
//                request.setAttribute("curGroup", e.getEntrysectionid().getSectionname());
                request.getSession().setAttribute("article", ArticleServlet.EDITING);

                request.getRequestDispatcher(AdminServlet.MAN_ADD_ENTRY).forward(request, response);
                src.getFeed(ArticleRss.NAME).preAdd();
                return;
            }
            request.getRequestDispatcher("admin?answer=" + imead.getValue(AdminServlet.POSTS)).forward(request, response);
        }
    }
}
