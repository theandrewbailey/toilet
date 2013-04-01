/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package libOdyssey.db;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author
 * alpha
 */
@Embeddable
public class IpdayPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "ip", nullable = false, length = 100)
    private String ip;
    @Basic(optional = false)
    @NotNull
    @Column(name = "day", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date day;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "dayinterval", nullable = false, length = 2147483647)
    private String dayinterval;

    public IpdayPK() {
    }

    public IpdayPK(String ip, Date day, String dayinterval) {
        this.ip = ip;
        this.day = day;
        this.dayinterval = dayinterval;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public String getDayinterval() {
        return dayinterval;
    }

    public void setDayinterval(String dayinterval) {
        this.dayinterval = dayinterval;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (ip != null ? ip.hashCode() : 0);
        hash += (day != null ? day.hashCode() : 0);
        hash += (dayinterval != null ? dayinterval.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof IpdayPK)) {
            return false;
        }
        IpdayPK other = (IpdayPK) object;
        if ((this.ip == null && other.ip != null) || (this.ip != null && !this.ip.equals(other.ip))) {
            return false;
        }
        if ((this.day == null && other.day != null) || (this.day != null && !this.day.equals(other.day))) {
            return false;
        }
        if ((this.dayinterval == null && other.dayinterval != null) || (this.dayinterval != null && !this.dayinterval.equals(other.dayinterval))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "libOdyssey.db.IpdayPK[ ip=" + ip + ", day=" + day + ", dayinterval=" + dayinterval + " ]";
    }
    
}
