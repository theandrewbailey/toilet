package toilet.bean;

import libWebsiteTools.Repository;
import toilet.db.Section;

/**
 *
 * @author alpha
 */
public abstract class SectionRepository implements Repository<Section> {

    public abstract Long count(String section);
}
