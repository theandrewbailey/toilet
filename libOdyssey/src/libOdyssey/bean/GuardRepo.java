package libOdyssey.bean;

import java.util.Date;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
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
        em.getTransaction().begin();
        Query q = em.createQuery("DELETE FROM Honeypot h WHERE h.expiresatatime < CURRENT_TIMESTAMP");
        q.executeUpdate();
        em.getTransaction().commit();
    }

    public boolean sessionsPerSecond(String ip, Integer numOfSessions, Integer seconds) {
        TypedQuery<Long> q = PU.createEntityManager().createQuery("SELECT COUNT(h) FROM Httpsession h WHERE h.ip = :ip AND h.atime > :time", Long.class);
        q.setParameter("ip", ip);
        q.setParameter("time", new Date(new Date().getTime() - (seconds * 1000)));
        Long res = q.getSingleResult();
        return res.longValue() >= numOfSessions.longValue();
    }

    public boolean emptySessionCheck(String ip, Integer numOfSessions, Integer seconds, Integer limit) {
        TypedQuery<Long> q = PU.createEntityManager().createQuery("SELECT COUNT(h) FROM Httpsession h WHERE h.ip = :ip AND h.atime > :time AND (SELECT COUNT(p) FROM Pagerequest p WHERE p.httpsessionid = h) >= :limit", Long.class);
        q.setParameter("ip", ip);
        q.setParameter("time", new Date(new Date().getTime() - (seconds * 1000)));
        q.setParameter("limit", limit);
        Long res = q.getSingleResult();
        return res.longValue() >= numOfSessions.longValue();
    }

    public boolean inHoneypot(String ip) {
        TypedQuery<Honeypot> q = PU.createEntityManager().createQuery("SELECT h FROM Honeypot h WHERE h.ip = :ip AND h.expiresatatime > CURRENT_TIMESTAMP", Honeypot.class);
        q.setParameter("ip", ip);
        try {
            q.getSingleResult();
            return true;
        } catch (NoResultException n) {
            return false;
        }
    }

    public void putInHoneypot(String ip) {
        Date now = new Date();
        long defaultTime = Long.valueOf(imead.getValue(FIRST_TIME_HONEYPOT));
        EntityManager em = PU.createEntityManager();
        em.getTransaction().begin();
        try {
            TypedQuery<Honeypot> q = em.createNamedQuery("Honeypot.findByIp", Honeypot.class);
            q.setParameter("ip", ip);
            Honeypot h = q.getSingleResult();
            h.setStartedatatime(now);
            long delta = h.getExpiresatatime().getTime() - now.getTime();
            long time = now.getTime() + Math.max(delta * 2, defaultTime);
            if (time > 9224257554000000L) {
                time = 9224257554000000L;
            }
            h.setExpiresatatime(new Date(time));
        } catch (NoResultException n) {
            em.persist(new Honeypot(null, ip, now, new Date(now.getTime() + defaultTime)));
        }
        em.getTransaction().commit();
    }
}
