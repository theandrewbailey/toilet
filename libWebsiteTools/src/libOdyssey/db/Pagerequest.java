/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libOdyssey.db;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author alpha
 */
@Entity
@Table(name = "pagerequest", schema = "tools")
@NamedQueries({
    @NamedQuery(name = "Pagerequest.findAll", query = "SELECT p FROM Pagerequest p"),
    @NamedQuery(name = "Pagerequest.findByPagerequestid", query = "SELECT p FROM Pagerequest p WHERE p.pagerequestid = :pagerequestid"),
    @NamedQuery(name = "Pagerequest.findByAtime", query = "SELECT p FROM Pagerequest p WHERE p.atime = :atime"),
    @NamedQuery(name = "Pagerequest.findByMethod", query = "SELECT p FROM Pagerequest p WHERE p.method = :method"),
    @NamedQuery(name = "Pagerequest.findByResponsecode", query = "SELECT p FROM Pagerequest p WHERE p.responsecode = :responsecode"),
    @NamedQuery(name = "Pagerequest.findByServed", query = "SELECT p FROM Pagerequest p WHERE p.served = :served"),
    @NamedQuery(name = "Pagerequest.findByParameters", query = "SELECT p FROM Pagerequest p WHERE p.parameters = :parameters"),
    @NamedQuery(name = "Pagerequest.findByRendered", query = "SELECT p FROM Pagerequest p WHERE p.rendered = :rendered")})
public class Pagerequest implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "pagerequestid", nullable = false)
    private Integer pagerequestid;
    @Basic(optional = false)
    @NotNull
    @Column(name = "atime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date atime;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "method", nullable = false, length = 100)
    private String method;
    @Basic(optional = false)
    @NotNull
    @Column(name = "responsecode", nullable = false)
    private int responsecode;
    @Basic(optional = false)
    @NotNull
    @Column(name = "served", nullable = false)
    private int served;
    @Size(max = 2147483647)
    @Column(name = "parameters", length = 2147483647)
    private String parameters;
    @Column(name = "rendered")
    private Integer rendered;
    @JoinColumn(name = "requestedpageid", referencedColumnName = "pageid", nullable = false)
    @ManyToOne(optional = false)
    private Page requestedpageid;
    @JoinColumn(name = "referredbypageid", referencedColumnName = "pageid")
    @ManyToOne
    private Page referredbypageid;
    @OneToMany(mappedBy = "camefrompagerequestid")
    private Collection<Pagerequest> pagerequestCollection;
    @JoinColumn(name = "camefrompagerequestid", referencedColumnName = "pagerequestid")
    @ManyToOne
    private Pagerequest camefrompagerequestid;
    @OneToMany(mappedBy = "pagerequestid")
    private Collection<Exceptionevent> exceptioneventCollection;

    public Pagerequest() {
    }

    public Pagerequest(Integer pagerequestid) {
        this.pagerequestid = pagerequestid;
    }

    public Pagerequest(Integer pagerequestid, Date atime, String method, int responsecode, int served) {
        this.pagerequestid = pagerequestid;
        this.atime = atime;
        this.method = method;
        this.responsecode = responsecode;
        this.served = served;
    }

    public Integer getPagerequestid() {
        return pagerequestid;
    }

    public void setPagerequestid(Integer pagerequestid) {
        this.pagerequestid = pagerequestid;
    }

    public Date getAtime() {
        return atime;
    }

    public void setAtime(Date atime) {
        this.atime = atime;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getResponsecode() {
        return responsecode;
    }

    public void setResponsecode(int responsecode) {
        this.responsecode = responsecode;
    }

    public int getServed() {
        return served;
    }

    public void setServed(int served) {
        this.served = served;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Integer getRendered() {
        return rendered;
    }

    public void setRendered(Integer rendered) {
        this.rendered = rendered;
    }

    public Page getRequestedpageid() {
        return requestedpageid;
    }

    public void setRequestedpageid(Page requestedpageid) {
        this.requestedpageid = requestedpageid;
    }

    public Page getReferredbypageid() {
        return referredbypageid;
    }

    public void setReferredbypageid(Page referredbypageid) {
        this.referredbypageid = referredbypageid;
    }

    public Collection<Pagerequest> getPagerequestCollection() {
        return pagerequestCollection;
    }

    public void setPagerequestCollection(Collection<Pagerequest> pagerequestCollection) {
        this.pagerequestCollection = pagerequestCollection;
    }

    public Pagerequest getCamefrompagerequestid() {
        return camefrompagerequestid;
    }

    public void setCamefrompagerequestid(Pagerequest camefrompagerequestid) {
        this.camefrompagerequestid = camefrompagerequestid;
    }

    public Collection<Exceptionevent> getExceptioneventCollection() {
        return exceptioneventCollection;
    }

    public void setExceptioneventCollection(Collection<Exceptionevent> exceptioneventCollection) {
        this.exceptioneventCollection = exceptioneventCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pagerequestid != null ? pagerequestid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Pagerequest)) {
            return false;
        }
        Pagerequest other = (Pagerequest) object;
        if ((this.pagerequestid == null && other.pagerequestid != null) || (this.pagerequestid != null && !this.pagerequestid.equals(other.pagerequestid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "libOdyssey.db.Pagerequest[ pagerequestid=" + pagerequestid + " ]";
    }
    
}
