package toilet.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.HashUtil;
import libWebsiteTools.JspFilter;
import libWebsiteTools.bean.GuardRepo;
import libWebsiteTools.file.FileServlet;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import libWebsiteTools.imead.Localization;
import libWebsiteTools.imead.LocalizationPK;
import libWebsiteTools.tag.AbstractInput;
import toilet.FirstTimeDetector;
import toilet.UtilStatic;

/**
 *
 * @author alpha
 */
@WebServlet(name = "AdminImead", description = "Edit IMEAD properties", urlPatterns = {"/adminImead"})
public class AdminImead extends ToiletServlet {

    public static final String MAN_IMEAD = "WEB-INF/manImead.jsp";
    private static final String CSP_TEMPLATE = "default-src data: %s; script-src %s; object-src 'none'; frame-ancestors 'self';";
    private static final String ACCEPTABLE_DOMAIN_TEMPLATE = "%s\n^https?://(?:10\\.[0-9]{1,3}\\.|192\\.168\\.)[0-9]{1,3}\\.[0-9]{1,3}(?::[0-9]{1,5})?(?:/.*)?$\n^https?://(?:[a-zA-Z]+\\.)+?google(?:\\.com)?(?:\\.[a-zA-Z]{2}){0,2}(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?googleusercontent(?:\\.com)?(?:\\.[a-zA-Z]{2}){0,2}(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?feedly\\.com(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?slack\\.com(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?bing\\.com(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?yandex(?:\\.com)?(?:\\.[a-zA-Z]{2})?(?:/.*)?$\n^https?://images\\.rambler\\.ru(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?yahoo(?:\\.com)?(?:\\.[a-zA-Z]{2})?(?:/.*)?$\n^https?://(?:[a-zA-Z]+\\.)+?duckduckgo\\.com(?:$|/.*)\n^https?://(?:[a-zA-Z]+\\.)+?baidu\\.com(?:$|/.*)";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //Object ssl = request.getAttribute("javax.servlet.request.ssl_session_mgr");
        if (FirstTimeDetector.isFirstTime(imead)) {
            request.getSession().setAttribute(AdminLoginServlet.PERMISSION, AdminLoginServlet.IMEAD);
            if (null == imead.getValue(GuardRepo.CANONICAL_URL)) {
                String canonicalRoot = AbstractInput.getTokenURL(request);
                Matcher originMatcher = GuardRepo.ORIGIN_PATTERN.matcher(canonicalRoot);
                ArrayList<Localization> locals = new ArrayList<>();
                if (originMatcher.matches()) {
                    String currentReg = originMatcher.group(2).replace(".", "\\.");
                    locals.add(new Localization("", GuardRepo.ACCEPTABLE_CONTENT_DOMAINS, String.format(ACCEPTABLE_DOMAIN_TEMPLATE, currentReg)));
                    //locals.add(new Localization("", OdysseyFilter.CERTIFICATE_NAME, ""));
                    locals.add(new Localization("", JspFilter.CONTENT_SECURITY_POLICY, String.format(CSP_TEMPLATE, originMatcher.group(1), originMatcher.group(1))));
                    locals.add(new Localization("", GuardRepo.CANONICAL_URL, canonicalRoot));
                }
                imead.upsert(locals);
                file.processArchive((fileupload) -> {
                    fileupload.setUrl(FileServlet.getImmutableURL(imead.getValue(GuardRepo.CANONICAL_URL), fileupload));
                }, true);
            }
            showProperties(request, response, imead);
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
                    String previousValue = imead.getLocal(l.getLocalizationPK().getKey(), l.getLocalizationPK().getLocalecode());
                    if (!HashUtil.ARGON2_ENCODING_PATTERN.matcher(previousValue).matches() && previousValue.equals(l.getValue())) {
                        errors.add(l.getLocalizationPK());
                        request.setAttribute("ERROR_MESSAGE", imead.getLocal("error_adminadmin", Local.resolveLocales(request)));
                    }
                    l.setValue(HashUtil.getArgon2Hash(l.getValue()));
                }
                if (!imead.getSupportedLocales().contains(l.getLocalizationPK().getKey())
                        || !l.getValue().equals(imead.getLocal(l.getLocalizationPK().getKey(), l.getLocalizationPK().getLocalecode()))) {
                    if (l.getLocalizationPK().getKey().startsWith("error_") || l.getLocalizationPK().getKey().startsWith("page_")) {
                        l.setValue(UtilStatic.htmlFormat(l.getValue(), true, false));
                    }
                    props.add(l);
                }
            }
            if (errors.isEmpty()) {
                imead.upsert(props);
                request.getServletContext().removeAttribute(FirstTimeDetector.FIRST_TIME_SETUP);
            }
        } else if (action.startsWith("delete")) {
            String[] params = action.split("\\|");
            imead.delete(new LocalizationPK(params[2],params[1]));
        }
        showProperties(request, response, imead);
    }

    class LocalizationRetriever implements Iterable<Localization>, Iterator<Localization> {

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

    public static void showProperties(HttpServletRequest request, HttpServletResponse response, IMEADHolder imead) throws ServletException, IOException {
        Map<Locale, List<Localization>> imeadProperties = new HashMap<>();
        for (Localization L : imead.getAll(null)) {
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
        request.setAttribute("locales", imead.getSupportedLocales());
        request.getRequestDispatcher(MAN_IMEAD).forward(request, response);
    }
}
