package toilet.tag;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import toilet.bean.SpruceGenerator;

public class Spruce extends SimpleTagSupport {

    @EJB
    private SpruceGenerator spruce;
    private String ATTRIBUTE_NAME="spruce_sentence";

    public Spruce() {
        super();
    }

    @Override
    public void doTag() throws JspException, IOException {
        if (!spruce.ready()) {
            return;
        }
        try {
            getJspContext().setAttribute(ATTRIBUTE_NAME, spruce.getAddSentence());
            getJspBody().invoke(null);
        } catch (NullPointerException ex) {
            getJspContext().getOut().print("I'm taking a hike. C'ya!");
        }
    }
}
