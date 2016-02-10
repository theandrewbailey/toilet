package libWebsiteTools.token;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

/**
 * generates and stores security tokens
 * @author alpha
 */
public class RequestTokenBucket implements Serializable {

    public static final String SESSION_ATTR_NAME = "$_REQUEST_TOKEN_BUCKET";
    private static final int MAXIMUM_TOKEN_COUNT = 300;
    private final LinkedHashMap<String, String> UUIDs = new LinkedHashMap<>();
    private int max = MAXIMUM_TOKEN_COUNT;

    public static RequestTokenBucket getRequestTokenBucket(HttpServletRequest req) {
        RequestTokenBucket bucket = (RequestTokenBucket) req.getSession().getAttribute(SESSION_ATTR_NAME);
        if (bucket == null) {
            bucket = new RequestTokenBucket();
            req.getSession().setAttribute(SESSION_ATTR_NAME, bucket);
        }
        return bucket;
    }

    public RequestTokenBucket() {
    }

    public RequestTokenBucket(int maximum) {
        this.max = maximum;
    }

    /**
     * generate a token with no particular referrer.
     * @return the security token generated
     */
    public String generateToken() {
        return generateToken(null);
    }

    /**
     * generate a token with a specific referrer. can be any string, but it can be validated with the token later, like checking the 'referer' header of the returning request.
     * @param referrer
     * @return the security token generated
     */
    public synchronized String generateToken(String referrer) {
        UUID val = UUID.randomUUID();
        String token = val.toString();
        addToken(token, referrer);
        return token;
    }

    /**
     * adds the token to the store, along with the referrer.
     * @param token
     * @param referrer 
     */
    public synchronized void addToken(String token, String referrer) {
        UUIDs.put(token, referrer);
        if (UUIDs.size() > max) {
            Iterator<Entry<String, String>> iter = UUIDs.entrySet().iterator();
            UUIDs.remove(iter.next().getKey());
        }
    }

    /**
     * has the token been generated for this session?
     * @param token
     * @return true if present, false if not
     */
    public synchronized boolean checkToken(String token) {
        return UUIDs.containsKey(token);
    }

    /**
     * has this token been generated with the given referrer for this session?
     * @param token
     * @param referrer
     * @return true if present, false if not
     */
    public synchronized boolean checkToken(String token, String referrer) {
        return UUIDs.containsKey(token) ? referrer.equals(UUIDs.get(token)) : false;
    }

    /**
     * has the token been generated for this session? if so, remove it from the set.
     * @param token
     * @return true if present, false if not
     */
    public synchronized boolean claimToken(String token) {
        if (checkToken(token)) {
            UUIDs.remove(token);
            return true;
        }
        return false;
    }

    /**
     * has this token been generated with the given referrer for this session? if so, remove it from the set.
     * @param token
     * @param referrer
     * @return true if present, false if not
     */
    public synchronized boolean claimToken(String token, String referrer) {
        if (checkToken(token, referrer)) {
            UUIDs.remove(token);
            return true;
        }
        return false;
    }
}
