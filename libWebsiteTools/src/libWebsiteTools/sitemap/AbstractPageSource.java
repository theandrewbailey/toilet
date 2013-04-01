package libWebsiteTools.sitemap;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author alpha
 */
public abstract class AbstractPageSource implements ServletContextListener, Iterable<UrlMap> {

    @EJB
    private SiteMaster siteMaster;
    @SuppressWarnings("unchecked")
    protected List<UrlMap> urlMap = Collections.EMPTY_LIST;

    @Override
    public Iterator<UrlMap> iterator() {
        return urlMap.iterator();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        siteMaster.addSource(this);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
