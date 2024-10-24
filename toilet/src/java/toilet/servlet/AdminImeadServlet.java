package toilet.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.Instant;
import libWebsiteTools.cache.JspFilter;
import libWebsiteTools.security.SecurityRepo;
import static libWebsiteTools.file.BaseFileServlet.getImmutableURL;
import libWebsiteTools.file.FileCompressorJob;
import libWebsiteTools.file.FileUtil;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.imead.Local;
import libWebsiteTools.imead.Localization;
import libWebsiteTools.imead.LocalizationPK;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.security.RequestTimer;
import libWebsiteTools.tag.AbstractInput;
import toilet.AllBeanAccess;
import toilet.ArticleProcessor;
import toilet.bean.ToiletBeanAccess;
import toilet.db.Article;

/**
 *
 * @author alpha
 */
@WebServlet(name = "AdminImead", description = "Edit IMEAD properties", urlPatterns = {"/adminImead"})
public class AdminImeadServlet extends AdminServlet {

    public static final String INITIAL_PROPERTIES_FILE = "/WEB-INF/IMEAD.properties";
    public static final String ADMIN_IMEAD = "WEB-INF/adminImead.jsp";
    private static final String CSP_TEMPLATE = "default-src data: 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'self'; report-uri %sreport";
    private static final String ALLOWED_ORIGINS_TEMPLATE = "%s\n^https?://(?:10\\.[0-9]{1,3}\\.|192\\.168\\.)[0-9]{1,3}\\.[0-9]{1,3}(?::[0-9]{1,5})?(?:/.*)?$\n^https?://(?:[a-zA-Z]+\\.)+?google(?:\\.com)?(?:\\.[a-zA-Z]{2}){0,2}(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?googleusercontent(?:\\.com)?(?:\\.[a-zA-Z]{2}){0,2}(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?feedly\\.com(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?slack\\.com(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?bing\\.com(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?yandex(?:\\.com)?(?:\\.[a-zA-Z]{2})?(?:/.*)?$\n^https?://images\\.rambler\\.ru(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?yahoo(?:\\.com)?(?:\\.[a-zA-Z]{2})?(?:/.*)?$\n^https?://(?:[a-zA-Z]+\\.)+?duckduckgo\\.com(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?baidu\\.com(?:$|/.*)";
    private static final Logger LOG = Logger.getLogger(AdminImeadServlet.class.getName());

    @Override
    public AdminServletPermission getRequiredPermission(HttpServletRequest req) {
        return AdminServletPermission.IMEAD;
    }

