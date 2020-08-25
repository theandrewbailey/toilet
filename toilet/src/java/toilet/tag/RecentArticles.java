package toilet.tag;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import toilet.bean.ArticleRepo;
import toilet.db.Article;

public class RecentArticles extends SimpleTagSupport {

    @EJB
    private ArticleRepo arts;
    private Integer number = 10;
    private String category;
    private String var = "_article";

    @Override
    public void doTag() throws JspException, IOException {
        for (Article e : arts.getSection(category, 1, number)) {
            getJspContext().setAttribute(getVar(), e);
            getJspBody().invoke(null);
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