package libWebsiteTools.tag;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.AllBeanAccess;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.file.BaseFileServlet;
import libWebsiteTools.file.Filemetadata;
import libWebsiteTools.imead.Local;
import libWebsiteTools.rss.Feed;
import libWebsiteTools.rss.DynamicFeed;

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
    public static final String LDJSON = "$_HTML_LDJSON";
    private static final String CSS_INTEGRITY_TEMPLATE = "<link rel=\"stylesheet\" href=\"%1$s\" integrity=\"%2$s-%3$s\"/>";
    private static final String CSS_TEMPLATE = "<link rel=\"stylesheet\" href=\"%1$s\"/>";
    public static final String SITE_CSS_KEY = "site_css";
    private boolean showCss = true;

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
    public static void addLDJSON(HttpServletRequest req, String json) {
        if (null == json) {
            return;
        }
        List jsons = (List) req.getAttribute(LDJSON);
        if (jsons == null) {
            jsons = new ArrayList<>();
            req.setAttribute(LDJSON, jsons);
        }
        jsons.add(json);
    }

    public static JsonObjectBuilder getLDBreadcrumb(String name, Integer position, String url) {
        JsonObjectBuilder crumb = Json.createObjectBuilder().add("@type", "ListItem").add("position", position).add("name", name);
        if (null != url) {
            crumb.add("item", url);
        }
        return crumb;
    }

    @SuppressWarnings("unchecked")
    public static List<Filemetadata> getCssFiles(AllBeanAccess beans, HttpServletRequest req) {
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
            for (String filename : beans.getImead().getLocal(SITE_CSS_KEY, Local.resolveLocales(beans.getImead(), req)).split("\n")) {
                List<Filemetadata> f = beans.getFile().getFileMetadata(Arrays.asList(filename));
                if (null != f && !f.isEmpty()) {
                    files.addAll(f);
                } else {
                    filenames.add(BaseFileServlet.getNameFromURL(filename));
                }
            }
            files.addAll(beans.getFile().getFileMetadata(filenames));
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
        HttpServletRequest req = ((HttpServletRequest) ((PageContext) getJspContext()).getRequest());
        AllBeanAccess beans = (AllBeanAccess) req.getAttribute(AllBeanAccess.class.getCanonicalName());
        if (showCss) {
            output.println(String.format("<meta charset=\"%s\"/>", ((PageContext) getJspContext()).getResponse().getCharacterEncoding()));
            Object baseURL = ((HttpServletRequest) ((PageContext) getJspContext()).getRequest()).getAttribute(SecurityRepo.BASE_URL);
            if (null != baseURL) {
                output.println(String.format("<base href=\"%s\"/>", baseURL.toString()));
            }
            for (Filemetadata f : getCssFiles(beans, (HttpServletRequest) ((PageContext) getJspContext()).getRequest())) {
                // TOTAL HACK: this assumes that the CSS is hosted locally 
                try {
                    // will create a unique URL based on the file's last update time, so browsers will get and cache a new resource
                    String url = f.getUrl();
                    // TOTAL HACK: this assumes that the etag is a base64 sha-2 hash of the file contents ONLY, for subresource integrity
                    switch (f.getEtag().length()) { // different flavors of sha-2 will have different digest lengths
                        case 44:
                            output.println(String.format(CSS_INTEGRITY_TEMPLATE, url, "sha256", f.getEtag()));
                            break;
                        case 64:
                            output.println(String.format(CSS_INTEGRITY_TEMPLATE, url, "sha384", f.getEtag()));
                            break;
                        case 88:
                            output.println(String.format(CSS_INTEGRITY_TEMPLATE, url, "sha512", f.getEtag()));
                            break;
                        default: // can't recognize
                            output.println(String.format(CSS_TEMPLATE, url));
                            break;
                    }
                } catch (IOException | NullPointerException e) {
                    //output.print(String.format(TEMPLATE, css));
                }
            }
        }
        try {
            for (Map.Entry<String, String> tag : (List<Map.Entry<String, String>>) getJspContext().findAttribute(META_NAME_TAGS)) {
                output.println(String.format("<meta name=\"%s\" content=\"%s\"/>", tag.getKey(), tag.getValue()));
            }
        } catch (NullPointerException n) {
        }
        try {
            for (Map.Entry<String, String> tag : (List<Map.Entry<String, String>>) getJspContext().findAttribute(META_PROPERTY_TAGS)) {
                output.println(String.format("<meta property=\"%s\" content=\"%s\"/>", tag.getKey(), tag.getValue()));
            }
        } catch (NullPointerException n) {
        }
        try {
            for (Map.Entry<String, String> tag : (List<Map.Entry<String, String>>) getJspContext().findAttribute(LINK_TAGS)) {
                output.println(String.format("<link rel=\"%s\" href=\"%s\">", tag.getKey(), tag.getValue()));
            }
        } catch (NullPointerException n) {
        }
        for (Feed feed : beans.getFeeds().getAll(null)) {
            if (feed instanceof DynamicFeed) {
                for (Map.Entry<String, String> entry : ((DynamicFeed) feed).getFeedURLs(req).entrySet()) {
                    output.println(String.format("<link rel=\"alternate\" href=\"%srss/%s\" title=\"%s\" type=\"%s\">",
                            beans.getImeadValue(SecurityRepo.BASE_URL), entry.getKey(), entry.getValue(), feed.getMimeType().toString()));
                }
            }
        }
        try {
            for (Object json: (List) getJspContext().findAttribute(LDJSON)) {
                output.println(String.format("<script type=\"application/ld+json\">%s</script>", json.toString()));
            }
        } catch (NullPointerException n) {
        }
    }

    /**
     * @param showCss the showCss to set
     */
    public void setShowCss(boolean showCss) {
        this.showCss = showCss;
    }
}
