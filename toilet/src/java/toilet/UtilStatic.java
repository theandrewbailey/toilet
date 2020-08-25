package toilet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.bean.ExceptionRepo;

public final class UtilStatic {

    private static final Logger LOG = Logger.getLogger(UtilStatic.class.getName());
    public static final Date EPOCH = new Date(0);
    public static final Pattern GENERAL_VALIDATION = Pattern.compile("^[\\x0A\\x0D\\x20-\\x7E\\u00A1-\\u052F]*$");

    public UtilStatic() {
        throw new UnsupportedOperationException("You cannot instantiate this class");
    }

    // try to NOT use this, it's not fast
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name, Class<T> type) {
        try {
            return (T) new InitialContext().lookup(name);
        } catch (NamingException n) {
            LOG.log(Level.SEVERE, "Attempted to look up invalid bean, name:" + name + " type:" + type.getName(), n);
            throw new RuntimeException(n);
        }
    }

    public static Collection<Future> finish(Collection<Future> these) {
        for (Future task : these) {
            try {
                task.get();
            } catch (InterruptedException | ExecutionException ex) {
                LOG.log(Level.SEVERE, "Tried to finish a bunch of jobs, but couldn't.", ex);
                getBean(ExceptionRepo.LOCAL_NAME, ExceptionRepo.class).add(null, "Multithread Exception", "Tried to finish a bunch of jobs, but couldn't.", ex);
                throw new RuntimeException(ex);
            }
        }
        return these;
    }

    /**
     * removes gratoutious amounts of spaces in the given string
     *
     * @param in input string
     * @return sans extra spaces
     */
    public static String removeSpaces(String in) {
        StringBuilder sb = new StringBuilder();
        for (String r : in.split(" ")) {
            if (!r.isEmpty()) {
                sb.append(r);
                sb.append(' ');
            }
        }
        return sb.toString().trim();
    }

    /**
     * removes validation breaking characters from the given string
     *
     * @param in input string
     * @param link keep "<" and ">", preserving embedded links
     * @param addPtags
     * @return formatted string
     */
    public static String htmlFormat(String in, boolean link, boolean addPtags) {
        StringBuilder sb = new StringBuilder(in.length() + 1000);
        if (addPtags) {
            sb.append("<p>");
        }
        in = removeSpaces(in);
        boolean inBrack = false;
        int index = 0;
        for (char c : in.toCharArray()) {
            switch (c) {
                case '<':
                    if (!link) {
                        sb.append("&lt;");
                    } else if (!inBrack) {
                        sb.append(c);
                        inBrack = true;
                    }
                    break;
                case '>':
                    if (!link) {
                        sb.append("&gt;");
                    } else if (inBrack) {
                        sb.append(c);
                        inBrack = false;
                    }
                    break;
                case '"':
                    if (!inBrack) {
                        sb.append("&quot;");
                    } else {
                        sb.append(c);
                    }
                    break;
                case '\'':
                    if (!inBrack) {
                        sb.append("&apos;");
                    } else {
                        sb.append(c);
                    }
                    break;
                case '&':
                    if (link) {
                        sb.append(c);
                    } else if (!inBrack) {
                        sb.append("&amp;");
                    } else {
                        sb.append(c);
                    }
                    break;
                case '\n':
                    if (!inBrack) {
                        sb.append("<br/>");
                    } else {
                        sb.append(c);
                    }
                    break;
                case '\r':
                    continue;
                default:
                    sb.append(c);
            }
            index++;
        }
        if (addPtags) {
            sb.append("</p>");
        }
        String out = sb.toString().replace("<br/><br/>", "</p>\n<p>");
        return out;
    }

    /**
     * reverses htmlFormat
     *
     * @param s input string
     * @param link
     * @return
     */
    public static String htmlUnformat(String s, boolean link) {
        String out = s.replace("</p>\n<p>", "\n\n").substring(3);
        out = out.substring(0, out.length() - 5);
        out = out.replace("</p><p>", "\n\n").replace("&quot;", "\"").replace("<br/>", "\n").replace("&", "&amp;");   // need to include amp, because browsers won't take it literally.
        out = link ? out : out.replace("&lt;", "<").replace("&gt;", ">");
        return out;
    }

    /**
     * tells the client to go to a new location. WHY is this not included in the
     * standard servlet API????
     *
     * @param res
     * @param newLocation
     */
    public static void permaMove(HttpServletResponse res, String newLocation) {
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader("Location", newLocation);
    }

}
