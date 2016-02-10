package libWebsiteTools.sitemap;

import java.util.Date;

/**
 *
 * @author alpha
 */
public class UrlMap {

    private String location;
    private Date lastmod;
    private ChangeFreq changefreq;
    private String priority;

    public UrlMap(String loc, Date mod, ChangeFreq freq, String pri) {
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
    public Date getLastmod() {
        return lastmod;
    }

}
