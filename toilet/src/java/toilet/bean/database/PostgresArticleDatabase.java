package toilet.bean.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import libWebsiteTools.imead.IMEADHolder;
import toilet.db.Article;

/**
 *
 * @author alpha
 */
public class PostgresArticleDatabase extends ArticleDatabase {

    public PostgresArticleDatabase(EntityManagerFactory toiletPU, IMEADHolder imead) {
        super(toiletPU, imead);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Article> search(String searchTerm, Integer limit) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            Query q = em.createNativeQuery("SELECT r.* FROM toilet.article r, websearch_to_tsquery(?1) query WHERE query @@ r.searchindexdata ORDER BY ts_rank_cd(r.searchindexdata, query) DESC, r.posted", Article.class);
            q.setParameter(1, searchTerm);
            if (null != limit) {
                q.setMaxResults(limit);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<String> getSearchSuggestion(String searchTerm, Integer limit) {
        if (null == searchTerm || searchTerm.isEmpty()) {
            return null;
        }
        EntityManager em = toiletPU.createEntityManager();
        List<String> suggestions = new ArrayList<>();
        try {
            Query q = em.createNativeQuery("SELECT word, similarity(?1, word) FROM toilet.articlewords WHERE (word % ?1) = TRUE ORDER BY similarity DESC");
            List results = q.setParameter(1, searchTerm).setMaxResults(limit).getResultList();
            Iterator iter = results.iterator();
            while (iter.hasNext()) {
                Object[] row = (Object[]) iter.next();
                if (1 == limit) {
                    Float sim = (Float) row[1];
                    if (0.4f < sim) {
                        suggestions.add(row[0].toString());
                        break;
                    }
                } else {
                    Float sim = (Float) row[1];
                    if (0.3f < sim) {
                        suggestions.add(row[0].toString());
                    }
                }
            }
            return suggestions;
        } catch (NoSuchElementException | NullPointerException n) {
        } finally {
            em.close();
        }
        return null;
    }

    @Override
    public void refreshSearch() {
        EntityManager em = toiletPU.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("REFRESH MATERIALIZED VIEW toilet.articlewords").executeUpdate();
        em.createNativeQuery("ANALYZE toilet.articlewords").executeUpdate();
        em.getTransaction().commit();
        em.close();
    }

}
