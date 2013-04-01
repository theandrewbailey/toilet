package libOdyssey;

import java.io.IOException;
import java.util.Date;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 *
 * @author alpha
 */
public class ResponseTag extends SimpleTagSupport {

    public static final String RENDER_TIME_PARAM = "$_LIBODYSSEY_REQUEST_RENDER_TIME";

    @Override
    public void doTag() throws JspException, IOException {
        Date start = (Date) getJspContext().getAttribute(RequestTime.TIME_PARAM, PageContext.REQUEST_SCOPE);
        getJspContext().setAttribute("requestTime", start);
        Long time = new Date().getTime() - start.getTime();
        getJspContext().setAttribute("renderMillis", time);
        getJspContext().setAttribute(RENDER_TIME_PARAM, time, PageContext.REQUEST_SCOPE);
        getJspBody().invoke(null);
    }
}
