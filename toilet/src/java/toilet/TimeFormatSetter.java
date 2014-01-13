package toilet;

import javax.ejb.EJB;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import libWebsiteTools.imead.IMEADHolder;
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
        sre.getServletRequest().setAttribute(HtmlTime.FORMAT_VAR, imead.getValue("page_dateFormat"));
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
    }
}
