package toilet.bean;

import libWebsiteTools.file.FileRepo;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import libWebsiteTools.bean.ExceptionRepo;
import libWebsiteTools.bean.GuardRepo;
import libWebsiteTools.HashUtil;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.Markdowner;
import libWebsiteTools.XmlNodeSearcher;
import libWebsiteTools.file.Brotlier;
import libWebsiteTools.file.FileServlet;
import libWebsiteTools.file.FileUtil;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.file.Gzipper;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.imead.Localization;
import libWebsiteTools.rss.FeedBucket;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import toilet.ArticleProcessor;
import toilet.UtilStatic;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.db.Section;
import toilet.rss.ArticleRss;
import toilet.rss.CommentRss;
import toilet.rss.ToiletRssItem;

/**
 * so I can sleep at night, knowing my stuff is being backed up
 *
 * @author alpha
 */
@Startup
@Singleton
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class BackupDaemon {

    public final static String MIMES_TXT = "mimes.txt";
    private final static String MASTER_DIR = "file_backup";
    private final static String CONTENT_DIR = "content";
    private final static Logger LOG = Logger.getLogger(BackupDaemon.class.getName());
    private final static Pattern IMEAD_BACKUP_FILE = Pattern.compile("IMEAD(?:-(.+?))?\\.properties");
    private final static Integer PROCESSING_CHUNK_SIZE = 16;
    @EJB
    private ArticleRepo arts;
    @EJB
    private CommentRepo comms;
    @EJB
    private ExceptionRepo error;
    @EJB
    private IMEADHolder imead;
    @EJB
    private FileRepo fileRepo;
    @EJB
    private UtilBean util;
    @Resource
    private ManagedExecutorService exec;

    @PostConstruct
    private void init() {
        exec.submit(() -> {
            return Markdowner.getHtml(BackupDaemon.class.getCanonicalName());
        });
    }

    /**
     * dumps articles, comments, and uploaded files to a directory (specified by
     * site_backup key in imead.keyValue)
     */
    @Schedule(hour = "1")
    @SuppressWarnings("StringBufferMayBeStringBuilder")
    public void backup() {
        LOG.entering(BackupDaemon.class.getName(), "backup");
        LOG.info("Backup procedure initiating");
        imead.evict();
        arts.evict();
        fileRepo.evict();
        String master = imead.getValue(MASTER_DIR);
        String content = master + CONTENT_DIR + File.separator;
        File contentDir = new File(content);
        if (!contentDir.exists()) {
            contentDir.mkdirs();
        }
        StringBuffer mimes = new StringBuffer(1048576);
        fileRepo.processArchive((f) -> {
            if (shouldFileBeBackedUp(f)) {
                String fn = content + f.getFilename();
                try {   // file exists in db, different hash or file not backed up yet
                    LOG.log(Level.FINE, "Writing file {0}", f.getFilename());
                    writeFile(fn, f.getFiledata());
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Error writing " + f.getFilename(), ex);
                    error.add(null, "Backup failure", ex.getMessage() + ExceptionRepo.NEWLINE + "while backing up " + fn, null);
                }
                mimes.append(f.getFilename()).append(": ").append(f.getMimetype()).append('\n');
            }
        }, false);

        try {
            LOG.log(Level.FINE, "Writing mimes.txt");
            writeFile(master + MIMES_TXT, mimes.toString().getBytes("UTF-8"));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error writing mimes.txt: " + master + MIMES_TXT, ex);
            error.add(null, "Backup failure", "Error writing mimes.txt: " + master + MIMES_TXT, ex);
        }
        try {
            LOG.log(Level.FINE, "Writing Articles.rss");
            writeFile(master + File.separator + ArticleRss.NAME, xmlToBytes(new ArticleRss().createFeed(null)));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error writing Articles.rss to file: " + master + File.separator + ArticleRss.NAME, ex);
            error.add(null, "Backup failure", "Error writing Articles.rss to file: " + master + File.separator + ArticleRss.NAME, ex);
        }
        try {
            LOG.log(Level.FINE, "Writing Comments.rss");
            writeFile(master + File.separator + CommentRss.NAME, xmlToBytes(new CommentRss().createFeed(null)));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error writing Comments.rss to file: " + master + File.separator + CommentRss.NAME, ex);
            error.add(null, "Backup failure", "Error writing Comments.rss to file: " + master + File.separator + CommentRss.NAME, ex);
        }
        LOG.info("Backup procedure finished");
        LOG.exiting(BackupDaemon.class.getName(), "backup");
        backupToZip();
    }

    /**
     * takes a zip file backup of the site and restores everything
     *
     * @param zip
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void restoreFromZip(ZipInputStream zip) throws Exception {
        final Map<String, String> mimes = new ConcurrentHashMap<>(1000);
        final Deque<Future> articleTasks = new ConcurrentLinkedDeque<>();
        final Deque<Future> fileTasks = new ConcurrentLinkedDeque<>();
        final Deque<Future> altTasks = new ConcurrentLinkedDeque<>();
        Future masterArticleTask = null;
        Future masterCommentTask = null;
        for (ZipEntry zipEntry = zip.getNextEntry(); zipEntry != null; zipEntry = zip.getNextEntry()) {
            if (zipEntry.isDirectory()) {
                continue;
            }
            LOG.log(Level.INFO, "Processing file: {0}", zipEntry.getName());
            switch (zipEntry.getName()) {
                case ArticleRss.NAME:
                    final InputStream articleStream = new ByteArrayInputStream(FileUtil.getByteArray(zip));
                    masterArticleTask = exec.submit(() -> {
                        final Deque<Future> conversionTasks = new ConcurrentLinkedDeque<>();
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.setIgnoringElementContentWhitespace(true);
                        Element articleRoot = dbf.newDocumentBuilder().parse(articleStream).getDocumentElement();
                        for (Node item : new XmlNodeSearcher(articleRoot.getFirstChild(), "item")) {
                            conversionTasks.add(exec.submit(() -> {
                                Article art = new Article();
                                art.setComments(Boolean.FALSE);
                                for (Node component : new XmlNodeSearcher(item, "link")) {
                                    art.setArticleid(Integer.decode(StateCache.getArticleIdFromURI(component.getTextContent())));
                                }
                                for (Node component : new XmlNodeSearcher(item, "title")) {
                                    String[] parts = component.getTextContent().split(": ", 2);
                                    art.setArticletitle(parts.length == 2 ? parts[1] : "");
                                }
                                for (Node component : new XmlNodeSearcher(item, "pubDate")) {
                                    art.setPosted(new SimpleDateFormat(FeedBucket.TIME_FORMAT).parse(component.getTextContent()));
                                }
                                for (Node component : new XmlNodeSearcher(item, "category")) {
                                    art.setSectionid(new Section(null, component.getTextContent()));
                                }
                                for (Node component : new XmlNodeSearcher(item, "comments")) {
                                    art.setComments(Boolean.TRUE);
                                }
                                for (Node component : new XmlNodeSearcher(item, "author")) {
                                    art.setPostedname(component.getTextContent());
                                }
                                for (Node component : new XmlNodeSearcher(item, ToiletRssItem.TAB_METADESC_ELEMENT_NAME)) {
                                    try {
                                        art.setDescription(URLDecoder.decode(component.getTextContent(), "UTF-8"));
                                    } catch (UnsupportedEncodingException enc) {
                                        throw new JVMNotSupportedError(enc);
                                    }
                                }
                                for (Node component : new XmlNodeSearcher(item, ToiletRssItem.MARKDOWN_ELEMENT_NAME)) {
                                    art.setPostedmarkdown(component.getTextContent());
                                }
                                if (null == art.getPostedmarkdown()) {
                                    // html will be created from markdown, no need to save it
                                    for (Node component : new XmlNodeSearcher(item, "description")) {
                                        art.setPostedhtml(component.getTextContent());
                                    }
                                }
                                art.setCommentCollection(null);
                                // conversion
                                return ArticleProcessor.convert(art);
                            }));
                        }
                        arts.deleteEverything();
                        return conversionTasks;
                    });
                    break;
                case CommentRss.NAME:
                    final InputStream commentStream = new ByteArrayInputStream(FileUtil.getByteArray(zip));
                    masterCommentTask = exec.submit(() -> {
                        final List<Comment> comments = new ArrayList<>();
                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.setIgnoringElementContentWhitespace(true);
                        Element commentRoot = dbf.newDocumentBuilder().parse(commentStream).getDocumentElement();
                        for (Node item : new XmlNodeSearcher(commentRoot.getFirstChild(), "item")) {
                            Comment comm = new Comment(comments.size() + 1);
                            for (Node component : new XmlNodeSearcher(item, "description")) {
                                comm.setPostedhtml(component.getTextContent().replace("&lt;", "<").replace("&gt;", ">"));
                            }
                            for (Node component : new XmlNodeSearcher(item, "pubDate")) {
                                comm.setPosted(new SimpleDateFormat(FeedBucket.TIME_FORMAT).parse(component.getTextContent()));
                            }
                            for (Node component : new XmlNodeSearcher(item, "author")) {
                                comm.setPostedname(component.getTextContent());
                            }
                            for (Node component : new XmlNodeSearcher(item, "link")) {
                                comm.setArticleid(new Article(Integer.decode(StateCache.getArticleIdFromURI(component.getTextContent()))));
                                break;
                            }
                            comments.add(comm);
                        }
                        for (Comment c : comments) {
                            c.setCommentid(null);
                        }
                        return comments;
                    });
                    break;
                case BackupDaemon.MIMES_TXT:
                    String mimeString = new String(FileUtil.getByteArray(zip));
                    altTasks.add(exec.submit(() -> {
                        for (String mimeEntry : mimeString.split("\n")) {
                            try {
                                String[] parts = mimeEntry.split(": ");
                                mimes.put(parts[0], parts[1]);
                            } catch (ArrayIndexOutOfBoundsException a) {
                            }
                        }
                        return null;
                    }));
                    break;
                default:
                    Matcher imeadBackup = IMEAD_BACKUP_FILE.matcher(zipEntry.getName());
                    if (imeadBackup.find()) {
                        String properties = new String(FileUtil.getByteArray(zip));
                        altTasks.add(exec.submit(() -> {
                            String locale = null != imeadBackup.group(1) ? imeadBackup.group(1) : "";
                            Properties props = new Properties();
                            try {
                                props.load(new StringReader(properties));
                                ArrayList<Localization> localizations = new ArrayList<>(props.size() * 2);
                                for (String key : props.stringPropertyNames()) {
                                    localizations.add(new Localization(locale, key, props.getProperty(key)));
                                }
                                imead.upsert(localizations);
                            } catch (IOException ex) {
                                error.add(null, "Can't restore properties", "Can't restore properties for locale " + locale, ex);
                            }
                            return props;
                        }));
                    } else {
                        Fileupload incomingFile = new Fileupload();
                        incomingFile.setAtime(new Date(zipEntry.getTime()));
                        incomingFile.setFilename(zipEntry.getName().replace("content/", ""));
                        incomingFile.setMimetype(zipEntry.getComment());
                        incomingFile.setFiledata(FileUtil.getByteArray(zip));
                        fileTasks.add(exec.submit(() -> {
                            incomingFile.setEtag(HashUtil.getSHA256Hash(incomingFile.getFiledata()));
                            if (null == incomingFile.getMimetype() && mimes.containsKey(incomingFile.getFilename())) {
                                incomingFile.setMimetype(mimes.get(incomingFile.getFilename()));
                                mimes.remove(incomingFile.getFilename());
                            } else if (null == incomingFile.getMimetype()) {
                                incomingFile.setMimetype(FileRepo.DEFAULT_MIME_TYPE);
                            }
                            Fileupload existingFile = fileRepo.get(incomingFile.getFilename());
                            if (null == existingFile) {
                                return incomingFile;
                            } else if (!incomingFile.getEtag().equals(existingFile.getEtag())) {
                                LOG.log(Level.INFO, "Existing file different, updating {0}", incomingFile.getFilename());
                                return incomingFile;
                            }
                            return null;
                        }));
                    }
                    break;
            }
        }
        zip.close();
        if (!fileTasks.isEmpty()) {
            UtilStatic.finish(altTasks).clear();
            altTasks.add(exec.submit(() -> {
                ArrayList<Fileupload> files = new ArrayList<>(fileTasks.size());
                for (Future task : fileTasks) {
                    try {
                        if (null != task.get() && task.get() instanceof Fileupload) {
                            Fileupload file = (Fileupload) task.get();
                            if (mimes.containsKey(file.getFilename())) {
                                file.setMimetype(mimes.get(file.getFilename()));
                            }
                            String fileUrl = FileServlet.getImmutableURL(imead.getValue(GuardRepo.CANONICAL_URL), file);
                            file.setUrl(fileUrl);
                            files.add(file);
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        LOG.log(Level.SEVERE, "Tried to process a bunch of files, but couldn't.", ex);
                    }
                    if (files.size() > PROCESSING_CHUNK_SIZE) {
                        final ArrayList<Fileupload> fileChunk = new ArrayList<>(files);
                        altTasks.add(exec.submit(() -> {
                            fileRepo.upsert(fileChunk);
                        }));
                        files.clear();
                    }
                }
                fileRepo.upsert(files);
            }, false));
        }
        if (null != masterArticleTask) {
            UtilStatic.finish(UtilStatic.finish(altTasks)).clear();
            final List<Article> articles = Collections.synchronizedList(new ArrayList<>());
            for (Future task : (Deque<Future>) masterArticleTask.get()) {
                if (null != task.get() && task.get() instanceof Article) {
                    articleTasks.add(exec.submit(() -> {
                        try {
                            Article art = (Article) task.get();
                            articles.add(art);
                            new ArticleProcessor(art, imead, fileRepo).call();
                        } catch (InterruptedException | ExecutionException ex) {
                            LOG.log(Level.SEVERE, "Tried to process a bunch of articles, but couldn't.", ex);
                        }
                    }));
                }
            }
            UtilStatic.finish(articleTasks).clear();
            // to do a job right, do it yourself.
            articles.sort((Article t, Article t1) -> {
                if (null != t.getArticleid() && null != t1.getArticleid()) {
                    return t.getArticleid() - t1.getArticleid();
                } else if (null != t.getArticleid()) {
                    return 1;
                } else if (null != t1.getArticleid()) {
                    return -1;
                }
                return 0;
            });
            for (Article art : articles) {
                art.setArticleid(null);
            }
            arts.upsert(articles);
            if (null != masterCommentTask) {
                comms.upsert((List<Comment>) masterCommentTask.get());
            }
        }
        imead.evict();
        exec.submit(() -> {
            util.resetArticleFeed();
            util.resetCommentFeed();
            arts.refreshSearch();
            fileRepo.processArchive((f) -> {
                exec.submit(new Brotlier(f));
                exec.submit(new Gzipper(f));
            }, false);
        });
    }

    /**
     * stuffs everything to a zip file in the backup directory (specified by
     * site_backup key in imead.keyValue) can be used for the import
     * functionality
     *
     * @see BackupDaemon.createZip(OutputStream wrapped)
     */
    public void backupToZip() {
        LOG.entering(BackupDaemon.class.getName(), "backupToZip");
        LOG.info("Backup to zip procedure initiating");
        String master = imead.getValue(MASTER_DIR);
        String zipName = getZipName();
        String fn = master + zipName;
        try (FileOutputStream out = new FileOutputStream(fn)) {
            createZip(out);
            LOG.info("Backup to zip procedure finished");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error writing zip file: " + fn, ex);
            error.add(null, "Backup failure", ex.getMessage() + ExceptionRepo.NEWLINE + "while backing up " + fn, null);
        }
        LOG.exiting(BackupDaemon.class.getName(), "backupToZip");
    }

    /**
     * stuffs all articles, comments, and uploaded files (with MIME types) into
     * zip format and dumps it on the given OutputStream can be used to save to
     * file or put on a request
     *
     * @param wrapped put zip on this
     * @throws IOException something went wrong (don't know how anything would,
     * just prove me wrong...)
     */
    @SuppressWarnings("StringBufferMayBeStringBuilder")
    public void createZip(OutputStream wrapped) throws IOException {
        String contentDir = CONTENT_DIR + "/";
        List<String> directories = new ArrayList<>();
        Future<byte[]> articleRss = exec.submit(() -> {
            return xmlToBytes(new ArticleRss().createFeed(null));
        });
        Future<byte[]> commentRss = exec.submit(() -> {
            return xmlToBytes(new CommentRss().createFeed(null));
        });
        StringBuffer mimes = new StringBuffer(1048576);
        Future files = exec.submit(() -> {
            fileRepo.processArchive((f) -> {
                if (shouldFileBeBackedUp(f)) {
                    mimes.append(f.getFilename()).append(": ").append(f.getMimetype()).append('\n');
                }
            }, false);
        });
        Future<Map<String, String>> IMEADbackup = exec.submit(() -> {
            Map<Locale, Properties> propMap = imead.getProperties();
            Map<String, String> localeFiles = new HashMap<>(propMap.size() * 2);
            for (Locale l : propMap.keySet()) {
                try {
                    StringWriter propertiesContent = new StringWriter(10000);
                    propMap.get(l).store(propertiesContent, null);
                    String localeString = l != Locale.ROOT ? l.toLanguageTag() : "";
                    localeFiles.put(localeString, propertiesContent.toString());
                } catch (IOException ex) {
                    error.add(null, "Can't backup properties", "Can't backup properties for locale " + l.toLanguageTag(), ex);
                }
            }
            return localeFiles;
        });

        try (ZipOutputStream zip = new ZipOutputStream(wrapped)) {
            long time = new Date().getTime();
            addEntryToZip(zip, ArticleRss.NAME, "text/xml", time, articleRss.get());
            addEntryToZip(zip, CommentRss.NAME, "text/xml", time, commentRss.get());
            for (Map.Entry<String, String> locale : IMEADbackup.get().entrySet()) {
                String name = 0 == locale.getKey().length() ? "IMEAD.properties" : "IMEAD-" + locale.getKey() + ".properties";
                addEntryToZip(zip, name, "text/plain", time, locale.getValue().getBytes());
            }
            files.get();
            addEntryToZip(zip, MIMES_TXT, "text/plain", time, mimes.toString().getBytes("UTF-8"));
            fileRepo.processArchive((f) -> {
                if (shouldFileBeBackedUp(f)) {
                    try {
                        String insideFilename = contentDir + f.getFilename();
                        String[] dirs = insideFilename.split("/");
                        dirs = Arrays.copyOf(dirs, dirs.length - 1);
                        StringBuilder subdir = new StringBuilder();
                        synchronized (zip) {
                            for (String d : dirs) {
                                subdir.append(d).append("/");
                                if (!directories.contains(subdir.toString())) {
                                    zip.putNextEntry(new ZipEntry(subdir.toString()));
                                    zip.closeEntry();
                                    directories.add(subdir.toString());
                                }
                            }
                            addEntryToZip(zip, insideFilename, f.getMimetype(), f.getAtime().getTime(), f.getFiledata());
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }, true);
        } catch (InterruptedException | ExecutionException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return site title (a-zA-Z_0-9 only) + today's date (year + month + day)
     * + ".zip"
     */
    public String getZipName() {
        return imead.getLocal(UtilBean.SITE_TITLE, "en").replaceAll("[^\\w]", "") + new SimpleDateFormat("yyyyMMdd'.zip'").format(new Date());
    }

    /**
     * adds a file to the given ZIP stream
     *
     * @param zip zip stream
     * @param name of file
     * @param mime file type
     * @param time modified time (from Date.getTime), defaults to -1
     * @param content file contents
     * @throws IOException something went wrong (why would it?)
     */
    private void addEntryToZip(ZipOutputStream zip, String name, String mime, long time, byte[] content) throws IOException {
        ZipEntry out = new ZipEntry(name);
        out.setMethod(ZipEntry.DEFLATED);
        out.setTime(time);
        out.setComment(mime);
        zip.putNextEntry(out);
        zip.write(content);
        zip.closeEntry();
    }

    /**
     * makes a really big string from the given XML object
     *
     * @param xml
     * @return big string
     */
    private byte[] xmlToBytes(Document xml) {
        DOMSource DOMsrc = new DOMSource(xml);
        StringWriter sw = new StringWriter();
        StreamResult str = new StreamResult(sw);
        Transformer trans = FeedBucket.getTransformer(false);
        try {
            trans.transform(DOMsrc, str);
            return sw.toString().getBytes("UTF-8");
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        }
    }

    /**
     * writes a file
     *
     * @param filename what to name it
     * @param content what it contains
     * @throws IOException something went boom
     */
    private void writeFile(String filename, byte[] content) throws IOException {
        File f = new File(filename);
        if (!f.exists()) {
            List<String> parts = Arrays.asList(filename.split("/"));
            String dir = "";
            for (String p : parts) {
                if (!p.equals(parts.get(parts.size() - 1))) {
                    dir += File.separator + p;
                }
            }
            File d = new File(dir);
            if (!d.exists()) {
                d.mkdirs();
            }
            f.createNewFile();
        }
        try (FileOutputStream tempStr = new FileOutputStream(f, false)) {
            tempStr.write(content);
        }
    }

    private static boolean shouldFileBeBackedUp(Fileupload f) {
        return !f.getFilename().startsWith("articlePreview/");
    }
}
