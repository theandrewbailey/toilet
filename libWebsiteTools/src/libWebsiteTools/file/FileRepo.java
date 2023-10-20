package libWebsiteTools.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import libWebsiteTools.db.Repository;

public class FileRepo implements Repository<Fileupload> {

    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private final EntityManagerFactory PU;
    private static final Logger LOG = Logger.getLogger(FileRepo.class.getName());

    public FileRepo(EntityManagerFactory PU) {
        this.PU = PU;
        evict();
    }

    @Override
    public synchronized void evict() {
        PU.getCache().evict(Fileupload.class);
        PU.getCache().evict(Filemetadata.class);
        EntityManager em = PU.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("REFRESH MATERIALIZED VIEW tools.filemetadata").executeUpdate();
        em.getTransaction().commit();
    }

    @Override
    public Fileupload get(Object filename) {
        Fileupload out;
        EntityManager em = PU.createEntityManager();
        try {
            out = em.find(Fileupload.class, filename);
            LOG.log(Level.FINEST, "File retrieved: {0}", filename);
            return out;
        } catch (NoResultException n) {
            LOG.log(Level.FINEST, "File doesn't exist: {0}", filename);
            return null;
        } finally {
            em.close();
        }
    }

    /**
     *
     * @param names
     * @return metadata of requested names, or everything for null
     */
    public List<Filemetadata> getFileMetadata(List<String> names) {
        if (null != names && names.isEmpty()) {
            return new ArrayList<>();
        }
        EntityManager em = PU.createEntityManager();
        try {
            return null == names ? em.createNamedQuery("Filemetadata.findAll", Filemetadata.class).getResultList()
                    : em.createNamedQuery("Filemetadata.findByFilenames", Filemetadata.class).setParameter("filenames", names).getResultList();
        } catch (NoResultException n) {
            return null;
        } finally {
            em.close();
        }
    }

    /**
     *
     * @param searchTerm
     * @return files that contain searchTerm
     */
    public List<Filemetadata> search(String searchTerm) {
        EntityManager em = PU.createEntityManager();
        try {
            return em.createQuery("SELECT f FROM Filemetadata f WHERE f.filename like CONCAT('%',:term,'%') ORDER BY f.filename", Filemetadata.class).setParameter("term", searchTerm).getResultList();
        } catch (NoResultException n) {
            return null;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Fileupload> upsert(Collection<Fileupload> entities) {
        ArrayList<Fileupload> out = new ArrayList<>(entities.size());
        EntityManager em = PU.createEntityManager();
        try {
            em.getTransaction().begin();
            for (Fileupload upload : entities) {
                if (null == em.find(Filemetadata.class, upload.getFilename())) {
                    em.persist(upload);
                    LOG.log(Level.INFO, "File added {0}", upload.getFilename());
                } else {
                    upload = em.merge(upload);
                    LOG.log(Level.INFO, "File upserted {0}", upload.getFilename());
                }
                out.add(upload);
            }
            em.getTransaction().commit();
        } catch (RuntimeException d) {
            LOG.log(Level.SEVERE, "Files not committed");
            throw d;
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        return out;
    }

    @Override
    public Fileupload delete(Object filename) {
        EntityManager em = PU.createEntityManager();
        Fileupload f = em.find(Fileupload.class, filename);
        if (null != f) {
            try {
                em.getTransaction().begin();
                em.remove(f);
                em.getTransaction().commit();
            } finally {
                em.close();
            }
        }
        return f;
    }

    /**
     * process all files one by one
     *
     * @param operation
     * @param transaction are you modifying anything?
     */
    @Override
    public void processArchive(Consumer<Fileupload> operation, Boolean transaction) {
        EntityManager em = PU.createEntityManager();
        try {
            if (transaction) {
                em.getTransaction().begin();
                Stream<Fileupload> results = em.createNamedQuery("Fileupload.findAll", Fileupload.class).getResultStream();
                results.forEach(operation);
                em.getTransaction().commit();
            } else {
                Stream<Fileupload> results = em.createNamedQuery("Fileupload.findAll", Fileupload.class).getResultStream();
                results.forEach(operation);
            }
        } finally {
            em.close();
        }
    }

    @Override
    public List<Fileupload> getAll(Integer limit) {
        EntityManager em = PU.createEntityManager();
        try {
            TypedQuery<Fileupload> q = em.createNamedQuery("Fileupload.findAll", Fileupload.class);
            if (null != limit) {
                q.setMaxResults(limit);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Long count() {
        EntityManager em = PU.createEntityManager();
        try {
            TypedQuery<Long> qn = em.createNamedQuery("Fileupload.count", Long.class);
            return qn.getSingleResult();
        } finally {
            em.close();
        }
    }
}
