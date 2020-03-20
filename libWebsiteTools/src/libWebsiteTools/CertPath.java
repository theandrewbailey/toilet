package libWebsiteTools;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author alpha
 * @param <k>
 */
public class CertPath<k extends Certificate> extends java.security.cert.CertPath {

    private static final Logger LOG = Logger.getLogger(CertPath.class.getName());

    private final List<k> certificates;

    public CertPath(List<k> certificates) {
        super(certificates.get(0).getType());
        this.certificates = certificates;
        /*LOG.log(Level.INFO, "Creating CertPath with: ");
        for (Certificate c : this.certificates) {
            if (c instanceof X509Certificate) {
                LOG.log(Level.INFO, ((X509Certificate) c).getSubjectX500Principal().toString());
            }
        }*/
    }

    @Override
    public Iterator<String> getEncodings() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getEncoded(String string) throws CertificateEncodingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<k> getCertificates() {
        return certificates;
    }

}
