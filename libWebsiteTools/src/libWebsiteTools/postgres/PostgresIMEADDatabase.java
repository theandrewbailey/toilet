package libWebsiteTools.postgres;

import jakarta.persistence.EntityManagerFactory;
import libWebsiteTools.imead.IMEADDatabase;

/**
 *
 * @author alpha
 */
public class PostgresIMEADDatabase extends IMEADDatabase {
    
    public PostgresIMEADDatabase(EntityManagerFactory PU) {
        super(PU);
    }
    
}
