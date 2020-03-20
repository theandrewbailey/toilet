package toilet.bean;

import libWebsiteTools.file.FileRepo;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libWebsiteTools.bean.ExceptionRepo;
import libWebsiteTools.bean.GuardRepo;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.Feed;
import libWebsiteTools.rss.AbstractRssFeed;
import libWebsiteTools.rss.RssChannel;
import libWebsiteTools.rss.RssItem;
import libWebsiteTools.rss.iFeed;
import org.python.util.PythonInterpreter;
import org.w3c.dom.Document;

@Startup
@Singleton
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Feed(SpruceGenerator.SPRUCE_FEED_NAME)
public class SpruceGenerator extends AbstractRssFeed {

    public static final String LOCAL_NAME = "java:module/SpruceGenerator";
    public static final String DICTIONARY_XML = "spruce_dictionary";
    public static final String SPRUCE_FEED_NAME = "Spruce.rss";
    public static final String ERROR = "Spruce cannot start properly!";
    private static final String LINK = "spruce";
    private static final String SPRUCE_COUNT = "rss_spruceCount";
    private static final Logger LOG = Logger.getLogger(SpruceGenerator.class.getName());
    private Document XML;
    private final RssChannel entries = new RssChannel("Spruce", LINK, "Some wisdom from Spruce");
    private Date lastEntry = new Date();
    private boolean changed = true;
//    private ScriptEngine py;
    private PythonInterpreter py;
    @EJB
    private FileRepo file;
    @EJB
    private IMEADHolder imead;
    @Resource
    private ManagedExecutorService exec;

    @PostConstruct
    public void setup() {
        if (!shouldBeReady()) {
            LOG.log(Level.WARNING, "Spruce is not configured, and will not be available. Goodbye.");
            return;
        }
        feeds.upsert(Arrays.asList(this));
    }

    /**
     *
     * @return is the interpreter running right now?
     */
    public boolean ready() {
        return py != null;
    }

    /**
     *
     * @return is the interpreter likely to be available?
     */
    public boolean shouldBeReady() {
        return null != imead.getValue(SPRUCE_COUNT)
                && null != imead.getValue(DICTIONARY_XML)
                && null != file.get(imead.getValue(DICTIONARY_XML));
    }

    private synchronized String getSentence() {
        try {
            lastEntry = new Date();
            return py.eval("doClause()").toString();
        } catch (Exception ex) {
            return null;
        }
    }

    private synchronized void addSentence(String s) {
        if (s == null) {
            s = getSentence();
        }
        RssItem i = new RssItem(s);
        i.setPubDate(lastEntry);
        i.setTitle("Spruce");
        i.setAuthor(entries.getWebMaster());
        i.setLink(entries.getLink());
        entries.addItemToTop(i);
        changed = true;
    }

    public String getAddSentence() {
        String out = getSentence();
        addSentence(out);
        return out;
    }

    private class InitializeSpruce implements Callable<PythonInterpreter> {

        @Override
        public PythonInterpreter call() throws Exception {
            if (py == null) {
                LOG.info("starting Spruce");
                Fileupload dictionary = file.get(imead.getValue(DICTIONARY_XML));
                if (null != dictionary) {
                    try {
                        byte[] dictionaryxml = dictionary.getFiledata();
//                    ScriptEngine pyse = new ScriptEngineManager().getEngineByName("python");
//                    pyse.eval("from spruce import *");
//                    pyse.put("dicxml", new String(dictionaryxml));
//                    pyse.eval("loadDicStr(dicxml)");
//                    pyse.eval("dicxml=None");
//                    py = pyse;
                        PythonInterpreter pi = new PythonInterpreter();
                        pi.exec("from spruce import *");
                        pi.set("dicxml", new String(dictionaryxml));
                        pi.exec("loadDicStr(dicxml)");
                        pi.exec("dicxml=None");
                        py = pi;
                        getAddSentence();
                        LOG.info("Spruce has been reset");
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, ERROR, ex);
                        postRemove();
                        StringWriter w = new StringWriter();
                        PrintWriter p = new PrintWriter(w, false);
                        ex.printStackTrace(p);
                        p.flush();
                        RssItem i = new RssItem(w.toString().replace("\n\tat ", ExceptionRepo.NEWLINE + " at "));
                        i.setTitle(ERROR);
                        i.setLink(entries.getLink());
                        i.setPubDate(lastEntry);
                        entries.addItem(i);
                    }
                }
            }
            return py;
        }
    }

    @Override
    public synchronized iFeed preAdd() {
        LOG.entering(SpruceGenerator.class.getName(), "preAdd");
        postRemove();

        exec.submit(new InitializeSpruce());

        entries.clearFeed();
        entries.setLink(imead.getValue(GuardRepo.CANONICAL_URL) + LINK);
        entries.setWebMaster(imead.getValue(UtilBean.MASTER));
        entries.setManagingEditor(entries.getWebMaster());
        entries.setLanguage(imead.getValue(UtilBean.LANGUAGE));
        entries.setCopyright(imead.getValue(UtilBean.COPYRIGHT));
        entries.setLimit(Integer.valueOf(imead.getValue(SPRUCE_COUNT)));
        entries.setTtl(60);

        LOG.exiting(SpruceGenerator.class.getName(), "preAdd");
        return this;
    }

    public long lastModified() {
        return lastEntry.getTime();
    }

    @Override
    public iFeed doHead(HttpServletRequest req, HttpServletResponse res) {
        res.setHeader("Cache-Control", "public, max-age=" + 300);
        res.setDateHeader("Last-Modified", lastEntry.getTime());
        res.setDateHeader("Expires", new Date().getTime() + 300000);
        return this;
    }

    @Override
    public Document preWrite(HttpServletRequest req, HttpServletResponse res) {
        doHead(req, res);
        if (new Date().getTime() - lastEntry.getTime() > 300000) {
            getAddSentence();
        }
        synchronized (entries) {
            if (changed) {
                changed = false;
                XML = refreshFeed(entries);
            }
        }
        return XML;
    }

    @Override
    public synchronized iFeed postRemove() {
        if (py != null) {
            py.cleanup();
            py = null;
        }
        return this;
    }
}
