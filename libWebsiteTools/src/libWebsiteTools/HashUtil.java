package libWebsiteTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author alphavm
 */
public class HashUtil {
    
    /**
     * @return SHA 256 MessageDigest
     */
    public static MessageDigest getSHA256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException x) {
            throw new JVMNotSupportedError(x);
        }
    }

    /**
     * @param toHash
     * @return base64 SHA 256 hash
     */
    public static String getHashAsBase64(byte[] toHash) {
        return getBase64(getSHA256().digest(toHash));
    }

    /**
     * @param toHash
     * @return base64 SHA 256 hash
     */
    public static String getHash(String toHash) {
        return getHashAsBase64(toHash.getBytes());
    }

    /**
     * from http://www.wikihow.com/Encode-a-String-to-Base64-With-Java
     *
     * @param stuff
     * @return stuff in Base64
     */
    public static String getBase64(byte[] stuff) {
        String base64code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
        StringBuilder encoded = new StringBuilder(stuff.length * 2);
        // determine how many padding bytes to add to the output
        int paddingCount = (3 - (stuff.length % 3)) % 3;
        // add any necessary padding to the input
        byte[] padded = new byte[stuff.length + paddingCount]; // initialized to zero by JVM
        System.arraycopy(stuff, 0, padded, 0, stuff.length);
        stuff = padded;
        // process 3 bytes at a time, churning out 4 output bytes
        for (int i = 0; i < stuff.length; i += 3) {
            int j = ((stuff[i] & 0xff) << 16)
                    + ((stuff[i + 1] & 0xff) << 8)
                    + (stuff[i + 2] & 0xff);
            encoded.append(base64code.charAt((j >> 18) & 0x3f))
                    .append(base64code.charAt((j >> 12) & 0x3f))
                    .append(base64code.charAt((j >> 6) & 0x3f))
                    .append(base64code.charAt(j & 0x3f));
        }
        // replace encoded padding nulls with "="
        return encoded.substring(0, encoded.length() - paddingCount) + "==".substring(0, paddingCount);
    }

    private static byte[] getByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1000];
        int read;
        while ((read = in.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        return baos.toByteArray();
    }
}
