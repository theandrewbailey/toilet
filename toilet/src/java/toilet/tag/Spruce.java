package toilet.tag;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import toilet.bean.ToiletBeanAccess;

public class Spruce extends SimpleTagSupport {

    private final String ATTRIBUTE_NAME = "spruce_sentence";

    public Spruce() {
        super();
    }

    @Override
    public void doTag() throws JspException, IOException {
//        HttpServletRequest req = ((HttpServletRequest) ((PageContext) getJspContext()).getRequest());
//        ToiletBeanAccess beans = (ToiletBeanAccess) req.getAttribute(libWebsiteTools.AllBeanAccess.class.getCanonicalName());
//        if (!beans.getSpruce().ready()) {
//            return;
//        }
//        try {
//            getJspContext().setAttribute(ATTRIBUTE_NAME, beans.getSpruce().getAddSentence());
//            getJspBody().invoke(null);
//        } catch (NullPointerException ex) {
//            getJspContext().getOut().print("I'm taking a hike. C'ya!");
//        }
    }
}
