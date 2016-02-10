/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libOdyssey.db;

import java.io.Serializable;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author alphavm
 */
@Entity
@Table(name = "exceptionevent", schema = "odyssey")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Exceptionevent.findAll", query = "SELECT e FROM Exceptionevent e"),
    @NamedQuery(name = "Exceptionevent.findByExceptioneventid", query = "SELECT e FROM Exceptionevent e WHERE e.exceptioneventid = :exceptioneventid"),
    @NamedQuery(name = "Exceptionevent.findByAtime", query = "SELECT e FROM Exceptionevent e WHERE e.atime = :atime"),
    @NamedQuery(name = "Exceptionevent.findByDescription", query = "SELECT e FROM Exceptionevent e WHERE e.description = :description"),
    @NamedQuery(name = "Exceptionevent.findByTitle", query = "SELECT e FROM Exceptionevent e WHERE e.title = :title")})
public class Exceptionevent implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "exceptioneventid", nullable = false)
    private Integer exceptioneventid;
    @Basic(optional = false)
    @NotNull
    @Column(name = "atime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date atime;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "description", nullable = false, length = 2147483647)
    private String description;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 65000)
    @Column(name = "title", nullable = false, length = 65000)
    private String title;
    @JoinColumn(name = "pagerequestid", referencedColumnName = "pagerequestid")
    @ManyToOne
    private Pagerequest pagerequestid;

    public Exceptionevent() {
    }

    public Exceptionevent(Integer exceptioneventid) {
        this.exceptioneventid = exceptioneventid;
    }

    public Exceptionevent(Integer exceptioneventid, Date atime, String description, String title) {
        this.exceptioneventid = exceptioneventid;
        this.atime = atime;
        this.description = description;
        this.title = title;
    }

    public Integer getExceptioneventid() {
        return exceptioneventid;
    }

    public void setExceptioneventid(Integer exceptioneventid) {
        this.exceptioneventid = exceptioneventid;
    }

    public Date getAtime() {
        return atime;
    }

    public void setAtime(Date atime) {
        this.atime = atime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Pagerequest getPagerequestid() {
        return pagerequestid;
    }

    public void setPagerequestid(Pagerequest pagerequestid) {
        this.pagerequestid = pagerequestid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (exceptioneventid != null ? exceptioneventid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Exceptionevent)) {
            return false;
        }
        Exceptionevent other = (Exceptionevent) object;
        if ((this.exceptioneventid == null && other.exceptioneventid != null) || (this.exceptioneventid != null && !this.exceptioneventid.equals(other.exceptioneventid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "libOdyssey.db.Exceptionevent[ exceptioneventid=" + exceptioneventid + " ]";
    }
    
}
