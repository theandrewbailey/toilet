package libWebsiteTools.turbo;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 *
 * @author alpha
 */
public class RequestTimes implements Comparable<RequestTimes> {

    private final String url;
    private final Map<String, Duration> timings;
    private final OffsetDateTime when;
    private final Boolean cached;

    public RequestTimes(String url, Map<String, Duration> timings, OffsetDateTime when, Boolean cached) {
        this.url = url;
        this.timings = timings;
        this.when = when;
        this.cached = cached;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the timings
     */
    public Map<String, Duration> getTimings() {
        return timings;
    }

    /**
     * @return the when
     */
    public OffsetDateTime getWhen() {
        return when;
    }

    /**
     * @return the cached
     */
    public Boolean isCached() {
        return cached;
    }

    @Override
    public int compareTo(RequestTimes t) {
        return Long.compare(this.getTimings().get("total").toNanos(), t.getTimings().get("total").toNanos());
    }
}
