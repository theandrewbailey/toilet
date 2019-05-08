package libWebsiteTools.imead;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.NullWriter;

/**
 *
 * @author alpha
 */
public class Local extends SimpleTagSupport {

    /**
     * this is the request attribute name that holds the list of user agent
     * locales + server default locale.
     */
    public static final String LOCALE_PARAM = "$_LIBIMEAD_LOCALES";
    /**
     * to override default locale selection, place a java.util.Locale object on
     * the SESSION with this name, and it will have first priority over user
     * agent and server locales. if a resource cannot be resolved for the
     * overridden locale, default behavior will be used.
     */
    public static final String OVERRIDE_LOCALE_PARAM = "$_LIBIMEAD_OVERRIDE_LOCALE";
    private static final Logger LOG = Logger.getLogger(Local.class.getName());
    @EJB
    protected IMEADHolder imead;
    private String key;
    private List<String> params = new ArrayList<>();

    private String locale;

    /**
     * retrieves locales off request, and sets them on either the session (if
     * created), or the request. returned list can have (in order):
     *
     * session overridden locale (see OVERRIDE_LOCALE_PARAM) user agent
     * (browser) set locales (see LOCALE_PARAM) server default locale
     * (locale.getDefault()) (will always be present)
     *
     * @param req
     * @return locales
     * @see OVERRIDE_LOCALE_PARAM
     * @see LOCALE_PARAM
     */
    @SuppressWarnings("unchecked")
    public static List<Locale> resolveLocales(HttpServletRequest req) {
        List<Locale> out;
        try {
            if (null != req.getSession(false)) {
                out = (List<Locale>) req.getSession().getAttribute(LOCALE_PARAM);
                if (null != out) {
                    return out;
                }
            }
            out = (List<Locale>) req.getAttribute(LOCALE_PARAM);
            if (null != out) {
                return out;
            }
        } catch (ClassCastException cce) {
            LOG.severe("Some unexpected object is occupying attribute " + LOCALE_PARAM + " in the session or request.");
            throw cce;
        }
        out = Collections.list(req.getLocales());
        if (null != req.getSession(false)) {
            Locale override = (Locale) req.getSession().getAttribute(OVERRIDE_LOCALE_PARAM);
            if (override != null) {
                out.add(0, override);
            }
        }
        out.add(Locale.getDefault());
        Locale generic = IMEADHolder.getLanguageOnly(Locale.getDefault());
        if (!generic.equals(Locale.getDefault())) {
            out.add(generic);
        }
        if (null != req.getSession(false)) {
            req.getSession().setAttribute(LOCALE_PARAM, out);
        } else {
            req.setAttribute(LOCALE_PARAM, out);
        }
        return out;
    }

    /**
     * convenience method for the other resolveLocales()
     *
     * @param jspc
     * @return
     */
    public static List<Locale> resolveLocales(JspContext jspc) {
        return resolveLocales((HttpServletRequest) ((PageContext) jspc).getRequest());
    }

    @SuppressWarnings({"unchecked", "UseSpecificCatch", "ThrowableResultIgnored"})
    protected String getValue() {
        try {
            getJspBody().invoke(new NullWriter());
        } catch (Exception n) {
        }
        try {
            if (locale != null) {
                return MessageFormat.format(imead.getLocal(getKey(), locale), getParams().toArray());
            }
            return MessageFormat.format(imead.getLocal(getKey(), resolveLocales(getJspContext())), getParams().toArray());
        } catch (EJBException e) {
            if (!(e.getCause() instanceof LocalizedStringNotFoundException)) {
                throw e;
            }
        }
        return "";
    }

    @Override
    public void doTag() throws JspException, IOException {
        getJspContext().getOut().print(getValue());
    }

    public void setLocale(String l) {
        locale = l;
    }

    public void setKey(String k) {
        key = k;
    }

    public String getKey() {
        return key;
    }

    public void setParams(List<String> p) {
        params = p;
    }

    public List<String> getParams() {
        return params;
    }
}
