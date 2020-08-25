package libWebsiteTools.cache;

import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.file.FileServlet;

/**
 *
 * @author alpha
 */
public class CachedContent extends CachedPage {

    private final List<Pattern> acceptableDomains;

    public CachedContent(HttpServletResponse res, byte[] capturedBody, List<Pattern> acceptableDomains) {
        super(res, capturedBody);
        this.acceptableDomains = acceptableDomains;
    }

    @Override
    public boolean isApplicable(HttpServletRequest req) {
        return FileServlet.isAuthorized(getContentType(), acceptableDomains, req);
    }
}
