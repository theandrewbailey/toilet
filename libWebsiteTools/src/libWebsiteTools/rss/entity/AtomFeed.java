package libWebsiteTools.rss.entity;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import libWebsiteTools.rss.iFeed;
import libWebsiteTools.rss.iPublishable;

/**
 *
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 */
public class AtomFeed extends AtomCommonAttribs implements iFeed, iPublishable {

    private final List<AtomPerson> authors = new ArrayList<AtomPerson>();
    private final List<AtomCategory> categorys = new ArrayList<AtomCategory>();
    private final List<AtomPerson> contributors = new ArrayList<AtomPerson>();
    private final List<AtomLink> links = new ArrayList<AtomLink>();
    private final List<AtomEntry> entries = new ArrayList<AtomEntry>();
    private AtomGenerator generator = new AtomGenerator();
    private AtomId id;
    private AtomId logo;
    private AtomText rights;
    private AtomText title;
    private AtomText subtitle;
    private AtomDate updated = new AtomDate();

    // TODO: this, add "links"
    /**
     * unlike what publish() is supposed to do, this takes the root element,
     * and appends the feed's elements directly to it
     * @param root of the document
     * @return root of the document (what else?)
     */
    @Override
    public Element publish(Element root) {
        super.publish(root);
        for (AtomPerson p : getAuthors()) {
            p.publish(root, "author");
        }
        for (AtomCategory c : getCategorys()) {
            c.publish(root);
        }
        for (AtomLink l : getLinks()) {
            l.publish(root, "link");
        }
        for (AtomPerson c : getContributors()) {
            c.publish(root, "contributor");
        }
        if (getGenerator() != null) {
            getGenerator().publish(root);
        }
        getId().publish(root, "id");
        if (getLogo() != null) {
            getLogo().publish(root, "logo");
        }
        if (getRights() != null) {
            getRights().publish(root, "rights");
        }
        getTitle().publish(root, "title");
        if (getSubtitle() != null) {
            getSubtitle().publish(root, "subtitle");
        }
        getUpdated().publish(root);
        for (AtomEntry e : getEntries()) {
            e.publish(root, "entry");
        }
        return root;
    }

    /**
     * rebuild the DOM behind this feed
     * @return feed XML
     */
    public Document refreshFeed() {
        Document XML = null;
        try {
            XML = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = XML.createElement("feed");
            XML.createAttributeNS("http://www.w3.org/2005/Atom", "");

            XML.appendChild(publish(root));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return XML;
    }

    /**
     * @return the authors
     */
    public List<AtomPerson> getAuthors() {
        return authors;
    }

    /**
     * @return the categorys
     */
    public List<AtomCategory> getCategorys() {
        return categorys;
    }

    /**
     * @return the contributors
     */
    public List<AtomPerson> getContributors() {
        return contributors;
    }

    /**
     * @return the generator
     */
    public AtomGenerator getGenerator() {
        return generator;
    }

    public List<AtomLink> getLinks() {
        return links;
    }

    public List<AtomEntry> getEntries() {
        return entries;
    }

    /**
     * @param generator the generator to set
     */
    public void setGenerator(AtomGenerator generator) {
        this.generator = generator;
    }

    /**
     * @return the id
     */
    public AtomId getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(AtomId id) {
        this.id = id;
    }

    /**
     * @return the logo
     */
    public AtomId getLogo() {
        return logo;
    }

    /**
     * @param logo the logo to set
     */
    public void setLogo(AtomId logo) {
        this.logo = logo;
    }

    /**
     * @return the rights
     */
    public AtomText getRights() {
        return rights;
    }

    /**
     * @param rights the rights to set
     */
    public void setRights(AtomText rights) {
        this.rights = rights;
    }

    /**
     * @return the title
     */
    public AtomText getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(AtomText title) {
        this.title = title;
    }

    /**
     * @return the subtitle
     */
    public AtomText getSubtitle() {
        return subtitle;
    }

    /**
     * @param subtitle the subtitle to set
     */
    public void setSubtitle(AtomText subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * @return the updated
     */
    public AtomDate getUpdated() {
        return updated;
    }

    /**
     * @param updated the updated to set
     */
    public void setUpdated(AtomDate updated) {
        this.updated = updated;
    }

    /**
     * @param req
     * @return result of refreshFeed()
     */
    @Override
    public Document preWrite(HttpServletRequest req) {
        return refreshFeed();
    }

    @Override
    public void postWrite(HttpServletRequest req) {
    }

    @Override
    public void preAdd() {
    }

    @Override
    public void postAdd() {
    }

    @Override
    public void preRemove() {
    }

    @Override
    public void postRemove() {
    }
}
