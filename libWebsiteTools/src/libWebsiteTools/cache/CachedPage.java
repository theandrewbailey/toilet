package libWebsiteTools.cache;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.HttpHeaders;
import libWebsiteTools.security.GuardFilter;

/**
 *
 * @author alpha
 */
public class CachedPage {

    private static final List<String> EXCLUDE_HEADERS = Arrays.asList(GuardFilter.STRICT_TRANSPORT_SECURITY, HttpHeaders.SET_COOKIE);
    private final Map<String, String> headers;
    private final byte[] body;
    private final int status;
    private final Date expires;
    private final Date created = new Date();
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
        this.headers = Collections.unmodifiableMap(heads);
        status = res.getStatus();
        CacheControl cc = CacheControl.valueOf(res.getHeader(HttpHeaders.CACHE_CONTROL));
        int diff = 400 > status ? cc.getMaxAge() * 1000 : cc.getMaxAge();
        expires = new Date(new Date().getTime() + diff);
        contentType = res.getContentType();
        this.lookup = lookup;
    }

    public boolean isApplicable(HttpServletRequest req) {
        return true;
    }

    public boolean isExpired(Date lastFlush) {
        Date now = new Date();
        return lastFlush.after(getCreated()) || now.after(getExpires());
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

    public Date getExpires() {
        return expires;
    }

    public String getLookup() {
        return lookup;
    }

    public Date getCreated() {
        return created;
    }
}
