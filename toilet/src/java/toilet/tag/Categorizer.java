package toilet.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.JVMNotSupportedError;
import toilet.bean.ToiletBeanAccess;
import toilet.bean.database.Section;

/**
 *
 * @author alpha
 */
public class Categorizer extends SimpleTagSupport {

    private String category;
    private Integer page;

    @Override
    public void doTag() throws JspException, IOException {
        if (category != null) {
            execute(category);
            return;
        }
        HttpServletRequest req = ((HttpServletRequest) ((PageContext) getJspContext()).getRequest());
        ToiletBeanAccess beans = (ToiletBeanAccess) req.getAttribute(libWebsiteTools.AllBeanAccess.class.getCanonicalName());
        for (Section sect : beans.getInstance(req).getSects().getAll(null)) {
            execute(sect.getName());
        }
    }

    private void execute(String catName) throws JspException, IOException {
        Object baseURL = ((HttpServletRequest) ((PageContext) getJspContext()).getRequest()).getAttribute(SecurityRepo.BASE_URL);
        if (null != baseURL) {
            getJspContext().setAttribute("_cate_url", getUrl(baseURL.toString(), catName, page));
            getJspContext().setAttribute("_cate_group", catName);
            getJspBody().invoke(null);
        }
    }

    public static String getUrl(String baseURL, String category, Integer page) {
        StringBuilder url = new StringBuilder(70).append(baseURL).append("index/");
        if (null != category && !category.isEmpty()) {
            try {
                String title = URLEncoder.encode(category, "UTF-8");
                url.append(title).append('/');
            } catch (UnsupportedEncodingException ex) {
                throw new JVMNotSupportedError(ex);
            }
        }
        if (null != page && 1 != page) {
            url.append(page);
        }
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
