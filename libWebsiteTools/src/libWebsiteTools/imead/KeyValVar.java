package libWebsiteTools.imead;

import java.io.IOException;
import javax.servlet.jsp.JspException;

/**
 *
 * @author alpha
 */
public class KeyValVar extends KeyVal {

    private String var;

    public void setVar(String k) {
        var = k;
    }

    @Override
    public void doTag() throws JspException, IOException {
        var = var == null ? getKey() : var;
        getJspContext().setAttribute(var, getValue());
    }
}
