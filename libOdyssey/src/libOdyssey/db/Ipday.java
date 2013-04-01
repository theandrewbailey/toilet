/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package libOdyssey.db;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author
 * alpha
 */
@Entity
@Table(name = "ipday", schema = "odyssey")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Ipday.findAll", query = "SELECT i FROM Ipday i"),
    @NamedQuery(name = "Ipday.findByIp", query = "SELECT i FROM Ipday i WHERE i.ipdayPK.ip = :ip"),
    @NamedQuery(name = "Ipday.findByDay", query = "SELECT i FROM Ipday i WHERE i.ipdayPK.day = :day"),
    @NamedQuery(name = "Ipday.findByDayinterval", query = "SELECT i FROM Ipday i WHERE i.ipdayPK.dayinterval = :dayinterval"),
    @NamedQuery(name = "Ipday.findByRequestedpages", query = "SELECT i FROM Ipday i WHERE i.requestedpages = :requestedpages"),
    @NamedQuery(name = "Ipday.findByTrafficpercent", query = "SELECT i FROM Ipday i WHERE i.trafficpercent = :trafficpercent"),
    @NamedQuery(name = "Ipday.findByVisits", query = "SELECT i FROM Ipday i WHERE i.visits = :visits")})
public class Ipday implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected IpdayPK ipdayPK;
    @Basic(optional = false)
    @NotNull
    @Column(name = "requestedpages", nullable = false)
    private long requestedpages;
    @Basic(optional = false)
    @NotNull
    @Column(name = "trafficpercent", nullable = false)
    private float trafficpercent;
    @Basic(optional = false)
    @NotNull
    @Column(name = "visits", nullable = false)
    private long visits;

    public Ipday() {
    }

    public Ipday(IpdayPK ipdayPK) {
        this.ipdayPK = ipdayPK;
    }

    public Ipday(IpdayPK ipdayPK, long requestedpages, float trafficpercent, long visits) {
        this.ipdayPK = ipdayPK;
        this.requestedpages = requestedpages;
        this.trafficpercent = trafficpercent;
        this.visits = visits;
    }

    public Ipday(String ip, Date day, String dayinterval) {
        this.ipdayPK = new IpdayPK(ip, day, dayinterval);
    }

    public IpdayPK getIpdayPK() {
        return ipdayPK;
    }

    public void setIpdayPK(IpdayPK ipdayPK) {
        this.ipdayPK = ipdayPK;
    }

    public long getRequestedpages() {
        return requestedpages;
    }

    public void setRequestedpages(long requestedpages) {
        this.requestedpages = requestedpages;
    }

    public float getTrafficpercent() {
        return trafficpercent;
    }

    public void setTrafficpercent(float trafficpercent) {
        this.trafficpercent = trafficpercent;
    }

    public long getVisits() {
        return visits;
    }

    public void setVisits(long visits) {
        this.visits = visits;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (ipdayPK != null ? ipdayPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Ipday)) {
            return false;
        }
        Ipday other = (Ipday) object;
        if ((this.ipdayPK == null && other.ipdayPK != null) || (this.ipdayPK != null && !this.ipdayPK.equals(other.ipdayPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "libOdyssey.db.Ipday[ ipdayPK=" + ipdayPK + " ]";
    }
    
}
