package libWebsiteTools.tag;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

public class Titler extends SimpleTagSupport {

    private static final String SEPARATOR = " | ";
    private static final int SOFT_LIMIT = 70;
    private String pageTitle = "";
    private String siteTitle = "";
    private Boolean siteTitleHide = false;
//    private Boolean siteTitleBefore = false;

    @Override
    public void doTag() throws JspException, IOException {
        StringBuilder title = new StringBuilder("<title>");
        int origCount = siteTitle.length() == 0 ? title.length() : title.length() - SEPARATOR.length();
        do {
            if (siteTitleHide) {
                title.append(pageTitle);
//            } else if (siteTitleBefore) {     // unsupported
//                title.append(siteTitle);
//                if (title.length() - origCount + pageTitle.length() > SOFT_LIMIT) {
//                    continue;
//                }
//                if (pageTitle.length() != 0) {
//                    title.append(SEPARATOR).append(pageTitle);
//                }
            } else {
                title.append(pageTitle);
                if (title.length() - origCount + siteTitle.length() > SOFT_LIMIT) {
                    continue;
                }
                if (pageTitle.length() != 0) {
                    title.append(SEPARATOR);
                }
                title.append(siteTitle);
            }
        } while (false);
        title.append("</title>");
        getJspContext().getOut().print(title.toString());
    }

    /**
     * @param pageTitle the pageTitle to set
     */
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    /**
     * @param siteTitle the siteTitle to set
     */
    public void setSiteTitle(String siteTitle) {
        this.siteTitle = siteTitle;
    }

    /**
     * @param hideTitle the hideTitle to set
     */
    public void setSiteTitleHide(Boolean hideTitle) {
        this.siteTitleHide = hideTitle;
    }

//    /**
//     * @param siteTitleBefore the siteTitleBefore to set
//     */
//    public void setSiteTitleBefore(Boolean siteTitleBefore) {
//        this.siteTitleBefore = siteTitleBefore;
//    }
}
