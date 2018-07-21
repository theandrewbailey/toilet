package libOdyssey.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import libWebsiteTools.imead.IMEADHolder;

/**
 *
 * @author alpha
 */
@Singleton
public class GuardHolder {

    // group 1 is the origin (with http), group 2 is domain and port specifically
    public static final Pattern ORIGIN_PATTERN = Pattern.compile("^((https?://)([^/]+?))/.*?$");
    public static final String CANONICAL_URL = "libOdyssey_guard_canonicalURL";
    @EJB
    private IMEADHolder imead;
    private static final String DOMAINS = "libOdyssey_guard_domains";
    private static final String GUARD_ENABLE = "libOdyssey_guard_enable";
    private static final String HANDLE_ERRORS = "libOdyssey_guard_errors";
    private static final String DENY_USER_AGENTS = "libOdyssey_guard_denyUserAgents";
    private static final String SESSIONS_PER_SECOND = "libOdyssey_guard_sessionsPerSecond";
    private static final String EMPTY_SESSIONS = "libOdyssey_guard_emptySessions";
    private static final String HONEYPOTS = "libOdyssey_guard_honeypots";
    private static final String ACCEPTABLE_CONTENT_DOMAINS = "site_acceptableContentDomains";
    private int[] sps;
    private int[] es;
    private List<Pattern> domains;
    private List<Pattern> denyUAs;
    private List<Pattern> honeyList;
    private List<Pattern> acceptableDomains;
    private boolean enableGuard;
    private boolean handleErrors;
    private String canonicalOrigin;
    private String canonicalDomain;

    public static boolean matchesAny(CharSequence subject, List<Pattern> regexes) {
        for (Pattern p : regexes) {
            if (p.matcher(subject).matches()) {
                return true;
            }
        }
        return false;
    }

    @PostConstruct
    public void refresh() {
        honeyList = getPatterns(HONEYPOTS);
        denyUAs = getPatterns(DENY_USER_AGENTS);
        domains = getPatterns(DOMAINS);
        acceptableDomains = getPatterns(ACCEPTABLE_CONTENT_DOMAINS);
        String[] Strsps = imead.getValue(SESSIONS_PER_SECOND).split("x");
        int[] tempSps = new int[Strsps.length];
        for (int x = 0; x < Strsps.length; x++) {
            tempSps[x] = Integer.valueOf(Strsps[x]);
        }
        sps = tempSps;
        String[] Stres = imead.getValue(EMPTY_SESSIONS).split("x");
        int[] tempEs = new int[Stres.length];
        for (int x = 0; x < Stres.length; x++) {
            tempEs[x] = Integer.valueOf(Stres[x]);
        }
        es = tempEs;
        enableGuard = Boolean.valueOf(imead.getValue(GUARD_ENABLE));
        handleErrors = Boolean.valueOf(imead.getValue(HANDLE_ERRORS));
        Matcher canonicalMatcher = GuardHolder.ORIGIN_PATTERN.matcher(imead.getValue(CANONICAL_URL));
        canonicalMatcher.matches();
        canonicalOrigin = canonicalMatcher.group(1);
        canonicalDomain = canonicalMatcher.group(3);
    }

    private List<Pattern> getPatterns(String key) {
        List<Pattern> temps = new ArrayList<>();
        for (String line : imead.getValue(key).split("\n")) {
            temps.add(Pattern.compile(line));
        }
        return Collections.unmodifiableList(temps);
    }

    /**
     *
     * @return
     * GuardHolder.ORIGIN_PATTERN.matcher(imead.getValue(CANONICAL_URL)).group(1)
     */
    public String getCanonicalOrigin() {
        return canonicalOrigin;
    }

    /**
     *
     * @return
     * GuardHolder.ORIGIN_PATTERN.matcher(imead.getValue(CANONICAL_URL)).group(3)
     */
    public String getCanonicalDomain() {
        return canonicalDomain;
    }

    public int[] getSps() {
        return sps;
    }

    public int[] getEs() {
        return es;
    }

    public List<Pattern> getDomains() {
        return domains;
    }

    public List<Pattern> getDenyUAs() {
        return denyUAs;
    }

    public List<Pattern> getHoneyList() {
        return honeyList;
    }

    public boolean isEnableGuard() {
        return enableGuard;
    }

    public boolean isHandleErrors() {
        return handleErrors;
    }

    /**
     * @return the acceptableDomains to send content to (like google and feedly)
     */
    public List<Pattern> getAcceptableDomains() {
        return acceptableDomains;
    }
}
