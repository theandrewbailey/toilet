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
public class PageonpagedayPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "pageid", nullable = false)
    private int pageid;
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
    @Basic(optional = false)
    @NotNull
    @Column(name = "secondarypage", nullable = false)
    private int secondarypage;

    public PageonpagedayPK() {
    }

    public PageonpagedayPK(int pageid, Date day, String dayinterval, int secondarypage) {
        this.pageid = pageid;
        this.day = day;
        this.dayinterval = dayinterval;
        this.secondarypage = secondarypage;
    }

    public int getPageid() {
        return pageid;
    }

    public void setPageid(int pageid) {
        this.pageid = pageid;
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

    public int getSecondarypage() {
        return secondarypage;
    }

    public void setSecondarypage(int secondarypage) {
        this.secondarypage = secondarypage;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) pageid;
        hash += (day != null ? day.hashCode() : 0);
        hash += (dayinterval != null ? dayinterval.hashCode() : 0);
        hash += (int) secondarypage;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PageonpagedayPK)) {
            return false;
        }
        PageonpagedayPK other = (PageonpagedayPK) object;
        if (this.pageid != other.pageid) {
            return false;
        }
        if ((this.day == null && other.day != null) || (this.day != null && !this.day.equals(other.day))) {
            return false;
        }
        if ((this.dayinterval == null && other.dayinterval != null) || (this.dayinterval != null && !this.dayinterval.equals(other.dayinterval))) {
            return false;
        }
        if (this.secondarypage != other.secondarypage) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "libOdyssey.db.PageonpagedayPK[ pageid=" + pageid + ", day=" + day + ", dayinterval=" + dayinterval + ", secondarypage=" + secondarypage + " ]";
    }
    
}
