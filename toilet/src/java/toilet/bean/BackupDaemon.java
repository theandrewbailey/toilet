package toilet.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import libOdyssey.bean.ExceptionRepo;
import libWebsiteTools.XmlNodeSearcher;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.rss.FeedBucket;
import libWebsiteTools.rss.iFeedBucket;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import toilet.UtilStatic;
import toilet.db.Article;
import toilet.db.Comment;
import toilet.db.Fileupload;
import toilet.db.Section;
import toilet.rss.ArticleRss;
import toilet.rss.CommentRss;

/**
 * so I can sleep at night, knowing my stuff is being backed up
 * @author alpha
 */
@Startup
@Singleton
public class BackupDaemon {

    public final static String MIMES_TXT = "mimes.txt";
    private final static String MASTER_DIR = "site_backup";
    private final static String CONTENT_DIR = "content";
    private final static Logger log = Logger.getLogger(BackupDaemon.class.getName());
    @EJB
    private EntryRepo entry;
    @EJB
    private ExceptionRepo error;
    @EJB
    private IMEADHolder imead;
    @EJB
    private FileRepo file;
    @EJB
    private iFeedBucket src;

    /**
     * dumps articles, comments, and uploaded files to a directory (specified by site_backup key in imead.keyValue)
     */
    @Schedule(hour = "1")
    //@PostConstruct
    public void backup() {
        log.entering(BackupDaemon.class.getName(), "backup");
        log.info("Backup procedure initiating");
        String master = imead.getValue(MASTER_DIR);
        String content = master + CONTENT_DIR;
        File contentDir = new File(content);
        if (!contentDir.exists()) {
            contentDir.mkdirs();
        }
        List<Fileupload> dbfiles = file.getUploadArchive();
        StringBuilder mimes=new StringBuilder(dbfiles.size()*40);
        
        for (Fileupload f : dbfiles) {
            String fn = content + File.separator + f.getFilename();
            try {   // file exists in db, different hash or file not backed up yet
                log.log(Level.FINE, "Writing file {0}", f.getFilename());
                writeFile(fn, f.getBinarydata());
            } catch (IOException ex) {
                log.log(Level.SEVERE, "Error writing " + f.getFilename(), ex);
                error.add(null, "Backup failure", null, ex);
            }
            mimes.append(f.getFilename()).append(": ").append(f.getMimetype()).append('\n');
        }

        try {
            log.log(Level.FINE, "Writing mimes.txt");
            writeFile(master + MIMES_TXT, mimes.toString().getBytes("UTF-8"));
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Error writing mimes.txt", ex);
        }
        try {
            log.log(Level.FINE, "Writing Articles.rss");
            writeFile(master + File.separator + ArticleRss.NAME, xmlToString(new ArticleRss().generateFeed(null)).getBytes("UTF-8"));
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error writing Articles.rss to file", ex);
        }
        try {
            log.log(Level.FINE, "Writing Comments.rss");
            writeFile(master + File.separator + CommentRss.NAME, xmlToString(new CommentRss().generateFeed(null)).getBytes("UTF-8"));
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error writing Comments.rss to file", ex);
        }
        log.info("Backup procedure finished");
        log.exiting(BackupDaemon.class.getName(), "backup");
        backupToZip();
    }

