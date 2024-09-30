package toilet.bean;

import toilet.IndexFetcher;
import libWebsiteTools.file.FileRepository;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.security.HashUtil;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.XmlNodeSearcher;
import libWebsiteTools.file.Brotlier;
import libWebsiteTools.file.BaseFileServlet;
import libWebsiteTools.file.FileUtil;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.file.Gzipper;
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
import libWebsiteTools.rss.Feed;

/**
 * so I can sleep at night, knowing my stuff is being backed up
 *
 * @author alpha
 */
public class BackupDaemon implements Runnable {

    public final static String MIMES_TXT = "mimes.txt";
    private final static String MASTER_DIR = "site_backup";
    private final static String CONTENT_DIR = "content";
//    private final static String CONTENT_ALT_DIR = "contentAlt";
    private final static Logger LOG = Logger.getLogger(BackupDaemon.class.getName());
    private final static Pattern IMEAD_BACKUP_FILE = Pattern.compile("IMEAD(?:-(.+?))?\\.properties");
    public final static Integer PROCESSING_CHUNK_SIZE = 16;
    private final ToiletBeanAccess beans;

    public BackupDaemon(ToiletBeanAccess beans) {
        this.beans = beans;
    }

    public static enum BackupTypes {
        ARTICLES, COMMENTS, LOCALIZATIONS, FILES;
    }

    @Override
    public void run() {
        cleanupZips();
        backup();
        backupToZip();
    }

    public void cleanupZips() {
        LOG.entering(BackupDaemon.class
                .getName(), "cleanupZips");
        LOG.info("Cleaning up zip backups");
        String master = beans.getImeadValue(MASTER_DIR);
        final String zipStem = getZipStem();
        File zipDir = new File(master);
        List<File> siteZips = Arrays.asList(zipDir.listFiles((File file)
                -> file.getName().startsWith(zipStem) && file.getName().endsWith(".zip")));
        Pattern datePattern = Pattern.compile(".*?(\\d{8})\\.zip$");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        // set files last modified dates by looking at filename timestamp
        for (File z : siteZips) {
            try {
                GregorianCalendar zipDay = new GregorianCalendar();
                Matcher dateMatcher = datePattern.matcher(z.getName());
                dateMatcher.find();
                zipDay.setTime(dateFormat.parse(dateMatcher.group(1)));
                GregorianCalendar actual = new GregorianCalendar();
                actual.setTime(new java.util.Date(z.lastModified()));
                if (zipDay.get(Calendar.YEAR) != actual.get(Calendar.YEAR)
                        || zipDay.get(Calendar.MONTH) != actual.get(Calendar.MONTH)
                        || zipDay.get(Calendar.DAY_OF_MONTH) != actual.get(Calendar.DAY_OF_MONTH)) {
                    z.setLastModified(zipDay.getTimeInMillis());
                }
            } catch (ParseException ex) {
            }
        }
        // sort by last modified date
        Collections.sort(siteZips, (File f, File g)
                -> Long.compare(f.lastModified(), g.lastModified()));
        // keep 1 backup per month for 2 years
        GregorianCalendar monthlies = new GregorianCalendar();
        monthlies.add(Calendar.YEAR, -2);
        monthlies.set(Calendar.DAY_OF_MONTH, 1);
        monthlies.set(Calendar.HOUR_OF_DAY, 0);
        monthlies.set(Calendar.MINUTE, 0);
        monthlies.set(Calendar.SECOND, 0);
        monthlies.set(Calendar.MILLISECOND, 0);
        // keep daily backups for 1 month
        GregorianCalendar dailies = new GregorianCalendar();
        dailies.add(Calendar.MONTH, -1);
        dailies.set(Calendar.HOUR_OF_DAY, 0);
        dailies.set(Calendar.MINUTE, 0);
        dailies.set(Calendar.SECOND, 0);
        dailies.set(Calendar.MILLISECOND, 0);
        for (File f : siteZips) {
            if (f.lastModified() >= dailies.getTimeInMillis()) {
                // this is a daily backup, watch for next day
                dailies.add(Calendar.DAY_OF_MONTH, 1);
            } else if (f.lastModified() >= monthlies.getTimeInMillis()) {
                // this is a monthly backup, watch for next month
                monthlies.add(Calendar.MONTH, 1);
            } else {
                // not a daily or monthly, so delete
                LOG.log(Level.INFO, "Deleting backup {0}", f.getName());
                f.delete();
            }
        }
        LOG.exiting(BackupDaemon.class
                .getName(), "cleanupZips");
    }

