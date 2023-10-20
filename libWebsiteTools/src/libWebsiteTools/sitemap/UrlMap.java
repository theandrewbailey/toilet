package libWebsiteTools.sitemap;

import java.time.OffsetDateTime;

/**
 *
 * @author alpha
 */
public class UrlMap {

    private final String location;
    private final OffsetDateTime lastmod;
    private final ChangeFreq changefreq;
    private final String priority;

    public UrlMap(String loc, OffsetDateTime mod, ChangeFreq freq, String pri) {
        if (loc.length() > 2047) {
            location = loc.substring(0, 2048);
        } else {
            location = loc;
        }
        lastmod = mod;
        changefreq = freq;
        priority = pri;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return the changefreq
     */
    public ChangeFreq getChangefreq() {
        return changefreq;
    }

    /**
     * @return the priority
     */
    public String getPriority() {
        return priority;
    }

    /**
     * @return the lastmod
     */
    public OffsetDateTime getLastmod() {
        return lastmod;
    }

}
