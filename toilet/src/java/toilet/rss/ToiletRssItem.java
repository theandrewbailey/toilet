package toilet.rss;

import libWebsiteTools.rss.RssItem;
import org.w3c.dom.Element;

/**
 *
 * @author alpha
 */
public class ToiletRssItem extends RssItem {

    private static final String TAB_URI = "https://theandrewbailey.com/";
    private static final String TAB_NAMESPACE = "tab";

    private static final String MARKDOWN_NAME = "markdownsource";
    public static final String MARKDOWN_ELEMENT_NAME = ToiletRssItem.TAB_NAMESPACE + ":" + ToiletRssItem.MARKDOWN_NAME;

    private static final String TAB_METADESC_NAME = "metadescription";
    public static final String TAB_METADESC_ELEMENT_NAME = ToiletRssItem.TAB_NAMESPACE + ":" + ToiletRssItem.TAB_METADESC_NAME;

    private String markdownSource;
    private String metadescription;

    /**
     * default constructor please do not use
     */
    public ToiletRssItem() {
    }

    /**
     * use this constructor
     *
     * @param iDesc description of the item in the feed (required)
     */
    public ToiletRssItem(String iDesc) {
        setDescription(iDesc);
    }

    @Override
    public Element publish(Element chan) {
        Element item = super.publish(chan);
        if (markdownSource != null) {
            Element n = chan.getOwnerDocument().createElementNS(TAB_URI, MARKDOWN_NAME);
            n.setPrefix(TAB_NAMESPACE);
            n.appendChild(chan.getOwnerDocument().createCDATASection(getMarkdownSource()));
            item.appendChild(n);
        }
        if (null != metadescription) {
            Element n = chan.getOwnerDocument().createElementNS(TAB_URI, TAB_METADESC_NAME);
            n.setPrefix(TAB_NAMESPACE);
            n.appendChild(chan.getOwnerDocument().createCDATASection(getMetadescription()));
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

    /**
     * @return the metadescription
     */
    public String getMetadescription() {
        return metadescription;
    }

    /**
     * @param metadescription the metadescription to set
     */
    public void setMetadescription(String metadescription) {
        this.metadescription = metadescription;
    }

}
