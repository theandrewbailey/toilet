package libWebsiteTools.imead.tag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.imead.LocaleResolver;

/**
 *
 * @author alpha
 */
public class LocalInitializer extends SimpleTagSupport {

    @Override
    public void doTag() throws JspException {
        LocaleResolver.resolveLocales((HttpServletRequest) ((PageContext) getJspContext()).getRequest());
    }
}
