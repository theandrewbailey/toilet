package libWebsiteTools.tag;

import java.io.StringWriter;

/**
 *
 * @author alpha
 */
public class Button extends AbstractInput {

    private String type;

    @Override
    public StringBuilder createTag() {
        StringBuilder out = new StringBuilder(400).append("<button type=\"").append(getType()).append("\" name=\"").append(getName());
        if (null != getId()) {
            out.append("\" id=\"").append(getId());
        }
        if (null != getAccesskey()) {
            out.append("\" accesskey=\"").append(getAccesskey());
        }
        if (getAutofocus()) {
            out.append("\" autofocus=\"autofocus");
        }
        if (getDisabled()) {
            out.append("\" disabled=\"disabled");
        }
        if (null != getStyleClass()) {
            out.append("\" class=\"").append(getStyleClass());
        }
        if (null != getTabindex()) {
            out.append("\" tabindex=\"").append(getTabindex().toString());
        }
        if (null != getTitle()) {
            out.append("\" title=\"").append(getTitle());
        }
        if (null != getValue()) {
            out.append("\" value=\"").append(getValue());
        }
        out.append("\">");
        StringWriter body = new StringWriter(100);
        try {
            getJspBody().invoke(body);
        } catch (Exception ex) {
        }
        out.append(body.toString());
        return out.append("</button>");
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
