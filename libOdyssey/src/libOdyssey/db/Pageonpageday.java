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
import javax.persistence.JoinColumns;
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
@Table(name = "pageonpageday", schema = "odyssey")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Pageonpageday.findAll", query = "SELECT p FROM Pageonpageday p"),
    @NamedQuery(name = "Pageonpageday.findByPageid", query = "SELECT p FROM Pageonpageday p WHERE p.pageonpagedayPK.pageid = :pageid"),
    @NamedQuery(name = "Pageonpageday.findByDay", query = "SELECT p FROM Pageonpageday p WHERE p.pageonpagedayPK.day = :day"),
    @NamedQuery(name = "Pageonpageday.findByDayinterval", query = "SELECT p FROM Pageonpageday p WHERE p.pageonpagedayPK.dayinterval = :dayinterval"),
    @NamedQuery(name = "Pageonpageday.findBySecondarypage", query = "SELECT p FROM Pageonpageday p WHERE p.pageonpagedayPK.secondarypage = :secondarypage"),
    @NamedQuery(name = "Pageonpageday.findByLinkedtopercent", query = "SELECT p FROM Pageonpageday p WHERE p.linkedtopercent = :linkedtopercent"),
    @NamedQuery(name = "Pageonpageday.findByLinkedfrompercent", query = "SELECT p FROM Pageonpageday p WHERE p.linkedfrompercent = :linkedfrompercent"),
    @NamedQuery(name = "Pageonpageday.findByLinkedtotimes", query = "SELECT p FROM Pageonpageday p WHERE p.linkedtotimes = :linkedtotimes"),
    @NamedQuery(name = "Pageonpageday.findByLinkedfromtimes", query = "SELECT p FROM Pageonpageday p WHERE p.linkedfromtimes = :linkedfromtimes")})
public class Pageonpageday implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected PageonpagedayPK pageonpagedayPK;
    @Basic(optional = false)
    @NotNull
    @Column(name = "linkedtopercent", nullable = false)
    private float linkedtopercent;
    @Basic(optional = false)
    @NotNull
    @Column(name = "linkedfrompercent", nullable = false)
    private float linkedfrompercent;
    @Basic(optional = false)
    @NotNull
    @Column(name = "linkedtotimes", nullable = false)
    private long linkedtotimes;
    @Basic(optional = false)
    @NotNull
    @Column(name = "linkedfromtimes", nullable = false)
    private long linkedfromtimes;
    @JoinColumns({
        @JoinColumn(name = "pageid", referencedColumnName = "pageid", nullable = false, insertable = false, updatable = false),
        @JoinColumn(name = "day", referencedColumnName = "day", nullable = false, insertable = false, updatable = false),
        @JoinColumn(name = "dayinterval", referencedColumnName = "dayinterval", nullable = false, insertable = false, updatable = false)})
    @ManyToOne(optional = false)
    private Pageday pageday;
    @JoinColumn(name = "secondarypage", referencedColumnName = "pageid", nullable = false, insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Page page;

    public Pageonpageday() {
    }

    public Pageonpageday(PageonpagedayPK pageonpagedayPK) {
        this.pageonpagedayPK = pageonpagedayPK;
    }

    public Pageonpageday(PageonpagedayPK pageonpagedayPK, float linkedtopercent, float linkedfrompercent, long linkedtotimes, long linkedfromtimes) {
        this.pageonpagedayPK = pageonpagedayPK;
        this.linkedtopercent = linkedtopercent;
        this.linkedfrompercent = linkedfrompercent;
        this.linkedtotimes = linkedtotimes;
        this.linkedfromtimes = linkedfromtimes;
    }

    public Pageonpageday(int pageid, Date day, String dayinterval, int secondarypage) {
        this.pageonpagedayPK = new PageonpagedayPK(pageid, day, dayinterval, secondarypage);
    }

    public PageonpagedayPK getPageonpagedayPK() {
        return pageonpagedayPK;
    }

    public void setPageonpagedayPK(PageonpagedayPK pageonpagedayPK) {
        this.pageonpagedayPK = pageonpagedayPK;
    }

    public float getLinkedtopercent() {
        return linkedtopercent;
    }

    public void setLinkedtopercent(float linkedtopercent) {
        this.linkedtopercent = linkedtopercent;
    }

    public float getLinkedfrompercent() {
        return linkedfrompercent;
    }

    public void setLinkedfrompercent(float linkedfrompercent) {
        this.linkedfrompercent = linkedfrompercent;
    }

    public long getLinkedtotimes() {
        return linkedtotimes;
    }

    public void setLinkedtotimes(long linkedtotimes) {
        this.linkedtotimes = linkedtotimes;
    }

    public long getLinkedfromtimes() {
        return linkedfromtimes;
    }

    public void setLinkedfromtimes(long linkedfromtimes) {
        this.linkedfromtimes = linkedfromtimes;
    }

    public Pageday getPageday() {
        return pageday;
    }

    public void setPageday(Pageday pageday) {
        this.pageday = pageday;
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
        hash += (pageonpagedayPK != null ? pageonpagedayPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Pageonpageday)) {
            return false;
        }
        Pageonpageday other = (Pageonpageday) object;
        if ((this.pageonpagedayPK == null && other.pageonpagedayPK != null) || (this.pageonpagedayPK != null && !this.pageonpagedayPK.equals(other.pageonpagedayPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "libOdyssey.db.Pageonpageday[ pageonpagedayPK=" + pageonpagedayPK + " ]";
    }
    
}
