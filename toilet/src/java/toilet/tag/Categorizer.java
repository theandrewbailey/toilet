package toilet.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.imead.Local;
import toilet.bean.StateCache;

/**
 *
 * @author alpha
 */
public class Categorizer extends SimpleTagSupport {

    @EJB
    private StateCache cache;
    private String category;
    private Integer page;

    @Override
    public void doTag() throws JspException, IOException {
        if (category != null) {
            execute(category);
            return;
        }
        for (String catName : cache.getArticleCategories()) {
            execute(catName);
        }
    }

    private void execute(String catName) throws JspException, IOException {
        HttpServletRequest req = ((HttpServletRequest) ((PageContext) getJspContext()).getRequest());
        Locale locale = (Locale) req.getAttribute(Local.OVERRIDE_LOCALE_PARAM);
        Object baseURL = ((HttpServletRequest) ((PageContext) getJspContext()).getRequest()).getAttribute(SecurityRepo.BASE_URL);
        getJspContext().setAttribute("_cate_url", getUrl(baseURL.toString(), catName, page, locale));
        getJspContext().setAttribute("_cate_group", catName);
        getJspBody().invoke(null);
    }

    public static String getUrl(String baseURL ,String category, Integer page, Locale lang) {
        StringBuilder url = new StringBuilder(70).append(baseURL).append("index/");
        //StringBuilder url = new StringBuilder(70).append("index/");
        if (null != category && !category.isEmpty()) {
            try {
                String title = URLEncoder.encode(category, "UTF-8");
                url.append(title).append('/');
            } catch (UnsupportedEncodingException ex) {
                throw new JVMNotSupportedError(ex);
            }
        }
        if (null != page) {
            url.append(page);
        }
//        if (null != lang) {
//            url.append("?lang=").append(lang.toLanguageTag());
//        }
        return url.toString();
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @param page the page to set
     */
    public void setPage(Integer page) {
        this.page = page;
    }
}
