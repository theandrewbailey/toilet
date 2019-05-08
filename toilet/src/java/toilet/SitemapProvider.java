package toilet;

import java.util.ArrayList;
import java.util.Collections;
import javax.ejb.EJB;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.sitemap.AbstractPageSource;
import libWebsiteTools.sitemap.ChangeFreq;
import libWebsiteTools.sitemap.UrlMap;

@WebListener("Gives out the site map.")
public class SitemapProvider extends AbstractPageSource {

    @EJB
    private IMEADHolder imead;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String baseUrl = imead.getValue(libOdyssey.bean.GuardRepo.CANONICAL_URL);
        urlMap = new ArrayList<>();
        urlMap.add(new UrlMap(baseUrl, null, ChangeFreq.daily, "0.7"));
        urlMap.add(new UrlMap(baseUrl + "spruce", null, ChangeFreq.always, "0.1"));
        urlMap = Collections.unmodifiableList(urlMap);
        super.contextInitialized(sce);
    }
}
