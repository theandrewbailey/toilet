package libWebsiteTools.rss.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.w3c.dom.Element;

/**
 *
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 */
public class AtomDate extends AtomCommonAttribs {

    private Date dateTime;

    @Override
    public Element publish(Element xml) {
        Element item = super.publish(xml, "updated");
        item.setTextContent(new SimpleDateFormat(DATE_FORMAT).format(getDateTime()));
        return item;
    }

    /**
     * @return the dateTime
     */
    public Date getDateTime() {
        return dateTime;
    }

    /**
     * @param dateTime the dateTime to set
     */
    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

}
