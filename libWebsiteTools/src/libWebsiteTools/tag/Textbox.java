package libWebsiteTools.tag;

import java.util.Collection;

public class Textbox extends AbstractInput {

    private Object datalist;

    @Override
    public String createTag() {
        if (null == datalist) {
            return super.createTag();
        }
        if (datalist instanceof Collection) {
            Collection datalistList = (Collection) datalist;
            StringBuilder out = label(new StringBuilder(400 + (datalistList.size() * 30)));
            String datalistId = "list-" + getId();
            return out.append(createIncompleteTag()).append(" list=\"list-").append(getId()).append("\"/>")
                    .append(Datalist.getDatalistTag(datalistId, datalistList)).toString();
        }else{
            StringBuilder out = label(new StringBuilder(400));
            return out.append(createIncompleteTag()).append(" list=\"").append(datalist.toString()).append("\"/>").toString();
        }
    }

    @Override
    public String getType() {
        return "text";
    }

    /**
     * @return the datalist
     */
    public Object getDatalist() {
        return datalist;
    }

    /**
     * @param datalist the datalist to set
     */
    public void setDatalist(Object datalist) {
        this.datalist = datalist;
    }
}
