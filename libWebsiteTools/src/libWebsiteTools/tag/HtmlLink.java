package libWebsiteTools.tag;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 *
 * @author alphavm
 */
public class HtmlLink extends SimpleTagSupport {

    public static final String LINK_TAGS = "$_HTML_LINK_TAGS";

    @SuppressWarnings("unchecked")
    public static void addTag(HttpServletRequest req, String rel, String href) {
        List tags = (List) req.getAttribute(LINK_TAGS);
        if (tags == null) {
            tags = new ArrayList<>();
            req.setAttribute(LINK_TAGS, tags);
        }
        tags.add(new AbstractMap.SimpleEntry<String, String>(rel, href));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doTag() throws JspException, IOException {
        List<Map.Entry<String, String>> tags = (List<Map.Entry<String, String>>) getJspContext().findAttribute(LINK_TAGS);
        if (tags == null) {
            return;
        }
        for (Map.Entry<String, String> tag : tags) {
            getJspContext().getOut().print(String.format("<meta name=\"%s\" content=\"%s\">", tag.getKey(), tag.getValue()));
        }
    }
}
