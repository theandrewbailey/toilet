package libWebsiteTools;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author alpha
 */
public class JVMNotSupportedError extends VirtualMachineError {
    public static final String SHA256_UNSUPPORTED = "This JVM does not support SHA-256. Get a better JVM.";
    public static final String UTF8_UNSUPPORTED = "This JVM does not support UTF-8. Get a better JVM.";
    private final Throwable cause;

    /**
     * for UTF8 not supported
     * @param enc 
     */
    public JVMNotSupportedError(UnsupportedEncodingException enc) {
        super(UTF8_UNSUPPORTED);
        cause = enc;
    }

    /**
     * for SHA 256 not supported
     * @param enc 
     */
    public JVMNotSupportedError(NoSuchAlgorithmException enc) {
        super(SHA256_UNSUPPORTED);
        cause = enc;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
}
