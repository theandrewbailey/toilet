package libWebsiteTools.postgres;

import jakarta.persistence.EntityManagerFactory;
import libWebsiteTools.file.FileDatabase;

/**
 *
 * @author alpha
 */
public class PostgresFileDatabase extends FileDatabase {
    
    public PostgresFileDatabase(EntityManagerFactory PU) {
        super(PU);
    }
    
}
