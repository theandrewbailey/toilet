package libWebsiteTools.imead;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletRequest;


/**
 *
 * @author alpha
 */
@WebListener("places a list of locale objects on the request")
//@WebFilter(filterName="LocaleResolver", description="places a list of locale objects on the request", dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE}, urlPatterns = {"*.jsp", "*.jspf", "*.jspx"})
public class LocaleResolver implements ServletRequestListener {
    /**
     * this is the request attribute name that holds the list of user agent locales + server default locale.
     */
    public static final String LOCALE_PARAM = "$_LIBIMEAD_LOCALES";
    /**
     * to override default locale selection, place a java.util.Locale object on the SESSION with this name, and it will have first priority over user agent and server locales. if a resource cannot be resolved for the overriden locale, default behavior will be used.
     */
    public static final String OVERRIDE_LOCALE_PARAM = "$_LIBIMEAD_OVERRIDE_LOCALE";

    /**
     * retrieves locales off request. returned list can have (in order):
     * 
     * session overridden locale (see OVERRIDE_LOCALE_PARAM)
     * user agent (browser) set locales (see LOCALE_PARAM)
     * server default locale (locale.getDefault()) (will always be present)
     * 
     * @param req
     * @return locales
     * @see OVERRIDE_LOCALE_PARAM
     * @see LOCALE_PARAM
     */
    public static List<Locale> resolveLocales(HttpServletRequest req) {
        List<Locale> out = Collections.list(req.getLocales());
        if (req.getSession(false) != null) {
            Locale override = (Locale) req.getSession().getAttribute(OVERRIDE_LOCALE_PARAM);
            if (override != null) {
                out.add(0, override);
            }
        }
        out.add(Locale.getDefault());
        return out;
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        ServletRequest request=sre.getServletRequest();
        if (request.getAttribute(LOCALE_PARAM) == null) {
            request.setAttribute(LOCALE_PARAM, resolveLocales((HttpServletRequest) request));
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
    }
}
