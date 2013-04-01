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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
@Table(name = "pageday", schema = "odyssey")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Pageday.findAll", query = "SELECT p FROM Pageday p"),
    @NamedQuery(name = "Pageday.findByDay", query = "SELECT p FROM Pageday p WHERE p.pagedayPK.day = :day"),
    @NamedQuery(name = "Pageday.findByDayinterval", query = "SELECT p FROM Pageday p WHERE p.pagedayPK.dayinterval = :dayinterval"),
    @NamedQuery(name = "Pageday.findByPageid", query = "SELECT p FROM Pageday p WHERE p.pagedayPK.pageid = :pageid"),
    @NamedQuery(name = "Pageday.findByTimes", query = "SELECT p FROM Pageday p WHERE p.times = :times"),
    @NamedQuery(name = "Pageday.findByHitpercent", query = "SELECT p FROM Pageday p WHERE p.hitpercent = :hitpercent"),
    @NamedQuery(name = "Pageday.findByAverage", query = "SELECT p FROM Pageday p WHERE p.average = :average"),
    @NamedQuery(name = "Pageday.findByStandarddeviation", query = "SELECT p FROM Pageday p WHERE p.standarddeviation = :standarddeviation")})
public class Pageday implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected PagedayPK pagedayPK;
    @Basic(optional = false)
    @NotNull
    @Column(name = "times", nullable = false)
    private long times;
    @Basic(optional = false)
    @NotNull
    @Column(name = "hitpercent", nullable = false)
    private float hitpercent;
    @Basic(optional = false)
    @NotNull
    @Column(name = "average", nullable = false)
    private float average;
    @Basic(optional = false)
    @NotNull
    @Column(name = "standarddeviation", nullable = false)
    private float standarddeviation;
    @JoinColumn(name = "pageid", referencedColumnName = "pageid", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Page page;

    public Pageday() {
    }

    public Pageday(PagedayPK pagedayPK) {
        this.pagedayPK = pagedayPK;
    }

    public Pageday(PagedayPK pagedayPK, long times, float hitpercent, float average, float standarddeviation) {
        this.pagedayPK = pagedayPK;
        this.times = times;
        this.hitpercent = hitpercent;
        this.average = average;
        this.standarddeviation = standarddeviation;
    }

    public Pageday(Date day, String dayinterval, int pageid) {
        this.pagedayPK = new PagedayPK(day, dayinterval, pageid);
    }

    public PagedayPK getPagedayPK() {
        return pagedayPK;
    }

    public void setPagedayPK(PagedayPK pagedayPK) {
        this.pagedayPK = pagedayPK;
    }

    public long getTimes() {
        return times;
    }

    public void setTimes(long times) {
        this.times = times;
    }

    public float getHitpercent() {
        return hitpercent;
    }

    public void setHitpercent(float hitpercent) {
        this.hitpercent = hitpercent;
    }

    public float getAverage() {
        return average;
    }

    public void setAverage(float average) {
        this.average = average;
    }

    public float getStandarddeviation() {
        return standarddeviation;
    }

    public void setStandarddeviation(float standarddeviation) {
        this.standarddeviation = standarddeviation;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pagedayPK != null ? pagedayPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Pageday)) {
            return false;
        }
        Pageday other = (Pageday) object;
        if ((this.pagedayPK == null && other.pagedayPK != null) || (this.pagedayPK != null && !this.pagedayPK.equals(other.pagedayPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "libOdyssey.db.Pageday[ pagedayPK=" + pagedayPK + " ]";
    }
    
}
