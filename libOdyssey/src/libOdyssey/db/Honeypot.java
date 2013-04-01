/*
 * To change this template, choose Tools | Templates
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
 * @author
 * alpha
 */
@Entity
@Table(name = "honeypot", schema = "odyssey")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Honeypot.findAll", query = "SELECT h FROM Honeypot h"),
    @NamedQuery(name = "Honeypot.findByHoneypotid", query = "SELECT h FROM Honeypot h WHERE h.honeypotid = :honeypotid"),
    @NamedQuery(name = "Honeypot.findByIp", query = "SELECT h FROM Honeypot h WHERE h.ip = :ip"),
    @NamedQuery(name = "Honeypot.findByStartedatatime", query = "SELECT h FROM Honeypot h WHERE h.startedatatime = :startedatatime"),
    @NamedQuery(name = "Honeypot.findByExpiresatatime", query = "SELECT h FROM Honeypot h WHERE h.expiresatatime = :expiresatatime")})
public class Honeypot implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "honeypotid", nullable = false)
    private Integer honeypotid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "ip", nullable = false, length = 100)
    private String ip;
    @Basic(optional = false)
    @NotNull
    @Column(name = "startedatatime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startedatatime;
    @Basic(optional = false)
    @NotNull
    @Column(name = "expiresatatime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresatatime;

    public Honeypot() {
    }

    public Honeypot(Integer honeypotid) {
        this.honeypotid = honeypotid;
    }

    public Honeypot(Integer honeypotid, String ip, Date startedatatime, Date expiresatatime) {
        this.honeypotid = honeypotid;
        this.ip = ip;
        this.startedatatime = startedatatime;
        this.expiresatatime = expiresatatime;
    }

    public Integer getHoneypotid() {
        return honeypotid;
    }

    public void setHoneypotid(Integer honeypotid) {
        this.honeypotid = honeypotid;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getStartedatatime() {
        return startedatatime;
    }

    public void setStartedatatime(Date startedatatime) {
        this.startedatatime = startedatatime;
    }

    public Date getExpiresatatime() {
        return expiresatatime;
    }

    public void setExpiresatatime(Date expiresatatime) {
        this.expiresatatime = expiresatatime;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (honeypotid != null ? honeypotid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Honeypot)) {
            return false;
        }
        Honeypot other = (Honeypot) object;
        if ((this.honeypotid == null && other.honeypotid != null) || (this.honeypotid != null && !this.honeypotid.equals(other.honeypotid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "libOdyssey.db.Honeypot[ honeypotid=" + honeypotid + " ]";
    }
    
}