    /**
     * dumps articles, comments, and uploaded files to a directory (specified by
     * site_backup key in beans.getImead().keyValue)
     */
    public void backup() {
        LOG.entering(BackupDaemon.class
                .getName(), "backup");
        LOG.info("Backup procedure initiating");
        beans.getImead().evict();
        beans.getArts().evict();
        beans.getFile().evict();
        if (null == beans.getImeadValue(MASTER_DIR)) {
            throw new IllegalArgumentException(MASTER_DIR + " not configured.");
        }
        final String master = beans.getImeadValue(MASTER_DIR) + getZipStem() + File.separator;
        String content = master + CONTENT_DIR + File.separator;
        File contentDir = new File(content);
        if (!contentDir.exists()) {
            contentDir.mkdirs();
        }
        final OffsetDateTime localNow = OffsetDateTime.now();
        final Queue<Future> backupTasks = new ConcurrentLinkedQueue<>();
        backupTasks.add(beans.getExec().submit(() -> {
            beans.getFile().processArchive((f) -> {
                if (shouldFileBeBackedUp(f)) {
                    String fn = content + f.getFilename();
                    try {
                        LOG.log(Level.FINE, "Writing file {0}", f.getFilename());
                        writeFile(fn, f.getAtime(), f.getFiledata());
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, "Error writing " + f.getFilename(), ex);
                        beans.getError().logException(null, "Backup failure", ex.getMessage() + SecurityRepo.NEWLINE + "while backing up " + fn, null);
                    }
                }
            }, false);
        }));
        backupTasks.add(beans.getExec().submit(() -> {
            try {
                LOG.log(Level.FINE, "Writing Articles.rss");
                writeFile(master + File.separator + ArticleRss.NAME, localNow, xmlToBytes(new ArticleRss().createFeed(beans, null, null)));
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Error writing Articles.rss to file: " + master + File.separator + ArticleRss.NAME, ex);
                beans.getError().logException(null, "Backup failure", "Error writing Articles.rss to file: " + master + File.separator + ArticleRss.NAME, ex);
            }
        }));
        backupTasks.add(beans.getExec().submit(() -> {
            try {
                LOG.log(Level.FINE, "Writing Comments.rss");
                writeFile(master + File.separator + CommentRss.NAME, localNow, xmlToBytes(Feed.refreshFeed(Arrays.asList(new CommentRss().createChannel(beans, beans.getComms().getAll(null))))));
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Error writing Comments.rss to file: " + master + File.separator + CommentRss.NAME, ex);
                beans.getError().logException(null, "Backup failure", "Error writing Comments.rss to file: " + master + File.separator + CommentRss.NAME, ex);
            }
        }));
        backupTasks.add(beans.getExec().submit(() -> {
            @SuppressWarnings("StringBufferMayBeStringBuilder")
            StringBuffer mimes = new StringBuffer(1000000);
            beans.getFile().processArchive((f) -> {
                if (shouldFileBeBackedUp(f)) {
                    mimes.append(f.getFilename()).append(": ").append(f.getMimetype()).append('\n');
                }
            }, false);
            try {
                LOG.log(Level.FINE, "Writing mimes.txt");
                writeFile(master + MIMES_TXT, localNow, mimes.toString().getBytes("UTF-8"));
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Error writing mimes.txt: " + master + MIMES_TXT, ex);
                beans.getError().logException(null, "Backup failure", "Error writing mimes.txt: " + master + MIMES_TXT, ex);
            }
        }));
        backupTasks.add(beans.getExec().submit(() -> {
            Map<Locale, Properties> propMap = beans.getImead().getProperties();
            Map<String, String> localeFiles = new HashMap<>(propMap.size() * 2);
            for (Locale l : propMap.keySet()) {
                try {
                    StringWriter propertiesContent = new StringWriter(10000);
                    propMap.get(l).store(propertiesContent, null);
                    String name = l != Locale.ROOT ? "IMEAD-" + l.toLanguageTag() + ".properties" : "IMEAD.properties";
                    writeFile(master + name, localNow, propertiesContent.toString().getBytes("UTF-8"));
                } catch (IOException ex) {
                    beans.getError().logException(null, "Can't backup properties", "Can't backup properties for locale " + l.toLanguageTag(), ex);
                }
            }
            return localeFiles;
        }));
        UtilStatic.finish(backupTasks);
        LOG.info("Backup procedure finished");
        LOG.exiting(BackupDaemon.class
                .getName(), "backup");
    }

    /**
     * takes a zip file backup of the site and restores everything
     *
     * @param zip
     */
    public void restoreFromZip(ZipInputStream zip) {
        final Map<String, String> mimes = new ConcurrentHashMap<>(1000);
        final Queue<Future> fileTasks = new ConcurrentLinkedQueue<>();
        final Queue<Future> altTasks = new ConcurrentLinkedQueue<>();
        Future<Queue<Future<Article>>> masterArticleTask = null;
        Future<List<Comment>> masterCommentTask = null;
        List<Fileupload> files = Collections.synchronizedList(new ArrayList<>(PROCESSING_CHUNK_SIZE * 2));
        try {
            for (ZipEntry zipFile = zip.getNextEntry(); zipFile != null; zipFile = zip.getNextEntry()) {
                if (zipFile.isDirectory()) {
                    continue;
                }
                LOG.log(Level.INFO, "Processing file: {0}", zipFile.getName());
                switch (zipFile.getName()) {
                    case ArticleRss.NAME:
                        final InputStream articleStream = new ByteArrayInputStream(FileUtil.getByteArray(zip));
                        masterArticleTask = beans.getExec().submit(() -> {
                            final Queue<Future<Article>> conversionTasks = new ConcurrentLinkedQueue<>();
                            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                            dbf.setIgnoringElementContentWhitespace(true);
                            Element articleRoot = dbf.newDocumentBuilder().parse(articleStream).getDocumentElement();
                            for (Node item : new XmlNodeSearcher(articleRoot.getFirstChild(), "item")) {
                                conversionTasks.add(beans.getExec().submit(() -> {
                                    Article art = new Article();
                                    art.setComments(Boolean.FALSE);
                                    for (Node component : new XmlNodeSearcher(item, "link")) {
                                        art.setArticleid(Integer.decode(IndexFetcher.getArticleIdFromURI(component.getTextContent().trim())));
                                    }
                                    for (Node component : new XmlNodeSearcher(item, "title")) {
                                        art.setArticletitle(component.getTextContent().trim());
                                    }
                                    for (Node component : new XmlNodeSearcher(item, "pubDate")) {
                                        art.setPosted(FeedBucket.parseTimeFormat(DateTimeFormatter.ISO_OFFSET_DATE_TIME, component.getTextContent().trim()));
                                    }
                                    for (Node component : new XmlNodeSearcher(item, "category")) {
                                        art.setSectionid(new Section(null, component.getTextContent().trim()));
                                    }
                                    for (Node component : new XmlNodeSearcher(item, "comments")) {
                                        art.setComments(Boolean.TRUE);
                                    }
                                    for (Node component : new XmlNodeSearcher(item, "author")) {
                                        art.setPostedname(component.getTextContent().trim());
                                    }
                                    for (Node component : new XmlNodeSearcher(item, ToiletRssItem.TAB_METADESC_ELEMENT_NAME)) {
                                        try {
                                            art.setDescription(URLDecoder.decode(component.getTextContent().trim(), "UTF-8"));
                                        } catch (UnsupportedEncodingException enc) {
                                            throw new JVMNotSupportedError(enc);
                                        }
                                    }
                                    for (Node component : new XmlNodeSearcher(item, ToiletRssItem.MARKDOWN_ELEMENT_NAME)) {
                                        art.setPostedmarkdown(component.getTextContent().trim());
                                    }
                                    for (Node component : new XmlNodeSearcher(item, "description")) {
                                        art.setPostedhtml(component.getTextContent().trim());
                                    }
                                    for (Node component : new XmlNodeSearcher(item, ToiletRssItem.SUGGESTION_ELEMENT_NAME)) {
                                        art.setSuggestion(component.getTextContent().trim());
                                    }
                                    for (Node component : new XmlNodeSearcher(item, ToiletRssItem.SUMMARY_ELEMENT_NAME)) {
                                        art.setSummary(component.getTextContent().trim());
                                    }
                                    for (Node component : new XmlNodeSearcher(item, ToiletRssItem.IMAGEURL_ELEMENT_NAME)) {
                                        art.setImageurl(component.getTextContent().trim());
                                    }
                                    art.setCommentCollection(null);
                                    // conversion
                                    if (null == art.getPostedhtml() || null == art.getPostedmarkdown()) {
                                        ArticleProcessor.convert(art);
                                    }
                                    return art;
                                }));
                            }
                            beans.getArts().delete(null);
                            return conversionTasks;
                        });
                        break;
                    case CommentRss.NAME:
                        final InputStream commentStream = new ByteArrayInputStream(FileUtil.getByteArray(zip));
                        masterCommentTask = beans.getExec().submit(() -> {
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
                                    comm.setPosted(FeedBucket.parseTimeFormat(DateTimeFormatter.ISO_OFFSET_DATE_TIME, component.getTextContent().trim()));
                                }
                                for (Node component : new XmlNodeSearcher(item, "author")) {
                                    comm.setPostedname(component.getTextContent());
                                }
                                for (Node component : new XmlNodeSearcher(item, "link")) {
                                    comm.setArticleid(new Article(Integer.decode(IndexFetcher.getArticleIdFromURI(component.getTextContent()))));
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
                        altTasks.add(beans.getExec().submit(() -> {
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
                        Matcher imeadBackup = IMEAD_BACKUP_FILE.matcher(zipFile.getName());
                        if (imeadBackup.find()) {
                            String properties = new String(FileUtil.getByteArray(zip));
                            altTasks.add(beans.getExec().submit(() -> {
                                String locale = null != imeadBackup.group(1) ? imeadBackup.group(1) : "";
                                Properties props = new Properties();
                                try {
                                    props.load(new StringReader(properties));
                                    ArrayList<Localization> localizations = new ArrayList<>(props.size() * 2);
                                    for (String key : props.stringPropertyNames()) {
                                        localizations.add(new Localization(locale, key, props.getProperty(key)));
                                    }
                                    beans.getImead().upsert(localizations);
                                } catch (IOException ex) {
                                    beans.getError().logException(null, "Can't restore properties", "Can't restore properties for locale " + locale, ex);
                                }
                                return props;
                            }));
                        } else if (zipFile.getName().startsWith(CONTENT_DIR)) {
                            Fileupload incomingFile = new Fileupload();
                            incomingFile.setAtime(Instant.ofEpochMilli(zipFile.getTime()).atOffset(ZoneOffset.UTC));
                            incomingFile.setFilename(zipFile.getName().replace(CONTENT_DIR + "/", ""));
                            incomingFile.setMimetype(zipFile.getComment());
                            incomingFile.setFiledata(FileUtil.getByteArray(zip));
                            fileTasks.add(beans.getExec().submit(() -> {
                                incomingFile.setEtag(HashUtil.getSHA256Hash(incomingFile.getFiledata()));
                                if (null == incomingFile.getMimetype() && mimes.containsKey(incomingFile.getFilename())) {
                                    incomingFile.setMimetype(mimes.get(incomingFile.getFilename()));
                                    mimes.remove(incomingFile.getFilename());
                                } else if (null == incomingFile.getMimetype()) {
                                    incomingFile.setMimetype(FileRepository.DEFAULT_MIME_TYPE);
                                }
                                Fileupload existingFile = beans.getFile().get(incomingFile.getFilename());
                                if (null == existingFile || !incomingFile.getEtag().equals(existingFile.getEtag())) {
                                    LOG.log(Level.INFO, "Existing file different, updating {0}", incomingFile.getFilename());
                                    files.add(incomingFile);
                                    synchronized (files) {
                                        if (files.size() > PROCESSING_CHUNK_SIZE) {
                                            final List<Fileupload> fileChunk = new ArrayList<>(files);
                                            altTasks.add(beans.getExec().submit(() -> {
                                                beans.getFile().upsert(fileChunk);
                                            }));
                                            files.clear();
                                        }
                                    }
                                }
                                return null;
                            }));
                        }
                        break;
                }
            }
            zip.close();
        } catch (IOException ix) {
            throw new RuntimeException(ix);
        }
        UtilStatic.finish(altTasks).clear();
        if (!fileTasks.isEmpty()) {
            UtilStatic.finish(fileTasks).clear();
            altTasks.add(beans.getExec().submit(() -> {
                if (!files.isEmpty()) {
                    final List<Fileupload> fileChunk = new ArrayList<>(files);
                    altTasks.add(beans.getExec().submit(() -> {
                        beans.getFile().upsert(fileChunk);
                    }));
                    files.clear();
                }
                beans.getFile().processArchive((file) -> {
                    if (mimes.containsKey(file.getFilename()) && !file.getMimetype().equals(mimes.get(file.getFilename()))) {
                        file.setMimetype(mimes.get(file.getFilename()));
                    }
                    String fileUrl = BaseFileServlet.getImmutableURL(beans.getImeadValue(SecurityRepo.BASE_URL), file);
                    if (!fileUrl.equals(file.getUrl())) {
                        file.setUrl(fileUrl);
                    }
                    files.add(file);
                    synchronized (files) {
                        if (files.size() > PROCESSING_CHUNK_SIZE) {
                            final List<Fileupload> fileChunk = new ArrayList<>(files);
                            altTasks.add(beans.getExec().submit(() -> {
                                beans.getFile().upsert(fileChunk);
                            }));
                            files.clear();
                        }
                    }
                }, false);
                if (!files.isEmpty()) {
                    final List<Fileupload> fileChunk = new ArrayList<>(files);
                    altTasks.add(beans.getExec().submit(() -> {
                        beans.getFile().upsert(fileChunk);
                    }));
                    files.clear();
                }
            }, true));
        }
        try {
            if (null != masterArticleTask) {
                List<Article> articles = new ArrayList<>();
                for (Future<Article> task : masterArticleTask.get()) {
                    Article art = task.get();
                    if (null != art) {
                        articles.add(art);
                    }
                }
                masterArticleTask.get().clear();
                articles.sort((Article a, Article r) -> {
                    if (null != a.getArticleid() && null != r.getArticleid()) {
                        return a.getArticleid() - r.getArticleid();
                    } else if (null != a.getArticleid()) {
                        return 1;
                    } else if (null != r.getArticleid()) {
                        return -1;
                    }
                    return 0;
                });
                Queue<Future<Article>> articleTasks = new ConcurrentLinkedQueue<>();
                UtilStatic.finish(altTasks).clear();
                for (Article art : articles) {
                    articleTasks.add(beans.getExec().submit(new ArticleProcessor(beans, art)));
                }
                articles.clear();
                for (Future<Article> f : articleTasks) {
                    while (true) {
                        try {
                            if (articles.isEmpty() || null != f.get(1L, TimeUnit.MILLISECONDS)) {
                                articles.add(f.get());
                                f.get().setArticleid(null);
                                break;
                            }
                        } catch (TimeoutException t) {
                            beans.getArts().upsert(articles);
                            articles.clear();
                        } catch (Exception ex) {
                            LOG.log(Level.SEVERE, "Error inserting articles", ex);
                        }
                    }
                }
                beans.getArts().upsert(articles);
                if (null != masterCommentTask) {
                    beans.getComms().upsert((List<Comment>) masterCommentTask.get());
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
        UtilStatic.finish(altTasks).clear();
        beans.reset();
        beans.getExec().submit(() -> {
            beans.getArts().refreshSearch();
            beans.getFile().processArchive((f) -> {
                beans.getExec().submit(new Brotlier(beans, f));
                beans.getExec().submit(new Gzipper(beans, f));
            }, false);
        });
    }

    /**
     * stuffs everything to a zip file in the backup directory (specified by
     * site_backup key in beans.getImead().keyValue) can be used for the import
     * functionality
     *
     * @see BackupDaemon.createZip(OutputStream wrapped)
     */
    public void backupToZip() {
        LOG.entering(BackupDaemon.class
                .getName(), "backupToZip");
        LOG.info("Backup to zip procedure initiating");
        String master = beans.getImeadValue(MASTER_DIR);
        String zipName = getZipName();
        String fn = master + zipName;
        try (FileOutputStream out = new FileOutputStream(fn)) {
            createZip(out, Arrays.asList(BackupTypes.values()));
            LOG.info("Backup to zip procedure finished");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error writing zip file: " + fn, ex);
            beans.getError().logException(null, "Backup failure", ex.getMessage() + SecurityRepo.NEWLINE + "while backing up " + fn, null);
        }
        LOG.exiting(BackupDaemon.class
                .getName(), "backupToZip");
    }

    /**
     * stuffs types of things into a zip and dumps it on the given OutputStream.
     * can be used to save to file or put on a response
     *
     * @param wrapped put zip on this
     * @param types put zip on this
     */
    public void createZip(OutputStream wrapped, List<BackupTypes> types) {
        final Queue<Future> altTasks = new ConcurrentLinkedQueue<>();
        OffsetDateTime localNow = OffsetDateTime.now();
        try (ZipOutputStream zip = new ZipOutputStream(wrapped)) {
            if (types.contains(BackupTypes.ARTICLES)) {
                altTasks.add(beans.getExec().submit(() -> {
                    byte[] xmlBytes = xmlToBytes(new ArticleRss().createFeed(beans, null, null));
                    try {
                        addFileToZip(zip, ArticleRss.NAME, "text/xml", localNow, xmlBytes);
                    } catch (IOException ix) {
                        throw new RuntimeException(ix);
                    }
                }));
            }
            if (types.contains(BackupTypes.COMMENTS)) {
                altTasks.add(beans.getExec().submit(() -> {
                    byte[] xmlBytes = xmlToBytes(Feed.refreshFeed(Arrays.asList(new CommentRss().createChannel(beans, beans.getComms().getAll(null)))));
                    try {
                        addFileToZip(zip, CommentRss.NAME, "text/xml", localNow, xmlBytes);
                    } catch (IOException ix) {
                        throw new RuntimeException(ix);
                    }
                }));
            }
            if (types.contains(BackupTypes.LOCALIZATIONS)) {
                altTasks.add(beans.getExec().submit(() -> {
                    Map<Locale, Properties> propMap = beans.getImead().getProperties();
                    Map<String, String> localeFiles = new HashMap<>(propMap.size() * 2);
                    for (Locale l : propMap.keySet()) {
                        try {
                            StringWriter propertiesContent = new StringWriter(10000);
                            propMap.get(l).store(propertiesContent, null);
                            String localeString = l != Locale.ROOT ? l.toLanguageTag() : "";
                            localeFiles.put(localeString, propertiesContent.toString());
                        } catch (IOException ex) {
                            beans.getError().logException(null, "Can't backup properties", "Can't backup properties for locale " + l.toLanguageTag(), ex);
                        }
                    }
                    for (Map.Entry<String, String> locale : localeFiles.entrySet()) {
                        String name = 0 == locale.getKey().length() ? "IMEAD.properties" : "IMEAD-" + locale.getKey() + ".properties";
                        addFileToZip(zip, name, "text/plain", localNow, locale.getValue().getBytes());
                    }
                    return localeFiles;
                }));
            }
            if (types.contains(BackupTypes.FILES)) {
                altTasks.add(beans.getExec().submit(() -> {
                    @SuppressWarnings("StringBufferMayBeStringBuilder")
                    StringBuffer mimes = new StringBuffer(1000000);
                    beans.getFile().processArchive((f) -> {
                        if (shouldFileBeBackedUp(f)) {
                            mimes.append(f.getFilename()).append(": ").append(f.getMimetype()).append('\n');
                        }
                    }, false);
                    try {
                        addFileToZip(zip, MIMES_TXT, "text/plain", localNow, mimes.toString().getBytes("UTF-8"));
                    } catch (IOException ix) {
                        throw new RuntimeException(ix);
                    }
                }));
                altTasks.add(beans.getExec().submit(() -> {
                    //List<String> directories = new ArrayList<>();
                    beans.getFile().processArchive((f) -> {
                        if (shouldFileBeBackedUp(f)) {
                            try {
                                addFileToZip(zip, CONTENT_DIR + "/" + f.getFilename(), f.getMimetype(), f.getAtime(), f.getFiledata());
//                                if (null != f.getGzipdata()) {
//                                    addFileToZip(zip, CONTENT_ALT_DIR + "/" + f.getFilename() + ".gz", f.getMimetype(), f.getAtime(), f.getGzipdata());
//                                }
//                                if (null != f.getBrdata()) {
//                                    addFileToZip(zip, CONTENT_ALT_DIR + "/" + f.getFilename() + ".br", f.getMimetype(), f.getAtime(), f.getBrdata());
//                                }
                            } catch (IOException ix) {
                                throw new RuntimeException(ix);
                            }
                        }
                    }, false);
                }));
            }
            UtilStatic.finish(altTasks);
        } catch (IOException | RuntimeException rx) {
            LOG.log(Level.SEVERE, null, rx);
        }
    }

    /**
     *
     * @return site title (a-zA-Z_0-9 only) + today's date (year + month + day)
     * + ".zip"
     */
    public String getZipName() {
        return getZipStem() + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'.zip'"));
    }

    /**
     * @return site title (a-zA-Z_0-9 only)
     */
    public String getZipStem() {
        Matcher originMatcher = SecurityRepo.ORIGIN_PATTERN.matcher(beans.getImead().getValue(SecurityRepo.BASE_URL));
        originMatcher.find();
        return originMatcher.group(3).replace(".", "-");
    }

    /**
     * adds a file to the given ZIP stream
     *
     * @param zip zip stream
     * @param name of file
     * @param mime file type
     * @param time modified time (from Date.getTime), defaults to -1
     * @param content file contents
     * @return the created entry
     * @throws IOException something went wrong
     */
    private ZipEntry addFileToZip(ZipOutputStream zip, String name, String mime, OffsetDateTime time, byte[] content) throws IOException {
        ZipEntry out = new ZipEntry(name);
        out.setMethod(ZipEntry.DEFLATED);
        out.setTime(time.toInstant().toEpochMilli());
        out.setComment(mime);
        synchronized (zip) {
            zip.putNextEntry(out);
            zip.write(content);
            zip.closeEntry();
        }
        return out;
    }

    /**
     * makes a really big "string" from the given XML object
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
    private void writeFile(String filename, OffsetDateTime time, byte[] content) throws IOException {
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
            try (FileOutputStream tempStr = new FileOutputStream(f, false)) {
                tempStr.write(content);
            }
        } else if (!Instant.ofEpochMilli(f.lastModified()).equals(time.toInstant())) {
            try (FileOutputStream tempStr = new FileOutputStream(f, false)) {
                tempStr.write(content);
            }
        }
    }

    /**
     * filter out any temporary cached files. currently none.
     *
     * @param f
     * @return true if this should be included in backups
     */
    private static boolean shouldFileBeBackedUp(Fileupload f) {
        return true;
    }
}
