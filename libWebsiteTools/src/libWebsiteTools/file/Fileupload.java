package libWebsiteTools.file;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 *
 * @author alpha
 */
@Entity
@Cacheable(true)
@Table(name = "fileupload", schema = "tools")
@NamedQueries({
    @NamedQuery(name = "Fileupload.findAll", query = "SELECT f FROM Fileupload f ORDER BY f.filename"),
    @NamedQuery(name = "Fileupload.count", query = "SELECT COUNT(f) FROM Fileupload f")})
public class Fileupload implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 1000)
    @Column(name = "filename", nullable = false, length = 1000)
    private String filename;
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
    @JoinColumn(name = "filename", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @OneToOne
    private Filemetadata filemetadata;

    public Fileupload() {
    }

    public Fileupload(String filename) {
        this.filename = filename;
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
        return filename.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Fileupload)) {
            return false;
        }
        Fileupload other = (Fileupload) object;
        return !((this.filename == null && other.filename != null) || (this.filename != null && !this.filename.equals(other.filename)));
    }

    @Override
    public String toString() {
        return "libOdyssey.db.Fileupload[ filename=" + filename + " ]";
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

    /**
     * @return the filesize
     */
    public Filemetadata getFilemetadata() {
        return filemetadata;
    }

    /**
     * @param filesize the filesize to set
     */
    public void setFilemetadata(Filemetadata filemetadata) {
        this.filemetadata = filemetadata;
    }

}
