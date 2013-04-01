/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package libOdyssey.db;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
import javax.persistence.OrderBy;
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
@Table(name = "httpsession", schema = "odyssey")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Httpsession.findAll", query = "SELECT h FROM Httpsession h"),
    @NamedQuery(name = "Httpsession.findByHttpsessionid", query = "SELECT h FROM Httpsession h WHERE h.httpsessionid = :httpsessionid"),
    @NamedQuery(name = "Httpsession.findByServersessionid", query = "SELECT h FROM Httpsession h WHERE h.serversessionid = :serversessionid"),
    @NamedQuery(name = "Httpsession.findByAtime", query = "SELECT h FROM Httpsession h WHERE h.atime = :atime"),
    @NamedQuery(name = "Httpsession.findByUseragent", query = "SELECT h FROM Httpsession h WHERE h.useragent = :useragent"),
    @NamedQuery(name = "Httpsession.findByBrowser", query = "SELECT h FROM Httpsession h WHERE h.browser = :browser"),
    @NamedQuery(name = "Httpsession.findByIsarealperson", query = "SELECT h FROM Httpsession h WHERE h.isarealperson = :isarealperson"),
    @NamedQuery(name = "Httpsession.findByIp", query = "SELECT h FROM Httpsession h WHERE h.ip = :ip"),
    @NamedQuery(name = "Httpsession.findByHamscore", query = "SELECT h FROM Httpsession h WHERE h.hamscore = :hamscore")})
public class Httpsession implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "httpsessionid", nullable = false)
    private Integer httpsessionid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "serversessionid", nullable = false, length = 250)
    private String serversessionid;
    @Basic(optional = false)
    @NotNull
    @Column(name = "atime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date atime;
    @Size(max = 65000)
    @Column(name = "useragent", length = 65000)
    private String useragent;
    @Size(max = 1000)
    @Column(name = "browser", length = 1000)
    private String browser;
    @Column(name = "isarealperson")
    private Boolean isarealperson;
    @Size(max = 100)
    @Column(name = "ip", length = 100)
    private String ip;
    @Column(name = "hamscore")
    private BigInteger hamscore;
    @JoinColumn(name = "pageid", referencedColumnName = "pageid")
    @ManyToOne
    private Page pageid;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "httpsessionid")
    @OrderBy("atime")
    private List<Pagerequest> pagerequestCollection;

    public Httpsession() {
    }

    public Httpsession(Integer httpsessionid) {
        this.httpsessionid = httpsessionid;
    }

    public Httpsession(Integer httpsessionid, String serversessionid, Date atime) {
        this.httpsessionid = httpsessionid;
        this.serversessionid = serversessionid;
        this.atime = atime;
    }

    public Integer getHttpsessionid() {
        return httpsessionid;
    }

    public void setHttpsessionid(Integer httpsessionid) {
        this.httpsessionid = httpsessionid;
    }

    public String getServersessionid() {
        return serversessionid;
    }

    public void setServersessionid(String serversessionid) {
        this.serversessionid = serversessionid;
    }

    public Date getAtime() {
        return atime;
    }

    public void setAtime(Date atime) {
        this.atime = atime;
    }

    public String getUseragent() {
        return useragent;
    }

    public void setUseragent(String useragent) {
        this.useragent = useragent;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public Boolean getIsarealperson() {
        return isarealperson;
    }

    public void setIsarealperson(Boolean isarealperson) {
        this.isarealperson = isarealperson;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public BigInteger getHamscore() {
        return hamscore;
    }

    public void setHamscore(BigInteger hamscore) {
        this.hamscore = hamscore;
    }

    public Page getPageid() {
        return pageid;
    }

    public void setPageid(Page pageid) {
        this.pageid = pageid;
    }

    @XmlTransient
    @JsonIgnore
    public List<Pagerequest> getPagerequestCollection() {
        return pagerequestCollection;
    }

    public void setPagerequestCollection(List<Pagerequest> pagerequestCollection) {
        this.pagerequestCollection = pagerequestCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (httpsessionid != null ? httpsessionid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Httpsession)) {
            return false;
        }
        Httpsession other = (Httpsession) object;
        if ((this.httpsessionid == null && other.httpsessionid != null) || (this.httpsessionid != null && !this.httpsessionid.equals(other.httpsessionid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "libOdyssey.db.Httpsession[ httpsessionid=" + httpsessionid + " ]";
    }
    
}
