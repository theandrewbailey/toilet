package libWebsiteTools.tag;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 * base class for all input elements
 * 
 * @author alpha
 */
public abstract class AbstractInput extends SimpleTagSupport {

    private String accesskey;
    private Boolean checked = false;
    private String id;
    private String label;
    private Boolean labelNextLine = true;
    private Integer length;
    private Integer maxLength;
    private Integer size;
    private String styleClass;
    private Integer tabindex;
    private String value;
    private String title;

    public abstract String getType();

    @Override
    public void doTag() throws JspException, IOException {
        getJspContext().getOut().print(generateTag());
    }

    protected void label(StringBuilder out) {
        if (getLabel() != null) {
            out.append("<label for=\"").append(getId());
            out.append("\">");
            out.append(getLabel());
            out.append(getLabelNextLine() ? "</label><br/>" : "</label>");
        }
    }

    public String generateTag() {
        StringBuilder out = new StringBuilder(1000);

        label(out);

        out.append("<input id=\"").append(getId());
        out.append("\" name=\"").append(getId());
        out.append("\" type=\"").append(getType());
        if (getAccesskey() != null) {
            out.append("\" accesskey=\"").append(getAccesskey());
        }
        if (getChecked()) {
            out.append("\" checked=\"checked");
        }
        if (getLength() != null) {
            out.append("\" length=\"").append(getLength().toString());
        }
        if (getMaxLength() != null) {
            out.append("\" maxlength=\"").append(getMaxLength().toString());
        }
        if (getSize() != null) {
            out.append("\" size=\"").append(getSize().toString());
        }
        if (getStyleClass() != null) {
            out.append("\" class=\"").append(getStyleClass());
        }
        if (getTabindex() != null) {
            out.append("\" tabindex=\"").append(getTabindex().toString());
        }
        if (getTitle() != null) {
            out.append("\" title=\"").append(getTitle());
        }
        if (getValue() != null) {
            out.append("\" value=\"").append(getValue());
        }
        out.append("\"/>");

        return out.toString();
    }

    /**
     * @return the accesskey
     */
    public String getAccesskey() {
        return accesskey;
    }

    /**
     * @param accesskey the accesskey to set
     */
    public void setAccesskey(String accesskey) {
        this.accesskey = accesskey;
    }

    /**
     * @return the checked
     */
    public Boolean getChecked() {
        return checked;
    }

    /**
     * @param checked the checked to set
     */
    public void setChecked(Boolean checked) {
        this.checked = checked;
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

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the length
     */
    public Integer getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(Integer length) {
        this.length = length;
    }

    /**
     * @return the tabindex
     */
    public Integer getTabindex() {
        return tabindex;
    }

    /**
     * @param tabindex the tabindex to set
     */
    public void setTabindex(Integer tabindex) {
        this.tabindex = tabindex;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the styleClass
     */
    public String getStyleClass() {
        return styleClass;
    }

    /**
     * @param styleClass the styleClass to set
     */
    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    /**
     * @return the maxLength
     */
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * @param maxLength the maxLength to set
     */
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @return the size
     */
    public Integer getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * @return the labelNextLine
     */
    public Boolean getLabelNextLine() {
        return labelNextLine;
    }

    /**
     * @param labelNextLine the labelNextLine to set
     */
    public void setLabelNextLine(Boolean labelNextLine) {
        this.labelNextLine = labelNextLine;
    }
}
