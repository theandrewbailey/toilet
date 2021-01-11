package toilet.bean;

import toilet.IndexFetcher;
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
import java.util.Date;
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
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
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
import libWebsiteTools.rss.SimpleRssFeed;
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
import toilet.servlet.ToiletServlet;

/**
 * so I can sleep at night, knowing my stuff is being backed up
 *
 * @author alpha
 */
@Startup
@Singleton
@LocalBean
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class BackupDaemon implements Runnable {

    public final static String MIMES_TXT = "mimes.txt";
    private final static String MASTER_DIR = "file_backup";
    private final static String CONTENT_DIR = "content";
    private final static Logger LOG = Logger.getLogger(BackupDaemon.class.getName());
    private final static Pattern IMEAD_BACKUP_FILE = Pattern.compile("IMEAD(?:-(.+?))?\\.properties");
    private final static Integer PROCESSING_CHUNK_SIZE = 16;
    @EJB
    private ToiletBeanAccess beans;

    public static enum BackupTypes {
        ARTICLES, COMMENTS, LOCALIZATIONS, FILES;
    }

    @Override
    public void run() {
        backup();
    }

    /**
     * dumps articles, comments, and uploaded files to a directory (specified by
     * site_backup key in beans.getImead().keyValue)
     */
    @Schedule(persistent = false, hour = "1")
    @SuppressWarnings("StringBufferMayBeStringBuilder")
    public void backup() {
        LOG.entering(BackupDaemon.class.getName(), "backup");
        LOG.info("Backup procedure initiating");
        beans.getImead().evict();
        beans.getArts().evict();
        beans.getFile().evict();
        Future<Boolean> zipTask = beans.getExec().submit(() -> {
            backupToZip();
            return true;
        });
        String master = beans.getImeadValue(MASTER_DIR);
        if (null == master) {
            throw new IllegalArgumentException(MASTER_DIR + " not configured.");
        }
        String content = master + CONTENT_DIR + File.separator;
        File contentDir = new File(content);
        if (!contentDir.exists()) {
            contentDir.mkdirs();
        }
        StringBuffer mimes = new StringBuffer(1048576);
        beans.getFile().processArchive((f) -> {
            if (shouldFileBeBackedUp(f)) {
                String fn = content + f.getFilename();
                try {   // file exists in db, different hash or file not backed up yet
                    LOG.log(Level.FINE, "Writing file {0}", f.getFilename());
                    writeFile(fn, f.getFiledata());
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Error writing " + f.getFilename(), ex);
                    beans.getError().logException(null, "Backup failure", ex.getMessage() + SecurityRepo.NEWLINE + "while backing up " + fn, null);
                }
                mimes.append(f.getFilename()).append(": ").append(f.getMimetype()).append('\n');
            }
        }, false);
        try {
            LOG.log(Level.FINE, "Writing mimes.txt");
            writeFile(master + MIMES_TXT, mimes.toString().getBytes("UTF-8"));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error writing mimes.txt: " + master + MIMES_TXT, ex);
            beans.getError().logException(null, "Backup failure", "Error writing mimes.txt: " + master + MIMES_TXT, ex);
        }
        try {
            LOG.log(Level.FINE, "Writing Articles.rss");
            writeFile(master + File.separator + ArticleRss.NAME, xmlToBytes(new ArticleRss(beans).createFeed(null, null)));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error writing Articles.rss to file: " + master + File.separator + ArticleRss.NAME, ex);
            beans.getError().logException(null, "Backup failure", "Error writing Articles.rss to file: " + master + File.separator + ArticleRss.NAME, ex);
        }
        try {
            LOG.log(Level.FINE, "Writing Comments.rss");
            writeFile(master + File.separator + CommentRss.NAME, xmlToBytes(SimpleRssFeed.refreshFeed(Arrays.asList(new CommentRss(beans).createChannel(beans.getComms().getAll(null))))));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error writing Comments.rss to file: " + master + File.separator + CommentRss.NAME, ex);
            beans.getError().logException(null, "Backup failure", "Error writing Comments.rss to file: " + master + File.separator + CommentRss.NAME, ex);
        }
        try {
            zipTask.get();
        } catch (InterruptedException | ExecutionException ex) {
        }
        LOG.info("Backup procedure finished");
        LOG.exiting(BackupDaemon.class.getName(), "backup");
    }

    /**
     * takes a zip file backup of the site and restores everything
     *
     * @param zip
     */
    public void restoreFromZip(ZipInputStream zip) {
        final Map<String, String> mimes = new ConcurrentHashMap<>(1000);
        final Queue<Future<Fileupload>> fileTasks = new ConcurrentLinkedQueue<>();
        final Queue<Future> altTasks = new ConcurrentLinkedQueue<>();
        Future<Queue<Future<Article>>> masterArticleTask = null;
        Future<List<Comment>> masterCommentTask = null;
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
                                        art.setPosted(new SimpleDateFormat(FeedBucket.TIME_FORMAT).parse(component.getTextContent().trim()));
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
                                    comm.setPosted(new SimpleDateFormat(FeedBucket.TIME_FORMAT).parse(component.getTextContent()));
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
                        } else {
                            Fileupload incomingFile = new Fileupload();
                            incomingFile.setAtime(new Date(zipFile.getTime()));
                            incomingFile.setFilename(zipFile.getName().replace("content/", ""));
                            incomingFile.setMimetype(zipFile.getComment());
                            incomingFile.setFiledata(FileUtil.getByteArray(zip));
                            fileTasks.add(beans.getExec().submit(() -> {
                                incomingFile.setEtag(HashUtil.getSHA256Hash(incomingFile.getFiledata()));
                                if (null == incomingFile.getMimetype() && mimes.containsKey(incomingFile.getFilename())) {
                                    incomingFile.setMimetype(mimes.get(incomingFile.getFilename()));
                                    mimes.remove(incomingFile.getFilename());
                                } else if (null == incomingFile.getMimetype()) {
                                    incomingFile.setMimetype(FileRepo.DEFAULT_MIME_TYPE);
                                }
                                Fileupload existingFile = beans.getFile().get(incomingFile.getFilename());
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
        } catch (IOException ix) {
            throw new RuntimeException(ix);
        }
        if (!fileTasks.isEmpty()) {
            UtilStatic.finish(altTasks).clear();
            altTasks.add(beans.getExec().submit(() -> {
                ArrayList<Fileupload> files = new ArrayList<>(PROCESSING_CHUNK_SIZE);
                for (Future<Fileupload> task : fileTasks) {
                    try {
                        Fileupload file = task.get();
                        if (null != file) {
                            if (mimes.containsKey(file.getFilename())) {
                                file.setMimetype(mimes.get(file.getFilename()));
                            }
                            String fileUrl = BaseFileServlet.getImmutableURL(beans.getImeadValue(SecurityRepo.BASE_URL), file);
                            file.setUrl(fileUrl);
                            files.add(file);
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        LOG.log(Level.SEVERE, "Tried to process a bunch of files, but couldn't.", ex);
                    }
                    if (files.size() > PROCESSING_CHUNK_SIZE) {
                        final ArrayList<Fileupload> fileChunk = new ArrayList<>(files);
                        altTasks.add(beans.getExec().submit(() -> {
                            beans.getFile().upsert(fileChunk);
                        }));
                        files.clear();
                    }
                }
                altTasks.add(beans.getExec().submit(() -> {
                    beans.getFile().upsert(files);
                }));
                fileTasks.clear();
            }, false));
        }
        try {
            if (null != masterArticleTask) {
                List<Article> articles = new ArrayList<>();
                UtilStatic.finish(UtilStatic.finish(altTasks)).clear();
                for (Future<Article> task : masterArticleTask.get()) {
                    Article art = task.get();
                    if (null != art) {
                        articles.add(art);
                    }
                }
                masterArticleTask.get().clear();
                // to do a job right, do it yourself.
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
        LOG.entering(BackupDaemon.class.getName(), "backupToZip");
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
        LOG.exiting(BackupDaemon.class.getName(), "backupToZip");
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
        long time = new Date().getTime();
        try (ZipOutputStream zip = new ZipOutputStream(wrapped)) {
            if (types.contains(BackupTypes.ARTICLES)) {
                altTasks.add(beans.getExec().submit(() -> {
                    byte[] xmlBytes = xmlToBytes(new ArticleRss(beans).createFeed(null, null));
                    synchronized (zip) {
                        try {
                            addFileToZip(zip, ArticleRss.NAME, "text/xml", time, xmlBytes);
                        } catch (IOException ix) {
                            throw new RuntimeException(ix);
                        }
                    }
                }));
            }
            if (types.contains(BackupTypes.COMMENTS)) {
                altTasks.add(beans.getExec().submit(() -> {
                    byte[] xmlBytes = xmlToBytes(SimpleRssFeed.refreshFeed(Arrays.asList(new CommentRss(beans).createChannel(beans.getComms().getAll(null)))));
                    synchronized (zip) {
                        try {
                            addFileToZip(zip, CommentRss.NAME, "text/xml", time, xmlBytes);
                        } catch (IOException ix) {
                            throw new RuntimeException(ix);
                        }
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
                    synchronized (zip) {
                        for (Map.Entry<String, String> locale : localeFiles.entrySet()) {
                            String name = 0 == locale.getKey().length() ? "IMEAD.properties" : "IMEAD-" + locale.getKey() + ".properties";
                            addFileToZip(zip, name, "text/plain", time, locale.getValue().getBytes());
                        }
                    }
                    return localeFiles;
                }));
            }
            if (types.contains(BackupTypes.FILES)) {
                altTasks.add(beans.getExec().submit(() -> {
                    StringBuilder mimes = new StringBuilder(1048576);
                    beans.getFile().processArchive((f) -> {
                        if (shouldFileBeBackedUp(f)) {
                            mimes.append(f.getFilename()).append(": ").append(f.getMimetype()).append('\n');
                        }
                    }, false);
                    synchronized (zip) {
                        try {
                            addFileToZip(zip, MIMES_TXT, "text/plain", time, mimes.toString().getBytes("UTF-8"));
                        } catch (IOException ix) {
                            throw new RuntimeException(ix);
                        }
                    }
                }));
                altTasks.add(beans.getExec().submit(() -> {
                    List<String> directories = new ArrayList<>();
                    beans.getFile().processArchive((f) -> {
                        if (shouldFileBeBackedUp(f)) {
                            try {
                                String insideFilename = CONTENT_DIR + "/" + f.getFilename();
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
                                    addFileToZip(zip, insideFilename, f.getMimetype(), f.getAtime().getTime(), f.getFiledata());
                                }
                            } catch (IOException ix) {
                                throw new RuntimeException(ix);
                            }
                        }
                    }, true);
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
        return beans.getImead().getLocal(ToiletServlet.SITE_TITLE, "en").replaceAll("[^\\w]", "") + new SimpleDateFormat("yyyyMMdd'.zip'").format(new Date());
    }

    /**
     * adds a file to the given ZIP stream
     *
     * @param zip zip stream
     * @param name of file
     * @param mime file type
     * @param time modified time (from Date.getTime), defaults to -1
     * @param content file contents
     * @throws IOException something went wrong
     */
    private void addFileToZip(ZipOutputStream zip, String name, String mime, long time, byte[] content) throws IOException {
        ZipEntry out = new ZipEntry(name);
        out.setMethod(ZipEntry.DEFLATED);
        out.setTime(time);
        out.setComment(mime);
        zip.putNextEntry(out);
        zip.write(content);
        zip.closeEntry();
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
        return true;
    }
}
