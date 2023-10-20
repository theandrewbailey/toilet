package libWebsiteTools.tag;

import java.io.IOException;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

public class Titler extends SimpleTagSupport {

    private static final String SEPARATOR = " | ";
    private static final int SOFT_LIMIT = 70;
    private String pageTitle = "";
    private String siteTitle = "";
    private Boolean siteTitleHide = false;

    @Override
    public void doTag() throws JspException, IOException {
        StringBuilder title = new StringBuilder("<title>");
        int origCount = siteTitle.length() == 0 ? title.length() : title.length() - SEPARATOR.length();
        do {
            if (siteTitleHide) {
                title.append(pageTitle);
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
        if (pageTitle != null) {
            this.pageTitle = pageTitle;
        }
    }

    /**
     * @param siteTitle the siteTitle to set
     */
    public void setSiteTitle(String siteTitle) {
        if (siteTitle != null) {
            this.siteTitle = siteTitle;
        }
    }

    /**
     * @param hideTitle the hideTitle to set
     */
    public void setSiteTitleHide(Boolean hideTitle) {
        if (hideTitle != null) {
            this.siteTitleHide = hideTitle;
        }
    }
}
