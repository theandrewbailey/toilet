package libWebsiteTools;

import at.gadermaier.argon2.Argon2Factory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * small utility to calculate scrypt hashes from plaintext.
 *
 * @author alpha
 */
public class CalculateArgon2App {

    private static final String[] PASSWORDS = new String[]{
        "this is just a test of the emergency hashing system",
        "if this was a real emergency, you would have been hacked already",
        "so use Argon2 and change the passwords in the toilet app"};
    private static final String SALT = "SOMEreallyLONGsaltTH1NGforP455WORD$";

    private static String hash(String password, String salt) {
        try {
            return Argon2Factory.create().setIterations(16).setMemoryInKiB(8192).setParallelism(2).hash(password.getBytes("UTF-8"), salt.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        }
    }

    public static void main(String[] args) {
        for (String p : PASSWORDS) {
            System.out.println(p + ": " + hash(p, SALT));
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("Enter text to hash: ");
                System.out.println(hash(in.readLine(), SALT));
            } catch (IOException io) {
                // FU java
                break;
            }
        }
    }
}
