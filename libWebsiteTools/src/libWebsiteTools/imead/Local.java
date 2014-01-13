package libWebsiteTools.imead;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJBException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import libWebsiteTools.imead.LocalizedStringNotFoundException;
import libWebsiteTools.NullWriter;

/**
 *
 * @author alpha
 */
public class Local extends KeyVal {

    /**
     * this is the request attribute name that holds the list of user agent locales + server default locale.
     */
    public static final String LOCALE_PARAM = "$_LIBIMEAD_LOCALES";
    /**
     * to override default locale selection, place a java.util.Locale object on the SESSION with this name, and it will have first priority over user agent and server locales. if a resource cannot be resolved for the overriden locale, default behavior will be used.
     */
    public static final String OVERRIDE_LOCALE_PARAM = "$_LIBIMEAD_OVERRIDE_LOCALE";

    private String locale;

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
    @SuppressWarnings("unchecked")
    protected String getValue() {
        try {
            getJspBody().invoke(new NullWriter());
        } catch (Exception n) {
        }
        if (locale != null) {
            try {
                return MessageFormat.format(imead.getLocal(getKey(), locale), getParams().toArray());
            } catch (EJBException e) {
                if (!(e.getCause() instanceof LocalizedStringNotFoundException)) {
                    throw e;
                }
            }
        }

        List<Locale> locales = (List<Locale>) getJspContext().findAttribute(LOCALE_PARAM);
        if (locales == null) {
            locales = resolveLocales((HttpServletRequest)((PageContext)getJspContext()).getRequest());
            getJspContext().setAttribute(LOCALE_PARAM, locales, PageContext.REQUEST_SCOPE);
        }
        try {
            return MessageFormat.format(imead.getLocal(getKey(), locales), getParams().toArray());
        } catch (EJBException e) {
            // not much that can be done; key was not found, must report
            throw e;
        }
    }

    @Override
    public void doTag() throws JspException, IOException {
        getJspContext().getOut().print(getValue());
    }

    public void setLocale(String l) {
        locale = l;
    }
}
