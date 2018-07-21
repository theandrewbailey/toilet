package libOdyssey.bean;

import java.util.Date;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import libOdyssey.db.Honeypot;
import libWebsiteTools.imead.IMEADHolder;

/**
 *
 * @author alpha
 */
@Stateless
public class GuardRepo {

    private static final String FIRST_TIME_HONEYPOT = "libOdyssey_firstHoneypot";
    @PersistenceUnit
    private EntityManagerFactory PU;
    @EJB
    private IMEADHolder imead;

    @Schedule(hour = "1")
    public void cleanHoneypot() {
        EntityManager em = PU.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Honeypot.cleanHoneypot").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public boolean inHoneypot(String ip) {
        EntityManager em = PU.createEntityManager();
        try {
            em.createNamedQuery("Honeypot.findByIpBeforeNow", Honeypot.class).setParameter("ip", ip).getSingleResult();
            return true;
        } catch (NoResultException n) {
            return false;
        } finally {
            em.close();
        }
    }

    public void putInHoneypot(String ip) {
        Date now = new Date();
        long defaultTime = Long.valueOf(imead.getValue(FIRST_TIME_HONEYPOT));
        EntityManager em = PU.createEntityManager();
        try {
            em.getTransaction().begin();
            try {
                Honeypot h = em.createNamedQuery("Honeypot.findByIp", Honeypot.class).setParameter("ip", ip).getSingleResult();
                h.setStartedatatime(now);
                long delta = h.getExpiresatatime().getTime() - now.getTime();
                long time = now.getTime() + Math.max(delta * 2, defaultTime);
                if (time > 9224257554000000L) {
                    time = 9224257554000000L;
                }
                h.setExpiresatatime(new Date(time));
            } catch (NoResultException n) {
                em.persist(new Honeypot(null, new Date(now.getTime() + defaultTime), ip, now));
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}
