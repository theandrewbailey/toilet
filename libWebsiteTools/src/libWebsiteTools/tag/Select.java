package libWebsiteTools.tag;

import java.util.Map;

/**
 *
 * @author alpha
 */
public class Select extends AbstractInput {

    private Map<Object, Object> parameters;
    private String selected;
    private Boolean mutliple = false;

    @Override
    public String createTag() {
        StringBuilder out = label(new StringBuilder(300 + getParameters().size() * 60));

        out.append("<select id=\"").append(getId()).append("\" name=\"").append(getId());
        if (null != getAccesskey()) {
            out.append("\" accesskey=\"").append(getAccesskey());
        }
        if (null != getStyleClass()) {
            out.append("\" class=\"").append(getStyleClass());
        }
        if (null != getSize()) {
            out.append("\" size=\"").append(getSize().toString());
        }
        if (null != getTabindex()) {
            out.append("\" tabindex=\"").append(getTabindex().toString());
        }
        if (null != getTitle()) {
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
        if (null != getValueMissing()) {
            out.append("\" data-valuemissing=\"").append(getValueMissing());
        }
        if (getMutliple()) {
            out.append("\" multiple=\"multiple");
        }
        out.append("\">");

        for (Map.Entry<Object, Object> entry : getParameters().entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map suboptions = (Map) entry.getValue();
                out.append("<optgroup label=\"").append(entry.getKey()).append("\">");
                for (Object subentry : suboptions.entrySet()) {
                    addOption(out, (Map.Entry) subentry);
                }
                out.append("</optgroup>");
            } else {
                addOption(out, entry);
            }
        }

        return out.append("</select>").toString();
    }

    private void addOption(StringBuilder out, Map.Entry entry) {
        String key = entry.getKey().toString();
        out.append("<option value=\"").append(key).append(key.equals(getSelected()) ? "\" selected=\"selected\">" : "\">").append(entry.getValue().toString()).append("</option>");
    }

    @Override
    public String getType() {
        throw new UnsupportedOperationException("Not needed");
    }

    public Map<Object, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<Object, Object> parameters) {
        this.parameters = parameters;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public Boolean getMutliple() {
        return mutliple;
    }

    public void setMutliple(Boolean mutliple) {
        this.mutliple = mutliple;
    }
}
