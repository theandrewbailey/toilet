package libWebsiteTools.imead.tag;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.NullWriter;

/**
 *
 * @author alpha
 */
public class KeyVal extends SimpleTagSupport {

    @EJB
    protected IMEADHolder imead;
    private String key;
    private List<String> params = new ArrayList<String>();

    @Override
    public void doTag() throws JspException, IOException {
        getJspContext().getOut().print(getValue());
    }

    protected String getValue() {
        try {
            getJspBody().invoke(new NullWriter());
        } catch (Exception n) {
        }
        String str = imead.getValue(key);
        str = MessageFormat.format(str, params.toArray());
        return str;
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
