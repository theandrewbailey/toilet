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
    public StringBuilder createTag() {
        StringBuilder checkbox=super.label(new StringBuilder());
        return super.createTag().append(checkbox);
    }
}
