package libOdyssey.bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceUnit;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.DateRange;
import libOdyssey.RangeGenerator;
import libOdyssey.ResponseTag;
import static libOdyssey.bean.SessionBean.getURL;
import libOdyssey.db.Page;
import libOdyssey.db.Pageday;
import libOdyssey.db.Pagerequest;

/**
 *
 * @author alpha
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class Analyzer {

    public static final String LOCAL_NAME = "java:module/Analyzer";
    public static final String GET_TIMES_QUERY = "SELECT DISTINCT CAST(EXTRACT(DAY FROM atime) AS INT) AS \"day\",CAST(EXTRACT(MONTH FROM atime) AS INT) AS \"month\",CAST(EXTRACT(YEAR FROM atime) AS INT) AS \"year\" FROM odyssey.httpsession ORDER BY \"year\" DESC,\"month\" DESC,\"day\" DESC;";
    private static final Logger LOG = Logger.getLogger(Analyzer.class.getName());
    @EJB
    private ExceptionRepo error;
    @PersistenceUnit
    private EntityManagerFactory PU;

    public void logRequest(HttpServletRequest req, HttpServletResponse res) {
        EntityManager em = PU.createEntityManager();
        boolean newSession = false;
        Long renderTime=(Long)req.getAttribute(ResponseTag.RENDER_TIME_PARAM);

        // log it
        String urlstr = getURL(req);
        if (urlstr == null) {
            return;
        }

        try {
            em.getTransaction().begin();

            // get DB row corresponding to URL
            Page page = getPageByUrl(urlstr);

            // create DB row for this page request
//            Pagerequest pr = new Pagerequest(null, new java.util.Date(), req.getMethod(), res.getStatus(), 0);
//            pr.setPageid(page);
//            if (renderTime != null) {
//                pr.setRendered(renderTime.intValue());
//            }
//            if (req.getHeader("Referer") != null) {
//                TypedQuery<Pagerequest> q = em.createQuery(newSession ? FIRST_REQUEST : SUBSEQUENT_REQUEST, Pagerequest.class);
//                String referred=req.getHeader("Referer");
//                if (!newSession) {
//                    referred = referred.substring(referred.indexOf(guard.getHostValue()) + guard.getHostValue().length());
//                    referred = getURL(req.getServletContext().getContextPath(), referred, null);
//                }
//                q.setParameter("page", anal.getPageByUrl(referred).getPageid());
//                q.setMaxResults(1);
//                try {
//                    pr.setCamefrompagerequestid(q.getSingleResult());
//                } catch (NoResultException x) {
//                }
//            }
//            pr.setParameters(ExceptionRepo.getParameters(req, "\n"));
//
//            pr.setServed((int)(new Date().getTime() - ((Date)req.getAttribute(RequestTime.TIME_PARAM)).getTime()));
//            em.persist(pr);
            em.getTransaction().commit();
        } catch (Exception x) {
            error.add(req, null, null, x);
        } finally {
            em.close();
        }
    }

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
        } finally {
            em.close();
        }
    }

    public List<Pagerequest> getHitsInRange(Date start, Date end){
        EntityManager em = PU.createEntityManager();
        try {
            TypedQuery<Pagerequest> q = em.createQuery("SELECT p FROM Pagerequest p WHERE p.atime BETWEEN :start AND :end", Pagerequest.class);
            q.setParameter("start", start);
            q.setParameter("end", end);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public boolean isRangeProcessed(Date day, Character dayInterval){
        EntityManager em = PU.createEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery("SELECT COUNT(p) FROM Pageday p WHERE p.pagedayPK.day = :day AND p.pagedayPK.dayinterval = :dayInterval", Long.class);
            q.setParameter("day", day);
            q.setParameter("dayInterval", dayInterval.toString());
            return q.getSingleResult() != 0L;
        } finally {
            em.close();
        }
    }

//    @Schedule(hour = "5")
    public void analyze() {
        LOG.entering(Analyzer.class.getName(), "analyze");
        LOG.info("Analyzing requests");
        SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd");
        EntityManager em=PU.createEntityManager();
        try {
            for (DateRange range:new RangeGenerator(this)){
                LOG.info(String.format("Analyzing {0}, interval {1}", f.format(range.getStart()), range.getDayInterval()));
                long totalCount=0L;
            // get pages in range
            // count hits
            // trace where they went to
            // count them, percent them
            // do again for where they came from
                List<Pagerequest> hits=getHitsInRange(range.getStart(), range.getEnd());
                Map<String, List<Pagerequest>> hitMap=new HashMap<>(hits.size());
    //            for(Pagerequest hit:hits){
    //                if (!hitMap.containsKey(hit.getPageid().getUrl())){
    //                    hitMap.put(hit.getPageid().getUrl(), new ArrayList<Pagerequest>());
    //                }
    //                hitMap.get(hit.getPageid().getUrl()).add(hit);
    //            }
                hits.clear();
                List<Pageday> pageDays=new ArrayList<>(hitMap.size()+10);
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
        } finally {
            em.close();
        }
        LOG.info("Analyzing requests finished");
        LOG.exiting(Analyzer.class.getName(), "analyze");
    }

    private void process() {
        
    }

    @PreDestroy
    public void destroy() {
    }
}
