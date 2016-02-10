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
public class HtmlMeta extends SimpleTagSupport {

    public static final String META_TAGS = "$_HTML_META_TAGS";

    @SuppressWarnings("unchecked")
    public static void addTag(HttpServletRequest req, String name, String content) {
        List tags = (List) req.getAttribute(META_TAGS);
        if (tags == null) {
            tags = new ArrayList<>();
            req.setAttribute(META_TAGS, tags);
        }
        tags.add(new AbstractMap.SimpleEntry<String, String>(name, content));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doTag() throws JspException, IOException {
        List<Map.Entry<String, String>> tags = (List<Map.Entry<String, String>>) getJspContext().findAttribute(META_TAGS);
        if (tags == null) {
            return;
        }
        for (Map.Entry<String, String> tag : tags) {
            getJspContext().getOut().print(String.format("<meta name=\"%s\" content=\"%s\"/>", tag.getKey(), tag.getValue()));
        }
    }
}
