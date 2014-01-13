package toilet.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.ejb.EJB;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.imead.IMEADHolder;
import toilet.bean.StateCache;
import toilet.bean.UtilBean;

/**
 *
 * @author alpha
 */
public class Categorizer extends SimpleTagSupport {

    @EJB
    private StateCache cache;
    @EJB
    private IMEADHolder imead;
    private String category;

    @Override
    public void doTag() throws JspException, IOException {
        if (category != null) {
            execute(category);
            return;
        }
        for (String o : cache.getArticleCategories()) {
            execute(o);
        }
    }

    private void execute(String o) throws JspException, IOException {
        getJspContext().setAttribute("_cate_url", getUrl(imead.getValue(UtilBean.THISURL), o));
        getJspContext().setAttribute("_cate_group", o);
        getJspBody().invoke(null);
    }

    public static String getUrl(String thisURL, String category) {
        StringBuilder out = new StringBuilder(thisURL).append("index/");
        try {
            String title = URLEncoder.encode(category, "UTF-8");
            out.append(title);
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        }
        return out.append("/1").toString();
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }
}
