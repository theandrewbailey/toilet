package toilet.db;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "comment", catalog = "toilet", schema = "toilet")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Comment.findAll", query = "SELECT c FROM Comment c ORDER BY c.posted"),
    @NamedQuery(name = "Comment.findByCommentid", query = "SELECT c FROM Comment c WHERE c.commentid = :commentid"),
    @NamedQuery(name = "Comment.findByPosted", query = "SELECT c FROM Comment c WHERE c.posted = :posted"),
    @NamedQuery(name = "Comment.findByPostedtext", query = "SELECT c FROM Comment c WHERE c.postedtext = :postedtext"),
    @NamedQuery(name = "Comment.findByPostedname", query = "SELECT c FROM Comment c WHERE c.postedname = :postedname"),
    @NamedQuery(name = "Comment.findByIsspam", query = "SELECT c FROM Comment c WHERE c.isspam = :isspam"),
    @NamedQuery(name = "Comment.findByIsapproved", query = "SELECT c FROM Comment c WHERE c.isapproved = :isapproved")})
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "commentid", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentid;
    @Basic(optional = false)
    @NotNull
    @Column(name = "posted", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date posted;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 65000)
    @Column(name = "postedtext", nullable = false, length = 65000)
    private String postedtext;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "postedname", nullable = false, length = 250)
    private String postedname;
//    @JoinColumn(name = "httpsessionid", referencedColumnName = "httpsessionid")
//    @ManyToOne
//    private Httpsession2 httpsessionid;
    @JoinColumn(name = "articleid", referencedColumnName = "articleid")
    @ManyToOne(optional = false)
    private Article articleid;
    @Column(name = "isspam")
    private Boolean isspam;
    @Column(name = "isapproved")
    private Boolean isapproved;

    public Comment() {
    }

    public Comment(Integer commentid) {
        this.commentid = commentid;
    }

    public Comment(Integer commentid, Date posted, String postedtext, String postedname) {
        this.commentid = commentid;
        this.posted = posted;
        this.postedtext = postedtext;
        this.postedname = postedname;
    }

    public Integer getCommentid() {
        return commentid;
    }

    public void setCommentid(Integer commentid) {
        this.commentid = commentid;
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

    public String getPostedname() {
        return postedname;
    }

    public void setPostedname(String postedname) {
        this.postedname = postedname;
    }

//    public Httpsession2 getHttpsessionid() {
//        return httpsessionid;
//    }
//
//    public void setHttpsessionid(Httpsession2 httpsessionid) {
//        this.httpsessionid = httpsessionid;
//    }

    public Article getArticleid() {
        return articleid;
    }

    public void setArticleid(Article articleid) {
        this.articleid = articleid;
    }

    public Boolean getIsspam() {
        return isspam;
    }

    public void setIsspam(Boolean isspam) {
        this.isspam = isspam;
    }

    public Boolean getIsapproved() {
        return isapproved;
    }

    public void setIsapproved(Boolean isapproved) {
        this.isapproved = isapproved;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (commentid != null ? commentid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Comment)) {
            return false;
        }
        Comment other = (Comment) object;
        if ((this.commentid == null && other.commentid != null) || (this.commentid != null && !this.commentid.equals(other.commentid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "toilet.db.Comment[ commentid=" + commentid + " ]";
    }
    
}
