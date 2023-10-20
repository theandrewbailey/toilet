package toilet.db;

import java.io.Serializable;
import java.time.OffsetDateTime;
import jakarta.persistence.Basic;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Cacheable(true)
@Table(name = "comment", schema = "toilet")
@NamedQueries({
    @NamedQuery(name = "Comment.findAll", query = "SELECT c FROM Comment c ORDER BY c.posted DESC"),
    @NamedQuery(name = "Comment.count", query = "SELECT COUNT(c) FROM Comment c")})
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "commentid", nullable = false)
    private Integer commentid;
    @Basic(optional = false)
    @NotNull
    @Column(name = "posted", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime posted;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10000000)
    @Column(name = "postedhtml", nullable = false, length = 10000000)
    private String postedhtml;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "postedname", nullable = false, length = 250)
    private String postedname;
    @Column(name = "isapproved")
    private Boolean isapproved;
    @Column(name = "isspam")
    private Boolean isspam;
    @Size(max = 10000000)
    @Column(name = "postedmarkdown", length = 10000000)
    private String postedmarkdown;
    @JoinColumn(name = "articleid", referencedColumnName = "articleid", nullable = false)
    @ManyToOne(optional = false)
    private Article articleid;

    public Comment() {
    }

    public Comment(Integer commentid) {
        this.commentid = commentid;
    }

    public Comment(Integer commentid, OffsetDateTime posted, String postedhtml, String postedname) {
        this.commentid = commentid;
        this.posted = posted;
        this.postedhtml = postedhtml;
        this.postedname = postedname;
    }

    public Integer getCommentid() {
        return commentid;
    }

    public void setCommentid(Integer commentid) {
        this.commentid = commentid;
    }

    public OffsetDateTime getPosted() {
        return posted;
    }

    public void setPosted(OffsetDateTime posted) {
        this.posted = posted;
    }

    public String getPostedhtml() {
        return postedhtml;
    }

    public void setPostedhtml(String postedhtml) {
        this.postedhtml = postedhtml;
    }

    public String getPostedname() {
        return postedname;
    }

    public void setPostedname(String postedname) {
        this.postedname = postedname;
    }

    public Boolean getIsapproved() {
        return isapproved;
    }

    public void setIsapproved(Boolean isapproved) {
        this.isapproved = isapproved;
    }

    public Boolean getIsspam() {
        return isspam;
    }

    public void setIsspam(Boolean isspam) {
        this.isspam = isspam;
    }

    public String getPostedmarkdown() {
        return postedmarkdown;
    }

    public void setPostedmarkdown(String postedmarkdown) {
        this.postedmarkdown = postedmarkdown;
    }

    public Article getArticleid() {
        return articleid;
    }

    public void setArticleid(Article articleid) {
        this.articleid = articleid;
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
        return !((this.commentid == null && other.commentid != null) || (this.commentid != null && !this.commentid.equals(other.commentid)));
    }

    @Override
    public String toString() {
        return "toilet.db.Comment[ commentid=" + commentid + " ]";
    }

}