    public void restoreFromZip(ZipInputStream zip) throws Exception {
        InputStream articleStream = null;
        InputStream commentStream = null;
        HashMap<String, String> mimes = new HashMap<String, String>(100);
        HashMap<String, Fileupload> files = new HashMap<String, Fileupload>(100);
        for (ZipEntry zipEntry = zip.getNextEntry(); zipEntry != null; zipEntry = zip.getNextEntry()) {
            if (zipEntry.isDirectory()) {
                continue;
            }
            if (zipEntry.getName().equals(ArticleRss.NAME)) {
                articleStream = new ByteArrayInputStream(getByteArray(zip));
            } else if (zipEntry.getName().equals(CommentRss.NAME)) {
                commentStream = new ByteArrayInputStream(getByteArray(zip));
            } else if (zipEntry.getName().equals(BackupDaemon.MIMES_TXT)) {
                String mimeString = new String(getByteArray(zip));
                for (String mimeEntry : mimeString.split("\n")) {
                    try {
                        String[] parts = mimeEntry.split(": ");
                        mimes.put(parts[0], parts[1]);
                    } catch (ArrayIndexOutOfBoundsException a) {
                    }
                }
            } else {
                Fileupload fileUpload = new Fileupload(null, getByteArray(zip), null, zipEntry.getName().replace("content/", ""), "application/octet-stream", new Date(zipEntry.getTime()));
                fileUpload.setEtag(UtilStatic.getHash(fileUpload.getBinarydata()));
                files.put(fileUpload.getFilename(), fileUpload);
            }
        }
        zip.close();
        for (Map.Entry<String, Fileupload> fileEntry : files.entrySet()) {
            if (mimes.containsKey(fileEntry.getKey())) {
                fileEntry.getValue().setMimetype(mimes.get(fileEntry.getValue().getFilename()));
            }
            Fileupload existing = file.getFile(fileEntry.getKey());
            if (existing != null) {
                if (!existing.getEtag().equals(fileEntry.getValue().getEtag())) {
                    file.deleteFile(existing.getFileuploadid());
                } else {
                    continue;
                }
            }
            file.addFile(fileEntry.getValue());
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        Element articleRoot = dbf.newDocumentBuilder().parse(articleStream).getDocumentElement();
        ArrayList<Article> articles = new ArrayList<Article>();
        for (Node item : new XmlNodeSearcher(articleRoot.getFirstChild(), "item")) {
            Article article = new Article();
            articles.add(article);
            for (Node component : new XmlNodeSearcher(item, "description")) {
                String text = component.getTextContent();
                article.setPostedtext(text.replace("&lt;", "<").replace("&gt;", ">"));
            }
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
            for (Node component : new XmlNodeSearcher(item, "comments")) {
                article.setEtag(component.getTextContent());
            }
            for (Node component : new XmlNodeSearcher(item, "author")) {
                article.setPostedname(component.getTextContent());
            }
            for (Node component : new XmlNodeSearcher(item, "guid")) {
                article.setDescription(component.getTextContent());
            }
            article.setComments(article.getEtag() != null);
            article.setCommentCollection(article.getComments() ? new ArrayList<Comment>() : null);
        }

        dbf.setIgnoringElementContentWhitespace(true);
        Element commentRoot = dbf.newDocumentBuilder().parse(commentStream).getDocumentElement();
        for (Node item : new XmlNodeSearcher(commentRoot.getFirstChild(), "item")) {
            Comment comm = new Comment();
            for (Node component : new XmlNodeSearcher(item, "description")) {
                comm.setPostedtext(component.getTextContent().replace("&lt;", "<").replace("&gt;", ">"));
            }
            for (Node component : new XmlNodeSearcher(item, "pubDate")) {
                comm.setPosted(new SimpleDateFormat(FeedBucket.TIME_FORMAT).parse(component.getTextContent()));
            }
            for (Node component : new XmlNodeSearcher(item, "author")) {
                comm.setPostedname(component.getTextContent());
            }
            for (Node component : new XmlNodeSearcher(item, "comments")) {
                for (Article a : articles) {
                    if (component.getTextContent().equals(a.getEtag())) {
                        a.getCommentCollection().add(comm);
                        break;
                    }
                }
            }
        }

        entry.deleteEverything();
        Collections.reverse(articles);
        for (Article article : articles) {
            List<Comment> artComm = (List<Comment>) article.getCommentCollection();
            String section = article.getSectionid() != null ? article.getSectionid().getName() : imead.getValue(EntryRepo.DEFAULT_CATEGORY);
            if (article.getComments()) {
                Collections.reverse(artComm);
            }
            article.setCommentCollection(null);

            Article added = entry.addEntry(article, section);

            for (Comment comm : artComm) {
                entry.addComment(added.getArticleid(), comm);
            }
        }

        src.getFeed(ArticleRss.NAME).preAdd();
        src.getFeed(CommentRss.NAME).preAdd();
    }

    /**
     * stuffs everything to a zip file in the backup directory (specified by site_backup key in imead.keyValue)
     * can be used for the import functionality
     * @see BackupDaemon.generateZip(OutputStream wrapped)
     */
    //@Schedule(hour="1", minute="30")
    public void backupToZip(){
        log.entering(BackupDaemon.class.getName(), "backupToZip");
        log.info("Backup to zip procedure initiating");
        String master = imead.getValue(MASTER_DIR);
        String zipName = getZipName();
        try {
            FileOutputStream out = new FileOutputStream(master + zipName);
            generateZip(out);
            out.close();
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Error writing zip file: " + zipName, ex);
        }
        log.info("Backup to zip procedure finished");
        log.exiting(BackupDaemon.class.getName(), "backupToZip");
    }

    /**
     * stuffs all articles, comments, and uploaded files (with MIME types) into zip format and dumps it on the given OutputStream
     * can be used to save to file or put on a request
     * @param wrapped put zip on this
     * @throws IOException something went wrong (don't know how anything would, just prove me wrong...)
     */
    public void generateZip(OutputStream wrapped) throws IOException {
        String contentDir = CONTENT_DIR + "/";
        List<Fileupload> dbfiles = file.getUploadArchive();
        List<String> directories = new ArrayList<String>();
        ZipOutputStream zip = new ZipOutputStream(wrapped);
        StringBuilder mimes = new StringBuilder(dbfiles.size() * 40);
        long time = new Date().getTime();

        addEntryToZip(zip, ArticleRss.NAME, "text/xml", time, xmlToString(new ArticleRss().generateFeed(null)).getBytes("UTF-8"));
        addEntryToZip(zip, CommentRss.NAME, "text/xml", time, xmlToString(new CommentRss().generateFeed(null)).getBytes("UTF-8"));
        for (Fileupload upload : dbfiles) {
            String insideFilename=contentDir + upload.getFilename();
            String[] dirs=insideFilename.split("/");
            dirs=Arrays.copyOf(dirs, dirs.length-1);
            StringBuilder subdir=new StringBuilder();
            for (String d:dirs){
                subdir.append(d).append("/");
                if (!directories.contains(subdir.toString())){
                    zip.putNextEntry(new ZipEntry(subdir.toString()));
                    zip.closeEntry();
                    directories.add(subdir.toString());
                }
            }

            addEntryToZip(zip, insideFilename, upload.getMimetype(), upload.getUploaded().getTime(), upload.getBinarydata());
            mimes.append(upload.getFilename()).append(": ").append(upload.getMimetype()).append('\n');
        }
        addEntryToZip(zip, MIMES_TXT, "text/plain", time, mimes.toString().getBytes("UTF-8"));

        zip.close();
    }

    /**
     * 
     * @return site title (a-zA-Z_0-9 only) + today's date (year + month + day) + ".zip"
     */
    public String getZipName(){
        return imead.getValue(UtilBean.SITE_TITLE).replaceAll("[^\\w]", "") + new SimpleDateFormat("yyyyMMdd'.zip'").format(new Date());
    }

    /**
     * adds a file to the given ZIP stream
     * will compress file if mimetype starts with "text/"
     * @param zip zip stream
     * @param name of file
     * @param mime file type
     * @param time modified time (from Date.getTime), defaults to -1
     * @param content file contents
     * @throws IOException something went wrong (why would it?)
     */
    private void addEntryToZip(ZipOutputStream zip, String name, String mime, long time, byte[] content) throws IOException {
        ZipEntry out = new ZipEntry(name);
        // dynamic compression ( setMethod(ZipEntry.DEFLATED) means everything is compressed)
//        boolean compressed = mime.startsWith("text/");
//        out.setMethod(compressed ? ZipEntry.DEFLATED : ZipEntry.STORED);
//        if (!compressed) {
//            CRC32 checksum = new CRC32();
//            checksum.update(content);
//            out.setCrc(checksum.getValue());
//            out.setCompressedSize(content.length);
//            out.setSize(content.length);
//        }
        out.setMethod(ZipEntry.DEFLATED);
        out.setTime(time);
        zip.putNextEntry(out);
        zip.write(content);
        zip.closeEntry();
    }

    /**
     * makes a really big string from the given XML object
     * @param xml
     * @return big string
     */
    private String xmlToString(Document xml) {
        DOMSource DOMsrc = new DOMSource(xml);
        StringWriter sw = new StringWriter();
        StreamResult str = new StreamResult(sw);
        Transformer trans = FeedBucket.getTransformer(false);
        try {
            trans.transform(DOMsrc, str);
            return sw.toString();
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * writes a file
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
                d.mkdir();
            }
            f.createNewFile();
        }
        FileOutputStream tempStr = new FileOutputStream(f, false);
        tempStr.write(content);
        tempStr.close();
    }

    private byte[] getByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[1000];
        int read;
        while ((read = in.read(buf)) != -1) {
            baos.write(buf, 0, read);
        }
        return baos.toByteArray();
    }
}
