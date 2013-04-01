package toilet;

import java.util.ArrayList;
import javax.ejb.EJB;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.sitemap.AbstractPageSource;
import libWebsiteTools.sitemap.ChangeFreq;
import libWebsiteTools.sitemap.UrlMap;
import toilet.bean.UtilBean;

@WebListener("Gives out the site map.")
public class SitemapProvider extends AbstractPageSource {

    @EJB
    private IMEADHolder imead;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String baseUrl = imead.getValue(UtilBean.THISURL);
        urlMap = new ArrayList<UrlMap>();
        urlMap.add(new UrlMap(baseUrl, null, ChangeFreq.daily, "0.7"));
        urlMap.add(new UrlMap(baseUrl + "spruce", null, ChangeFreq.always, "0.1"));
        super.contextInitialized(sce);
    }
}
