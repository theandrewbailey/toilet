package libWebsiteTools.security;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author alpha
 * @param <k>
 */
public class CertPath<k extends Certificate> extends java.security.cert.CertPath {

    private final List<k> certificates;

    public CertPath(List<k> certificates) {
        super(certificates.get(0).getType());
        this.certificates = certificates;
    }

    /**
     * 
     * @return earliest expiration date of all certificates in chain. Only works for X509 Certificates.
     */
    public Date getExpiration() {
        Date earliest = null;
        for (Certificate c : certificates) {
            if (c instanceof X509Certificate && (null == earliest || ((X509Certificate) c).getNotAfter().before(earliest))) {
                earliest = ((X509Certificate) c).getNotAfter();
            }
        }
        return earliest;
    }

    @Override
    public Iterator<String> getEncodings() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public byte[] getEncoded(String string) throws CertificateEncodingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<k> getCertificates() {
        return certificates;
    }

}
