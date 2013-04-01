/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package libWebsiteTools.imead.db;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author alpha
 */
@Embeddable
public class LocalizationPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 1000)
    @Column(name = "key", nullable = false, length = 1000)
    private String key;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "localecode", nullable = false, length = 100)
    private String localecode;

    public LocalizationPK() {
    }

    public LocalizationPK(String key, String localecode) {
        this.key = key;
        this.localecode = localecode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLocalecode() {
        return localecode;
    }

    public void setLocalecode(String localecode) {
        this.localecode = localecode;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (key != null ? key.hashCode() : 0);
        hash += (localecode != null ? localecode.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof LocalizationPK)) {
            return false;
        }
        LocalizationPK other = (LocalizationPK) object;
        if ((this.key == null && other.key != null) || (this.key != null && !this.key.equals(other.key))) {
            return false;
        }
        if ((this.localecode == null && other.localecode != null) || (this.localecode != null && !this.localecode.equals(other.localecode))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "toilet.db.LocalizationPK[ key=" + key + ", localecode=" + localecode + " ]";
    }
    
}
