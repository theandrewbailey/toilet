package libWebsiteTools;

import com.lambdaworks.crypto.SCryptUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * small utility to calculate scrypt hashes from plaintext.
 * 
 * @author alpha
 */
public class CalculateScryptApp {

    private static String[] PASSWORDS = new String[]{
        "this is just a test of the emergency hashing system",
        "if this was a real emergency, you would have been hacked already",
        "so use scrypt and change the passwords in the toilet app"};

    private static String hash(String s) {
        return SCryptUtil.scrypt(s, 1 << 12, 8, 1);
    }

    public static void main(String[] args) {
        for (String p : PASSWORDS) {
            System.out.println(p + ": " +hash(p));
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("Enter text to hash: ");
                System.out.println(hash(in.readLine()));
            } catch (IOException io) {
                // FU java
                break;
            }
        }
    }
}
