package libWebsiteTools.tag;

import java.io.IOException;
import java.math.BigInteger;
import javax.ejb.EJB;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.HashUtil;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.file.FileServlet;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;

/**
 *
 * @author alpha
 */
public class HtmlCss extends SimpleTagSupport {

    @EJB
    private FileRepo file;
    @EJB
    private IMEADHolder imead;

    @Override
    public void doTag() throws IOException {
        for (String css : imead.getLocal("page_css", Local.resolveLocales(getJspContext())).split("\n")) {
            // TOTAL HACK: this assumes that the CSS is hosted locally 
            try {
                Fileupload f = file.getFile(file.getFilename(css));
                // will generate a unique URL based on the file's last update time, so browsers will get and cache a new resource
                long time = f.getAtime().getTime() & FileServlet.MAX_AGE_MILLISECONDS >> 8;
                String queryparam = HashUtil.getBase64(BigInteger.valueOf(time).toByteArray(), "~~");
                // TOTAL HACK: this assumes that the etag is a base64 sha-2 hash of the file contents ONLY, for subresource integrity
                switch (f.getEtag().length()) { // different flavors of sha-2 will have different digest lengths
                    case 44:
                        getJspContext().getOut().print(String.format("<link rel=\"stylesheet\" href=\"%s?%s\" integrity=\"sha256-%s\"/>",
                                css, queryparam, f.getEtag()));
                        break;
                    case 64:
                        getJspContext().getOut().print(String.format("<link rel=\"stylesheet\" href=\"%s?%s\" integrity=\"sha384-%s\"/>",
                                css, queryparam, f.getEtag()));
                        break;
                    case 88:
                        getJspContext().getOut().print(String.format("<link rel=\"stylesheet\" href=\"%s?%s\" integrity=\"sha512-%s\"/>",
                                css, queryparam, f.getEtag()));
                        break;
                    default: // can't recognize
                        getJspContext().getOut().print(String.format("<link rel=\"stylesheet\" href=\"%s?%s\"/>",
                                css, queryparam));
                        break;
                }
            } catch (Exception e) {
                getJspContext().getOut().print(String.format("<link rel=\"stylesheet\" href=\"%s\"/>", css));
            }
        }
    }
}
