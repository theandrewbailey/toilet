/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package libWebsiteTools.imead.db;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author alpha
 */
@Entity
@Table(name = "localization", schema = "imead")
@XmlRootElement
@Cacheable(false)
@NamedQueries({
    @NamedQuery(name = "Localization.findAll", query = "SELECT l FROM Localization l"),
    @NamedQuery(name = "Localization.findByKey", query = "SELECT l FROM Localization l WHERE l.localizationPK.key = :key"),
    @NamedQuery(name = "Localization.findByLocalecode", query = "SELECT l FROM Localization l WHERE l.localizationPK.localecode = :localecode"),
    @NamedQuery(name = "Localization.findByValue", query = "SELECT l FROM Localization l WHERE l.value = :value")})
public class Localization implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected LocalizationPK localizationPK;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 65000)
    @Column(name = "value", nullable = false, length = 65000)
    private String value;

    public Localization() {
    }

    public Localization(LocalizationPK localizationPK) {
        this.localizationPK = localizationPK;
    }

    public Localization(LocalizationPK localizationPK, String value) {
        this.localizationPK = localizationPK;
        this.value = value;
    }

    public Localization(String key, String localecode) {
        this.localizationPK = new LocalizationPK(key, localecode);
    }

    public LocalizationPK getLocalizationPK() {
        return localizationPK;
    }

    public void setLocalizationPK(LocalizationPK localizationPK) {
        this.localizationPK = localizationPK;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (localizationPK != null ? localizationPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Localization)) {
            return false;
        }
        Localization other = (Localization) object;
        if ((this.localizationPK == null && other.localizationPK != null) || (this.localizationPK != null && !this.localizationPK.equals(other.localizationPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "toilet.db.Localization[ localizationPK=" + localizationPK + " ]";
    }
    
}
