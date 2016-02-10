package toilet.bean;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;
import libWebsiteTools.HashUtil;
import libWebsiteTools.imead.IMEADHolder;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.db.Section;

/**
 *
 * @author alpha
 */
@Stateless
public class EntryRepo {

    public static final String DEFAULT_CATEGORY = "entry_defaultCategory";
    public static final String LOCAL_NAME = "java:module/EntryRepo";
    private static final Logger log = Logger.getLogger(EntryRepo.class.getName());
    @PersistenceUnit
    private EntityManagerFactory toiletPU;
    @EJB
    private IMEADHolder imead;
    @EJB
    private StateCache cache;

    public void evict() {
        toiletPU.getCache().evict(Article.class);
        toiletPU.getCache().evict(Comment.class);
        toiletPU.getCache().evict(Section.class);
    }

    public List<Article> getSection(String sect, Integer page, Integer perPage) {
        log.log(Level.FINE, "Retrieving articles, section {0}, page {1}, per page {2}", new Object[]{sect, page, perPage});
        if (imead.getValue(EntryRepo.DEFAULT_CATEGORY).equals(sect)) {
            sect = null;
        }
        EntityManager em = toiletPU.createEntityManager();
        try {
            TypedQuery<Article> q = sect == null
                    ? em.createQuery("SELECT a FROM Article a ORDER BY a.posted DESC", Article.class)
                    : em.createQuery("SELECT a FROM Article a WHERE a.sectionid.name=:s ORDER BY a.posted DESC", Article.class).setParameter("s", sect);

            if (page != null && perPage != null) {
                q.setFirstResult(perPage * (page - 1));        // pagination start
                q.setMaxResults(perPage);                      // pagination limit
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Long countArticlesInSection(String sect) {
        log.log(Level.FINE, "Counting articles in section {0}", sect);
        if (imead.getValue(EntryRepo.DEFAULT_CATEGORY).equals(sect)) {
            sect = null;
        }
        EntityManager em = toiletPU.createEntityManager();
        try {
            TypedQuery<Long> qn = sect == null
                    ? em.createQuery("SELECT COUNT(a) FROM Article a", Long.class)
                    : em.createQuery("SELECT COUNT(a) FROM Article a WHERE a.sectionid.name=:s", Long.class).setParameter("s", sect);
            Long output = qn.getSingleResult();
            log.log(Level.FINE, "Counted articles in section {0}, got {1}", new Object[]{sect, output});
            return output;
        } finally {
            em.close();
        }
    }

    /**
     * will save articles (or just one)
     *
     * @param articles map of article to section
     * @return last article saved
     */
    public Article addArticles(Map<Article, String> articles) {
        Article dbArt = null;
        EntityManager em = toiletPU.createEntityManager();

        try {
            em.getTransaction().begin();
            for (Map.Entry<Article, String> entry : articles.entrySet()) {
                Article art = entry.getKey();
                String sect = entry.getValue();
                boolean getnew = art.getArticleid() == null;
                dbArt = getnew ? new Article() : em.find(Article.class, art.getArticleid());
                dbArt = dbArt == null ? new Article() : dbArt;

                dbArt.setModified(new Date(new Date().getTime() / 1000 * 1000));
                dbArt.setPosted(art.getPosted() == null ? dbArt.getModified() : art.getPosted());
                dbArt.setComments(art.getComments());
                dbArt.setCommentCollection(art.getComments() ? dbArt.getCommentCollection() : null);
                dbArt.setArticletitle(art.getArticletitle());
                dbArt.setPostedhtml(art.getPostedhtml());
                dbArt.setPostedmarkdown(art.getPostedmarkdown());
                dbArt.setPostedname(art.getPostedname());
                dbArt.setDescription(art.getDescription());

                if (dbArt.getSectionid() == null || !dbArt.getSectionid().getName().equals(sect)) {
                    Section esec;
                    TypedQuery<Section> q = em.createQuery("SELECT s FROM Section s WHERE s.name = :name", Section.class);
                    q.setParameter("name", sect);
                    try {
                        esec = q.getSingleResult();
                    } catch (NoResultException ex) {
                        esec = new Section();
                        esec.setName(sect);
                        em.persist(esec);
                    }
                    dbArt.setSectionid(esec);
                }
                dbArt.setEtag(HashUtil.getHashAsBase64(hashArticle(dbArt, dbArt.getCommentCollection(), sect)));

                if (getnew) {
                    em.persist(dbArt);
                }
                log.log(Level.INFO, "Entry added {0}, section {1}", new Object[]{art.getArticletitle(), sect});
            }

            em.getTransaction().commit();
            cache.reset();
            return dbArt;
        } finally {
            em.close();
        }
    }

    public Article getArticle(Integer articleId) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            return em.find(Article.class, articleId);
        } finally {
            em.close();
        }
    }

    public List<Article> getArticleArchive(Integer limit) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            TypedQuery<Article> q = em.createQuery("SELECT a FROM Article a ORDER BY a.posted DESC", Article.class);
            if (null != limit) {
                q.setMaxResults(limit);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Comment> getCommentArchive(Integer limit) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            TypedQuery<Comment> q = em.createQuery("SELECT c FROM Comment c ORDER BY c.posted DESC", Comment.class);
            if (null != limit) {
                q.setMaxResults(limit);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * will add comments
     *
     * @param comments map of comment to article id
     * @return last updated article
     */
    public Article addComments(Map<Comment, Integer> comments) {
        Article e = null;
        HashSet<Integer> updatedArticles = new HashSet<>(comments.size() * 2);
        EntityManager em = toiletPU.createEntityManager();
        try {
            em.getTransaction().begin();
            for (Map.Entry<Comment, Integer> entry : comments.entrySet()) {
                e = em.find(Article.class, entry.getValue());
                Comment c = entry.getKey();
                if (c.getPosted() == null) {
                    c.setPosted(new Date());
                }
                c.setArticleid(e);
                em.persist(c);
                log.info("Comment added");
                updatedArticles.add(e.getArticleid());
            }
            em.getTransaction().commit();
            for (Integer articleid : updatedArticles) {
                Article a = em.find(Article.class, articleid);
                em.refresh(a);
                updateArticleHash(articleid);
            }
            return e;
        } finally {
            em.close();
        }
    }

    public void deleteArticle(Integer articleId) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            Article e = em.find(Article.class, articleId);
            em.getTransaction().begin();

            if (e.getSectionid().getArticleCollection().size() == 1) {
                em.remove(e);
                em.remove(e.getSectionid());
            } else {
                em.remove(e);
            }

            em.getTransaction().commit();
            log.info("Article deleted");
        } finally {
            em.close();
        }
    }

    public void deleteComment(Integer commentId) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            em.getTransaction().begin();
            Comment c = em.find(Comment.class, commentId);
            Article e = c.getArticleid();
            em.remove(c);
            em.getTransaction().commit();
            log.info("Comment deleted");
            em.refresh(e);
            updateArticleHash(e.getArticleid());
        } finally {
            em.close();
        }
    }

    public void deleteEverything() {
        EntityManager em = toiletPU.createEntityManager();
        try {
            em.getTransaction().begin();
            for (Comment c : getCommentArchive(null)) {
                em.remove(em.find(Comment.class, c.getCommentid()));
            }
            for (Article a : getArticleArchive(null)) {
                Article e = em.find(Article.class, a.getArticleid());
                if (e.getSectionid().getArticleCollection().size() == 1) {
                    em.remove(e);
                    em.remove(e.getSectionid());
                } else {
                    em.remove(e);
                }
            }
            em.createNativeQuery("ALTER SEQUENCE toilet.comment_commentid_seq RESTART").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE toilet.article_articleid_seq RESTART").executeUpdate();
            em.getTransaction().commit();
            log.info("All articles and comments deleted");
        } finally {
            em.close();
        }
    }

    private void updateArticleHash(Integer articleId) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            em.getTransaction().begin();
            Article art = em.find(Article.class, articleId);
            art.setEtag(HashUtil.getBase64(hashArticle(art, art.getCommentCollection(), art.getSectionid().getName())));
            art.setModified(new Date(new Date().getTime() / 1000 * 1000));
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    private byte[] hashArticle(Article e, Collection<Comment> comments, String sect) {
        MessageDigest sha = HashUtil.getSHA256();
        sha.update(e.getArticletitle().getBytes());
        sha.update(e.getPostedhtml().getBytes());
        sha.update(sect.getBytes());
        sha.update(e.getPostedname().getBytes());
        if (e.getDescription() != null) {
            sha.update(e.getDescription().getBytes());
        }
        sha.update(e.getModified().toString().getBytes());
        if (comments != null) {
            for (Comment c : comments) {
                sha.update(c.getPostedhtml().getBytes());
                sha.update(c.getPosted().toString().getBytes());
                sha.update(c.getPostedname().getBytes());
            }
        }
        return sha.digest();
    }
}
