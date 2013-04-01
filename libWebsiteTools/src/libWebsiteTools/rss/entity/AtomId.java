package libWebsiteTools.rss.entity;

import org.w3c.dom.Element;

/**
 *
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 */
public class AtomId extends AtomCommonAttribs {

    private String uri = "http://you.should.have.used.the.other.constructor";

    public AtomId() {
    }

    public AtomId(String u) {
        uri = u;
    }

    @Override
    public Element publish(Element xml, String name) {
        Element item = super.publish(xml, name);
        item.setTextContent(getUri());
        return item;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
}
