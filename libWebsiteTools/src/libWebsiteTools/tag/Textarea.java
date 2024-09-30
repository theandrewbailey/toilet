package libWebsiteTools.tag;

public class Textarea extends AbstractInput {

    private Integer height;

    @Override
    public StringBuilder createTag() {
        StringBuilder out = label(new StringBuilder(1000));

        out.append("<textarea name=\"").append(getName());
        if (null != getId()) {
            out.append("\" id=\"").append(getId());
        }
        if (null != height) {
            out.append("\" rows=\"").append(height.toString());
        } else if (null == height && null != getValue()) {
            int rows = 0;
            for (String line : getValue().split("\n")) {
                rows += (line.length() / 70) + 1;
            }
            out.append("\" rows=\"").append(Math.max(3, rows));
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
            out.append(getValue().replace("&", "&amp;"));
        }
        out.append("</textarea>");

        return out;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Not needed");
    }
}
