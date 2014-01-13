/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package libWebsiteTools.imead;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author alphavm
 */
@Entity
@Table(name = "keyvalue", schema = "IMEAD")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Keyvalue.findAll", query = "SELECT k FROM Keyvalue k"),
    @NamedQuery(name = "Keyvalue.findByKey", query = "SELECT k FROM Keyvalue k WHERE k.key = :key"),
    @NamedQuery(name = "Keyvalue.findByValue", query = "SELECT k FROM Keyvalue k WHERE k.value = :value")})
public class Keyvalue implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 1000)
    @Column(name = "key", nullable = false, length = 1000)
    private String key;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "value", nullable = false, length = 2147483647)
    private String value;

    public Keyvalue() {
    }

    public Keyvalue(String key) {
        this.key = key;
    }

    public Keyvalue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
        hash += (key != null ? key.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Keyvalue)) {
            return false;
        }
        Keyvalue other = (Keyvalue) object;
        if ((this.key == null && other.key != null) || (this.key != null && !this.key.equals(other.key))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "libWebsiteTools.imead.db.Keyvalue[ key=" + key + " ]";
    }
    
}
