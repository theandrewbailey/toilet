package libWebsiteTools.tag;

public class Checkbox extends AbstractInput {

    public Checkbox() {
        setLabelNextLine(false);
        setValue("true");
    }

    @Override
    public String getType() {
        return "checkbox";
    }

    @Override
    protected StringBuilder label(StringBuilder out) {
        return out;
    }

    @Override
    public String createTag() {
        return super.label(new StringBuilder(super.createTag())).toString();
    }
}
