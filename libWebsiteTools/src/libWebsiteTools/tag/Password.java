package libWebsiteTools.tag;

public class Password extends AbstractInput {

    @Override
    public String getType() {
        return "password";
    }

    @Override
    public String getPattern() {
        return null;
    }
}
