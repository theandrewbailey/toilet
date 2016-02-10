package libWebsiteTools.file;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;

@Stateless
public class FileRepo {

    public static final String LOCAL_NAME = "java:module/FileRepo";
    @PersistenceUnit
    private EntityManagerFactory PU;
    private static final Logger log = Logger.getLogger(FileRepo.class.getName());
    private static final String FIND_FILENAME = "SELECT f FROM Fileupload f WHERE f.filename = :filename";
    private static final String ALL_FILES = "SELECT f FROM Fileupload f ORDER BY f.filename";
    private static final Pattern FILE_URL = Pattern.compile("^.*?/content/(.*?)(?:\\?.*)?$");

    public void evict() {
        PU.getCache().evict(Fileupload.class);
    }

    public String getFilename(CharSequence URL) {
        Matcher m = FILE_URL.matcher(URL);
        if (!m.matches() || null == m.group(1)) {
            throw new NoResultException();
        }
        return m.group(1);
    }

    public Fileupload getFile(String name) {
        EntityManager em = PU.createEntityManager();
        TypedQuery<Fileupload> q = em.createQuery(FIND_FILENAME, Fileupload.class);
        q.setParameter("filename", name);
        try {
            log.log(Level.FINEST, "File retrieved: {0}", name);
            return q.getSingleResult();
        } catch (NoResultException n) {
            return null;
        } finally {
            em.close();
        }
    }

    public void addFiles(List<Fileupload> uploads) {
        EntityManager em = PU.createEntityManager();
        try {
            em.getTransaction().begin();
            for (Fileupload upload : uploads) {
                em.persist(upload);
                log.log(Level.INFO, "File added {0}", upload.getFilename());
            }
            em.getTransaction().commit();
            PU.getCache().evict(Fileupload.class);
        } catch (RuntimeException d) {
            if (null != d.getCause() && d.getCause().getCause() instanceof SQLException) {
                // this assumes that the problematic file is the only one uploaded
                log.log(Level.INFO, "File already exists: {0}", uploads.get(0).getFilename());
            }
            log.log(Level.SEVERE, "Files not committed");
            throw d;
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    public void deleteFile(Integer fileUploadId) {
        EntityManager em = PU.createEntityManager();
        try {
            em.getTransaction().begin();
            em.remove(em.find(Fileupload.class, fileUploadId));
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<Fileupload> getUploadArchive() {
        EntityManager em = PU.createEntityManager();
        try {
            return em.createQuery(ALL_FILES, Fileupload.class).getResultList();
        } finally {
            em.close();
        }
    }
}
