package toilet.servlet;

import jakarta.servlet.annotation.WebServlet;

/**
 * Defines the permissions used to administer the site. A password is associated
 * with each one. When entered into /adminLogin, the password is checked against
 * the hash stored at the given IMEAD key. If successful, the user is directed
 * to the URL associated with the matching hash.
 *
 * @author alpha
 */
public enum AdminServletPermission {
    EDIT_POSTS("admin_editPosts", AdminPostServlet.class.getAnnotation(WebServlet.class).urlPatterns()[0]),
    FILES("admin_files", AdminFileServlet.class.getAnnotation(WebServlet.class).urlPatterns()[0]),
    HEALTH("admin_health", AdminHealthServlet.class.getAnnotation(WebServlet.class).urlPatterns()[0]),
    IMEAD("admin_imead", AdminImeadServlet.class.getAnnotation(WebServlet.class).urlPatterns()[0]),
    IMPORT_EXPORT("admin_importExport", AdminImportServlet.class.getAnnotation(WebServlet.class).urlPatterns()[0]);

    private final String key;
    private final String url;

    AdminServletPermission(String imeadKey, String path) {
        this.key = imeadKey;
        this.url = path;
    }

    public String getKey() {
        return key;
    }

    public String getUrl() {
        return url;
    }
}
