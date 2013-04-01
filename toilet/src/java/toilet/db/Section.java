package toilet.db;

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

@Entity
@Table(name = "section", schema = "toilet")
@NamedQueries({
    @NamedQuery(name = "Section.findAll", query = "SELECT s FROM Section s"),
    @NamedQuery(name = "Section.findBySectionid", query = "SELECT s FROM Section s WHERE s.sectionid = :sectionid"),
    @NamedQuery(name = "Section.findByName", query = "SELECT s FROM Section s WHERE s.name = :name")})
public class Section implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "sectionid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sectionid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 250)
    @Column(name = "name")
    private String name;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sectionid")
    private Collection<Article> articleCollection;

    public Section() {
    }

    public Section(Integer sectionid) {
        this.sectionid = sectionid;
    }

    public Section(Integer sectionid, String name) {
        this.sectionid = sectionid;
        this.name = name;
    }

    public Integer getSectionid() {
        return sectionid;
    }

    public void setSectionid(Integer sectionid) {
        this.sectionid = sectionid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Article> getArticleCollection() {
        return articleCollection;
    }

    public void setArticleCollection(Collection<Article> articleCollection) {
        this.articleCollection = articleCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (sectionid != null ? sectionid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Section)) {
            return false;
        }
        Section other = (Section) object;
        if ((this.sectionid == null && other.sectionid != null) || (this.sectionid != null && !this.sectionid.equals(other.sectionid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "toilet.db.Section[ sectionid=" + sectionid + " ]";
    }
    
}
