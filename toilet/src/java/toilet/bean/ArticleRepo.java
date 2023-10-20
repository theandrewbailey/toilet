package toilet.bean;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TypedQuery;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.db.Repository;
import libWebsiteTools.imead.IMEADHolder;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.db.Section;

/**
 *
 * @author alpha
 */
public class ArticleRepo implements Repository<Article> {

    public static final String DEFAULT_CATEGORY = "entry_defaultCategory";
    private static final Logger LOG = Logger.getLogger(ArticleRepo.class.getName());
    private static final List<Integer> EXCLUDE_NOTHING = Arrays.asList(new Integer[]{Integer.MIN_VALUE});
    private static final Pattern ARTICLE_TERM = Pattern.compile("(.+?)(?=(?: \\d.*)|(?:[:,] .*)|(?: \\(\\d+\\))|(?: \\()|(?: IX|IV|V?I{0,3})$)");
    private final EntityManagerFactory toiletPU;
    private final IMEADHolder imead;

    public ArticleRepo(EntityManagerFactory toiletPU, IMEADHolder imead) {
        this.toiletPU = toiletPU;
        this.imead = imead;
    }

    @Override
    public void evict() {
        toiletPU.getCache().evict(Article.class);
    }

    public List<Article> getBySection(String sect, Integer page, Integer perPage, List<Integer> exclude) {
        if (null != sect && sect.equals(imead.getValue(ArticleRepo.DEFAULT_CATEGORY))) {
            sect = null;
        }
        EntityManager em = toiletPU.createEntityManager();
        try {
            TypedQuery<Article> q = sect == null
                    ? em.createNamedQuery("Article.findAll", Article.class)
                    : em.createNamedQuery("Article.findBySection", Article.class).setParameter("section", sect);
            q.setParameter("exclude", null == exclude ? EXCLUDE_NOTHING : exclude);
            if (page != null && perPage != null) {
                q.setFirstResult(perPage * (page - 1));        // pagination start
                q.setMaxResults(perPage);                      // pagination limit
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Long count() {
        EntityManager em = toiletPU.createEntityManager();
        try {
            TypedQuery<Long> qn = em.createNamedQuery("Article.count", Long.class);
            Long output = qn.getSingleResult();
            return output;
        } finally {
            em.close();
        }
    }

    /**
     *
     * @param art Article to get an appropriate search term from
     * @return String suitable to pass to article search to retrieve similar
     * articles
     */
    public static String getArticleSuggestionTerm(Article art) {
        String term = art.getArticletitle();
        if (null == term) {
            return "";
        }
        Matcher articleMatch = ARTICLE_TERM.matcher(term);
        if (articleMatch.find()) {
            term = articleMatch.group(1).trim();
        }
        return term;
    }

    @SuppressWarnings("unchecked")
    public List<Article> search(String searchTerm) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            StoredProcedureQuery q = em.createStoredProcedureQuery("toilet.search_articles", Article.class);
            q.registerStoredProcedureParameter(1, String.class, ParameterMode.IN).setParameter(1, searchTerm);
            q.execute();
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<String> searchSuggestion(String searchTerm, Integer limit) {
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
        EntityManager em = toiletPU.createEntityManager();

        try {
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
                dbArt.setEtag(HashUtil.getSHA256Hash(hashArticle(dbArt, dbArt.getCommentCollection(), sect)));
                if (getnew) {
                    em.persist(dbArt);
                }
                out.add(dbArt);
                LOG.log(Level.INFO, "Article added {0}, section {1}", new Object[]{art.getArticletitle(), sect});
            }
            em.getTransaction().commit();
            return out;
        } catch (Throwable x) {
            LOG.throwing(ArticleRepo.class.getCanonicalName(), "addArticles", x);
            throw x;
        } finally {
            em.close();
        }
    }

    public void refreshSearch() {
        EntityManager em = toiletPU.createEntityManager();
        em.createStoredProcedureQuery("toilet.refresh_articlesearch").execute();
        em.close();
    }

    @Override
    public Article get(Object articleId) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            return em.find(Article.class, articleId);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Article> getAll(Integer limit) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            TypedQuery<Article> q = em.createNamedQuery("Article.findAll", Article.class).setParameter("exclude", EXCLUDE_NOTHING);
            if (null != limit) {
                q.setMaxResults(limit);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Article delete(Object articleId) {
        EntityManager em = toiletPU.createEntityManager();
        try {
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
                em.createStoredProcedureQuery("toilet.refresh_articlesearch").execute();
                em.getTransaction().commit();
                LOG.info("All articles and comments deleted");
                return null;
            } else {
                Article e = em.find(Article.class, articleId);
                em.getTransaction().begin();
                if (e.getSectionid().getArticleCollection().size() == 1) {
                    em.remove(e);
                    em.remove(e.getSectionid());
                } else {
                    em.remove(e);
                }
                em.getTransaction().commit();
                LOG.info("Article deleted");
                return e;
            }
        } finally {
            em.close();
        }
    }

    public static void updateArticleHash(EntityManager em, Integer articleId) {
        Article art = em.find(Article.class, articleId);
        art.setEtag(Base64.getEncoder().encodeToString(hashArticle(art, art.getCommentCollection(), art.getSectionid().getName())));
        art.setModified(OffsetDateTime.now());
    }

    @Override
    public void processArchive(Consumer<Article> operation, Boolean transaction) {
        EntityManager em = toiletPU.createEntityManager();
        try {
            if (transaction) {
                em.getTransaction().begin();
                em.createNamedQuery("Article.findAll", Article.class).setParameter("exclude", EXCLUDE_NOTHING).getResultStream().forEachOrdered(operation);
                em.getTransaction().commit();
            } else {
                em.createNamedQuery("Article.findAll", Article.class).setParameter("exclude", EXCLUDE_NOTHING).getResultStream().forEachOrdered(operation);
            }
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
