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

/**
 *
 * @author alpha
 */
@Entity
@Table(name = "fileupload", schema = "tools", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"filename"})})
@NamedQueries({
    @NamedQuery(name = "Fileupload.findAll", query = "SELECT f FROM Fileupload f ORDER BY f.filename"),
    @NamedQuery(name = "Fileupload.findByFilename", query = "SELECT f FROM Fileupload f WHERE f.filename = :filename"),
    @NamedQuery(name = "Fileupload.getMetadata", query = "SELECT f.fileuploadid, f.atime, f.etag, f.mimetype, f.url FROM Fileupload f WHERE f.filename = :filename")})
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
    @Basic(optional = false)
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
    @Size(min = 1, max = 100)
    @Column(name = "mimetype", nullable = false, length = 100)
    private String mimetype;
    @Size(max = 65000)
    @Column(name = "url", length = 65000)
    private String url;
    @Basic(optional = false)
    @Lob
    @Column(name = "gzipdata")
    private byte[] gzipdata;
    @Basic(optional = false)
    @Lob
    @Column(name = "brdata")
    private byte[] brdata;

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
        hash += (fileuploadid != null ? fileuploadid.hashCode() : filename != null ? filename.hashCode() : 0);
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
        return !((this.filename == null && other.filename != null) || (this.filename != null && !this.filename.equals(other.filename)));
    }

    @Override
    public String toString() {
        return "libOdyssey.db.Fileupload[ fileuploadid=" + fileuploadid + " ]";
    }

    /**
     * @return the gzipdata
     */
    public byte[] getGzipdata() {
        return gzipdata;
    }

    /**
     * @param gzipdata the gzipdata to set
     */
    public void setGzipdata(byte[] gzipdata) {
        this.gzipdata = gzipdata;
    }

    /**
     * @return the brdata
     */
    public byte[] getBrdata() {
        return brdata;
    }

    /**
     * @param brdata the brdata to set
     */
    public void setBrdata(byte[] brdata) {
        this.brdata = brdata;
    }

}
