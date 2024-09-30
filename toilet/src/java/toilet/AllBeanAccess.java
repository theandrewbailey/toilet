package toilet;

import jakarta.ejb.Local;
import libWebsiteTools.db.Repository;
import toilet.bean.ArticleRepository;
import toilet.bean.BackupDaemon;
import toilet.bean.database.CommentDatabase;
import toilet.bean.database.SectionDatabase;
import toilet.db.Comment;
import toilet.db.Section;
//import toilet.bean.SpruceGenerator;

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

//    public SpruceGenerator getSpruce();
}
