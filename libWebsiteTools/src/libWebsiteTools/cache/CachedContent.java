package libWebsiteTools.cache;

import java.util.List;
import java.util.regex.Pattern;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import libWebsiteTools.file.BaseFileServlet;

/**
 *
 * @author alpha
 */
public class CachedContent extends CachedPage {

    private final List<Pattern> acceptableDomains;

    public CachedContent(List<Pattern> acceptableDomains, HttpServletResponse res, byte[] capturedBody, String lookup) {
        super(res, capturedBody, lookup);
        this.acceptableDomains = acceptableDomains;
    }

    @Override
    public boolean isApplicable(HttpServletRequest req) {
        return BaseFileServlet.isAuthorized(req, getContentType(), acceptableDomains);
    }
}
