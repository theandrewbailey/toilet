package toilet.tag;

import java.io.IOException;
import java.net.URLEncoder;
import javax.ejb.EJB;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import toilet.bean.ArticleStateCache;

/**
 *
 * @author alpha
 */
public class Categorizer extends SimpleTagSupport {

    @EJB
    private ArticleStateCache cache;

    @Override
    public void doTag() throws JspException, IOException {
        for (String o : cache.getArticleCategories()) {
            getJspContext().setAttribute("url", URLEncoder.encode(o, "UTF-8"));
            getJspContext().setAttribute("group", o);
            getJspBody().invoke(null);
        }
    }
}
