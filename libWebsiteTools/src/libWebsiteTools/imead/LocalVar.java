package libWebsiteTools.imead;

import java.io.IOException;
import jakarta.servlet.jsp.JspException;

/**
 *
 * @author alpha
 */
public class LocalVar extends Local {

    private String var;

    public void setVar(String k) {
        var = k;
    }

    @Override
    public void doTag() throws JspException, IOException {
        try {
            var = var == null ? getKey() : var;
            getJspContext().setAttribute(var, getValue());
        } catch (LocalizedStringNotFoundException lx) {
            getJspContext().setAttribute(var, var);
        }
    }
}
