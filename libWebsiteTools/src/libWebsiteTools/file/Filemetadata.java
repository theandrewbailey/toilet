package libWebsiteTools.file;

import java.io.Serializable;
import java.time.OffsetDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

/**
 *
 * @author alpha
 */
@Entity
@Table(name = "Filemetadata", schema = "tools")
@NamedQueries({
    @NamedQuery(name = "Filemetadata.findAll", query = "SELECT f FROM Filemetadata f ORDER BY f.filename"),
    @NamedQuery(name = "Filemetadata.findByFilenames", query = "SELECT f FROM Filemetadata f WHERE f.filename in :filenames ORDER BY f.filename")})
public class Filemetadata implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Size(max = 1000)
    @Column(name = "filename", length = 1000)
    private String filename;
    @Column(name = "datasize")
    private Integer datasize;
    @Column(name = "gzipsize")
    private Integer gzipsize;
    @Column(name = "brsize")
    private Integer brsize;
    @Column(name = "atime", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private OffsetDateTime atime;
    @Size(min = 1, max = 250)
    @Column(name = "etag", nullable = false, length = 250)
    private String etag;
    @Size(min = 1, max = 100)
    @Column(name = "mimetype", nullable = false, length = 100)
    private String mimetype;
    @Size(max = 65000)
    @Column(name = "url", length = 65000)
    private String url;

    public Filemetadata() {
    }

    public Filemetadata(String filename, OffsetDateTime atime) {
        this.filename = filename;
        this.atime = atime;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getDatasize() {
        return datasize;
    }

    public void setDatasize(Integer datasize) {
        this.datasize = datasize;
    }

    public Integer getGzipsize() {
        return gzipsize;
    }

    public void setGzipsize(Integer gzipsize) {
        this.gzipsize = gzipsize;
    }

    public Integer getBrsize() {
        return brsize;
    }

    public void setBrsize(Integer brsize) {
        this.brsize = brsize;
    }

    public OffsetDateTime getAtime() {
        return atime;
    }

    public void setAtime(OffsetDateTime atime) {
        this.atime = atime;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
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
}
