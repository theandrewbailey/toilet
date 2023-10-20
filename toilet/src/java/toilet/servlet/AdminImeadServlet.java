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
import libWebsiteTools.cache.JspFilter;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.file.BaseFileServlet;
import libWebsiteTools.file.FileUtil;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.imead.Local;
import libWebsiteTools.imead.Localization;
import libWebsiteTools.imead.LocalizationPK;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.tag.AbstractInput;
import toilet.AllBeanAccess;
import toilet.UtilStatic;
import toilet.bean.ToiletBeanAccess;

/**
 *
 * @author alpha
 */
@WebServlet(name = "AdminImead", description = "Edit IMEAD properties", urlPatterns = {"/adminImead"})
public class AdminImeadServlet extends ToiletServlet {

    public static final String FIRST_TIME_SETUP = "FIRST_TIME_SETUP";
    public static final String ADMIN_IMEAD = "WEB-INF/adminImead.jsp";
    private static final String CSP_TEMPLATE = "default-src data: 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'self'; report-uri %sreport";
    private static final String ALLOWED_ORIGINS_TEMPLATE = "%s\n^https?://(?:10\\.[0-9]{1,3}\\.|192\\.168\\.)[0-9]{1,3}\\.[0-9]{1,3}(?::[0-9]{1,5})?(?:/.*)?$\n^https?://(?:[a-zA-Z]+\\.)+?google(?:\\.com)?(?:\\.[a-zA-Z]{2}){0,2}(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?googleusercontent(?:\\.com)?(?:\\.[a-zA-Z]{2}){0,2}(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?feedly\\.com(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?slack\\.com(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?bing\\.com(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?yandex(?:\\.com)?(?:\\.[a-zA-Z]{2})?(?:/.*)?$\n^https?://images\\.rambler\\.ru(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?yahoo(?:\\.com)?(?:\\.[a-zA-Z]{2})?(?:/.*)?$\n^https?://(?:[a-zA-Z]+\\.)+?duckduckgo\\.com(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?baidu\\.com(?:$|/.*)";
    private static final Logger LOG = Logger.getLogger(AdminImeadServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ToiletBeanAccess beans = allBeans.getInstance(request);
        loadProperties(beans);
        if (UtilStatic.isFirstTime(beans)) {
            request.getSession().setAttribute(AdminLoginServlet.PERMISSION, AdminLoginServlet.IMEAD);
            if (null == beans.getImeadValue(SecurityRepo.BASE_URL)) {
                String canonicalRoot = AbstractInput.getTokenURL(request);
                if (!canonicalRoot.endsWith("/")) {
                    canonicalRoot += "/";
                }
                Matcher originMatcher = SecurityRepo.ORIGIN_PATTERN.matcher(canonicalRoot);
                ArrayList<Localization> locals = new ArrayList<>();
                if (originMatcher.matches()) {
                    String currentReg = originMatcher.group(2).replace(".", "\\.");
                    locals.add(new Localization("", SecurityRepo.ALLOWED_ORIGINS, String.format(ALLOWED_ORIGINS_TEMPLATE, currentReg)));
                    //locals.add(new Localization("", OdysseyFilter.CERTIFICATE_NAME, ""));
                    locals.add(new Localization("", JspFilter.CONTENT_SECURITY_POLICY, String.format(CSP_TEMPLATE, canonicalRoot)));
                    locals.add(new Localization("", SecurityRepo.BASE_URL, canonicalRoot));
                }
                beans.getImead().upsert(locals);
                request.setAttribute(SecurityRepo.BASE_URL, canonicalRoot);
                beans.getFile().processArchive((fileupload) -> {
                    fileupload.setUrl(BaseFileServlet.getImmutableURL(beans.getImeadValue(SecurityRepo.BASE_URL), fileupload));
                }, true);
            }
            showProperties(beans, request, response);
        } else {
            super.doGet(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!AdminLoginServlet.IMEAD.equals(request.getSession().getAttribute(AdminLoginServlet.PERMISSION))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        ToiletBeanAccess beans = allBeans.getInstance(request);
        loadProperties(beans);
        // save things
        String action = AbstractInput.getParameter(request, "action");
        if (null == action) {
        } else if ("save".equals(action) || "".equals(action)) {
            ArrayList<Localization> props = new ArrayList<>();
            HashSet<LocalizationPK> errors = new HashSet<>();
            request.setAttribute("ERRORS", errors);
            for (Localization l : new LocalizationRetriever(request)) {
                if (l.getLocalizationPK().getKey().startsWith("admin_")
                        && !HashUtil.ARGON2_ENCODING_PATTERN.matcher(l.getValue()).matches()) {
                    String previousValue = beans.getImead().getLocal(l.getLocalizationPK().getKey(), l.getLocalizationPK().getLocalecode());
                    if (!HashUtil.ARGON2_ENCODING_PATTERN.matcher(previousValue).matches() && previousValue.equals(l.getValue())) {
                        errors.add(l.getLocalizationPK());
                        request.setAttribute(CoronerServlet.ERROR_MESSAGE_PARAM, beans.getImead().getLocal("error_adminadmin", Local.resolveLocales(beans.getImead(), request)));
                    }
                    l.setValue(HashUtil.getArgon2Hash(l.getValue()));
                }
                if (!beans.getImead().getLocaleStrings().contains(l.getLocalizationPK().getKey())
                        || !l.getValue().equals(beans.getImead().getLocal(l.getLocalizationPK().getKey(), l.getLocalizationPK().getLocalecode()))) {
                    if (l.getLocalizationPK().getKey().startsWith("error_") || l.getLocalizationPK().getKey().startsWith("page_")) {
                        l.setValue(l.getValue());
                    }
                    props.add(l);
                }
            }
            if (errors.isEmpty()) {
                beans.getImead().upsert(props);
                request.getServletContext().removeAttribute(FIRST_TIME_SETUP);
                beans.getGlobalCache().clear();
            }
        } else if (action.startsWith("delete")) {
            String[] params = action.split("\\|");
            beans.getImead().delete(new LocalizationPK(params[2], params[1]));
            beans.getGlobalCache().clear();
        }
        showProperties(beans, request, response);
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
                return new Localization(AbstractInput.getParameter(req, "locale" + current), AbstractInput.getParameter(req, "key" + current), AbstractInput.getParameter(req, "value" + current));
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
            if (property.getLocalizationPK().getKey().startsWith("admin_") || property.getLocalizationPK().getKey().startsWith("security_")) {
                security.add(property);
            }
        }
        for (Localization property : security) {
            imeadProperties.get(Locale.forLanguageTag("")).remove(property);
        }
        request.setAttribute("security", security);
        request.setAttribute("imeadProperties", imeadProperties);
        request.setAttribute("locales", beans.getImead().getLocaleStrings());
        request.getRequestDispatcher(ADMIN_IMEAD).forward(request, response);
    }

    private void loadProperties(ToiletBeanAccess beans) {
        List<Localization> locals = new ArrayList<>();
        // in a new release, some properties may be added
        try {
            locals.addAll(getNewLocalizations(beans, getServletContext(), "/WEB-INF/IMEAD.properties", Locale.ROOT));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        if (UtilStatic.isFirstTime(beans)) {
            getServletContext().setAttribute(FIRST_TIME_SETUP, FIRST_TIME_SETUP);
            beans.getArts().refreshSearch();
            // load and save default files
            try {
                loadFile(beans, getServletContext(), "/WEB-INF/toiletwave.css", "text/css");
                loadFile(beans, getServletContext(), "/WEB-INF/toiletwave.js", "text/javascript");
                if (null == beans.getImeadValue("site_css")) {
                    locals.add(new Localization("", "site_css", "toiletwave.css"));
                }
                if (null == beans.getImeadValue("site_javascript")) {
                    locals.add(new Localization("", "site_javascript", "toiletwave.js"));
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        beans.getImead().upsert(locals);
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
                beans.getImead().getLocal(property.getKey().toString(), locale.toLanguageTag());
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
    private Properties getProperties(InputStream file) throws IOException {
        Properties IMEAD = new Properties();
        IMEAD.load(file);
        return IMEAD;
    }

    private void loadFile(ToiletBeanAccess beans, ServletContext c, String filename, String type) throws IOException {
        String servedName = filename.substring("/WEB-INF/".length());
        if (null == beans.getFile().get(servedName)) {
            Fileupload cssFile = new Fileupload();
            cssFile.setAtime(OffsetDateTime.now());
            cssFile.setFiledata(FileUtil.getByteArray(c.getResourceAsStream(filename)));
            cssFile.setEtag(HashUtil.getSHA256Hash(cssFile.getFiledata()));
            cssFile.setFilename(servedName);
            cssFile.setMimetype(type);
            beans.getFile().upsert(Arrays.asList(cssFile));
        }
    }
}
