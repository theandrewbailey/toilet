package libWebsiteTools.tag;

import java.io.IOException;
import java.math.BigInteger;
import javax.ejb.EJB;
import javax.servlet.jsp.JspWriter;
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
    //private static final String INTEGRITY_TEMPLATE = "<link rel=\"prefetch\" class=\"defer-stylesheet\" href=\"%1$s?%2$s=%3$s\" integrity=\"%4$s-%5$s\"/><noscript><link rel=\"stylesheet\" href=\"%1$s?%2$s=%3$s\" integrity=\"%4$s-%5$s\"/></noscript>";
    //private static final String TEMPLATE = "<link rel=\"prefetch\" class=\"defer-stylesheet\" href=\"%1$s\"/><noscript><link rel=\"stylesheet\" href=\"%1$s\"/></noscript>";
    private static final String INTEGRITY_TEMPLATE = "<link rel=\"stylesheet\" href=\"%1$s?%2$s=%3$s\" integrity=\"%4$s-%5$s\" crossorigin=\"anonymous\"/>";
    private static final String TEMPLATE = "<link rel=\"stylesheet\" href=\"%1$s\"/>";

    @Override
    public void doTag() throws IOException {
        JspWriter output = getJspContext().getOut();
        for (String css : imead.getLocal("page_css", Local.resolveLocales(getJspContext())).split("\n")) {
            // TOTAL HACK: this assumes that the CSS is hosted locally 
            try {
                Fileupload f = file.getFileMetadata(FileRepo.getFilename(css));
                // will generate a unique URL based on the file's last update time, so browsers will get and cache a new resource
                String queryparam = getQueryParam(f);
                // TOTAL HACK: this assumes that the etag is a base64 sha-2 hash of the file contents ONLY, for subresource integrity
                switch (f.getEtag().length()) { // different flavors of sha-2 will have different digest lengths
                    case 44:
                        output.print(String.format(INTEGRITY_TEMPLATE, css, FileServlet.IMMUTABLE_PARAM, queryparam, "sha256", f.getEtag()));
                        break;
                    case 64:
                        output.print(String.format(INTEGRITY_TEMPLATE, css, FileServlet.IMMUTABLE_PARAM, queryparam, "sha384", f.getEtag()));
                        break;
                    case 88:
                        output.print(String.format(INTEGRITY_TEMPLATE, css, FileServlet.IMMUTABLE_PARAM, queryparam, "sha512", f.getEtag()));
                        break;
                    default: // can't recognize
                        output.print(String.format(TEMPLATE, css + "?" + FileServlet.IMMUTABLE_PARAM + "=" + queryparam));
                        break;
                }
            } catch (IOException e) {
                output.print(String.format(TEMPLATE, css));
            }
        }
    }

    public static String getQueryParam(Fileupload f) throws IOException {
        if (null == f) {
            throw new IOException("CSS file not available.");
        }
        return getQueryParam(f.getAtime().getTime());
    }

    public static String getQueryParam(Long milliseconds) {
        long time = (milliseconds & FileServlet.MAX_AGE_MILLISECONDS) >> 8;
        return HashUtil.getBase64(BigInteger.valueOf(time).toByteArray(), "~~").replace('/', '|');
    }
}
