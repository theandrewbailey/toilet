package libWebsiteTools.imead;

import java.io.IOException;
import java.io.StringWriter;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

/**
 *
 * @author alpha
 */
public class Parameter extends SimpleTagSupport {

    private Object object;

    @Override
    public void doTag() throws JspException, IOException {
        Local daddy = (Local) getParent();
        if (object == null) {
            StringWriter fakeout = new StringWriter(500);
            getJspBody().invoke(fakeout);
            daddy.getParams().add(fakeout.getBuffer().toString());
        } else {
            daddy.getParams().add(object.toString());
        }
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
