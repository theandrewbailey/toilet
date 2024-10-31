package libWebsiteTools.tag;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.turbo.RequestTimer;

/**
 *
 * @author alpha
 */
public class ResponseTag extends SimpleTagSupport {

    @Override
    public void doTag() throws JspException, IOException {
        OffsetDateTime start = RequestTimer.getStartTime(((HttpServletRequest) ((PageContext) getJspContext()).getRequest()));
        getJspContext().setAttribute("requestTime", start);
        Duration d = Duration.between(OffsetDateTime.now(), start).abs();
        Long time = d.toMillis();
        getJspContext().setAttribute("renderMillis", time);
        getJspBody().invoke(null);
    }
}
