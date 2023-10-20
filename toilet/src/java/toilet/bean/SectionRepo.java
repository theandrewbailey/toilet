package toilet.bean;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import libWebsiteTools.db.Repository;
import libWebsiteTools.imead.IMEADHolder;
import toilet.UtilStatic;
import toilet.db.Article;
import toilet.db.Section;

/**
 *
 * @author alpha
 */
public class SectionRepo implements Repository<Section> {

    private final EntityManagerFactory toiletPU;
    private final IMEADHolder imead;
    private List<Section> allSections;

    public SectionRepo(EntityManagerFactory toiletPU, IMEADHolder imead) {
        this.toiletPU = toiletPU;
        this.imead = imead;
    }

    @Override
    public Collection<Section> upsert(Collection<Section> entities) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Section get(Object id) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            if (id instanceof Integer) {
                return em.find(Section.class, id);
            } else if (id instanceof String) {
                return em.createNamedQuery("Section.findByName", Section.class).setParameter("name", id).getSingleResult();
            }
        } catch (NoResultException n) {
            return null;
        } finally {
            em.close();
        }
        throw new IllegalArgumentException("Bad type. Must be String or Integer.");
    }

    @Override
    public Section delete(Object id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * @param limit ignored
     * @return sections sorted by popularity
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<Section> getAll(Integer limit) {
        if (null == allSections) {
            EntityManager em = toiletPU.createEntityManager();
            Article empty = new Article();
            try {
                List<Object[]> sectionsByArticlesPosted = em.createNamedQuery("Section.byArticlesPosted").getResultList();
                em.close();
                double now = OffsetDateTime.now().toInstant().toEpochMilli();
                TreeMap<Double, Section> popularity = new TreeMap<>();
                for (Object[] data : sectionsByArticlesPosted) {
                    Section section = (Section) data[0];
                    if (section.getName().equals(imead.getValue(ArticleRepo.DEFAULT_CATEGORY))) {
                        continue;
                    }
                    double years = (now - ((OffsetDateTime) data[1]).toInstant().toEpochMilli()) / 31536000000.0;
                    double points = ((Long) data[2]).doubleValue();
                    // score = average posts per year since category first started
                    double score = UtilStatic.score(points, years, 1.8);
                    popularity.put(score, section);
                    List<Article> articles = new ArrayList<>(section.getArticleCollection().size());
                    for (int i = 0; i < section.getArticleCollection().size(); i++) {
                        articles.add(empty);
                    }
                    section.setArticleCollection(articles);
                }
                allSections = new ArrayList<>(popularity.values());
                Collections.reverse(allSections);
            } catch (ArrayIndexOutOfBoundsException ex) {
                return new ArrayList<>();
            }
        }
        return allSections;
    }

    @Override
    public void processArchive(Consumer<Section> operation, Boolean transaction) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            if (transaction) {
                em.getTransaction().begin();
                em.createNamedQuery("Section.findAll", Section.class).getResultStream().forEachOrdered(operation);
                em.getTransaction().commit();
            } else {
                em.createNamedQuery("Section.findAll", Section.class).getResultStream().forEachOrdered(operation);
            }
        } finally {
            em.close();
        }
    }

    @Override
    public void evict() {
        toiletPU.getCache().evict(Section.class);
        allSections = null;
    }

    @Override
    public Long count() {
        long count = getAll(null).size();
        return count;
    }

}
