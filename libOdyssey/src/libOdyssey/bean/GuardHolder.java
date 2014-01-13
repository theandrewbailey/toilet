package libOdyssey.bean;

import java.util.ArrayList;
import java.util.List;
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

    @EJB
    private IMEADHolder imead;
    public static final String HOST = "libOdyssey_guard_host";
    private static final String GUARD_ENABLE = "libOdyssey_guard_enable";
    private static final String HANDLE_ERRORS = "libOdyssey_guard_errors";
    private static final String DENY_USER_AGENTS = "libOdyssey_guard_denyUserAgents";
    private static final String SESSIONS_PER_SECOND = "libOdyssey_guard_sessionsPerSecond";
    private static final String EMPTY_SESSIONS = "libOdyssey_guard_emptySessions";
    private static final String HONEYPOTS = "libOdyssey_guard_honeypots";
    private int[] sps;
    private int[] es;
    private String hostValue;
    private List<Pattern> denyUAs;
    private List<Pattern> honeyList;
    private boolean enableGuard;
    private boolean handleErrors;

    @PostConstruct
    public void refresh() {
        List<Pattern> tempHoney = new ArrayList<>();
        for (String h : imead.getValue(HONEYPOTS).split("\n")) {
            tempHoney.add(Pattern.compile(h));
        }
        honeyList = tempHoney;
        List<Pattern> tempUAs = new ArrayList<>();
        for (String ua : imead.getValue(DENY_USER_AGENTS).split("\n")) {
            tempUAs.add(Pattern.compile(ua));
        }
        denyUAs = tempUAs;
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
        hostValue = imead.getValue(HOST);
        enableGuard = Boolean.valueOf(imead.getValue(GUARD_ENABLE));
        handleErrors = Boolean.valueOf(imead.getValue(HANDLE_ERRORS));
    }

    public int[] getSps() {
        return sps;
    }

    public int[] getEs() {
        return es;
    }

    public String getHostValue() {
        return hostValue;
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
}
