package toilet.bean.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import libWebsiteTools.imead.IMEADHolder;
import toilet.bean.ArticleRepository;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.db.Section;

/**
 *
 * @author alpha
 */
public abstract class ArticleDatabase implements ArticleRepository {

    private static final Logger LOG = Logger.getLogger(ArticleRepository.class.getName());
    private static final List<Integer> EXCLUDE_NOTHING = Arrays.asList(new Integer[]{Integer.MIN_VALUE});
    protected final EntityManagerFactory toiletPU;
    protected final IMEADHolder imead;

    public ArticleDatabase(EntityManagerFactory toiletPU, IMEADHolder imead) {
        this.toiletPU = toiletPU;
        this.imead = imead;
    }

    @Override
    public List<Article> getBySection(String sect, Integer page, Integer perPage, List<Integer> exclude) {
        if (null != sect && sect.equals(imead.getValue(ArticleDatabase.DEFAULT_CATEGORY))) {
            sect = null;
        }
        try (EntityManager em = toiletPU.createEntityManager()) {
            TypedQuery<Article> q = sect == null
                    ? em.createNamedQuery("Article.findAll", Article.class)
                    : em.createNamedQuery("Article.findBySection", Article.class).setParameter("section", sect);
            q.setParameter("exclude", null == exclude ? EXCLUDE_NOTHING : exclude);
            if (page != null && perPage != null) {
                q.setFirstResult(perPage * (page - 1));        // pagination start
                q.setMaxResults(perPage);                      // pagination limit
            }
            return q.getResultList();
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
    @Override
    public List<Article> upsert(Collection<Article> articles) {
        LOG.log(Level.INFO, "Upserting {0} articles", articles.size());
        Article dbArt;
        ArrayList<Article> out = new ArrayList<>(articles.size());
        try (EntityManager em = toiletPU.createEntityManager()) {
            em.getTransaction().begin();
            for (Article art : articles) {
                String sect = art.getSectionid().getName();
                boolean getnew = art.getArticleid() == null;
                dbArt = getnew ? new Article() : em.find(Article.class, art.getArticleid());
                // TODO: figure out how to upsert
                dbArt.setPosted(art.getPosted() == null ? dbArt.getModified() : art.getPosted());
                dbArt.setComments(art.getComments());
                dbArt.setCommentCollection(art.getComments() ? dbArt.getCommentCollection() : null);
                dbArt.setArticletitle(art.getArticletitle());
                dbArt.setPostedhtml(art.getPostedhtml());
                dbArt.setPostedmarkdown(art.getPostedmarkdown());
                dbArt.setPostedname(art.getPostedname());
                dbArt.setDescription(art.getDescription());
                dbArt.setSummary(art.getSummary());
                dbArt.setImageurl(art.getImageurl());
                dbArt.setSuggestion(art.getSuggestion());
                dbArt.setModified(OffsetDateTime.now());

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
                dbArt.setEtag(Base64.getEncoder().encodeToString(ArticleRepository.hashArticle(dbArt, dbArt.getCommentCollection(), sect)));
                if (getnew) {
                    em.persist(dbArt);
                }
                out.add(dbArt);
                LOG.log(Level.INFO, "Article added {0}, section {1}", new Object[]{art.getArticletitle(), sect});
            }
            em.getTransaction().commit();
            return out;
        } catch (Throwable x) {
            LOG.throwing(ArticleRepository.class.getCanonicalName(), "addArticles", x);
            throw x;
        }
    }

    @Override
    public Article get(Object articleId) {
        try (EntityManager em = toiletPU.createEntityManager()) {
            return em.find(Article.class, articleId);
        }
    }

    @Override
    public List<Article> getAll(Integer limit) {
        try (EntityManager em = toiletPU.createEntityManager()) {
            TypedQuery<Article> q = em.createNamedQuery("Article.findAll", Article.class).setParameter("exclude", EXCLUDE_NOTHING);
            if (null != limit) {
                q.setMaxResults(limit);
            }
            return q.getResultList();
        }
    }

    @Override
    public Article delete(Object articleId) {
        try (EntityManager em = toiletPU.createEntityManager()) {
            if (null == articleId) {
                em.getTransaction().begin();
                for (Comment c : em.createNamedQuery("Comment.findAll", Comment.class).getResultList()) {
                    em.remove(c);
                }
                for (Article a : em.createNamedQuery("Article.findAll", Article.class).setParameter("exclude", EXCLUDE_NOTHING).getResultList()) {
                    em.remove(a);
                }
                for (Section s : em.createNamedQuery("Section.findAll", Section.class).getResultList()) {
                    em.remove(s);
                }
                em.createNativeQuery("ALTER SEQUENCE toilet.comment_commentid_seq RESTART; ALTER SEQUENCE toilet.article_articleid_seq RESTART;").executeUpdate();
                em.getTransaction().commit();
                refreshSearch();
                LOG.info("All articles and comments deleted");
                return null;
            } else {
                Article e = em.find(Article.class, articleId);
                em.getTransaction().begin();
                TypedQuery<Long> qn = em.createNamedQuery("Article.countBySection", Long.class).setParameter("section", e.getSectionid().getName());
                if (qn.getSingleResult() == 1) {
                    em.remove(e);
                    em.remove(e.getSectionid());
                } else {
                    em.remove(e);
                }
                em.getTransaction().commit();
                LOG.info("Article deleted");
                return e;
            }
        }
    }

    @Override
    public void processArchive(Consumer<Article> operation, Boolean transaction) {
        try (EntityManager em = toiletPU.createEntityManager()) {
            if (transaction) {
                em.getTransaction().begin();
                em.createNamedQuery("Article.findAll", Article.class).setParameter("exclude", EXCLUDE_NOTHING).getResultStream().forEachOrdered(operation);
                em.getTransaction().commit();
            } else {
                em.createNamedQuery("Article.findAll", Article.class).setParameter("exclude", EXCLUDE_NOTHING).getResultStream().forEachOrdered(operation);
            }
        }
    }

    @Override
    public ArticleRepository evict() {
        toiletPU.getCache().evict(Article.class);
        return this;
    }

    @Override
    public Long count() {
        try (EntityManager em = toiletPU.createEntityManager()) {
            TypedQuery<Long> qn = em.createNamedQuery("Article.count", Long.class);
            Long output = qn.getSingleResult();
            return output;
        }
    }
}
