/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package toilet.db;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
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

/**
 *
 * @author alphavm
 */
@Entity
@Table(name = "article", schema = "toilet")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Article.findAllDesc", query = "SELECT a FROM Article a ORDER BY a.posted DESC"),
    @NamedQuery(name = "Article.findAllAsc", query = "SELECT a FROM Article a ORDER BY a.posted ASC"),
    @NamedQuery(name = "Article.findByArticleid", query = "SELECT a FROM Article a WHERE a.articleid = :articleid"),
    @NamedQuery(name = "Article.findByArticletitle", query = "SELECT a FROM Article a WHERE a.articletitle = :articletitle"),
    @NamedQuery(name = "Article.findByEtag", query = "SELECT a FROM Article a WHERE a.etag = :etag"),
    @NamedQuery(name = "Article.findByModified", query = "SELECT a FROM Article a WHERE a.modified = :modified"),
    @NamedQuery(name = "Article.findByPosted", query = "SELECT a FROM Article a WHERE a.posted = :posted"),
    @NamedQuery(name = "Article.findByPostedhtml", query = "SELECT a FROM Article a WHERE a.postedhtml = :postedhtml"),
    @NamedQuery(name = "Article.findByPostedmarkdown", query = "SELECT a FROM Article a WHERE a.postedmarkdown = :postedmarkdown"),
    @NamedQuery(name = "Article.findByPostedname", query = "SELECT a FROM Article a WHERE a.postedname = :postedname"),
    @NamedQuery(name = "Article.findByComments", query = "SELECT a FROM Article a WHERE a.comments = :comments"),
    @NamedQuery(name = "Article.findByDescription", query = "SELECT a FROM Article a WHERE a.description = :description")})
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "articleid", nullable = false)
    private Integer articleid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "articletitle", nullable = false, length = 250)
    private String articletitle;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "etag", nullable = false, length = 250)
    private String etag;
    @Basic(optional = false)
    @NotNull
    @Column(name = "modified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;
    @Basic(optional = false)
    @NotNull
    @Column(name = "posted", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date posted;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "postedhtml", nullable = false, length = 2147483647)
    private String postedhtml;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "postedmarkdown", nullable = false, length = 2147483647)
    private String postedmarkdown;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "postedname", nullable = false, length = 250)
    private String postedname;
    @Column(name = "comments")
    private Boolean comments;
    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;
    @JoinColumn(name = "sectionid", referencedColumnName = "sectionid", nullable = false)
    @ManyToOne(optional = false)
    private Section sectionid;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articleid")
    @OrderBy("posted ASC")
    private Collection<Comment> commentCollection;

    public Article() {
    }

    public Article(Integer articleid) {
        this.articleid = articleid;
    }

    public Article(Integer articleid, String articletitle, String etag, Date modified, Date posted, String postedhtml, String postedmarkdown, String postedname) {
        this.articleid = articleid;
        this.articletitle = articletitle;
        this.etag = etag;
        this.modified = modified;
        this.posted = posted;
        this.postedhtml = postedhtml;
        this.postedmarkdown = postedmarkdown;
        this.postedname = postedname;
    }

    public Integer getArticleid() {
        return articleid;
    }

    public void setArticleid(Integer articleid) {
        this.articleid = articleid;
    }

    public String getArticletitle() {
        return articletitle;
    }

    public void setArticletitle(String articletitle) {
        this.articletitle = articletitle;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Date getPosted() {
        return posted;
    }

    public void setPosted(Date posted) {
        this.posted = posted;
    }

    public String getPostedhtml() {
        return postedhtml;
    }

    public void setPostedhtml(String postedhtml) {
        this.postedhtml = postedhtml;
    }

    public String getPostedmarkdown() {
        return postedmarkdown;
    }

    public void setPostedmarkdown(String postedmarkdown) {
        this.postedmarkdown = postedmarkdown;
    }

    public String getPostedname() {
        return postedname;
    }

    public void setPostedname(String postedname) {
        this.postedname = postedname;
    }

    public Boolean getComments() {
        return comments;
    }

    public void setComments(Boolean comments) {
        this.comments = comments;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Section getSectionid() {
        return sectionid;
    }

    public void setSectionid(Section sectionid) {
        this.sectionid = sectionid;
    }

    @XmlTransient
    public Collection<Comment> getCommentCollection() {
        return commentCollection;
    }

    public void setCommentCollection(Collection<Comment> commentCollection) {
        this.commentCollection = commentCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (articleid != null ? articleid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Article)) {
            return false;
        }
        Article other = (Article) object;
        if ((this.articleid == null && other.articleid != null) || (this.articleid != null && !this.articleid.equals(other.articleid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "toilet.db.Article[ articleid=" + articleid + " ]";
    }
    
}
