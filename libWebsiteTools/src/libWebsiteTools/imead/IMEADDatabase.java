package libWebsiteTools.imead;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import libWebsiteTools.security.HashUtil;

/**
 *
 * @author alpha
 */
public class IMEADDatabase extends IMEADHolder {

    private static final Logger LOG = Logger.getLogger(IMEADDatabase.class.getName());
    protected final EntityManagerFactory PU;

    public IMEADDatabase(EntityManagerFactory PU) {
        this.PU = PU;
        evict();
    }

    /**
     * refresh cache of all properties from the DB
     */
    @Override
    public IMEADHolder evict() {
        LOG.entering(IMEADHolder.class.getName(), "evict");
        PU.getCache().evict(Localization.class);
        localizedCache = Collections.unmodifiableMap(getProperties());
        localizedHash = HashUtil.getSHA256Hash(localizedCache.toString());
        patterns.clear();
        filteredCache.clear();
        LOG.exiting(IMEADHolder.class.getName(), "evict");
        return this;
    }

    /**
     * remove property from DB and refresh cache
     *
     * @param localPK
     */
    @Override
    public Localization delete(Object localPK) {
        EntityManager em = PU.createEntityManager();
        Localization out = em.find(Localization.class, localPK);
        try {
            em.getTransaction().begin();
            em.remove(out);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        evict();
        return out;
    }

    /**
     * add all specified Localizations to DB and refresh cache
     *
     * @param entities
     */
    @Override
    public List<Localization> upsert(Collection<Localization> entities) {
        ArrayList<Localization> out = new ArrayList<>(entities.size());
        EntityManager em = PU.createEntityManager();
        boolean dirty = false;
        try {
            em.getTransaction().begin();
            for (Localization l : entities) {
                Localization existing = em.find(Localization.class, l.localizationPK);
                if (null != existing && !existing.getValue().equals(l.getValue())) {
                    existing.setValue(l.getValue());
                    dirty = true;
                    out.add(existing);
                } else if (null == existing) {
                    em.persist(l);
                    dirty = true;
                    out.add(l);
                }
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        if (dirty) {
            evict();
        }
        return out;
    }

    @Override
    public Localization get(Object localPK) {
        EntityManager em = PU.createEntityManager();
        try {
            return em.find(Localization.class, localPK);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Localization> getAll(Integer limit) {
        EntityManager em = PU.createEntityManager();
        try {
            TypedQuery<Localization> q = em.createNamedQuery("Localization.findAll", Localization.class);
            if (null != limit) {
                q.setMaxResults(limit);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void processArchive(Consumer<Localization> operation, Boolean transaction) {
        EntityManager em = PU.createEntityManager();
        try {
            if (transaction) {
                em.getTransaction().begin();
                em.createNamedQuery("Localization.findAll", Localization.class).getResultStream().forEachOrdered(operation);
                em.getTransaction().commit();
            } else {
                em.createNamedQuery("Localization.findAll", Localization.class).getResultStream().forEachOrdered(operation);
            }
        } finally {
            em.close();
        }
    }

    @Override
    public Long count() {
        EntityManager em = PU.createEntityManager();
        try {
            TypedQuery<Long> qn = em.createNamedQuery("Localization.count", Long.class);
            return qn.getSingleResult();
        } finally {
            em.close();
        }
    }
}
