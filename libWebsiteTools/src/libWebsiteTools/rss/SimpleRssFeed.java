package libWebsiteTools.rss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * a complete implementation of an RSS feed (includes DOM)
 *
 * @author alpha
 */
public abstract class SimpleRssFeed extends AbstractRssFeed {

    protected final Collection<RssChannel> channels = new ArrayList<>();

    /**
     *
     * @param chan
     */
    public void addChannel(RssChannel chan) {
        channels.add(chan);
    }

    /**
     * builds the entire DOM behind this RSS feed with all built-in channels,
     * controlled by addChannel and removeChannel
     *
     * @return XML document
     */
    public Document refreshFeed() {
        return refreshFeed(channels);
    }

    /**
     * rebuild the XML DOM behind this RSS feed, with the given channel
     *
     * @param channel
     * @return RSS XML output
     */
    public Document refreshFeed(RssChannel channel) {
        return refreshFeed(Arrays.asList(channel));
    }

    /**
     * rebuild the DOM behind this RSS feed, with the given channels
     *
     * @param channels
     * @return RSS XML output
     */
    public static Document refreshFeed(Collection<RssChannel> channels) {
        Document XML = null;
        try {
            XML = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = XML.createElement("rss");
            XML.appendChild(root);
            root.setAttribute("version", "2.0");
            for (RssChannel chan : channels) {
                chan.publish(root);
            }
        } catch (ParserConfigurationException | DOMException x) {
            throw new RuntimeException(x.getMessage(), x);
        }
        return XML;
    }

    /**
     * @param req
     * @return result of refreshFeed() (the no param override)
     */
    @Override
    public Document preWrite(HttpServletRequest req, HttpServletResponse res) {
        return refreshFeed();
    }
}
