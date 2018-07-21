package toilet.bean;

import libWebsiteTools.file.FileRepo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import libOdyssey.bean.ExceptionRepo;
import libOdyssey.bean.GuardHolder;
import libWebsiteTools.HashUtil;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.Markdowner;
import libWebsiteTools.XmlNodeSearcher;
import libWebsiteTools.file.Brotlier;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.file.Gzipper;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.FeedBucket;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import toilet.ArticlePreProcessor;
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
public class BackupDaemon {

    public final static String MIMES_TXT = "mimes.txt";
    private final static String MASTER_DIR = "site_backup";
    private final static String CONTENT_DIR = "content";
    private final static Logger LOG = Logger.getLogger(BackupDaemon.class.getName());
    @EJB
    private EntryRepo entry;
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
    public void backup() {
        LOG.entering(BackupDaemon.class.getName(), "backup");
        LOG.info("Backup procedure initiating");
        imead.populateCache();
        entry.evict();
        fileRepo.evict();
        String master = imead.getValue(MASTER_DIR);
        String content = master + CONTENT_DIR + File.separator;
        File contentDir = new File(content);
        if (!contentDir.exists()) {
            contentDir.mkdirs();
        }
        List<Fileupload> dbfiles = fileRepo.getUploadArchive();
        StringBuilder mimes = new StringBuilder(dbfiles.size() * 40);

        for (Fileupload f : dbfiles) {
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

        try {
            LOG.log(Level.FINE, "Writing mimes.txt");
            writeFile(master + MIMES_TXT, mimes.toString().getBytes("UTF-8"));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error writing mimes.txt", ex);
        }
        try {
            LOG.log(Level.FINE, "Writing Articles.rss");
            writeFile(master + File.separator + ArticleRss.NAME, xmlToBytes(new ArticleRss().generateFeed(null)));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error writing Articles.rss to file", ex);
        }
        try {
            LOG.log(Level.FINE, "Writing Comments.rss");
            writeFile(master + File.separator + CommentRss.NAME, xmlToBytes(new CommentRss().generateFeed(null)));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error writing Comments.rss to file", ex);
        }
        LOG.info("Backup procedure finished");
        LOG.exiting(BackupDaemon.class.getName(), "backup");
        backupToZip();
    }

    public void restoreFromZip(ZipInputStream zip) throws Exception {
        HashMap<String, String> mimes = new HashMap<>(1000);
        final Map<Article, String> articles = Collections.synchronizedMap(new LinkedHashMap<>());
        final Map<Comment, Integer> comments = Collections.synchronizedMap(new LinkedHashMap<>());
        final Deque<Future> threadedTasks = new ConcurrentLinkedDeque<>();
        for (ZipEntry zipEntry = zip.getNextEntry(); zipEntry != null; zipEntry = zip.getNextEntry()) {
            if (zipEntry.isDirectory()) {
                continue;
            }
            LOG.log(Level.INFO, "Processing file: {0}", zipEntry.getName());
            switch (zipEntry.getName()) {
                case ArticleRss.NAME:
                    final InputStream articleStream = new ByteArrayInputStream(getByteArray(zip));
                    threadedTasks.add(exec.submit(() -> {
                        {
                            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                            dbf.setIgnoringElementContentWhitespace(true);
                            Element articleRoot = dbf.newDocumentBuilder().parse(articleStream).getDocumentElement();
                            for (Node item : new XmlNodeSearcher(articleRoot.getFirstChild(), "item")) {
                                Article article = new Article(articles.size() + 1);
                                article.setComments(Boolean.FALSE);
                                for (Node component : new XmlNodeSearcher(item, "title")) {
                                    String[] parts = component.getTextContent().split(": ", 2);
                                    article.setArticletitle(parts.length == 2 ? parts[1] : imead.getValue(EntryRepo.DEFAULT_CATEGORY));
                                }
                                for (Node component : new XmlNodeSearcher(item, "pubDate")) {
                                    article.setPosted(new SimpleDateFormat(FeedBucket.TIME_FORMAT).parse(component.getTextContent()));
                                }
                                for (Node component : new XmlNodeSearcher(item, "category")) {
                                    article.setSectionid(new Section(null, component.getTextContent()));
                                }
                                for (Node component : new XmlNodeSearcher(item, "link")) {
                                    article.setEtag(component.getTextContent());
                                }
                                for (Node component : new XmlNodeSearcher(item, "comments")) {
                                    article.setComments(Boolean.TRUE);
                                }
                                for (Node component : new XmlNodeSearcher(item, "author")) {
                                    article.setPostedname(component.getTextContent());
                                }
                                for (Node component : new XmlNodeSearcher(item, ToiletRssItem.TAB_METADESC_ELEMENT_NAME)) {
                                    try {
                                        article.setDescription(URLDecoder.decode(component.getTextContent(), "UTF-8"));
                                    } catch (UnsupportedEncodingException enc) {
                                        throw new JVMNotSupportedError(enc);
                                    }
                                }
                                for (Node component : new XmlNodeSearcher(item, ToiletRssItem.MARKDOWN_ELEMENT_NAME)) {
                                    article.setPostedmarkdown(component.getTextContent());
                                }
                                for (Node component : new XmlNodeSearcher(item, "description")) {
                                    article.setPostedhtml(component.getTextContent());
                                }
                                article.setCommentCollection(null);
                                String section = article.getSectionid() != null ? article.getSectionid().getName() : imead.getValue(EntryRepo.DEFAULT_CATEGORY);
                                articles.put(article, section);
                            }
                            ArrayList<Article> processed = new ArrayList<>(articles.keySet());
                            Collections.reverse(processed);
                            int count = 0;
                            for (Article a : processed) {
                                a.setArticleid(++count);
                            }
                            return articles;
                        }
                    }));
                    break;
                case CommentRss.NAME:
                    final InputStream commentStream = new ByteArrayInputStream(getByteArray(zip));
                    threadedTasks.add(exec.submit(() -> {
                        {
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
                                    comments.put(comm, Integer.decode(StateCache.getArticleIdFromURI(component.getTextContent())));
                                    break;
                                }
                            }
                            for (Comment c : comments.keySet()) {
                                c.setCommentid(null);
                            }
                            return comments;
                        }
                    }));
                    break;
                case BackupDaemon.MIMES_TXT:
                    String mimeString = new String(getByteArray(zip));
                    for (String mimeEntry : mimeString.split("\n")) {
                        try {
                            String[] parts = mimeEntry.split(": ");
                            mimes.put(parts[0], parts[1]);
                        } catch (ArrayIndexOutOfBoundsException a) {
                        }
                    }
                    break;
                default:
                    Fileupload incomingFile = new Fileupload();
                    incomingFile.setAtime(new Date(zipEntry.getTime()));
                    incomingFile.setFilename(zipEntry.getName().replace("content/", ""));
                    incomingFile.setFiledata(getByteArray(zip));
                    threadedTasks.add(exec.submit(() -> {
                        {
                            incomingFile.setMimetype("application/octet-stream");
                            incomingFile.setUrl(imead.getValue(GuardHolder.CANONICAL_URL) + "/content/" + incomingFile.getFilename());
                            incomingFile.setEtag(HashUtil.getHashAsBase64(incomingFile.getFiledata()));
                            if (mimes.containsKey(incomingFile.getFilename())) {
                                incomingFile.setMimetype(mimes.get(incomingFile.getFilename()));
                            }
                            Fileupload existingFile = fileRepo.getFile(incomingFile.getFilename());
                            if (null == existingFile) {
                                fileRepo.addFiles(Arrays.asList(incomingFile));
                            } else if (!incomingFile.getEtag().equals(existingFile.getEtag())) {
                                fileRepo.deleteFile(existingFile.getFileuploadid());
                                LOG.log(Level.INFO, "Existing file different, updating {0}", incomingFile.getFilename());
                                fileRepo.addFiles(Arrays.asList(incomingFile));
                            }
                            return incomingFile;
                        }
                    }));
                    break;
            }
        }
        zip.close();
        UtilStatic.finish(threadedTasks).clear();
        exec.submit(() -> {
            {
                for (String filename : mimes.keySet()) {
                    Fileupload file = fileRepo.getFile(filename);
                    if (null != file && null == file.getMimetype()) {
                        file.setMimetype(mimes.get(filename));
                        fileRepo.upsertFiles(Arrays.asList(file));
                    }
                }
                return mimes;
            }
        });
        for (Article article : articles.keySet()) {
            // conversion
            if (null != article.getPostedmarkdown()) {
                ArticlePreProcessor converter = new ArticlePreProcessor(article, imead, fileRepo);
                threadedTasks.add(exec.submit(converter));
            } else if (null != article.getPostedhtml()) {
                threadedTasks.add(exec.submit(() -> {
                    article.setPostedmarkdown(Markdowner.getMarkdown(article.getPostedhtml()));
                    new ArticlePreProcessor(article, imead, fileRepo).call();
                }));
                LOG.log(Level.INFO, "The markdown for article {0} had to be generated from HTML.", article.getArticletitle());
            } else {
                LOG.log(Level.INFO, "The text for article {0} cannot be recovered.", article.getArticletitle());
            }
        }
        if (!articles.isEmpty()) {
            entry.deleteEverything();
            UtilStatic.finish(threadedTasks).clear();
            for (Article article : articles.keySet()) {
                article.setArticleid(null);
            }
            entry.addArticles(UtilStatic.reverse(articles));
            if (!comments.isEmpty()) {
                entry.addComments(comments);
            }
        }
        util.resetArticleFeed();
        util.resetCommentFeed();
        exec.submit(() -> {
            {
                for (String filename : mimes.keySet()) {
                    Fileupload file = fileRepo.getFile(filename);
                    exec.submit(new Brotlier(file));
                    exec.submit(new Gzipper(file));
                }
            }
        });
    }

    /**
     * stuffs everything to a zip file in the backup directory (specified by
     * site_backup key in imead.keyValue) can be used for the import
     * functionality
     *
     * @see BackupDaemon.generateZip(OutputStream wrapped)
     */
    public void backupToZip() {
        LOG.entering(BackupDaemon.class.getName(), "backupToZip");
        LOG.info("Backup to zip procedure initiating");
        String master = imead.getValue(MASTER_DIR);
        String zipName = getZipName();
        String fn = master + zipName;
        try (FileOutputStream out = new FileOutputStream(fn)) {
            generateZip(out);
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
    public void generateZip(OutputStream wrapped) throws IOException {
        String contentDir = CONTENT_DIR + "/";
        List<String> directories = new ArrayList<>();

        Future<byte[]> articleRss = exec.submit(() -> {
            {
                return xmlToBytes(new ArticleRss().generateFeed(null));
            }
        });
        Future<byte[]> commentRss = exec.submit(() -> {
            {
                return xmlToBytes(new CommentRss().generateFeed(null));
            }
        });
        StringBuilder mimes = new StringBuilder(1048576);
        Future<List<Fileupload>> files = exec.submit(() -> {
            {
                List<Fileupload> dbfiles = fileRepo.getUploadArchive();
                for (Fileupload f : dbfiles) {
                    mimes.append(f.getFilename()).append(": ").append(f.getMimetype()).append('\n');
                }
                return dbfiles;
            }
        });

        try (ZipOutputStream zip = new ZipOutputStream(wrapped)) {
            long time = new Date().getTime();

            addEntryToZip(zip, ArticleRss.NAME, "text/xml", time, articleRss.get());
            addEntryToZip(zip, CommentRss.NAME, "text/xml", time, commentRss.get());
            files.get();
            addEntryToZip(zip, MIMES_TXT, "text/plain", time, mimes.toString().getBytes("UTF-8"));
            for (Fileupload upload : files.get()) {
                String insideFilename = contentDir + upload.getFilename();
                String[] dirs = insideFilename.split("/");
                dirs = Arrays.copyOf(dirs, dirs.length - 1);
                StringBuilder subdir = new StringBuilder();
                for (String d : dirs) {
                    subdir.append(d).append("/");
                    if (!directories.contains(subdir.toString())) {
                        zip.putNextEntry(new ZipEntry(subdir.toString()));
                        zip.closeEntry();
                        directories.add(subdir.toString());
                    }
                }
                addEntryToZip(zip, insideFilename, upload.getMimetype(), upload.getAtime().getTime(), upload.getFiledata());
                mimes.append(upload.getFilename()).append(": ").append(upload.getMimetype()).append('\n');
            }
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
        return imead.getValue(UtilBean.SITE_TITLE).replaceAll("[^\\w]", "") + new SimpleDateFormat("yyyyMMdd'.zip'").format(new Date());
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

    private byte[] getByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[65536];
        int read;
        while ((read = in.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        return baos.toByteArray();
    }
}
