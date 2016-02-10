package libWebsiteTools.imead;

/**
 *
 * @author alpha
 */
public class LocalizedStringNotFoundException extends RuntimeException {

    public LocalizedStringNotFoundException(String key, String locale) {
        super("No key available for " + key + " in " + locale);
    }

    public LocalizedStringNotFoundException(String key, String locale, Throwable cause) {
        super("No key available for " + key + " in " + locale, cause);
    }
}
