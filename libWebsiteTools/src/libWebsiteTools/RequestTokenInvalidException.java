package libWebsiteTools;

/**
 * A RuntimeException used to indicate that a request token check has failed.
 * @author alpha
 */
public class RequestTokenInvalidException extends RuntimeException {

    public static final String MESSAGE = "This request's token is invalid, a token was not found, or came from an unexpected referrer.";

    public RequestTokenInvalidException() {
        super(MESSAGE);
    }
}
