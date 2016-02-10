package toilet;

import javax.ejb.EJB;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import libWebsiteTools.tag.HtmlTime;

/**
 *
 * @author alpha
 */
@WebListener("Sets the default time format from imead value \"page_dateFormat\".")
public class TimeFormatSetter implements ServletRequestListener {

    @EJB
    private IMEADHolder imead;

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        try {
            HttpServletRequest req = (HttpServletRequest) sre.getServletRequest();
            if (null == req.getAttribute(HtmlTime.FORMAT_VAR)) {
                req.setAttribute(HtmlTime.FORMAT_VAR, imead.getLocal("page_dateFormat", Local.resolveLocales(req)));
            }
        } catch (RuntimeException re) {
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
    }
}
