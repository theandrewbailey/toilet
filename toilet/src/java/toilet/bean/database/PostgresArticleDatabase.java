package toilet.bean.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import libWebsiteTools.imead.IMEADHolder;
import toilet.bean.ArticleRepository;
import toilet.db.Article;

/**
 *
 * @author alpha
 */
public class PostgresArticleDatabase extends ArticleDatabase {

    private final Map<String, List<Article>> searchCache = Collections.synchronizedMap(new LinkedHashMap<>(100));

    public PostgresArticleDatabase(EntityManagerFactory toiletPU, IMEADHolder imead) {
        super(toiletPU, imead);
    }

    @Override
    public ArticleRepository evict() {
        searchCache.clear();
        return super.evict();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Article> search(String searchTerm, Integer limit) {
        if (searchCache.containsKey(searchTerm)) {
            return searchCache.get(searchTerm);
        }
        try (EntityManager em = toiletPU.createEntityManager()) {
            Query q = em.createNativeQuery("SELECT r.* FROM toilet.article r, websearch_to_tsquery(?1) query WHERE query @@ r.searchindexdata ORDER BY ts_rank_cd(r.searchindexdata, query) DESC, r.posted", Article.class);
            q.setParameter(1, searchTerm);
            if (null != limit) {
                q.setMaxResults(limit);
            }
            if (60 < searchCache.size()) {
                searchCache.remove(searchCache.keySet().iterator().next());
            }
            searchCache.put(searchTerm, q.getResultList());
            return q.getResultList();
        }
    }

    @Override
    public List<String> getSearchSuggestion(String searchTerm, Integer limit) {
        if (null == searchTerm || searchTerm.isEmpty()) {
            return null;
        }
        List<String> suggestions = new ArrayList<>();
        try (EntityManager em = toiletPU.createEntityManager()) {
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
        }
        return null;
    }

    @Override
    public void refreshSearch() {
        try (EntityManager em = toiletPU.createEntityManager()) {
            em.getTransaction().begin();
            em.createNativeQuery("REFRESH MATERIALIZED VIEW toilet.articlewords").executeUpdate();
            em.createNativeQuery("ANALYZE toilet.articlewords").executeUpdate();
            em.getTransaction().commit();
        }
    }
}
