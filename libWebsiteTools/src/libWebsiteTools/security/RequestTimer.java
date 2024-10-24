package libWebsiteTools.security;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author alpha
 */
@WebListener("Set time")
public class RequestTimer implements ServletRequestListener {

    public static final String SERVER_TIMING = "Server-Timing";
    private static final String START_TIME_PARAM = "$_LIBWEBSITETOOLS_START_TIME";
    private static final String FRONT_TIME_PARAM = "$_LIBWEBSITETOOLS_FRONT_TIME";
    private static final String REQUEST_TIMINGS_PARAM = "$_LIBWEBSITETOOLS_REQUEST_TIMINGS";

    public static OffsetDateTime getStartTime(ServletRequest req) {
        OffsetDateTime time = (OffsetDateTime) req.getAttribute(START_TIME_PARAM);
        if (null == time) {
            time = OffsetDateTime.now();
            req.setAttribute(START_TIME_PARAM, time);
        }
        return time;
    }

    public static OffsetDateTime getFrontTime(ServletRequest req) {
        OffsetDateTime time = (OffsetDateTime) req.getAttribute(FRONT_TIME_PARAM);
        if (null == time) {
            time = OffsetDateTime.now();
            req.setAttribute(FRONT_TIME_PARAM, time);
        }
        return time;
    }

    public static Duration getElapsed(OffsetDateTime time) {
        return Duration.between(time, OffsetDateTime.now()).abs();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Duration> getTimings(ServletRequest req) {
        Map<String, Duration> timings = (Map) (req.getAttribute(REQUEST_TIMINGS_PARAM));
        if (null == timings) {
            timings = new LinkedHashMap<>();
            req.setAttribute(REQUEST_TIMINGS_PARAM, timings);
        }
        return timings;
    }

    public static void addTiming(ServletRequest req, String name, Duration elapsed) {
        getTimings(req).put(name, elapsed);
    }

    public static String getTimingHeader(ServletRequest req, Boolean cached) {
        ArrayList<String> parts = new ArrayList<>();
        if (cached) {
            parts.add("hit");
        } else if (false == cached) {
            parts.add("miss");
        }
        OffsetDateTime time = (OffsetDateTime) req.getAttribute(FRONT_TIME_PARAM);
        if (null != time) {
            Duration d = Duration.between(getStartTime(req), time).abs();
            addTiming(req, "back", d);
            d = getElapsed(time);
            addTiming(req, "front", d);
        }
        addTiming(req, "total", getElapsed(getStartTime(req)));
        for (Map.Entry<String, Duration> timing : getTimings(req).entrySet()) {
            parts.add(timing.getKey() + String.format(";dur=%.3f", timing.getValue().toNanos() / 1000000.0));
        }
        String header = String.join(", ", parts);
        return header;
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        HttpServletRequest req = (HttpServletRequest) sre.getServletRequest();
        getStartTime(req);
    }
}
