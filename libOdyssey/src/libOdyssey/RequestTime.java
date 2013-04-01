package libOdyssey;

import java.util.Date;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

/**
 *
 * @author alpha
 */
@WebListener("Saves the time the request started")
public class RequestTime implements ServletRequestListener {

    public static final String TIME_PARAM = "$_LIBODYSSEY_REQUEST_START_TIME";

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        sre.getServletRequest().setAttribute(TIME_PARAM, new Date());
    }
}
