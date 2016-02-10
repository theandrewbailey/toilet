package toilet.bean;

import libWebsiteTools.file.FileRepo;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.bean.ExceptionRepo;
import libOdyssey.bean.GuardHolder;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.Feed;
import libWebsiteTools.rss.entity.AbstractRssFeed;
import libWebsiteTools.rss.entity.RssChannel;
import libWebsiteTools.rss.entity.RssItem;
import org.python.util.PythonInterpreter;
import org.w3c.dom.Document;

@Startup
@Singleton
@Feed(SpruceGenerator.SPRUCE_FEED_NAME)
public class SpruceGenerator extends AbstractRssFeed {

    public static final String LOCAL_NAME = "java:module/SpruceGenerator";
    public static final String DICTIONARY_XML = "spruce_dictionary";
    public static final String SPRUCE_FEED_NAME = "Spruce.rss";
    public static final String ERROR = "Spruce cannot start properly!";
    private static final String LINK = "spruce";
    private static final String SPRUCE_COUNT = "rss_spruceCount";
    private static final Logger log = Logger.getLogger(SpruceGenerator.class.getName());
    @EJB
    private FileRepo file;
    @EJB
    private IMEADHolder imead;
    private Document XML;
    private final RssChannel entries = new RssChannel("Spruce", LINK, "Some wisdom from Spruce");
    private Date lastEntry = new Date();
    private boolean changed = true;
//    private ScriptEngine py;
    private PythonInterpreter py;

    @PostConstruct
    public void setup() {
        feeds.addFeed(this);
    }

    public boolean ready() {
        return py != null;
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

    private class InitializeSpruce implements Runnable {

        @Override
        public synchronized void run() {
            if (py == null) {
                log.info("starting Spruce");
                try {
                    byte[] dictionaryxml = file.getFile(imead.getValue(DICTIONARY_XML)).getFiledata();
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
                    log.info("Spruce has been reset");
                } catch (Exception ex) {
                    log.log(Level.SEVERE, ERROR, ex);
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
    }

    @Override
    public synchronized void preAdd() {
        log.entering(SpruceGenerator.class.getName(), "preAdd");
        postRemove();

        // because ManagedThreadFactory and/or ManagedExecutorService suck
        new Thread(new InitializeSpruce()).start();

        entries.clearFeed();
        entries.setLink(imead.getValue(GuardHolder.CANONICAL_URL) + LINK);
        entries.setWebMaster(imead.getValue(UtilBean.MASTER));
        entries.setManagingEditor(entries.getWebMaster());
        entries.setLanguage(imead.getValue(UtilBean.LANGUAGE));
        entries.setCopyright(imead.getValue(UtilBean.COPYRIGHT));
        entries.setLimit(Integer.valueOf(imead.getValue(SPRUCE_COUNT)));
        entries.setTtl(60);

        log.exiting(SpruceGenerator.class.getName(), "preAdd");
    }

    public long lastModified(){
        return lastEntry.getTime();
    }

    @Override
    public Document preWrite(HttpServletRequest req, HttpServletResponse res) {
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
    public synchronized void postRemove() {
        if (py != null) {
            py.cleanup();
            py = null;
        }
    }
}
