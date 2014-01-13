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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author alphavm
 */
@Entity
@Table(name = "fileupload", catalog = "toilet", schema = "toilet", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"filename"})})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Fileupload.findAll", query = "SELECT f FROM Fileupload f"),
    @NamedQuery(name = "Fileupload.findByFileuploadid", query = "SELECT f FROM Fileupload f WHERE f.fileuploadid = :fileuploadid"),
    @NamedQuery(name = "Fileupload.findByEtag", query = "SELECT f FROM Fileupload f WHERE f.etag = :etag"),
    @NamedQuery(name = "Fileupload.findByFilename", query = "SELECT f FROM Fileupload f WHERE f.filename = :filename"),
    @NamedQuery(name = "Fileupload.findByMimetype", query = "SELECT f FROM Fileupload f WHERE f.mimetype = :mimetype"),
    @NamedQuery(name = "Fileupload.findByUploaded", query = "SELECT f FROM Fileupload f WHERE f.uploaded = :uploaded")})
public class Fileupload implements Serializable {
    @Basic(optional = false, fetch = FetchType.LAZY)
    @NotNull
    @Lob
    @Column(name = "binarydata", nullable = false)
    private byte[] binarydata;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "fileuploadid", nullable = false)
    private Integer fileuploadid;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "etag", nullable = false, length = 250)
    private String etag;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 1000)
    @Column(name = "filename", nullable = false, length = 1000)
    private String filename;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "mimetype", nullable = false, length = 250)
    private String mimetype;
    @Basic(optional = false)
    @NotNull
    @Column(name = "uploaded", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date uploaded;

    public Fileupload() {
    }

    public Fileupload(Integer fileuploadid) {
        this.fileuploadid = fileuploadid;
    }

    public Fileupload(Integer fileuploadid, byte[] binarydata, String etag, String filename, String mimetype, Date uploaded) {
        this.fileuploadid = fileuploadid;
        this.binarydata = binarydata;
        this.etag = etag;
        this.filename = filename;
        this.mimetype = mimetype;
        this.uploaded = uploaded;
    }

    public Integer getFileuploadid() {
        return fileuploadid;
    }

    public void setFileuploadid(Integer fileuploadid) {
        this.fileuploadid = fileuploadid;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public Date getUploaded() {
        return uploaded;
    }

    public void setUploaded(Date uploaded) {
        this.uploaded = uploaded;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (fileuploadid != null ? fileuploadid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Fileupload)) {
            return false;
        }
        Fileupload other = (Fileupload) object;
        if ((this.fileuploadid == null && other.fileuploadid != null) || (this.fileuploadid != null && !this.fileuploadid.equals(other.fileuploadid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "toilet.db.Fileupload[ fileuploadid=" + fileuploadid + " ]";
    }

    public byte[] getBinarydata() {
        return binarydata;
    }

    public void setBinarydata(byte[] binarydata) {
        this.binarydata = binarydata;
    }
    
}
