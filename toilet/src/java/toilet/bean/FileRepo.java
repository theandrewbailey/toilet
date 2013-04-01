package toilet.bean;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.persistence.exceptions.DatabaseException;
import toilet.db.Fileupload;

@Stateless
public class FileRepo {

    public static final String LOCAL_NAME = "java:module/FileRepo";
    @PersistenceUnit(name = UtilBean.PERSISTENCE)
    private EntityManagerFactory toiletPU;
    private static final Logger log = Logger.getLogger(FileRepo.class.getName());
    private static final String FIND_FILENAME = "SELECT f FROM Fileupload f WHERE f.filename = :filename";
    private static final String ALL_FILES = "SELECT f FROM Fileupload f ORDER BY f.filename";

    public String getFilename(HttpServletRequest request) {
        String[] uri = request.getRequestURI().split("/content/");
        if (uri.length == 1 || uri.length > 3) {
            throw new NoResultException();
        }
        return uri[1];
    }

    public Fileupload getFile(String name) {
        EntityManager em = toiletPU.createEntityManager();
        TypedQuery<Fileupload> q = em.createQuery(FIND_FILENAME, Fileupload.class);
        q.setParameter("filename", name);
        try {
            log.log(Level.FINEST, "File retrieved: {0}", name);
            return q.getSingleResult();
        } catch (NoResultException n) {
            return null;
        }
    }

    public void addFile(Fileupload upload) {
        EntityManager em = toiletPU.createEntityManager();
        em.getTransaction().begin();
        em.persist(upload);
        try {
            em.getTransaction().commit();
            log.log(Level.FINE, "File committed to db: {0}", upload.getFilename());
        } catch (DatabaseException d) {
            log.log(Level.INFO, "File already exists: {0}", upload.getFilename());
        }
    }

    public void deleteFile(Integer fileUploadId) {
        EntityManager em = toiletPU.createEntityManager();
        em.getTransaction().begin();
        em.remove(em.find(Fileupload.class, fileUploadId));
        em.getTransaction().commit();
    }

    public List<Fileupload> getUploadArchive() {
        return toiletPU.createEntityManager().createQuery(ALL_FILES, Fileupload.class).getResultList();
    }
}
