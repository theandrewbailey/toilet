package libOdyssey;

import java.util.Date;
import javax.ejb.EJB;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;
import libOdyssey.bean.Analyzer;

/**
 *
 * @author alpha
 */
@WebListener("Saves the time the request started, and logs request to analytics")
public class RequestTime implements ServletRequestListener {

    public static final String TIME_PARAM = "$_LIBODYSSEY_REQUEST_START_TIME";
    @EJB
    private Analyzer anal;

    public static Date getRequestTime(HttpServletRequest req){
        return (Date)req.getAttribute(TIME_PARAM);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        HttpServletRequest req = (HttpServletRequest) sre.getServletRequest();
        // log request
        if (null != req.getAttribute(OdysseyFilter.KILLED_REQUEST)) {
            return;
        }
        if (null == req.getAttribute(ResponseTag.RENDER_TIME_PARAM)) {
            req.setAttribute(ResponseTag.RENDER_TIME_PARAM, new Date().getTime() - ((Date) req.getAttribute(RequestTime.TIME_PARAM)).getTime());
        }
        //anal.logRequest(req, res);
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        sre.getServletRequest().setAttribute(TIME_PARAM, new Date());
    }
}