    @Override
    public boolean isAuthorized(HttpServletRequest req) {
        return isAuthorized(req, AdminServletPermission.IMEAD) || allBeans.getInstance(req).isFirstTime(req);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ToiletBeanAccess beans = allBeans.getInstance(request);
        Instant start = Instant.now();
        loadProperties(beans);
        if (beans.isFirstTime(request)) {
            if (null == beans.getImeadValue(SecurityRepo.BASE_URL)) {
                ArrayList<Localization> locals = new ArrayList<>();
                String canonicalRoot = AbstractInput.getTokenURL(request);
                if (!canonicalRoot.endsWith("/")) {
                    canonicalRoot += "/";
                }
                Matcher originMatcher = SecurityRepo.ORIGIN_PATTERN.matcher(canonicalRoot);
                if (originMatcher.matches()) {
                    String currentReg = originMatcher.group(2).replace(".", "\\.");
                    locals.add(new Localization("", SecurityRepo.ALLOWED_ORIGINS, String.format(ALLOWED_ORIGINS_TEMPLATE, currentReg)));
                    locals.add(new Localization("", JspFilter.CONTENT_SECURITY_POLICY, String.format(CSP_TEMPLATE, canonicalRoot)));
                    locals.add(new Localization("", SecurityRepo.BASE_URL, canonicalRoot));
                }
                beans.getImead().upsert(locals);
                beans.getImead().evict();
                locals.clear();
                beans.getArts().refreshSearch();
                request.setAttribute(SecurityRepo.BASE_URL, canonicalRoot);
                // load and save default files
                try {
                    loadFile(beans, getServletContext(), "/WEB-INF/toiletwave.css", "text/css");
                    loadFile(beans, getServletContext(), "/WEB-INF/toiletwave.js", "text/javascript");
                    locals.add(new Localization("", "site_css", "toiletwave.css"));
                    locals.add(new Localization("", "site_javascript", "toiletwave.js"));
                    beans.getImead().upsert(locals);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
            request.setAttribute("FIRST_TIME_SETUP", "FIRST_TIME_SETUP");
        }
        RequestTimer.addTiming(request, "query", Duration.between(start, Instant.now()));
        showProperties(beans, request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ToiletBeanAccess beans = allBeans.getInstance(request);
        Instant start = Instant.now();
        loadProperties(beans);
        boolean initialFirstTime = beans.isFirstTime(request);
        // save things
        String action = AbstractInput.getParameter(request, "action");
        if (null == action) {
        } else if ("save".equals(action) || "".equals(action)) {
            ArrayList<Localization> props = new ArrayList<>();
            HashSet<LocalizationPK> errors = new HashSet<>();
            request.setAttribute("ERRORS", errors);
            String argon2_parameters = beans.getImeadValue("site_argon2_parameters");
            for (Localization l : new LocalizationRetriever(request)) {
                String previousValue = beans.getImead().getLocal(l.getLocalizationPK().getKey(), l.getLocalizationPK().getLocalecode());
                if (!l.getValue().equals(previousValue)) {
                    if (l.getLocalizationPK().getKey().startsWith("admin_")
                            && !HashUtil.ARGON2_ENCODING_PATTERN.matcher(l.getValue()).matches()) {
                        if (null != previousValue && !HashUtil.ARGON2_ENCODING_PATTERN.matcher(previousValue).matches() && previousValue.equals(l.getValue())) {
                            errors.add(l.getLocalizationPK());
                            request.setAttribute(CoronerServlet.ERROR_MESSAGE_PARAM, beans.getImead().getLocal("error_adminadmin", Local.resolveLocales(beans.getImead(), request)));
                        }
                        l.setValue(HashUtil.getArgon2Hash(argon2_parameters, l.getValue()));
                    }
                    if (l.getLocalizationPK().getKey().endsWith("_markdown")) {
                        Article a = new Article(1);
                        a.setPostedmarkdown(l.getValue());
                        a.setArticletitle(l.getLocalizationPK().getKey());
                        new ArticleProcessor(beans, a).call();
                        props.add(new Localization(l.getLocalizationPK().getLocalecode(), l.getLocalizationPK().getKey().replaceFirst("_markdown$", ""), l.getValue()));
                    }
                    props.add(l);
                }
            }
            if (errors.isEmpty()) {
                beans.getImead().upsert(props);
                beans.reset();
            }
        } else if (action.startsWith("delete")) {
            String[] params = action.split("\\|");
            beans.getImead().delete(new LocalizationPK(params[2], params[1]));
            beans.reset();
        }
        RequestTimer.addTiming(request, "save", Duration.between(start, Instant.now()));
        if (initialFirstTime && !beans.isFirstTime(request)) {
            response.sendRedirect(request.getAttribute(SecurityRepo.BASE_URL).toString());
        } else {
            showProperties(beans, request, response);
        }
    }

    private static class LocalizationRetriever implements Iterable<Localization>, Iterator<Localization> {

        private final HttpServletRequest req;
        private int current = -1;

        public LocalizationRetriever(HttpServletRequest req) {
            this.req = req;
        }

        @Override
        public boolean hasNext() {
            String key = AbstractInput.getParameter(req, "key" + (current + 1));
            return null != key && !key.isEmpty();
        }

        @Override
        public Localization next() {
            if (hasNext()) {
                ++current;
                return new Localization(AbstractInput.getParameter(req, "locale" + current).trim(), AbstractInput.getParameter(req, "key" + current).trim(), AbstractInput.getParameter(req, "value" + current));
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Localization> iterator() {
            return this;
        }
    }

    public static void showProperties(AllBeanAccess beans, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<Locale, List<Localization>> imeadProperties = new HashMap<>();
        for (Localization L : beans.getImead().getAll(null)) {
            Locale locale = Locale.forLanguageTag(L.getLocalizationPK().getLocalecode());
            if (!imeadProperties.containsKey(locale)) {
                imeadProperties.put(locale, new ArrayList<>());
            }
            imeadProperties.get(locale).add(L);
        }
        List<Localization> security = new ArrayList<>();
        for (Localization property : imeadProperties.get(Locale.forLanguageTag(""))) {
            if (property.getLocalizationPK().getKey().startsWith("admin_") || property.getLocalizationPK().getKey().startsWith("site_security_")) {
                security.add(property);
            }
        }
        for (Localization property : security) {
            imeadProperties.get(Locale.forLanguageTag("")).remove(property);
        }
        request.setAttribute("security", security);
        request.setAttribute("imeadProperties", imeadProperties);
        List<String> locales = new ArrayList<>();
        for (Locale l : beans.getImead().getLocales()) {
            locales.add(l.toString());
        }
        request.setAttribute("locales", locales);
        request.getRequestDispatcher(ADMIN_IMEAD).forward(request, response);
    }

    private void loadProperties(ToiletBeanAccess beans) {
        List<Localization> locals = new ArrayList<>();
        // in a new release, some properties may be added
        try {
            locals.addAll(getNewLocalizations(beans, getServletContext(), INITIAL_PROPERTIES_FILE, Locale.ROOT));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        if (!locals.isEmpty()) {
            beans.getImead().upsert(locals);
        }
    }

    /**
     * reads a properties file from deployed WAR, and returns Localization
     * objects that aren't already loaded. this will not return Localizations
     * that are changed; only ones that aren't already present.
     *
     * @param c
     * @param filename
     * @param locale
     * @return
     * @throws IOException
     */
    private List<Localization> getNewLocalizations(ToiletBeanAccess beans, ServletContext c, String filename, Locale locale) throws IOException {
        List<Localization> locals = new ArrayList<>();
        Properties IMEAD = getProperties(c.getResourceAsStream(filename));
        for (Map.Entry<Object, Object> property : IMEAD.entrySet()) {
            try {
                beans.getImead().getLocal(property.getKey().toString(), locale.toLanguageTag()).toString();
            } catch (RuntimeException r) {
                locals.add(new Localization(locale.toString(), property.getKey().toString(), property.getValue().toString()));
            }
        }
        return locals;
    }

    /**
     * reads a properties file from deployed WAR, and returns a properties
     * object
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static Properties getProperties(InputStream file) throws IOException {
        Properties IMEAD = new Properties();
        IMEAD.load(file);
        return IMEAD;
    }

    /**
     * Must be called after BASE_URL is set.
     *
     * @param beans
     * @param c
     * @param filename
     * @param type
     * @throws IOException
     */
    private void loadFile(ToiletBeanAccess beans, ServletContext c, String filename, String type) throws IOException {
        String servedName = filename.substring("/WEB-INF/".length());
        if (null == beans.getFile().get(servedName)) {
            Fileupload cssFile = new Fileupload();
            cssFile.setAtime(OffsetDateTime.now());
            cssFile.setFiledata(FileUtil.getByteArray(c.getResourceAsStream(filename)));
            cssFile.setEtag(HashUtil.getSHA256Hash(cssFile.getFiledata()));
            cssFile.setFilename(servedName);
            cssFile.setMimetype(type);
            cssFile.setUrl(getImmutableURL(beans.getImeadValue(SecurityRepo.BASE_URL), cssFile));
            beans.getFile().upsert(Arrays.asList(cssFile));
            FileCompressorJob.startAllJobs(beans, cssFile);
        }
    }
}
