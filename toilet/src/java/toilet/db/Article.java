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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "article", schema = "toilet")
@NamedQueries({
    @NamedQuery(name = "Article.findAll", query = "SELECT a FROM Article a ORDER BY a.posted"),
    @NamedQuery(name = "Article.findByArticleid", query = "SELECT a FROM Article a WHERE a.articleid = :articleid"),
    @NamedQuery(name = "Article.findByArticletitle", query = "SELECT a FROM Article a WHERE a.articletitle = :articletitle"),
    @NamedQuery(name = "Article.findByPosted", query = "SELECT a FROM Article a WHERE a.posted = :posted"),
    @NamedQuery(name = "Article.findByPostedtext", query = "SELECT a FROM Article a WHERE a.postedtext = :postedtext"),
    @NamedQuery(name = "Article.findByEtag", query = "SELECT a FROM Article a WHERE a.etag = :etag"),
    @NamedQuery(name = "Article.findByModified", query = "SELECT a FROM Article a WHERE a.modified = :modified"),
    @NamedQuery(name = "Article.findByPostedname", query = "SELECT a FROM Article a WHERE a.postedname = :postedname"),
    @NamedQuery(name = "Article.findByComments", query = "SELECT a FROM Article a WHERE a.comments = :comments"),
    @NamedQuery(name = "Article.findByDescription", query = "SELECT a FROM Article a WHERE a.description = :description")})
public class Article implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "articleid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer articleid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 250)
    @Column(name = "articletitle")
    private String articletitle;
    @Basic(optional = false)
    @NotNull
    @Column(name = "posted")
    @Temporal(TemporalType.TIMESTAMP)
    private Date posted;
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 65000)
    @Column(name = "postedtext")
    private String postedtext;
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 250)
    @Column(name = "etag")
    private String etag;
    @Basic(optional = false)
    @NotNull
    @Column(name = "modified")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 250)
    @Column(name = "postedname")
    private String postedname;
    @Column(name = "comments")
    private Boolean comments;
    @Size(max = 65000)
    @Column(name = "description")
    private String description;
//    @JoinColumn(name = "urlid", referencedColumnName = "urlid")
//    @ManyToOne
//    private Url urlid;
    @JoinColumn(name = "sectionid", referencedColumnName = "sectionid")
    @ManyToOne(optional = false)
    private Section sectionid;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "articleid")
    @OrderBy("posted")
    private Collection<Comment> commentCollection;

    @Transient
    private int commentCount=0;

    public int getCommentCount(){
        if (commentCollection != null) {
            return commentCollection.size();
        }
        return commentCount;
    }
    public void setCommentCount(int c){
        commentCount=c;
    }

    public Article() {
    }

    public Article(Integer articleid) {
        this.articleid = articleid;
    }

    public Article(Integer articleid, String articletitle, Date posted, String postedtext, String etag, Date modified, String postedname) {
        this.articleid = articleid;
        this.articletitle = articletitle;
        this.posted = posted;
        this.postedtext = postedtext;
        this.etag = etag;
        this.modified = modified;
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

    public Date getPosted() {
        return posted;
    }

    public void setPosted(Date posted) {
        this.posted = posted;
    }

    public String getPostedtext() {
        return postedtext;
    }

    public void setPostedtext(String postedtext) {
        this.postedtext = postedtext;
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

//    public Url getUrlid() {
//        return urlid;
//    }
//
//    public void setUrlid(Url urlid) {
//        this.urlid = urlid;
//    }

    public Section getSectionid() {
        return sectionid;
    }

    public void setSectionid(Section sectionid) {
        this.sectionid = sectionid;
    }

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
