/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package libWebsiteTools.file;

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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author alphavm
 */
@Entity
@Table(name = "fileupload", schema = "files")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Fileupload.findAll", query = "SELECT f FROM Fileupload f"),
    @NamedQuery(name = "Fileupload.findByFileuploadid", query = "SELECT f FROM Fileupload f WHERE f.fileuploadid = :fileuploadid"),
    @NamedQuery(name = "Fileupload.findByAtime", query = "SELECT f FROM Fileupload f WHERE f.atime = :atime"),
    @NamedQuery(name = "Fileupload.findByEtag", query = "SELECT f FROM Fileupload f WHERE f.etag = :etag"),
    @NamedQuery(name = "Fileupload.findByFilename", query = "SELECT f FROM Fileupload f WHERE f.filename = :filename"),
    @NamedQuery(name = "Fileupload.findByMimetype", query = "SELECT f FROM Fileupload f WHERE f.mimetype = :mimetype"),
    @NamedQuery(name = "Fileupload.findByUrl", query = "SELECT f FROM Fileupload f WHERE f.url = :url")})
public class Fileupload implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "fileuploadid", nullable = false)
    private Integer fileuploadid;
    @Basic(optional = false)
    @NotNull
    @Column(name = "atime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date atime;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 250)
    @Column(name = "etag", nullable = false, length = 250)
    private String etag;
    @Basic(optional = false, fetch = FetchType.LAZY)
    @NotNull
    @Lob
    @Column(name = "filedata", nullable = false)
    private byte[] filedata;
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
    @Size(max = 65000)
    @Column(name = "url", length = 65000)
    private String url;

    public Fileupload() {
    }

    public Fileupload(Integer fileuploadid) {
        this.fileuploadid = fileuploadid;
    }

    public Fileupload(Integer fileuploadid, Date atime, String etag, byte[] filedata, String filename, String mimetype) {
        this.fileuploadid = fileuploadid;
        this.atime = atime;
        this.etag = etag;
        this.filedata = filedata;
        this.filename = filename;
        this.mimetype = mimetype;
    }

    public Integer getFileuploadid() {
        return fileuploadid;
    }

    public void setFileuploadid(Integer fileuploadid) {
        this.fileuploadid = fileuploadid;
    }

    public Date getAtime() {
        return atime;
    }

    public void setAtime(Date atime) {
        this.atime = atime;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public byte[] getFiledata() {
        return filedata;
    }

    public void setFiledata(byte[] filedata) {
        this.filedata = filedata;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
        return "libWebsiteTools.file.Fileupload[ fileuploadid=" + fileuploadid + " ]";
    }
    
}
