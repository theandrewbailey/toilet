package libWebsiteTools.tag;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.file.BaseFileServlet;
import libWebsiteTools.file.Filemetadata;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.rss.iDynamicFeed;
import libWebsiteTools.rss.iFeed;

/**
 * puts <meta> and <link> tags on pages, including stylesheets. will link all
 * css files described in imead.localization site_css value, unless showCss is
 * false.
 *
 * @author alpha
 */
public class HtmlMeta extends SimpleTagSupport {

    public static final String META_NAME_TAGS = "$_HTML_META_NAME_TAGS";
    public static final String META_PROPERTY_TAGS = "$_HTML_META_PROPERTY_TAGS";
    public static final String LINK_TAGS = "$_HTML_LINK_TAGS";
    private static final String CSS_INTEGRITY_TEMPLATE = "<link rel=\"stylesheet\" href=\"%1$s\" integrity=\"%2$s-%3$s\"/>";
    private static final String CSS_TEMPLATE = "<link rel=\"stylesheet\" href=\"%1$s\"/>";
    public static final String SITE_CSS_KEY = "site_css";
    private boolean showCss = true;
    @EJB
    private FileRepo file;
    @EJB
    private IMEADHolder imead;
    @EJB
    private FeedBucket bucket;

    @SuppressWarnings("unchecked")
    public static void addPropertyTag(HttpServletRequest req, String property, String content) {
        List tags = (List) req.getAttribute(META_PROPERTY_TAGS);
        if (tags == null) {
            tags = new ArrayList<>();
            req.setAttribute(META_PROPERTY_TAGS, tags);
        }
        tags.add(new AbstractMap.SimpleEntry<>(property, content));
    }

    @SuppressWarnings("unchecked")
    public static void addNameTag(HttpServletRequest req, String name, String content) {
        List tags = (List) req.getAttribute(META_NAME_TAGS);
        if (tags == null) {
            tags = new ArrayList<>();
            req.setAttribute(META_NAME_TAGS, tags);
        }
        tags.add(new AbstractMap.SimpleEntry<>(name, content));
    }

    @SuppressWarnings("unchecked")
    public static void addLink(HttpServletRequest req, String rel, String href) {
        if (null == rel || null == href) {
            return;
        }
        List tags = (List) req.getAttribute(LINK_TAGS);
        if (tags == null) {
            tags = new ArrayList<>();
            req.setAttribute(LINK_TAGS, tags);
        }
        tags.add(new AbstractMap.SimpleEntry<>(rel, href));
    }

    @SuppressWarnings("unchecked")
    public static List<Filemetadata> getCssFiles(HttpServletRequest req, IMEADHolder imead, FileRepo file) {
        try {
            List files = (List) req.getAttribute(SITE_CSS_KEY);
            if (files != null) {
                return files;
            }
        } catch (Exception x) {
        }
        try {
            List<String> filenames = new ArrayList<>();
            List<Filemetadata> files = new ArrayList<>();
            for (String filename : imead.getLocal(SITE_CSS_KEY, Local.resolveLocales(req, imead)).split("\n")) {
                List<Filemetadata> f = file.getFileMetadata(Arrays.asList(filename));
                if (null != f && !f.isEmpty()) {
                    files.addAll(f);
                } else {
                    filenames.add(BaseFileServlet.getNameFromURL(filename));
                }
            }
            files.addAll(file.getFileMetadata(filenames));
            req.setAttribute(SITE_CSS_KEY, files);
            return files;
        } catch (Exception x) {
            return new ArrayList<>();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void doTag() throws JspException, IOException {
        JspWriter output = getJspContext().getOut();
        if (showCss) {
            output.print(String.format("<meta charset=\"%s\"/>", ((PageContext) getJspContext()).getResponse().getCharacterEncoding()));
            Object baseURL = ((HttpServletRequest) ((PageContext) getJspContext()).getRequest()).getAttribute(SecurityRepo.BASE_URL);
            if (null != baseURL) {
                output.print(String.format("<base href=\"%s\"/>", baseURL.toString()));
            }
            for (Filemetadata f : getCssFiles((HttpServletRequest) ((PageContext) getJspContext()).getRequest(), imead, file)) {
                // TOTAL HACK: this assumes that the CSS is hosted locally 
                try {
                    // will create a unique URL based on the file's last update time, so browsers will get and cache a new resource
                    String url = f.getUrl();
                    // TOTAL HACK: this assumes that the etag is a base64 sha-2 hash of the file contents ONLY, for subresource integrity
                    switch (f.getEtag().length()) { // different flavors of sha-2 will have different digest lengths
                        case 44:
                            output.print(String.format(CSS_INTEGRITY_TEMPLATE, url, "sha256", f.getEtag()));
                            break;
                        case 64:
                            output.print(String.format(CSS_INTEGRITY_TEMPLATE, url, "sha384", f.getEtag()));
                            break;
                        case 88:
                            output.print(String.format(CSS_INTEGRITY_TEMPLATE, url, "sha512", f.getEtag()));
                            break;
                        default: // can't recognize
                            output.print(String.format(CSS_TEMPLATE, url));
                            break;
                    }
                } catch (IOException | NullPointerException e) {
                    //output.print(String.format(TEMPLATE, css));
                }
            }
        }
        try {
            for (Map.Entry<String, String> tag : (List<Map.Entry<String, String>>) getJspContext().findAttribute(META_NAME_TAGS)) {
                output.print(String.format("<meta name=\"%s\" content=\"%s\"/>", tag.getKey(), tag.getValue()));
            }
        } catch (NullPointerException n) {
        }
        try {
            for (Map.Entry<String, String> tag : (List<Map.Entry<String, String>>) getJspContext().findAttribute(META_PROPERTY_TAGS)) {
                output.print(String.format("<meta property=\"%s\" content=\"%s\"/>", tag.getKey(), tag.getValue()));
            }
        } catch (NullPointerException n) {
        }
        try {
            for (Map.Entry<String, String> tag : (List<Map.Entry<String, String>>) getJspContext().findAttribute(LINK_TAGS)) {
                output.print(String.format("<link rel=\"%s\" href=\"%s\">", tag.getKey(), tag.getValue()));
            }
        } catch (NullPointerException n) {
        }
        HttpServletRequest req = ((HttpServletRequest) ((PageContext) getJspContext()).getRequest());
        for (iFeed feed : bucket.getAll(null)) {
            if (feed instanceof iDynamicFeed) {
                for (Map.Entry<String, String> entry : ((iDynamicFeed) feed).getFeedURLs(req).entrySet()) {
                    output.print(String.format("<link rel=\"alternate\" href=\"%srss/%s\" title=\"%s\" type=\"%s\">",
                            imead.getValue(SecurityRepo.BASE_URL), entry.getKey(), entry.getValue(), feed.getMimeType().toString()));
                }
            }
        }
    }

    /**
     * @param showCss the showCss to set
     */
    public void setShowCss(boolean showCss) {
        this.showCss = showCss;
    }
}
