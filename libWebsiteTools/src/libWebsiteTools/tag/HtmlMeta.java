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
 * @author alpha
 */
public class HtmlMeta extends SimpleTagSupport {

    public static final String META_TAGS = "$_HTML_META_TAGS";
    public static final String META_PROPERTIES = "$_HTML_META_PROPERTIES";
    public static final String LINK_TAGS = "$_HTML_LINK_TAGS";

    @SuppressWarnings("unchecked")
    public static void addTag(HttpServletRequest req, String name, String content) {
        List tags = (List) req.getAttribute(META_PROPERTIES);
        if (tags == null) {
            tags = new ArrayList<>();
            req.setAttribute(META_PROPERTIES, tags);
        }
        tags.add(new AbstractMap.SimpleEntry<>(name, content));
    }

    @SuppressWarnings("unchecked")
    public static void addProperty(HttpServletRequest req, String name, String content) {
        List tags = (List) req.getAttribute(META_TAGS);
        if (tags == null) {
            tags = new ArrayList<>();
            req.setAttribute(META_TAGS, tags);
        }
        tags.add(new AbstractMap.SimpleEntry<>(name, content));
    }

    @SuppressWarnings("unchecked")
    public static void addLink(HttpServletRequest req, String rel, String href) {
        List tags = (List) req.getAttribute(LINK_TAGS);
        if (tags == null) {
            tags = new ArrayList<>();
            req.setAttribute(LINK_TAGS, tags);
        }
        tags.add(new AbstractMap.SimpleEntry<>(rel, href));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doTag() throws JspException, IOException {
        try {
            for (Map.Entry<String, String> tag : (List<Map.Entry<String, String>>) getJspContext().findAttribute(META_TAGS)) {
                getJspContext().getOut().print(String.format("<meta name=\"%s\" content=\"%s\"/>", tag.getKey(), tag.getValue()));
            }
        } catch (NullPointerException n) {
        }
        try {
            for (Map.Entry<String, String> tag : (List<Map.Entry<String, String>>) getJspContext().findAttribute(META_PROPERTIES)) {
                getJspContext().getOut().print(String.format("<meta property=\"%s\" content=\"%s\"/>", tag.getKey(), tag.getValue()));
            }
        } catch (NullPointerException n) {
        }
        try {
            for (Map.Entry<String, String> tag : (List<Map.Entry<String, String>>) getJspContext().findAttribute(LINK_TAGS)) {
                getJspContext().getOut().print(String.format("<link rel=\"%s\" href=\"%s\">", tag.getKey(), tag.getValue()));
            }
        } catch (NullPointerException n) {
        }
    }
}
