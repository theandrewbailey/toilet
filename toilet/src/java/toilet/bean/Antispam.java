package toilet.bean;

import java.util.regex.Pattern;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import libWebsiteTools.imead.IMEADHolder;

/**
 *
 * @author alpha
 */
//@Stateless
public class Antispam {

    @EJB
    private IMEADHolder imead;
    private static final String SPAMWORDS = "entry_spamwords";

    public boolean isSpam(String commentText) {
        commentText = commentText.toLowerCase();
        for (String words : imead.getValue(SPAMWORDS).split("\n")) {
            if (Pattern.matches(words, commentText)) {
                return true;
            }
        }
        return false;
    }
}
