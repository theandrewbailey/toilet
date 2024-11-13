package toilet.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import toilet.bean.ToiletBeanAccess;
import toilet.bean.database.Article;

public class RecentArticles extends SimpleTagSupport {

    private Integer number = 10;
    private String category;
    private String var = "_article";

    @Override
    @SuppressWarnings("unchecked")
    public void doTag() throws JspException, IOException {
        List<Integer> excludes = null;
        List<Article> articles = null;
        HttpServletRequest req = ((HttpServletRequest) ((PageContext) getJspContext()).getRequest());
        try {
            articles = (List<Article>) req.getAttribute("articles");
            excludes = articles.stream().mapToInt((t) -> {
                return t.getArticleid();
            }).boxed().collect(Collectors.toList());
        } catch (NullPointerException x) {
            if (null == articles) {
                articles = new ArrayList<>();
                req.setAttribute("articles", articles);
            }
        }
        ToiletBeanAccess beans = (ToiletBeanAccess) req.getAttribute(libWebsiteTools.AllBeanAccess.class.getCanonicalName());
//        Instant start = Instant.now();
        List<Article> latest = beans.getArts().getBySection(category, 1, number, excludes);
        if (2 > latest.size()) {
            latest = beans.getArts().getBySection(category, 1, number, null);
        }
//        RequestTimer.addTiming(req, "recent-" + category, Duration.between(start, Instant.now()));
        for (Article e : latest) {
            getJspContext().setAttribute(getVar(), e);
            getJspBody().invoke(null);
            articles.add(e);
        }
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }
}
