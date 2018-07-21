package libWebsiteTools.file;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.Date;
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
import libWebsiteTools.JVMNotSupportedError;

@Stateless
public class FileRepo {

    public static final String LOCAL_NAME = "java:module/FileRepo";
    @PersistenceUnit
    private EntityManagerFactory PU;
    private static final Logger LOG = Logger.getLogger(FileRepo.class.getName());
    private static final Pattern FILE_URL = Pattern.compile("^.*?/content/(.*?)(?:\\?.*)?$");
    private static final Pattern IMMUTABLE_URL = Pattern.compile("^.*?/contentImmutable/[~/]+?/(.*?)(?:\\?.*)?$");

    public void evict() {
        PU.getCache().evict(Fileupload.class);
    }

    public static String getFilename(CharSequence URL) {
        try {
            Matcher m = FILE_URL.matcher(URLDecoder.decode(URL.toString(), "UTF-8"));
            if (!m.matches() || null == m.group(1)) {
                String immutable = getImmutable(URL);
                if (null == immutable) {
                    throw new NoResultException("Unable to get content filename from " + URL);
                }
            }
            return m.group(1);
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        }
    }

    public static String getImmutable(CharSequence URL) {
        try {
            Matcher m = FILE_URL.matcher(URLDecoder.decode(URL.toString(), "UTF-8"));
            if (!m.matches() || null == m.group(1)) {
                return null;
            }
            return m.group(1);
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        }
    }

    public Fileupload getFile(String name) {
        EntityManager em = PU.createEntityManager();
        TypedQuery<Fileupload> q = em.createNamedQuery("Fileupload.findByFilename", Fileupload.class);
        q.setParameter("filename", name);
        try {
            LOG.log(Level.FINEST, "File retrieved: {0}", name);
            return q.getSingleResult();
        } catch (NoResultException n) {
            return null;
        } finally {
            em.close();
        }
    }

    public Fileupload getFileMetadata(String name) {
        EntityManager em = PU.createEntityManager();
        try {
            LOG.log(Level.FINEST, "File retrieved: {0}", name);
            Object[] o = (Object[]) em.createNamedQuery("Fileupload.getMetadata").setParameter("filename", name).getSingleResult();
            Fileupload fu = new Fileupload((Integer) o[0]);
            fu.setAtime((Date) o[1]);
            fu.setEtag(o[2].toString());
            fu.setMimetype(o[3].toString());
            return fu;
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
                LOG.log(Level.INFO, "File added {0}", upload.getFilename());
            }
            em.getTransaction().commit();
            PU.getCache().evict(Fileupload.class);
        } catch (RuntimeException d) {
            if (null != d.getCause() && d.getCause().getCause() instanceof SQLException) {
                // this assumes that the problematic file is the only one uploaded
                LOG.log(Level.INFO, "File already exists: {0}", uploads.get(0).getFilename());
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

    public void upsertFiles(List<Fileupload> uploads) {
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
            return em.createNamedQuery("Fileupload.findAll", Fileupload.class).getResultList();
        } finally {
            em.close();
        }
    }
}
