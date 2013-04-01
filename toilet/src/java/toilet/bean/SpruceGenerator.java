package toilet.bean;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.servlet.http.HttpServletRequest;
import libOdyssey.bean.ExceptionRepo;
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
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
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
    private PythonInterpreter py;

    @PostConstruct
    public void setup() {
        feeds.addFeed(this);
    }

    @Lock(LockType.READ)
    public boolean ready() {
        return py != null;
    }

//    @Lock(LockType.WRITE)
//    public String getRandomWord() {
//        py.exec("w=randomWord()");
//        return py.get("w").toString();
//    }

    @Lock(LockType.WRITE)
    private String getSentence() {
        lastEntry = new Date();
        py.exec("s=doClause()");
        return py.get("s").toString();
    }

    @Lock(LockType.WRITE)
    private void addSentence(String s) {
        if (s == null) {
            s = getSentence();
        }
        RssItem i = new RssItem(s);
        i.setPubDate(lastEntry);
        i.setTitle("Spruce");
        i.setAuthor(entries.getWebMaster());
        i.setLink(entries.getLink());
        entries.addItem(i);
        changed = true;
    }

    @Lock(LockType.READ)
    public String getAddSentence() {
        String out = getSentence();
        addSentence(out);
        return out;
    }

    @Override
    @Lock(LockType.WRITE)
    public void preAdd() {
        log.entering(SpruceGenerator.class.getName(), "preAdd");
        postRemove();

        try {
            byte[] dictionaryxml = file.getFile(imead.getValue(DICTIONARY_XML)).getBinarydata();
            StringBuilder loaddictionary = new StringBuilder("loadDicStr(\"\"\"");
            loaddictionary.append(new String(dictionaryxml));
            loaddictionary.append("\"\"\")");
            py = new PythonInterpreter();
            py.exec("from spruce import *");
            py.exec(loaddictionary.toString());
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

        entries.clearFeed();
        entries.setLink(imead.getValue(UtilBean.THISURL) + LINK);
        entries.setWebMaster(imead.getValue(UtilBean.MASTER));
        entries.setManagingEditor(entries.getWebMaster());
        entries.setLanguage(imead.getValue(UtilBean.LANGUAGE));
        entries.setCopyright(imead.getValue(UtilBean.COPYRIGHT));
        entries.setLimit(Integer.valueOf(imead.getValue(SPRUCE_COUNT)));
        entries.setTtl(60);

        log.exiting(SpruceGenerator.class.getName(), "preAdd");
    }

    @Override
    @Lock(LockType.READ)
    public Document preWrite(HttpServletRequest req) {
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
    @Lock(LockType.WRITE)
    public void postRemove() {
        if (py != null) {
            py.cleanup();
            py = null;
        }
    }
}
