package toilet.bean;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import libWebsiteTools.file.FileRepository;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Local;
import libWebsiteTools.rss.RssChannel;
import libWebsiteTools.rss.RssItem;
//import org.python.util.PythonInterpreter;
import org.w3c.dom.Document;
import libWebsiteTools.rss.Feed;
import libWebsiteTools.rss.DynamicFeed;

public abstract class SpruceGenerator implements DynamicFeed {

    public static final String DICTIONARY_XML = "spruce_dictionary";
    public static final String SPRUCE_FEED_NAME = "Spruce.rss";
    public static final String ERROR = "Spruce cannot start properly!";
    private final IMEADHolder imead;
    private final FileRepository file;
    private final ManagedExecutorService exec;
    private static final String LINK = "spruce";
    private static final String SPRUCE_COUNT = "site_rss_spruceCount";
    private static final Logger LOG = Logger.getLogger(SpruceGenerator.class.getName());
    private Document XML;
    private final RssChannel entries = new RssChannel("Spruce", LINK, "Some wisdom from Spruce");
    private OffsetDateTime lastEntry = OffsetDateTime.now();
    private boolean changed = true;
    private static final Map<String, String> URL = Collections.unmodifiableMap(new HashMap<String, String>() {
        {
            put(SPRUCE_FEED_NAME, "Spruce");
        }
    });
//    private ScriptEngine py;
//    private PythonInterpreter py;

    public SpruceGenerator(IMEADHolder imead, FileRepository file, ManagedExecutorService exec) {
        this.imead = imead;
        this.file = file;
        this.exec = exec;
//        if (!shouldBeReady()) {
//            LOG.log(Level.WARNING, "Spruce is not configured, and will not be available. Goodbye.");
//            return;
//        }
    }

//    /**
//     *
//     * @return is the interpreter running right now?
//     */
//    public boolean ready() {
//        return py != null;
//    }
//
//    /**
//     *
//     * @return is the interpreter likely to be available?
//     */
//    public boolean shouldBeReady() {
//        return null != imead.getValue(SPRUCE_COUNT)
//                && null != imead.getValue(DICTIONARY_XML)
//                && null != file.get(imead.getValue(DICTIONARY_XML));
//    }
//
//    private synchronized String getSentence() {
//        try {
//            lastEntry = OffsetDateTime.now();
//            return py.eval("doClause()").toString();
//        } catch (Exception ex) {
//            return null;
//        }
//    }
//
//    private synchronized void addSentence(String s) {
//        if (s == null) {
//            s = getSentence();
//        }
//        RssItem i = new RssItem(s);
//        i.setPubDate(lastEntry);
//        i.setTitle("Spruce");
//        i.setAuthor(entries.getWebMaster());
//        i.setLink(entries.getLink());
//        entries.addItemToTop(i);
//        changed = true;
//    }
//
//    public String getAddSentence() {
//        String out = getSentence();
//        addSentence(out);
//        return out;
//    }
//
//    @Override
//    public String getName() {
//        return SPRUCE_FEED_NAME;
//    }
//
//    @Override
//    public Map<String, String> getFeedURLs(HttpServletRequest req) {
//        return URL;
//    }
//
//    @Override
//    public boolean willHandle(String name) {
//        return false;
//    }
//
//    @Override
//    public synchronized Feed preAdd() {
//        LOG.entering(SpruceGenerator.class.getName(), "preAdd");
//        postRemove();
//
//        exec.submit(() -> {
//            if (py == null) {
//                LOG.info("starting Spruce");
//                Fileupload dictionary = file.get(imead.getValue(DICTIONARY_XML));
//                if (null != dictionary) {
//                    try {
//                        byte[] dictionaryxml = dictionary.getFiledata();
////                    ScriptEngine pyse = new ScriptEngineManager().getEngineByName("python");
////                    pyse.eval("from spruce import *");
////                    pyse.put("dicxml", new String(dictionaryxml));
////                    pyse.eval("loadDicStr(dicxml)");
////                    pyse.eval("dicxml=None");
////                    py = pyse;
//                        PythonInterpreter pi = new PythonInterpreter();
//                        pi.exec("from spruce import *");
//                        pi.set("dicxml", new String(dictionaryxml));
//                        pi.exec("loadDicStr(dicxml)");
//                        pi.exec("dicxml=None");
//                        py = pi;
//                        getAddSentence();
//                        LOG.info("Spruce has been reset");
//                    } catch (Exception ex) {
//                        LOG.log(Level.SEVERE, ERROR, ex);
//                        postRemove();
//                        StringWriter w = new StringWriter();
//                        PrintWriter p = new PrintWriter(w, false);
//                        ex.printStackTrace(p);
//                        p.flush();
//                        RssItem i = new RssItem(w.toString().replace("\n\tat ", SecurityRepo.NEWLINE + " at "));
//                        i.setTitle(ERROR);
//                        i.setLink(entries.getLink());
//                        i.setPubDate(lastEntry);
//                        entries.addItem(i);
//                    }
//                }
//            }
//        });
//
//        entries.clearFeed();
//        entries.setLink(imead.getValue(SecurityRepo.BASE_URL) + LINK);
//        entries.setWebMaster(imead.getValue(Feed.MASTER));
//        entries.setManagingEditor(entries.getWebMaster());
//        entries.setLanguage(imead.getValue(Feed.LANGUAGE));
//        entries.setCopyright(imead.getValue(Feed.COPYRIGHT));
//        entries.setLimit(Integer.valueOf(imead.getValue(SPRUCE_COUNT)));
//        entries.setTtl(60);
//
//        LOG.exiting(SpruceGenerator.class.getName(), "preAdd");
//        return this;
//    }
//
//    @Override
//    public long getLastModified(HttpServletRequest req) {
//        return lastEntry.toInstant().toEpochMilli();
//    }
//
//    @Override
//    public Feed doHead(HttpServletRequest req, HttpServletResponse res) {
//        res.setHeader("Cache-Control", "public, max-age=" + 300);
//        res.setDateHeader("Last-Modified", lastEntry.toInstant().toEpochMilli());
//        res.setDateHeader("Expires", OffsetDateTime.now().toInstant().toEpochMilli() + 300000);
//        req.removeAttribute(Local.LOCALE_PARAM);
//        ToiletBeanAccess beans = (ToiletBeanAccess) req.getAttribute(libWebsiteTools.AllBeanAccess.class.getCanonicalName());
//        req.setAttribute(Local.OVERRIDE_LOCALE_PARAM, Locale.forLanguageTag(beans.getImeadValue(Feed.LANGUAGE)));
//        return this;
//    }
//
//    @Override
//    public Document preWrite(HttpServletRequest req, HttpServletResponse res) {
//        doHead(req, res);
//        if (OffsetDateTime.now().toInstant().toEpochMilli() - lastEntry.toInstant().toEpochMilli() > 300000) {
//            getAddSentence();
//        }
//        synchronized (entries) {
//            if (changed) {
//                changed = false;
//                XML = Feed.refreshFeed(Arrays.asList(entries));
//            }
//        }
//        return XML;
//    }
//
//    @Override
//    public synchronized Feed postRemove() {
//        if (py != null) {
//            py.cleanup();
//            py = null;
//        }
//        return this;
//    }
}
