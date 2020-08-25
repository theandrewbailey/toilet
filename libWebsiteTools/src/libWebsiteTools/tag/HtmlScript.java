package libWebsiteTools.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
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
public class HtmlScript extends SimpleTagSupport {

    public static final String SITE_JAVASCRIPT_KEY = "site_javascript";
    private static final String INTEGRITY_TEMPLATE = "<script type=\"%s\" src=\"%s\" async=\"async\" integrity=\"%s-%s\"></script>";
    private static final String TEMPLATE = "<script type=\"%s\" src=\"%s\" async=\"async\"></script>";
    @EJB
    private FileRepo file;
    @EJB
    private IMEADHolder imead;

    @SuppressWarnings("unchecked")
    public static List<Filemetadata> getJavascriptFiles(HttpServletRequest req, IMEADHolder imead, FileRepo file) {
        try {
            List files = (List) req.getAttribute(SITE_JAVASCRIPT_KEY);
            if (files != null) {
                return files;
            }
        } catch (Exception x) {
        }
        try {
            List<String> filenames = new ArrayList<>();
            List<Filemetadata> files = new ArrayList<>();
            for (String filename : imead.getLocal(SITE_JAVASCRIPT_KEY, Local.resolveLocales(req, imead)).split("\n")) {
                List<Filemetadata> f = file.getFileMetadata(Arrays.asList(filename));
                if (null != f && !f.isEmpty()) {
                    files.addAll(f);
                } else {
                    filenames.add(FileServlet.getNameFromURL(filename));
                }
            }
            files.addAll(file.getFileMetadata(filenames));
            req.setAttribute(SITE_JAVASCRIPT_KEY, files);
            return files;
        } catch (Exception x) {
            return new ArrayList<>();
        }
    }

    @Override
    public void doTag() throws IOException {
        for (Filemetadata f : getJavascriptFiles((HttpServletRequest) ((PageContext) getJspContext()).getRequest(), imead, file)) {
            // TOTAL HACK: this assumes that the CSS is hosted locally 
            try {
                // will create a unique URL based on the file's last update time, so browsers will get and cache a new resource
                String url = f.getUrl();
                // TOTAL HACK: this assumes that the etag is a base64 sha-2 hash of the file contents ONLY, for subresource integrity
                switch (f.getEtag().length()) { // different flavors of sha-2 will have different digest lengths
                    case 44:
                        getJspContext().getOut().print(String.format(INTEGRITY_TEMPLATE, f.getMimetype(), url, "sha256", f.getEtag()));
                        break;
                    case 64:
                        getJspContext().getOut().print(String.format(INTEGRITY_TEMPLATE, f.getMimetype(), url, "sha384", f.getEtag()));
                        break;
                    case 88:
                        getJspContext().getOut().print(String.format(INTEGRITY_TEMPLATE, f.getMimetype(), url, "sha512", f.getEtag()));
                        break;
                    default: // can't recognize
                        getJspContext().getOut().print(String.format(TEMPLATE, f.getMimetype(), url));
                        break;
                }
            } catch (IOException | NullPointerException e) {
                //getJspContext().getOut().print(String.format("<script type=\"text/javascript\" src=\"%s\" async=\"true\"></script>", javascript));
            }
        }
    }
}
