package libWebsiteTools.imead.tag;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJBException;
import javax.servlet.jsp.JspException;
import libWebsiteTools.imead.LocaleResolver;
import libWebsiteTools.imead.LocalizedStringNotFoundException;
import libWebsiteTools.NullWriter;

/**
 *
 * @author alpha
 */
public class Local extends KeyVal {

    private String locale;

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
        List<Locale> locales = (List<Locale>) getJspContext().findAttribute(LocaleResolver.LOCALE_PARAM);
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
