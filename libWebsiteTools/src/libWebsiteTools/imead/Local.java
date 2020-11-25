package libWebsiteTools.imead;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.ws.rs.core.HttpHeaders;
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
    @EJB
    protected IMEADHolder imead;
    private String key;
    private List<String> params = new ArrayList<>();

    private String locale;

    /**
     * retrieves locales off request, and sets them on the request.returned list
     * can have (in order):
     *
     * session locale (see OVERRIDE_LOCALE_PARAM) user agent (browser) set
     * locales (see LOCALE_PARAM) server default locale (locale.getDefault())
     * (will always be present)
     *
     * @param req
     * @param imead
     * @return locales
     * @see OVERRIDE_LOCALE_PARAM
     * @see LOCALE_PARAM
     */
    @SuppressWarnings("unchecked")
    public static List<Locale> resolveLocales(IMEADHolder imead, HttpServletRequest req) {
        List<Locale> out = (List<Locale>) req.getAttribute(LOCALE_PARAM);
        if (null == out) {
            LinkedHashSet<Locale> lset = new LinkedHashSet<>();
            Locale override = (Locale) req.getAttribute(OVERRIDE_LOCALE_PARAM);
            if (override != null && !lset.contains(override)) {
                lset.add(override);
            }
            Collection<Locale> locales = imead.getLocales();
            String header = req.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
            if (null != header) {
                List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(header);
                for (Locale l : Locale.filter(ranges, locales)) {
                    if (locales.contains(l)) {
                        lset.add(l);
                    }
                }
            }
            if (lset.isEmpty()) {
                if (locales.contains(Locale.getDefault()) && !lset.contains(Locale.getDefault())) {
                    lset.add(Locale.getDefault());
                }
                Locale generic = Locale.forLanguageTag(Locale.getDefault().getLanguage());
                if (locales.contains(generic) && !generic.equals(Locale.getDefault()) && !lset.contains(generic)) {
                    lset.add(generic);
                }
            }
            if (!lset.contains(Locale.ROOT)) {
                lset.add(Locale.ROOT);
            }
            out = new ArrayList<>(lset);
            req.setAttribute(LOCALE_PARAM, out);
        }
        return out;
    }

    public static String getLocaleString(IMEADHolder imead, HttpServletRequest req) {
        ArrayList<String> langTags = new ArrayList<>();
        for (Locale l : resolveLocales(imead, req)) {
            langTags.add(l.toLanguageTag());
        }
        return String.join(", ", langTags);
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
            return MessageFormat.format(imead.getLocal(getKey(), resolveLocales(imead, (HttpServletRequest) ((PageContext) getJspContext()).getRequest())), getParams().toArray());
        } catch (EJBException e) {
            if (!(e.getCause() instanceof LocalizedStringNotFoundException)) {
                throw e;
            }
        } catch (NullPointerException n) {
        }
        return "";
    }

    @Override
    public void doTag() throws JspException, IOException {
        try {
            getJspContext().getOut().print(getValue());
        } catch (IOException x) {
            // don't do anything
        }
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
