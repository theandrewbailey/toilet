/*
 * To change this template, choose Tools | Templates
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author
 * alpha
 */
@Entity
@Table(name = "pagerequest", schema = "odyssey")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Pagerequest.findAll", query = "SELECT p FROM Pagerequest p"),
    @NamedQuery(name = "Pagerequest.findByPagerequestid", query = "SELECT p FROM Pagerequest p WHERE p.pagerequestid = :pagerequestid"),
    @NamedQuery(name = "Pagerequest.findByAtime", query = "SELECT p FROM Pagerequest p WHERE p.atime = :atime"),
    @NamedQuery(name = "Pagerequest.findByIp", query = "SELECT p FROM Pagerequest p WHERE p.ip = :ip"),
    @NamedQuery(name = "Pagerequest.findByResponsecode", query = "SELECT p FROM Pagerequest p WHERE p.responsecode = :responsecode"),
    @NamedQuery(name = "Pagerequest.findByServed", query = "SELECT p FROM Pagerequest p WHERE p.served = :served"),
    @NamedQuery(name = "Pagerequest.findByMethod", query = "SELECT p FROM Pagerequest p WHERE p.method = :method"),
    @NamedQuery(name = "Pagerequest.findByRendered", query = "SELECT p FROM Pagerequest p WHERE p.rendered = :rendered"),
    @NamedQuery(name = "Pagerequest.findByParameters", query = "SELECT p FROM Pagerequest p WHERE p.parameters = :parameters")})
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
    @Column(name = "ip", nullable = false, length = 100)
    private String ip;
    @Basic(optional = false)
    @NotNull
    @Column(name = "responsecode", nullable = false)
    private int responsecode;
    @Basic(optional = false)
    @NotNull
    @Column(name = "served", nullable = false)
    private int served;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "method", nullable = false, length = 100)
    private String method;
    @Column(name = "rendered")
    private Integer rendered;
    @Size(max = 2147483647)
    @Column(name = "parameters", length = 2147483647)
    private String parameters;
    @OneToMany(mappedBy = "camefrompagerequestid")
    private Collection<Pagerequest> pagerequestCollection;
    @JoinColumn(name = "camefrompagerequestid", referencedColumnName = "pagerequestid")
    @ManyToOne
    private Pagerequest camefrompagerequestid;
    @JoinColumn(name = "pageid", referencedColumnName = "pageid", nullable = false)
    @ManyToOne(optional = false)
    private Page pageid;
    @JoinColumn(name = "httpsessionid", referencedColumnName = "httpsessionid", nullable = false)
    @ManyToOne(optional = false)
    private Httpsession httpsessionid;
    @OneToMany(mappedBy = "pagerequestid")
    private Collection<Exceptionevent> exceptioneventCollection;

    public Pagerequest() {
    }

    public Pagerequest(Integer pagerequestid) {
        this.pagerequestid = pagerequestid;
    }

    public Pagerequest(Integer pagerequestid, Date atime, String ip, int responsecode, int served, String method) {
        this.pagerequestid = pagerequestid;
        this.atime = atime;
        this.ip = ip;
        this.responsecode = responsecode;
        this.served = served;
        this.method = method;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Integer getRendered() {
        return rendered;
    }

    public void setRendered(Integer rendered) {
        this.rendered = rendered;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @XmlTransient
    @JsonIgnore
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

    public Page getPageid() {
        return pageid;
    }

    public void setPageid(Page pageid) {
        this.pageid = pageid;
    }

    public Httpsession getHttpsessionid() {
        return httpsessionid;
    }

    public void setHttpsessionid(Httpsession httpsessionid) {
        this.httpsessionid = httpsessionid;
    }

    @XmlTransient
    @JsonIgnore
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
