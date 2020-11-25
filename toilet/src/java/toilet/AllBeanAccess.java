package toilet;

import javax.ejb.Local;
import toilet.bean.ArticleRepo;
import toilet.bean.BackupDaemon;
import toilet.bean.CommentRepo;
import toilet.bean.SectionRepo;

/**
 * Easy way to ensure static functions have access to requisite bean classes.
 *
 * @author alpha
 */
@Local
public interface AllBeanAccess extends libWebsiteTools.AllBeanAccess {

    public ArticleRepo getArts();

    public CommentRepo getComms();

    public SectionRepo getSects();

    public BackupDaemon getBackup();
}
