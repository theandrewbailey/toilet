package toilet.bean;

import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
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
    @PersistenceUnit(name = UtilBean.PERSISTENCE)
    private EntityManagerFactory toiletPU;
    @EJB
    private IMEADHolder imead;
    @EJB
    private StateCache cache;

    public List<Article> getSection(String sect, Integer page, Integer perPage) {
        log.log(Level.FINE, "Retrieving articles, section {0}, page {1}, per page {2}", new Object[]{sect, page, perPage});
        if (imead.getValue(EntryRepo.DEFAULT_CATEGORY).equals(sect)) {
            sect = null;
        }
        EntityManager em = toiletPU.createEntityManager();
        TypedQuery<Article> q = sect == null
                ? em.createQuery("SELECT a FROM Article a ORDER BY a.posted desc", Article.class)
                : em.createQuery("SELECT a FROM Article a WHERE a.sectionid.name=:s ORDER BY a.posted desc", Article.class).setParameter("s", sect);

        if (page != null && perPage != null) {
            q.setFirstResult(perPage * (page - 1));        // pagination start
            q.setMaxResults(perPage);                      // pagination limit
        }
        return q.getResultList();
    }

    public Long countArticlesInSection(String sect) {
        log.log(Level.FINE, "Counting articles in section {0}", sect);
        if (imead.getValue(EntryRepo.DEFAULT_CATEGORY).equals(sect)) {
            sect = null;
        }
        EntityManager em = toiletPU.createEntityManager();
        TypedQuery<Long> qn = sect == null
                ? em.createQuery("SELECT COUNT(a) FROM Article a", Long.class)
                : em.createQuery("SELECT COUNT(a) FROM Article a WHERE a.sectionid.name=:s", Long.class).setParameter("s", sect);
        return qn.getSingleResult();
    }

    public Article addEntry(Article art, String sect) {
        log.log(Level.FINE, "Entry added {0}, section {1}", new Object[]{art.getArticletitle(), sect});
        EntityManager em = toiletPU.createEntityManager();
        boolean getnew = art.getArticleid() == null;

        em.getTransaction().begin();
        Article dbArt = getnew ? new Article() : em.find(Article.class, art.getArticleid());

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

        em.getTransaction().commit();
        cache.reset();
        return dbArt;
    }

    public Article getEntry(Integer entryId) {
        return toiletPU.createEntityManager().find(Article.class, entryId);
    }

    public List<Article> getArticleArchive(Integer limit) {
        TypedQuery<Article> q = toiletPU.createEntityManager().createQuery("SELECT a FROM Article a ORDER BY a.posted DESC", Article.class);
        if (limit != null) {
            q.setMaxResults(limit);
        }
        List<Article> out = q.getResultList();
        Collections.reverse(out);
        return out;
    }

    public List<Comment> getCommentArchive(Integer limit) {
        TypedQuery<Comment> q = toiletPU.createEntityManager().createQuery("SELECT c FROM Comment c ORDER BY c.posted DESC", Comment.class);
        if (limit != null) {
            q.setMaxResults(limit);
        }
        List<Comment> out = q.getResultList();
        Collections.reverse(out);
        return out;
    }

    public Article addComment(Integer entryId, Comment c) {
        log.fine("Comment added");
        EntityManager em = toiletPU.createEntityManager();
        Article e = em.find(Article.class, entryId);
        em.getTransaction().begin();

        if (c.getPosted() == null) {
            c.setPosted(new Date());
        }
        c.setArticleid(e);
        em.persist(c);
        em.getTransaction().commit();
        em.refresh(e);
        updateArticleHash(e.getArticleid());
        return e;
    }

    public void deleteEntry(Integer entryId) {
        log.fine("Entry deleted");
        EntityManager em = toiletPU.createEntityManager();
        Article e = em.find(Article.class, entryId);
        em.getTransaction().begin();

        if (e.getSectionid().getArticleCollection().size() == 1) {
            em.remove(e);
            em.remove(e.getSectionid());
        } else {
            em.remove(e);
        }

        em.getTransaction().commit();
    }

    public void deleteComment(Integer commentId) {
        log.fine("Comment deleted");
        EntityManager em = toiletPU.createEntityManager();
        em.getTransaction().begin();
        Comment c = em.find(Comment.class, commentId);
        Article e = c.getArticleid();
        em.remove(c);
        em.getTransaction().commit();
        em.refresh(e);
        updateArticleHash(e.getArticleid());
    }

    public void deleteEverything() {
        log.info("Completely resetting articles and comments");
        EntityManager em = toiletPU.createEntityManager();
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
    }

    private void updateArticleHash(Integer articleId) {
        EntityManager em = toiletPU.createEntityManager();
        em.getTransaction().begin();
        Article art = em.find(Article.class, articleId);
        art.setEtag(HashUtil.getBase64(hashArticle(art, art.getCommentCollection(), art.getSectionid().getName())));
        art.setModified(new Date(new Date().getTime() / 1000 * 1000));
        em.getTransaction().commit();
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
