package libWebsiteTools;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author alpha
 */
public class HashUtil {

    /**
     *
     * @param bytes to convert to hexadecimal
     * @return string of hexadecimal
     */
    public static String getHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * @return SHA-256 MessageDigest
     */
    public static MessageDigest getSHA256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException x) {
            throw new JVMNotSupportedError(x);
        }
    }

    /**
     *
     * @param toHash to hash with SHA-256
     * @return SHA-256 hash
     */
    public static byte[] getSHA256(byte[] toHash) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(toHash);
        } catch (NoSuchAlgorithmException x) {
            throw new JVMNotSupportedError(x);
        }
    }

    /**
     * @param toHash to hash with SHA-256
     * @return base64 SHA-256 hash
     */
    public static String getSHA256HashAsBase64(byte[] toHash) {
        return Base64.getEncoder().encodeToString(getSHA256(toHash));
    }

    /**
     * @param toHash to hash with SHA-256
     * @return base64 SHA-256 hash
     */
    public static String getSHA256Hash(String toHash) {
        try {
            return getSHA256HashAsBase64(toHash.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException enc) {
            throw new JVMNotSupportedError(enc);
        }
    }

    /**
     *
     * @param key key for HMAC-SHA-256
     * @param data data to HMAC-SHA-256
     * @return string of base64 HMAC-SHA-256
     */
    public static String getHmacSHA256(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(hmac.doFinal(data.getBytes("UTF-8")));
        } catch (NoSuchAlgorithmException ex) {
            throw new JVMNotSupportedError(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        } catch (InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
    }
}
