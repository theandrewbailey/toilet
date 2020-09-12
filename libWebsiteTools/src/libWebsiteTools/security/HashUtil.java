package libWebsiteTools.security;

import at.gadermaier.argon2.Argon2;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import libWebsiteTools.JVMNotSupportedError;

/**
 *
 * @author alpha
 */
public abstract class HashUtil {

    public static final Pattern ARGON2_ENCODING_PATTERN = Pattern.compile("^\\$(?<type>\\w*?)\\$v=(?<v>\\d*?)\\$m=(?<m>\\d*?),t=(?<t>\\d*?),p=(?<p>\\d*?)\\$(?<salt>[A-Za-z0-9\\+\\/\\=]*?)\\$(?<hash>[A-Za-z0-9\\+\\/\\=]*?)$");
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
     * @param toHash to hash with SHA-256
     * @return base64 SHA-256 hash
     */
    public static String getSHA256Hash(byte[] toHash) {
        try {
            return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(toHash));
        } catch (NoSuchAlgorithmException x) {
            throw new JVMNotSupportedError(x);
        }
    }

    /**
     * @param toHash to hash with SHA-256
     * @return base64 SHA-256 hash
     */
    public static String getSHA256Hash(String toHash) {
        try {
            return getSHA256Hash(toHash.getBytes("UTF-8"));
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
    public static String getHmacSHA256Hash(String key, String data) {
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

    /**
     * Returns an encoded argon2 hash. Uses defaults.
     *
     * @param password password (presumably) to be hashed
     * @return argon2 hash
     */
    public static String getArgon2Hash(String password) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            return Argon2.create().hash(password.getBytes("UTF-8"), salt).asEncoded();
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        }
    }

    /**
     * Verifies that the provided password created the provided encoded hash.
     *
     * @param encoded an argon2 encoded hash
     * @param password password to test
     * @return did it blend?
     * @see HashUtil.getArgon2Hash
     */
    public static boolean verifyArgon2Hash(String encoded, String password) {
        return Argon2.checkHash(encoded, password);
    }
}
