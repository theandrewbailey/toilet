package libWebsiteTools.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.file.FileServlet;
import libWebsiteTools.file.Filemetadata;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;

/**
 *
 * @author alpha
 */
public class HtmlCss extends SimpleTagSupport {

    //private static final String INTEGRITY_TEMPLATE = "<link rel=\"prefetch\" class=\"defer-stylesheet\" href=\"%1$s?%2$s=%3$s\" integrity=\"%4$s-%5$s\"/><noscript><link rel=\"stylesheet\" href=\"%1$s?%2$s=%3$s\" integrity=\"%4$s-%5$s\"/></noscript>";
    //private static final String TEMPLATE = "<link rel=\"prefetch\" class=\"defer-stylesheet\" href=\"%1$s\"/><noscript><link rel=\"stylesheet\" href=\"%1$s\"/></noscript>";
    private static final String INTEGRITY_TEMPLATE = "<link rel=\"stylesheet\" href=\"%1$s\" integrity=\"%2$s-%3$s\"/>";
    private static final String TEMPLATE = "<link rel=\"stylesheet\" href=\"%1$s\"/>";
    public static final String PAGE_CSS_KEY = "page_css";
    @EJB
    private FileRepo file;
    @EJB
    private IMEADHolder imead;

    @SuppressWarnings("unchecked")
    public static List<Filemetadata> getCssFiles(HttpServletRequest req, IMEADHolder imead, FileRepo file) {
        try {
            List files = (List) req.getAttribute(PAGE_CSS_KEY);
            if (files != null) {
                return files;
            }
        } catch (Exception x) {
        }
        try {
            List<String> filenames = new ArrayList<>();
            List<Filemetadata> files = new ArrayList<>();
            for (String filename : imead.getLocal(PAGE_CSS_KEY, Local.resolveLocales(req)).split("\n")) {
                List<Filemetadata> f = file.getFileMetadata(Arrays.asList(filename));
                if (null != f && !f.isEmpty()) {
                    files.addAll(f);
                } else {
                    filenames.add(FileServlet.getNameFromURL(filename));
                }
            }
            files.addAll(file.getFileMetadata(filenames));
            req.setAttribute(PAGE_CSS_KEY, files);
            return files;
        } catch (Exception x) {
            return new ArrayList<>();
        }
    }

    @Override
    public void doTag() throws IOException {
        JspWriter output = getJspContext().getOut();
        for (Filemetadata f : getCssFiles((HttpServletRequest) ((PageContext) getJspContext()).getRequest(), imead, file)) {
            // TOTAL HACK: this assumes that the CSS is hosted locally 
            try {
                // will create a unique URL based on the file's last update time, so browsers will get and cache a new resource
                String url = f.getUrl();
                // TOTAL HACK: this assumes that the etag is a base64 sha-2 hash of the file contents ONLY, for subresource integrity
                switch (f.getEtag().length()) { // different flavors of sha-2 will have different digest lengths
                    case 44:
                        output.print(String.format(INTEGRITY_TEMPLATE, url, "sha256", f.getEtag()));
                        break;
                    case 64:
                        output.print(String.format(INTEGRITY_TEMPLATE, url, "sha384", f.getEtag()));
                        break;
                    case 88:
                        output.print(String.format(INTEGRITY_TEMPLATE, url, "sha512", f.getEtag()));
                        break;
                    default: // can't recognize
                        output.print(String.format(TEMPLATE, url));
                        break;
                }
            } catch (IOException | NullPointerException e) {
                //output.print(String.format(TEMPLATE, css));
            }
        }
    }
}
