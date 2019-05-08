package toilet.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.ejb.EJB;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import libOdyssey.bean.GuardRepo;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.imead.IMEADHolder;
import toilet.bean.StateCache;

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
        getJspContext().setAttribute("_cate_url", getUrl(imead.getValue(GuardRepo.CANONICAL_URL), o, 1));
        getJspContext().setAttribute("_cate_group", o);
        getJspBody().invoke(null);
    }

    public static String getUrl(String thisURL, String category, Integer page) {
        StringBuilder out = new StringBuilder(70).append(thisURL).append("index/");
        if (null != category) {
            try {
                String title = URLEncoder.encode(category, "UTF-8");
                out.append(title).append('/');
            } catch (UnsupportedEncodingException ex) {
                throw new JVMNotSupportedError(ex);
            }
        }
        return null == page ? out.toString() : out.append(page).toString();
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }
}
