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
    protected void label(StringBuilder out) {
    }

    @Override
    public String generateTag() {
        StringBuilder out = new StringBuilder(super.generateTag());
        super.label(out);
        return out.toString();
    }
}
