/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package libOdyssey.db;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
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
@Table(name = "page", schema = "odyssey")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Page.findAll", query = "SELECT p FROM Page p"),
    @NamedQuery(name = "Page.findByPageid", query = "SELECT p FROM Page p WHERE p.pageid = :pageid"),
    @NamedQuery(name = "Page.findByUrl", query = "SELECT p FROM Page p WHERE p.url = :url"),
    @NamedQuery(name = "Page.findByParameters", query = "SELECT p FROM Page p WHERE p.parameters = :parameters")})
public class Page implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "pageid", nullable = false)
    private Integer pageid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 65000)
    @Column(name = "url", nullable = false, length = 65000)
    private String url;
    @Size(max = 2147483647)
    @Column(name = "parameters", length = 2147483647)
    private String parameters;
    @OneToMany(mappedBy = "pageid")
    private Collection<Httpsession> httpsessionCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "page")
    private Collection<Pageonpageday> pageonpagedayCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pageid")
    private Collection<Pagerequest> pagerequestCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "page")
    private Collection<Pageday> pagedayCollection;

    public Page() {
    }

    public Page(Integer pageid) {
        this.pageid = pageid;
    }

    public Page(Integer pageid, String url) {
        this.pageid = pageid;
        this.url = url;
    }

    public Integer getPageid() {
        return pageid;
    }

    public void setPageid(Integer pageid) {
        this.pageid = pageid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @XmlTransient
    @JsonIgnore
    public Collection<Httpsession> getHttpsessionCollection() {
        return httpsessionCollection;
    }

    public void setHttpsessionCollection(Collection<Httpsession> httpsessionCollection) {
        this.httpsessionCollection = httpsessionCollection;
    }

    @XmlTransient
    @JsonIgnore
    public Collection<Pageonpageday> getPageonpagedayCollection() {
        return pageonpagedayCollection;
    }

    public void setPageonpagedayCollection(Collection<Pageonpageday> pageonpagedayCollection) {
        this.pageonpagedayCollection = pageonpagedayCollection;
    }

    @XmlTransient
    @JsonIgnore
    public Collection<Pagerequest> getPagerequestCollection() {
        return pagerequestCollection;
    }

    public void setPagerequestCollection(Collection<Pagerequest> pagerequestCollection) {
        this.pagerequestCollection = pagerequestCollection;
    }

    @XmlTransient
    @JsonIgnore
    public Collection<Pageday> getPagedayCollection() {
        return pagedayCollection;
    }

    public void setPagedayCollection(Collection<Pageday> pagedayCollection) {
        this.pagedayCollection = pagedayCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pageid != null ? pageid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Page)) {
            return false;
        }
        Page other = (Page) object;
        if ((this.pageid == null && other.pageid != null) || (this.pageid != null && !this.pageid.equals(other.pageid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "libOdyssey.db.Page[ pageid=" + pageid + " ]";
    }
    
}
