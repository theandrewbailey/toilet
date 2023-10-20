package libWebsiteTools.tag;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.security.GuardFilter;

/**
 *
 * @author alpha
 */
public class ResponseTag extends SimpleTagSupport {

    public static final String RENDER_TIME_PARAM = "$_security_REQUEST_RENDER_TIME";

    @Override
    public void doTag() throws JspException, IOException {
        OffsetDateTime start = (OffsetDateTime) getJspContext().getAttribute(GuardFilter.TIME_PARAM, PageContext.REQUEST_SCOPE);
        getJspContext().setAttribute("requestTime", start);
        Duration d = Duration.between(OffsetDateTime.now(), start).abs();
        Long time = d.toMillis();
        getJspContext().setAttribute("renderMillis", time);
        getJspContext().setAttribute(RENDER_TIME_PARAM, time, PageContext.REQUEST_SCOPE);
        getJspBody().invoke(null);
    }
}
