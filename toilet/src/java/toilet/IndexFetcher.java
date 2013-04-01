package toilet;

import java.util.List;
import libWebsiteTools.imead.IMEADHolder;
import toilet.bean.EntryRepo;
import toilet.bean.UtilBean;
import toilet.db.Article;

/**
 *
 * @author alpha
 */
public class IndexFetcher {

    public static final String PPP = "index_ppp";
    public static final String PAC = "index_pac";
    private IMEADHolder imead;
    private UtilBean util;
    private EntryRepo entry;
    private String URI;
    private int page = 1;
    private String group = null;
    private int pagesAroundCurrent = 3;
    private int postsPerPage = 5;
    private boolean valid = false;
    private int count = 0;
    private int first = 1;

    public IndexFetcher(String inURI) {
        util = UtilStatic.getBean(UtilBean.LOCAL_NAME, UtilBean.class);
        entry = UtilStatic.getBean(EntryRepo.LOCAL_NAME, EntryRepo.class);
        imead = UtilStatic.getBean(UtilBean.IMEAD_LOCAL_NAME, IMEADHolder.class);
        URI = inURI;
        try {
            page = Integer.valueOf(UtilStatic.getPageNumber(URI));
            group = util.getIdFromURI(URI.replace("%20", " "));
        } catch (Exception e) {
            return;
        }
        try {
            Integer.valueOf(group);
            group = null;
        } catch (Exception e) {
        }
        if (imead.getValue(EntryRepo.DEFAULT_CATEGORY).equals(group)) {
            group = null;
        }
        valid = true;

        // pagination params
        if (imead.getValue(PPP) != null) {
            postsPerPage = Integer.parseInt(imead.getValue(PPP));
        }
        if (imead.getValue(PAC) != null) {
            pagesAroundCurrent = Integer.parseInt(imead.getValue(PAC));
        }
        // get total of all, to display number of pages limit
        if (count == 0) {
            double counted = entry.countArticlesInSection(group);
            count = (int) Math.ceil(counted / postsPerPage);
        }
        // wierd algoritim to determine how many pagination links to other pages on this page
        if (page + pagesAroundCurrent > count) {
            first = count - pagesAroundCurrent * 2;
        } else if (page - pagesAroundCurrent > 0) {
            first = page - pagesAroundCurrent;
        }
        if (first < 1) {
            first = 1;
        }
    }

    public List<Article> getArticles() {
        return entry.getSection(group, page, postsPerPage);
    }

    public int getCount() {
        return count;
    }

    public int getFirst() {
        return first;
    }

    public int getLast() {
        return Math.min(first + pagesAroundCurrent * 2, count);
    }

    public String getLink() {
        return imead.getValue(UtilBean.THISURL) + "index/" + (group != null ? group + "/" : "");
    }

    public String getDescription() {
        return imead.getValue(UtilBean.SITE_TITLE) + ", " + (group == null ? "all categories, page " : group + " category, page ") + page;
    }

    public boolean isValid() {
        return valid;
    }

    public String getGroup() {
        return group;
    }

    public int getPage() {
        return page;
    }
}
