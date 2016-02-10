package toilet.tag;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import toilet.bean.EntryRepo;
import toilet.db.Article;

public class Recent extends SimpleTagSupport {

    @EJB
    private EntryRepo entry;
    private Integer number = 10;
    private String category;
    private String var = "_recentEntry";

    @Override
    public void doTag() throws JspException, IOException {
        for (Article e : entry.getSection(category, 1, number)) {
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
