package toilet.rss;

import libWebsiteTools.rss.entity.RssItem;
import org.w3c.dom.Element;

/**
 *
 * @author alphavm
 */
public class MarkdownRssItem extends RssItem {

    public static final String NAMESPACE = "markdown";
    public static final String ELEMENT_NAME = "source";
    public static final String FULL_ELEMENT_NAME = MarkdownRssItem.NAMESPACE + ":" + MarkdownRssItem.ELEMENT_NAME;
    public static final String MARKDOWN_URI = "urn:X-" + FULL_ELEMENT_NAME;
    private String markdownSource;

    /**
     * default constructor
     * please do not use
     */
    public MarkdownRssItem(){}

    /**
     * use this constructor
     * @param iDesc description of the item in the feed (required)
     */
    public MarkdownRssItem(String iDesc) {
        setDescription(iDesc);
    }

    @Override
    public Element publish(Element chan) {
        Element item = super.publish(chan);
        if (markdownSource != null) {
            Element n = chan.getOwnerDocument().createElementNS(MARKDOWN_URI, ELEMENT_NAME);
            n.setPrefix(NAMESPACE);
            n.appendChild(chan.getOwnerDocument().createCDATASection(getMarkdownSource()));
            item.appendChild(n);
        }
        return item;
    }

    public String getMarkdownSource() {
        return markdownSource;
    }

    public void setMarkdownSource(String markdownSource) {
        this.markdownSource = markdownSource;
    }
    
}
