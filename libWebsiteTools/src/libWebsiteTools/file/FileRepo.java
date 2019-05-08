package libWebsiteTools.file;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import libOdyssey.bean.GuardRepo;

@Stateless
public class FileRepo {

    public static final String LOCAL_NAME = "java:module/FileRepo";
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    @PersistenceUnit
    private EntityManagerFactory PU;
    private static final Logger LOG = Logger.getLogger(FileRepo.class.getName());

    public void evict() {
        PU.getCache().evict(Fileupload.class);
        PU.getCache().evict(Filemetadata.class);
    }

    public Fileupload getFile(String name) {
        EntityManager em = PU.createEntityManager();
        try {
            return em.find(Fileupload.class, name, GuardRepo.USE_CACHE_HINT);
        } catch (NoResultException n) {
            return null;
        } finally {
            LOG.log(Level.FINEST, "File retrieved: {0}", name);
            em.close();
        }
    }

    /**
     *
     * @param names
     * @return metadata of requested names, or everything for null
     */
    public List<Filemetadata> getFileMetadata(List<String> names) {
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

    public void addFiles(Collection<Fileupload> uploads) {
        EntityManager em = PU.createEntityManager();
        Fileupload last = null;
        try {
            em.getTransaction().begin();
            for (Fileupload upload : uploads) {
                last = upload;
                em.persist(upload);
                LOG.log(Level.INFO, "File added {0}", upload.getFilename());
            }
            em.getTransaction().commit();
            PU.getCache().evict(Fileupload.class);
        } catch (RuntimeException d) {
            if (null != d.getCause() && d.getCause().getCause() instanceof SQLException) {
                // this assumes that the problematic file is the only one uploaded
                if (null != last) {
                    LOG.log(Level.INFO, "File already exists: {0}", last.getFilename());
                } else {
                    LOG.log(Level.INFO, "Something happened");
                }
            }
            LOG.log(Level.SEVERE, "Files not committed");
            throw d;
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    public void upsertFiles(Collection<Fileupload> uploads) {
        EntityManager em = PU.createEntityManager();
        try {
            em.getTransaction().begin();
            for (Fileupload upload : uploads) {
                em.merge(upload);
                LOG.log(Level.INFO, "File upserted {0}", upload.getFilename());
            }
            em.getTransaction().commit();
            PU.getCache().evict(Fileupload.class);
        } catch (RuntimeException d) {
            LOG.log(Level.SEVERE, "Files not committed");
            throw d;
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    public void deleteFile(String filename) {
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
    }

    /**
     * process all files one by one
     *
     * @param operation
     */
    public void processUploadArchive(Consumer<Fileupload> operation) {
        EntityManager em = PU.createEntityManager();
        try {
            em.createNamedQuery("Fileupload.findAll", Fileupload.class).getResultStream().forEachOrdered(operation);
        } finally {
            em.close();
        }
    }
}
