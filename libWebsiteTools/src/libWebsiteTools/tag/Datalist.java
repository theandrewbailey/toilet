package libWebsiteTools.tag;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 *
 * @author alpha
 */
public class Datalist extends SimpleTagSupport {

    private Collection options;
    private String id;

    @Override
    public void doTag() throws JspException, IOException {
        getJspContext().getOut().print(getDatalistTag(getId(), getOptions()));
    }

    /**
     * Creates a <datalist> with the specified ID and <option> nodes
     * @param id
     * @param options
     * @return datalist
     */
    @SuppressWarnings("unchecked")
    public static String getDatalistTag(String id, Collection options) {
        StringBuilder out = new StringBuilder(30 + (options.size() * 30));
        out.append("<datalist id=\"").append(id).append("\">");
        if (options instanceof Map) {
            Map<Object, Object> map = (Map) options;
            for (Map.Entry<Object, Object> option : map.entrySet()) {
                out.append("<option value=\"").append(option.getKey().toString()).append("\">").append(option.getValue().toString()).append("</option>");
            }
        } else {
            for (Object option : options) {
                out.append("<option value=\"").append(option.toString()).append("\">").append(option.toString()).append("</option>");
            }
        }
        return out.append("</datalist>").toString();
    }

    /**
     * @return the options
     */
    public Collection getOptions() {
        return options;
    }

    /**
     * @param options the options to set
     */
    public void setOptions(Collection options) {
        this.options = options;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

}
