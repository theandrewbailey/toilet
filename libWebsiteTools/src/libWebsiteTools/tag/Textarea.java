package libWebsiteTools.tag;

public class Textarea extends AbstractInput {

    private Integer height;

    @Override
    public String generateTag() {
        StringBuilder out = new StringBuilder(1000);

        label(out);

        out.append("<textarea id=\"").append(getId());
        out.append("\" name=\"").append(getId());
        out.append("\" rows=\"").append(height.toString());
        if (getLength() != null){
            out.append("\" cols=\"").append(getLength().toString());
        }
        if (getAccesskey() != null) {
            out.append("\" accesskey=\"").append(getAccesskey());
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
        if (getAutofocus()) {
            out.append("\" autofocus=\"autofocus");
        }
        if (getDisabled()) {
            out.append("\" disabled=\"disabled");
        }
        if (getRequired()) {
            out.append("\" required=\"required");
        }
        out.append("\">");
        if (getValue() != null) {
            out.append(getValue());
        }
        out.append("</textarea>");

        return out.toString();
    }

    /**
     * @param height the height to set
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Not needed");
    }
}
