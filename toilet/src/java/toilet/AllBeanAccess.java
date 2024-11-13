package toilet;

import jakarta.ejb.Local;
import libWebsiteTools.Repository;
import toilet.bean.ArticleRepository;
import toilet.bean.BackupDaemon;
import toilet.bean.database.Comment;
import toilet.bean.database.Section;

/**
 * Easy way to ensure static functions have access to requisite bean classes.
 *
 * @author alpha
 */
@Local
public interface AllBeanAccess extends libWebsiteTools.AllBeanAccess {

    public ArticleRepository getArts();

    public Repository<Comment> getComms();

    public Repository<Section> getSects();

    public BackupDaemon getBackup();
}
