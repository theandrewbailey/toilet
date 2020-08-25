package libWebsiteTools.rss;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * represents a single entry in an rss channel (article, post, podcast...)
 * shouldn't need to extend unless you are adding a namespace to the feed
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 */
public class RssItem implements Serializable, iPublishable {

    private String title;           // standard RSS parameters
    private String link;
    private String description = "Replace this description";
    private String author;
    private String comments;
    private String guid;
    private String src;             // source parameters (name and url where it comes from)
    private String srcUrl;
    private String enclosure;       // enclosure parameters
    private String eMime;
    private Integer eLength;
    private Date pubDate = new Date();
    private boolean guidPermaLink = true;
    private final ArrayList<RssCategory> cats = new ArrayList<>();

    /**
     * default constructor
     * please do not use
     */
    public RssItem(){}

    /**
     * use this constructor
     * @param iDesc description of the item in the feed (required)
     */
    public RssItem(String iDesc) {
        description = iDesc;
    }

    /**
     * add a category to this item
     * @param name
     * @param domain
     */
    public void addCategory(String name, String domain) {
        cats.add(new RssCategory(name, domain));
    }

    @Override
    public Element publish(Element chan) {
        Document XML = chan.getOwnerDocument();
        Element item = XML.createElement("item");
        Element n;
        chan.appendChild(item);

        RssChannel.textNode(item, "title", getTitle(), true);
        RssChannel.textNode(item, "link", getLink(), true);
        RssChannel.textNode(item, "author", getAuthor(), true);

        for (RssCategory c : cats) {
            c.publish(item);
        }

        RssChannel.textNode(item, "comments", getComments(), true);

        if (getEnclosure() != null) {
            n = XML.createElement("enclosure");
            n.setAttribute("url", getEnclosure());
            n.setAttribute("length", getELength().toString());
            n.setAttribute("type", getEMime());
            item.appendChild(n);
        }

        if (getGuid() != null) {
            n = RssChannel.textNode(item, "guid", getGuid(), false);

            if (!isGuidPermaLink()) {
                n.setAttribute("isPermaLink", "false");
            }
        }
        if (getPubDate() != null) {
            RssChannel.textNode(item, "pubDate", new SimpleDateFormat(FeedBucket.TIME_FORMAT).format(getPubDate()), true);
        }
        if (getSrc() != null) {
            n = RssChannel.textNode(item, "source", getSrc(), true);
            n.setAttribute("url", getSrcUrl());
        }

        RssChannel.textNode(item, "description", getDescription(), true);
        return item;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * @param link the link to set
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the comments
     */
    public String getComments() {
        return comments;
    }

    /**
     * @param comments the comments to set
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * @return the guid
     */
    public String getGuid() {
        return guid;
    }

    /**
     * @param guid the guid to set
     */
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     * @return the src
     */
    public String getSrc() {
        return src;
    }

    /**
     * @param src the src to set
     */
    public void setSrc(String src) {
        this.src = src;
    }

    /**
     * @return the srcUrl
     */
    public String getSrcUrl() {
        return srcUrl;
    }

    /**
     * @param srcUrl the srcUrl to set
     */
    public void setSrcUrl(String srcUrl) {
        this.srcUrl = srcUrl;
    }

    /**
     * @return the enclosure
     */
    public String getEnclosure() {
        return enclosure;
    }

    /**
     * @param enclosure the enclosure to set
     */
    public void setEnclosure(String enclosure) {
        this.enclosure = enclosure;
    }

    /**
     * @return the eMime
     */
    public String getEMime() {
        return eMime;
    }

    /**
     * @param eMime the eMime to set
     */
    public void setEMime(String eMime) {
        this.eMime = eMime;
    }

    /**
     * @return the eLength
     */
    public Integer getELength() {
        return eLength;
    }

    /**
     * @param eLength the eLength to set
     */
    public void setELength(Integer eLength) {
        this.eLength = eLength;
    }

    /**
     * @return the pubDate
     */
    public Date getPubDate() {
        return pubDate;
    }

    /**
     * @param pubDate the pubDate to set
     */
    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    /**
     * @return the guidPermaLink
     */
    public boolean isGuidPermaLink() {
        return guidPermaLink;
    }

    /**
     * @param guidPermaLink the guidPermaLink to set
     */
    public void setGuidPermaLink(boolean guidPermaLink) {
        this.guidPermaLink = guidPermaLink;
    }
}