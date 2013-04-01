package toilet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.util.Base64;

public final class UtilStatic {

    private static final Logger logger = Logger.getLogger(UtilStatic.class.getName());
    public static final String SHA256_UNSUPPORTED = "SHA-256 hashing is not supported on this JVM!";

    public UtilStatic() {
        throw new UnsupportedOperationException("You cannot instantiate this class");
    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name, Class<T> type) {
        try {
            return (T) new InitialContext().lookup(name);
        } catch (NamingException n) {
            logger.log(Level.SEVERE, "Attempted to look up invalid bean, name:" + name + " type:" + type.getName(), n);
            throw new RuntimeException(n);
        }
    }

    /**
     * detect page numbers from URI
     *
     * @param URI
     * @return
     */
    public static String getPageNumber(String URI) {
        if ("/".equals(URI)) {
            return "1";
        }
        int x = 1;
        String[] parts = URI.split("/");
        if ("toilet".equals(parts[x])) {
            x++;
        }
        if (parts.length == x) {
            return "1";
        } else if ("index".equals(parts[x])) {
            String out;
            if (parts.length == x || parts.length == x + 1) {
                out = "1";
            } else {
                if (parts.length == x + 2) {
                    out = parts[x + 1];
                } else {
                    out = parts[x + 2];
                }
            }
            try {
                Integer.valueOf(out);
            } catch (NumberFormatException n) {
                out = "1";
            }
            return out;
        } else if ("article".equals(parts[x])) {
            if (parts.length == x || parts.length == x + 1 || parts.length == x + 2) {
                return "1";
            } else {
                return parts[x + 2];
            }
        } else {
            throw new RuntimeException();
        }
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
        return sb.toString();
    }

    /**
     * removes validation breaking characters from the given string
     *
     * @param in input string
     * @param link keep "<" and ">", preserving embedded links
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
     * @param req
     * @return the url string, with parameters
     */
    public static String getURL(HttpServletRequest req) {
        String urlstr = req.getRequestURI();
        if (urlstr == null) {
            urlstr = "/index";
        }
        String qstring = req.getQueryString();
        if (qstring != null) {
            return urlstr + "?" + qstring;
        }
        return urlstr;
    }

    /**
     * @return SHA 256 MessageDigest
     */
    public static MessageDigest getHasher() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException n) {
            logger.severe(SHA256_UNSUPPORTED);
            throw new RuntimeException(SHA256_UNSUPPORTED, n);
        }
    }

    /**
     * @param stuff
     * @return stuff in Base64
     */
    public static String getBase64(byte[] stuff) {
        return new String(Base64.encode(stuff));
    }

    /**
     * @param toHash
     * @return base64 SHA 256 hash
     */
    public static String getHash(String toHash) {
        return getHash(toHash.getBytes());
    }

    /**
     * @param toHash
     * @return base64 SHA 256 hash
     */
    public static String getHash(byte[] toHash) {
        return getBase64(getHasher().digest(toHash));
    }

    /**
     * tells the client to go to a new location. WHY is this not included in the standard servlet API????
     *
     * @param res
     * @param newLocation
     */
    public static void permaMove(HttpServletResponse res, String newLocation) {
        res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        res.setHeader("Location", newLocation);
    }

    /**
     * retrieves a string from a multipart upload request
     *
     * @param req
     * @param param
     * @return value | null
     */
    public static String getParam(HttpServletRequest req, String param) {
        try {
            return new BufferedReader(new InputStreamReader(req.getPart(param).getInputStream())).readLine();
        } catch (Exception e) {
            return null;
        }
    }
}
