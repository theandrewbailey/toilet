package libWebsiteTools.tag;

public class Searchbox extends AbstractInput {

    @Override
    public String getType() {
        return "search";
    }

    @Override
    public Boolean getRequired() {
        return true;
    }

    @Override
    public String getPattern() {
        return null != pattern ? pattern : "^[\\u{0020}-\\u{007E}\\u{00A1}-\\u{052F}]*$";
    }
}
