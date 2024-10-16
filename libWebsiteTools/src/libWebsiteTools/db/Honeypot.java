package libWebsiteTools.db;

import java.io.Serializable;
import java.time.OffsetDateTime;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 *
 * @author alpha
 */
@Entity
@Table(name = "honeypot", schema = "tools")
@NamedQueries({
    @NamedQuery(name = "Honeypot.findByIp", query = "SELECT h FROM Honeypot h WHERE h.ip = :ip"),
    @NamedQuery(name = "Honeypot.findByIpBeforeNow", query = "SELECT h FROM Honeypot h WHERE h.ip = :ip AND h.expiresatatime > :now"),
    @NamedQuery(name = "Honeypot.clean", query = "DELETE FROM Honeypot h WHERE h.expiresatatime < :now")})
public class Honeypot implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "honeypotid", nullable = false)
    private Integer honeypotid;
    @Basic(optional = false)
    @NotNull
    @Column(name = "expiresatatime", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime expiresatatime;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "ip", nullable = false, length = 100)
    private String ip;
    @Basic(optional = false)
    @NotNull
    @Column(name = "startedatatime", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime startedatatime;

    public Honeypot() {
    }

    public Honeypot(Integer honeypotid) {
        this.honeypotid = honeypotid;
    }

    public Honeypot(Integer honeypotid, OffsetDateTime expiresatatime, String ip, OffsetDateTime startedatatime) {
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

    public OffsetDateTime getExpiresatatime() {
        return expiresatatime;
    }

    public void setExpiresatatime(OffsetDateTime expiresatatime) {
        this.expiresatatime = expiresatatime;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public OffsetDateTime getStartedatatime() {
        return startedatatime;
    }

    public void setStartedatatime(OffsetDateTime startedatatime) {
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
        return "libWebsiteTools.db.Honeypot[ honeypotid=" + honeypotid + " ]";
    }
    
}
