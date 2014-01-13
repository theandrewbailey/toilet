package libOdyssey.bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;
import libOdyssey.DateRange;
import libOdyssey.RangeGenerator;
import libOdyssey.db.Page;
import libOdyssey.db.Pageday;
import libOdyssey.db.Pagerequest;

/**
 *
 * @author alpha
 */
@Singleton
public class Analyzer {

    @PersistenceUnit
    private EntityManagerFactory PU;
    public static final String getTimesQuery = "SELECT DISTINCT CAST(EXTRACT(DAY FROM atime) AS INT) AS \"day\",CAST(EXTRACT(MONTH FROM atime) AS INT) AS \"month\",CAST(EXTRACT(YEAR FROM atime) AS INT) AS \"year\" FROM odyssey.httpsession ORDER BY \"year\" DESC,\"month\" DESC,\"day\" DESC;";
    private static final Logger log = Logger.getLogger(Analyzer.class.getName());

    public Page getPageByUrl(String url) {
        EntityManager em = PU.createEntityManager();
        try {
            return em.createNamedQuery("Page.findByUrl", Page.class).setParameter("url", url).getSingleResult();
        } catch (NoResultException n){
            Page out = new Page(null, url);
            em.getTransaction().begin();
            em.persist(out);
            em.getTransaction().commit();
            return getPageByUrl(url);
        }
    }

    public List<Pagerequest> getHitsInRange(Date start, Date end){
        TypedQuery<Pagerequest> q = PU.createEntityManager().createQuery("SELECT p FROM Pagerequest p WHERE p.atime BETWEEN :start AND :end", Pagerequest.class);
        q.setParameter("start", start);
        q.setParameter("end", end);
        return q.getResultList();
    }

    public boolean isRangeProcessed(Date day, Character dayInterval){
        EntityManager em = PU.createEntityManager();
        TypedQuery<Long> q = em.createQuery("SELECT COUNT(p) FROM Pageday p WHERE p.pagedayPK.day = :day AND p.pagedayPK.dayinterval = :dayInterval", Long.class);
        q.setParameter("day", day);
        q.setParameter("dayInterval", dayInterval.toString());
        return q.getSingleResult().longValue() != 0L;
    }

//    @Schedule(hour = "5")
    public void analyze() {
        log.entering(Analyzer.class.getName(), "analyze");
        log.info("Analyzing requests");
        SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd");
        EntityManager em=PU.createEntityManager();
        for (DateRange range:new RangeGenerator(this)){
            log.info(String.format("Analyzing {0}, interval {1}", f.format(range.getStart()), range.getDayInterval()));
            long totalCount=0L;
        // get pages in range
        // count hits
        // trace where they went to
        // count them, percent them
        // do again for where they came from
            List<Pagerequest> hits=getHitsInRange(range.getStart(), range.getEnd());
            Map<String, List<Pagerequest>> hitMap=new HashMap<String, List<Pagerequest>>(hits.size());
//            for(Pagerequest hit:hits){
//                if (!hitMap.containsKey(hit.getPageid().getUrl())){
//                    hitMap.put(hit.getPageid().getUrl(), new ArrayList<Pagerequest>());
//                }
//                hitMap.get(hit.getPageid().getUrl()).add(hit);
//            }
            hits.clear();
            List<Pageday> pageDays=new ArrayList<Pageday>(hitMap.size()+10);
            for(Map.Entry<String, List<Pagerequest>> hit:hitMap.entrySet()){
                double average=0, stdDev=0;
//                Page p=hit.getValue().get(0).getPageid();
                totalCount+=hit.getValue().size();
                for (Pagerequest pr:hit.getValue()){
                    average+=pr.getServed();
                }
                average/=hit.getValue().size();
                for (Pagerequest pr:hit.getValue()){
                    stdDev += Math.pow(pr.getServed()-average, 2);
                }
                //stdDev/=hit.getValue().size();
                //stdDev=Math.sqrt(stdDev);
                // add to pagedays
                //pageDays.add(new Pageday(new PagedayPK(range.getStart(), range.getDayInterval()+"", p.getPageid()), hit.getValue().size(), 0, (float)average, (float)stdDev));
            }
            hitMap.clear();
            em.getTransaction().begin();
            for (Pageday p:pageDays){
                p.setHitpercent(1.0f*p.getTimes()/totalCount);
                em.persist(p);
            }
            em.getTransaction().commit();
        }
        log.info("Analyzing requests finished");
        log.exiting(Analyzer.class.getName(), "analyze");
    }

    private void process() {
        
    }

    @PreDestroy
    public void destroy() {
    }
}
