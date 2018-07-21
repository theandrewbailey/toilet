package libWebsiteTools.tag;

public class Textarea extends AbstractInput {

    private Integer height;

    @Override
    public String generateTag() {
        StringBuilder out = new StringBuilder(1000);

        label(out);

        out.append("<textarea id=\"").append(getId());
        out.append("\" name=\"").append(getId());
        if (null != height) {
            out.append("\" rows=\"").append(height.toString());
        }
        if (null != getLength()) {
            out.append("\" cols=\"").append(getLength().toString());
        }
        if (null != getAccesskey()) {
            out.append("\" accesskey=\"").append(getAccesskey());
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
        if (null != getPattern()) {
            out.append("\" data-pattern=\"").append(getPattern());
        }
        if (null != getPatternMismatch()) {
            out.append("\" data-patternmismatch=\"").append(getPatternMismatch());
        }
        if (getAutofocus()) {
            out.append("\" autofocus=\"autofocus");
        }
        if (getDisabled()) {
            out.append("\" disabled=\"disabled");
        }
        if (getRequired()) {
            out.append("\" required=\"required");
        }
        if (null != getValueMissing()) {
            out.append("\" data-valuemissing=\"").append(getValueMissing());
        }
        out.append("\">");
        if (getValue() != null) {
            out.append(getValue());
        }
        out.append("</textarea>");

        return out.toString();
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Not needed");
    }
}
