package libWebsiteTools.tag;

import javax.servlet.http.HttpServletRequest;
import libWebsiteTools.HashUtil;
import libWebsiteTools.token.RequestTokenBucket;
import libWebsiteTools.token.RequestTokenChecker;

/**
 *
 * @author alpha
 */
public class RequestToken extends Hidden {

    public static final String ID_NAME = "$_REQUEST_TOKEN";
    public static final String DISABLE_REFERRER_CHECK = "$_DISABLE_REFERRER_CHECK";
    private String reqid;
    private boolean disableReferrerCheck = false;

    /**
     * returns the name of the request token parameter that the token should be under.
     * @param req
     * @return 
     */
    public static String getHash(HttpServletRequest req) {
        return HashUtil.getHash(req.getSession().getId() + ID_NAME);
    }

    @Override
    public String generateTag() {
        RequestTokenBucket bucket = RequestTokenBucket.getRequestTokenBucket(req);
        String url = req.getAttribute(RequestTokenChecker.ORIGINAL_REQUEST_URL).toString();
        setValue(bucket.generateToken(url));
        req.setAttribute(ID_NAME, getValue());
        reqid = getHash(req);
        return super.generateTag();
    }

    public boolean isDisableReferrerCheck() {
        return disableReferrerCheck;
    }

    public void setDisableReferrerCheck(boolean disableReferrerCheck) {
        this.disableReferrerCheck = disableReferrerCheck;
    }

    @Override
    public String getId() {
        return reqid;
    }
}
