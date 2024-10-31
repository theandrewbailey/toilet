package libWebsiteTools.turbo;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.RuntimeDelegate;
import libWebsiteTools.security.GuardFilter;

/**
 *
 * @author alpha
 */
public class CachedPage {

    private static final List<String> EXCLUDE_HEADERS = Arrays.asList(GuardFilter.STRICT_TRANSPORT_SECURITY, HttpHeaders.SET_COOKIE, HttpHeaders.VARY, RequestTimer.SERVER_TIMING);
    private final Map<String, String> headers;
    private final byte[] body;
    private final int status;
    private final OffsetDateTime expires;
    private final OffsetDateTime created = OffsetDateTime.now();
    private final AtomicInteger hits = new AtomicInteger();
    private final String contentType;
    private final String lookup;

    public CachedPage(HttpServletResponse res, byte[] capturedBody, String lookup) {
        body = capturedBody;
        Map<String, String> heads = new HashMap<>();
        for (String header : res.getHeaderNames()) {
            if (!EXCLUDE_HEADERS.contains(header)) {
                heads.put(header, res.getHeader(header));
            }
        }
        heads.put(HttpHeaders.CONTENT_LENGTH, Integer.toString(capturedBody.length));
        this.headers = Collections.unmodifiableMap(heads);
        status = res.getStatus();
        CacheControl cc = RuntimeDelegate.getInstance().createHeaderDelegate(CacheControl.class).fromString(res.getHeader(HttpHeaders.CACHE_CONTROL));
        expires = created.plusSeconds(cc.getMaxAge());
        contentType = res.getContentType();
        this.lookup = lookup;
    }

    public boolean isApplicable(HttpServletRequest req) {
        return true;
    }

    public boolean isExpired(OffsetDateTime lastFlush) {
        return lastFlush.isAfter(getCreated()) || OffsetDateTime.now().isAfter(getExpires());
    }

    public int hit() {
        return hits.incrementAndGet();
    }

    public int getHits() {
        return hits.get();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String header) {
        return headers.get(header);
    }

    public byte[] getBody() {
        return body;
    }

    public int getStatus() {
        return status;
    }

    public String getContentType() {
        return contentType;
    }

    public OffsetDateTime getExpires() {
        return expires;
    }

    public String getLookup() {
        return lookup;
    }

    public OffsetDateTime getCreated() {
        return created;
    }
}
