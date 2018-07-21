package libWebsiteTools.tag;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.file.FileServlet;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;

/**
 *
 * @author alpha
 */
public class HtmlScript extends SimpleTagSupport {

    public static final String PAGE_JAVASCRIPT = "page_javascript";
    private static final String INTEGRITY_TEMPLATE = "<script type=\"%s\" src=\"%s?%s=%s\" async=\"true\" integrity=\"%s-%s\" crossorigin=\"anonymous\"></script>";
    private static final String TEMPLATE = "<script type=\"%s\" src=\"%s?%s=%s\" async=\"true\"></script>";
    @EJB
    private FileRepo file;
    @EJB
    private IMEADHolder imead;

    @Override
    public void doTag() throws IOException {
        for (String javascript : imead.getLocal(PAGE_JAVASCRIPT, Local.resolveLocales(getJspContext())).split("\n")) {
            // TOTAL HACK: this assumes that the CSS is hosted locally 
            try {
                Fileupload f = file.getFileMetadata(FileRepo.getFilename(javascript));
                // will generate a unique URL based on the file's last update time, so browsers will get and cache a new resource
                String queryparam = HtmlCss.getQueryParam(f);
                // TOTAL HACK: this assumes that the etag is a base64 sha-2 hash of the file contents ONLY, for subresource integrity
                switch (f.getEtag().length()) { // different flavors of sha-2 will have different digest lengths
                    case 44:
                        getJspContext().getOut().print(String.format(INTEGRITY_TEMPLATE,
                                f.getMimetype(), javascript, FileServlet.IMMUTABLE_PARAM, queryparam, "sha256", f.getEtag()));
                        break;
                    case 64:
                        getJspContext().getOut().print(String.format(INTEGRITY_TEMPLATE,
                                f.getMimetype(), javascript, FileServlet.IMMUTABLE_PARAM, queryparam, "sha384", f.getEtag()));
                        break;
                    case 88:
                        getJspContext().getOut().print(String.format(INTEGRITY_TEMPLATE,
                                f.getMimetype(), javascript, FileServlet.IMMUTABLE_PARAM, queryparam, "sha512", f.getEtag()));
                        break;
                    default: // can't recognize
                        getJspContext().getOut().print(String.format(TEMPLATE,
                                f.getMimetype(), javascript, FileServlet.IMMUTABLE_PARAM, queryparam));
                        break;
                }
            } catch (IOException e) {
                getJspContext().getOut().print(String.format("<script type=\"text/javascript\" src=\"%s\" async=\"true\"></script>", javascript));
            }
        }
    }
}
