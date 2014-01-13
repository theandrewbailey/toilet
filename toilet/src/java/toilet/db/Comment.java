/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

/**
 *
 * @author alphavm
 */
@Entity
@Table(name = "comment", catalog = "toilet", schema = "toilet")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Comment.findAll", query = "SELECT c FROM Comment c"),
    @NamedQuery(name = "Comment.findByCommentid", query = "SELECT c FROM Comment c WHERE c.commentid = :commentid"),
    @NamedQuery(name = "Comment.findByPosted", query = "SELECT c FROM Comment c WHERE c.posted = :posted"),
    @NamedQuery(name = "Comment.findByPostedhtml", query = "SELECT c FROM Comment c WHERE c.postedhtml = :postedhtml"),
    @NamedQuery(name = "Comment.findByPostedname", query = "SELECT c FROM Comment c WHERE c.postedname = :postedname"),
    @NamedQuery(name = "Comment.findByIsapproved", query = "SELECT c FROM Comment c WHERE c.isapproved = :isapproved"),
    @NamedQuery(name = "Comment.findByIsspam", query = "SELECT c FROM Comment c WHERE c.isspam = :isspam"),
    @NamedQuery(name = "Comment.findByPostedmarkdown", query = "SELECT c FROM Comment c WHERE c.postedmarkdown = :postedmarkdown")})
public class Comment implements Serializable {
    @JoinColumn(name = "articleid", referencedColumnName = "articleid", nullable = false)
    @ManyToOne(optional = false)
    private Article articleid;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "commentid", nullable = false)
    private Integer commentid;
    @Basic(optional = false)
    @NotNull
    @Column(name = "posted", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date posted;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10485760)
    @Column(name = "postedhtml", nullable = false, length = 10485760)
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
    @Size(max = 10485760)
    @Column(name = "postedmarkdown", length = 10485760)
    private String postedmarkdown;

    public Comment() {
    }

    public Comment(Integer commentid) {
        this.commentid = commentid;
    }

    public Comment(Integer commentid, Date posted, String postedhtml, String postedname) {
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

    public Article getArticleid() {
        return articleid;
    }

    public void setArticleid(Article articleid) {
        this.articleid = articleid;
    }
    
}
