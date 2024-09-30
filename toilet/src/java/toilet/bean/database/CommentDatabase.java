package toilet.bean.database;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import libWebsiteTools.db.Repository;
import toilet.bean.ArticleRepository;
import toilet.db.Article;
import toilet.db.Comment;

/**
 *
 * @author alpha
 */
public class CommentDatabase implements Repository<Comment> {

    private static final Logger LOG = Logger.getLogger(CommentDatabase.class.getName());
    private final EntityManagerFactory toiletPU;

    public CommentDatabase(EntityManagerFactory toiletPU) {
        this.toiletPU = toiletPU;
    }

    /**
     * only supports adding comments.
     *
     * @param entities
     * @return
     */
    @Override
    public List<Comment> upsert(Collection<Comment> entities) {
        ArrayList<Comment> out = new ArrayList<>(entities.size());
        HashSet<Integer> updatedArticles = new HashSet<>(entities.size() * 2);
        EntityManager em = toiletPU.createEntityManager();
        try {
            em.getTransaction().begin();
            for (Comment c : entities) {
                Article art = em.find(Article.class, c.getArticleid().getArticleid());
                if (c.getPosted() == null) {
                    c.setPosted(OffsetDateTime.now());
                }
                c.setArticleid(art);
                em.persist(c);
                out.add(c);
                LOG.info("Comment added");
                updatedArticles.add(art.getArticleid());
            }
            em.getTransaction().commit();
            em.getTransaction().begin();
            for (Integer articleid : updatedArticles) {
                Article a = em.find(Article.class, articleid);
                em.refresh(a);
                ArticleRepository.updateArticleHash(a);
            }
            em.getTransaction().commit();
            return out;
        } finally {
            em.close();
        }
    }

    @Override
    public Comment get(Object commentId) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            return em.find(Comment.class, commentId);
        } finally {
            em.close();
        }
    }

    @Override
    public Comment delete(Object commentId) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            em.getTransaction().begin();
            Comment c = em.find(Comment.class, commentId);
            Article e = c.getArticleid();
            em.remove(c);
            em.getTransaction().commit();
            LOG.info("Comment deleted");
            em.refresh(e);
            em.getTransaction().begin();
            ArticleRepository.updateArticleHash(e);
            em.getTransaction().commit();
            return c;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Comment> getAll(Integer limit) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            TypedQuery<Comment> q = em.createNamedQuery("Comment.findAll", Comment.class);
            if (null != limit) {
                q.setMaxResults(limit);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void processArchive(Consumer<Comment> operation, Boolean transaction) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CommentDatabase evict() {
        toiletPU.getCache().evict(Comment.class);
        return this;
    }

    @Override
    public Long count() {
        LOG.log(Level.FINE, "Counting comments");
        EntityManager em = toiletPU.createEntityManager();
        try {
            TypedQuery<Long> qn = em.createNamedQuery("Comment.count", Long.class);
            Long output = qn.getSingleResult();
            LOG.log(Level.FINE, "Counted comments, got {0}", new Object[]{output});
            return output;
        } finally {
            em.close();
        }
    }

}
