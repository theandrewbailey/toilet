/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libWebsiteTools.db;

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

/**
 *
 * @author alpha
 */
@Entity
@Table(name = "honeypot", schema = "tools")
@NamedQueries({
    @NamedQuery(name = "Honeypot.findByIp", query = "SELECT h FROM Honeypot h WHERE h.ip = :ip"),
    @NamedQuery(name = "Honeypot.findByIpBeforeNow", query = "SELECT h FROM Honeypot h WHERE h.ip = :ip AND h.expiresatatime > CURRENT_TIMESTAMP"),
    @NamedQuery(name = "Honeypot.clean", query = "DELETE FROM Honeypot h WHERE h.expiresatatime < CURRENT_TIMESTAMP")})
public class Honeypot implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "honeypotid", nullable = false)
    private Integer honeypotid;
    @Basic(optional = false)
    @NotNull
    @Column(name = "expiresatatime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresatatime;
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

    public Honeypot() {
    }

    public Honeypot(Integer honeypotid) {
        this.honeypotid = honeypotid;
    }

    public Honeypot(Integer honeypotid, Date expiresatatime, String ip, Date startedatatime) {
        this.honeypotid = honeypotid;
        this.expiresatatime = expiresatatime;
        this.ip = ip;
        this.startedatatime = startedatatime;
    }

    public Integer getHoneypotid() {
        return honeypotid;
    }

    public void setHoneypotid(Integer honeypotid) {
        this.honeypotid = honeypotid;
    }

    public Date getExpiresatatime() {
        return expiresatatime;
    }

    public void setExpiresatatime(Date expiresatatime) {
        this.expiresatatime = expiresatatime;
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
        return !((this.honeypotid == null && other.honeypotid != null) || (this.honeypotid != null && !this.honeypotid.equals(other.honeypotid)));
    }

    @Override
    public String toString() {
        return "libOdyssey.db.Honeypot[ honeypotid=" + honeypotid + " ]";
    }
    
}
