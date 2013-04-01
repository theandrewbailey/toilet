package libWebsiteTools;

import java.util.ArrayList;
import org.w3c.dom.Node;

/**
 * populates an internal list (that is, itself) with direct child nodes of an XML node that have the given name.
 * @author alpha
 */
public class XmlNodeSearcher extends ArrayList<Node> {

    public XmlNodeSearcher(Node parent, String nodeName) {
        super();
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (nodeName.equals(child.getNodeName())) {
                add(child);
            }
        }
    }
}
