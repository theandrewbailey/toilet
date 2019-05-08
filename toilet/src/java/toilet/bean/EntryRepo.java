package toilet.bean;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceUnit;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import libOdyssey.bean.GuardRepo;
import libWebsiteTools.HashUtil;
import libWebsiteTools.JVMNotSupportedError;
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

    private static final Logger LOG = Logger.getLogger(EntryRepo.class.getName());
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
        LOG.log(Level.FINE, "Retrieving articles, section {0}, page {1}, per page {2}", new Object[]{sect, page, perPage});
        if (imead.getValue(EntryRepo.DEFAULT_CATEGORY).equals(sect)) {
            sect = null;
        }
        EntityManager em = toiletPU.createEntityManager();
        try {
            TypedQuery<Article> q = sect == null
                    ? em.createNamedQuery("Article.findSummaries", Article.class)
                    : em.createNamedQuery("Article.findSummariesBySection", Article.class).setParameter("section", sect);
            if (page != null && perPage != null) {
                q.setFirstResult(perPage * (page - 1));        // pagination start
                q.setMaxResults(perPage);                      // pagination limit
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Long countArticles(String sect) {
        LOG.log(Level.FINE, "Counting articles in section {0}", sect);
        if (imead.getValue(EntryRepo.DEFAULT_CATEGORY).equals(sect)) {
            sect = null;
        }
        EntityManager em = toiletPU.createEntityManager();
        try {
            TypedQuery<Long> qn = sect == null
                    ? em.createNamedQuery("Article.countArticles", Long.class)
                    : em.createNamedQuery("Article.countArticlesBySection", Long.class).setParameter("section", sect);
            Long output = qn.getSingleResult();
            LOG.log(Level.FINE, "Counted articles in section {0}, got {1}", new Object[]{sect, output});
            return output;
        } finally {
            em.close();
        }
    }

    public Long countComments() {
        LOG.log(Level.FINE, "Counting comments");
        EntityManager em = toiletPU.createEntityManager();
        try {
            TypedQuery<Long> qn = em.createNamedQuery("Comment.countComments", Long.class);
            Long output = qn.getSingleResult();
            LOG.log(Level.FINE, "Counted comments, got {0}", new Object[]{output});
            return output;
        } finally {
            em.close();
        }
    }

    public List search(String searchTerm) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            StoredProcedureQuery q = em.createStoredProcedureQuery("toilet.search_articles", Article.class);
            q.registerStoredProcedureParameter("initialterm", String.class, ParameterMode.IN).setParameter("initialterm", searchTerm);
            q.execute();
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * will save articles (or just one). articles must be pre-processed before
     * adding.
     *
     * @see ArticleProcessor
     * @param articles map of article to section
     * @return last article saved
     */
    public Article addArticles(Map<Article, String> articles) {
        Article dbArt = null;
        EntityManager em = toiletPU.createEntityManager();

        try {
            em.getTransaction().begin();
            for (Map.Entry<Article, String> entry : articles.entrySet()) {
                try {
                    Article art = entry.getKey();
                    String sect = entry.getValue();
                    boolean getnew = art.getArticleid() == null;
                    dbArt = getnew ? new Article() : em.find(Article.class, art.getArticleid());

                    dbArt.setPosted(art.getPosted() == null ? dbArt.getModified() : art.getPosted());
                    dbArt.setComments(art.getComments());
                    dbArt.setCommentCollection(art.getComments() ? dbArt.getCommentCollection() : null);
                    dbArt.setArticletitle(art.getArticletitle());
                    dbArt.setPostedhtml(art.getPostedhtml());
                    dbArt.setPostedmarkdown(art.getPostedmarkdown());
                    dbArt.setPostedamp(art.getPostedamp());
                    dbArt.setPostedname(art.getPostedname());
                    dbArt.setDescription(art.getDescription());
                    dbArt.setSummary(art.getSummary());
                    dbArt.setImageurl(art.getImageurl());
                    dbArt.setModified(new Date(new Date().getTime() / 1000 * 1000));

                    if (dbArt.getSectionid() == null || !dbArt.getSectionid().getName().equals(sect)) {
                        Section esec;
                        TypedQuery<Section> q = em.createNamedQuery("Section.findByName", Section.class).setParameter("name", sect);
                        try {
                            esec = q.getSingleResult();
                        } catch (NoResultException ex) {
                            esec = new Section();
                            esec.setName(sect);
                            em.persist(esec);
                        }
                        dbArt.setSectionid(esec);
                    }
                    dbArt.setEtag(HashUtil.getSHA256HashAsBase64(hashArticle(dbArt, dbArt.getCommentCollection(), sect)));

                    if (getnew) {
                        em.persist(dbArt);
                    }
                    LOG.log(Level.INFO, "Entry added {0}, section {1}", new Object[]{art.getArticletitle(), sect});
                } catch (Throwable x) {
                    LOG.throwing(EntryRepo.class.getCanonicalName(), "addArticles", x);
                }
            }
            em.getTransaction().commit();
            em.createStoredProcedureQuery("toilet.refresh_articlesearch").execute();
            cache.reset();
            return dbArt;
        } catch (Throwable x) {
            LOG.throwing(EntryRepo.class.getCanonicalName(), "addArticles", x);
            throw x;
        } finally {
            em.close();
        }
    }

    public Article getArticle(Integer articleId) {
        EntityManager em = toiletPU.createEntityManager();
        //em.setProperty(QueryHints.CACHE_USAGE, CacheUsage.CheckCacheThenDatabase);
        //em.setProperty(QueryHints.READ_ONLY, HintValues.TRUE);
        try {
            return em.find(Article.class, articleId, GuardRepo.USE_CACHE_HINT);
        } finally {
            em.close();
        }
    }

    public List<Article> getArticleArchive(Integer limit) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            TypedQuery<Article> q = em.createNamedQuery("Article.findAll", Article.class);
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
            TypedQuery<Comment> q = em.createNamedQuery("Comment.findAll", Comment.class);
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
                LOG.info("Comment added");
                updatedArticles.add(e.getArticleid());
            }
            em.getTransaction().commit();
            em.getTransaction().begin();
            for (Integer articleid : updatedArticles) {
                Article a = em.find(Article.class, articleid);
                em.refresh(a);
                updateArticleHash(em, articleid);
            }
            em.getTransaction().commit();
            return e;
        } finally {
            em.close();
        }
    }

    @Deprecated
    private void deleteArticle(Integer articleId) {
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
            //em.createNativeQuery(REINDEX_TRIGRAM_QUERY).executeUpdate();
            em.getTransaction().commit();
            LOG.info("Article deleted");
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
            LOG.info("Comment deleted");
            em.refresh(e);
            em.getTransaction().begin();
            updateArticleHash(em, e.getArticleid());
            em.getTransaction().commit();
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
            em.createNativeQuery("ALTER SEQUENCE toilet.comment_commentid_seq RESTART; ALTER SEQUENCE toilet.article_articleid_seq RESTART;").executeUpdate();
            em.createStoredProcedureQuery("toilet.refresh_articlesearch").execute();
            em.getTransaction().commit();
            LOG.info("All articles and comments deleted");
        } finally {
            em.close();
        }
    }

    private void updateArticleHash(EntityManager em, Integer articleId) {
        Article art = em.find(Article.class, articleId);
        art.setEtag(Base64.getEncoder().encodeToString(hashArticle(art, art.getCommentCollection(), art.getSectionid().getName())));
        art.setModified(new Date(new Date().getTime() / 1000 * 1000));
    }

    /**
     * process all articles one by one
     *
     * @param operation
     */
    public void processArticleArchive(Consumer<Article> operation) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            em.createNamedQuery("Article.findAll", Article.class).getResultStream().forEachOrdered(operation);
        } finally {
            em.close();
        }
    }

    private static byte[] hashArticle(Article e, Collection<Comment> comments, String sect) {
        try {
            MessageDigest sha = HashUtil.getSHA256();
            sha.update(e.getArticletitle().getBytes("UTF-8"));
            sha.update(e.getPostedhtml().getBytes("UTF-8"));
            sha.update(sect.getBytes("UTF-8"));
            sha.update(e.getPostedname().getBytes("UTF-8"));
            if (e.getDescription() != null) {
                sha.update(e.getDescription().getBytes("UTF-8"));
            }
            sha.update(e.getModified().toString().getBytes("UTF-8"));
            if (comments != null) {
                for (Comment c : comments) {
                    sha.update(c.getPostedhtml().getBytes("UTF-8"));
                    sha.update(c.getPosted().toString().getBytes("UTF-8"));
                    sha.update(c.getPostedname().getBytes("UTF-8"));
                }
            }
            return sha.digest();
        } catch (UnsupportedEncodingException enc) {
            throw new JVMNotSupportedError(enc);
        }
    }

}
