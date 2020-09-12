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
import toilet.bean.SpruceGenerator;

@WebListener("Gives out the site map.")
public class SitemapProvider extends AbstractPageSource {

    @EJB
    private IMEADHolder imead;
    @EJB
    private SpruceGenerator spruce;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        urlMap = new ArrayList<>();
        try {
            String baseUrl = imead.getValue(libWebsiteTools.security.SecurityRepo.BASE_URL);
            urlMap.add(new UrlMap(baseUrl, null, ChangeFreq.daily, "0.7"));
            if (spruce.shouldBeReady()) {
                urlMap.add(new UrlMap(baseUrl + "spruce", null, ChangeFreq.always, "0.1"));
            }
        } catch (NullPointerException n) {
        }
        urlMap = Collections.unmodifiableList(urlMap);
        super.contextInitialized(sce);
    }
}
