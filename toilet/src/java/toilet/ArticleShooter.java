package toilet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.imageio.ImageIO;
import libOdyssey.bean.GuardRepo;
import libWebsiteTools.HashUtil;
import libWebsiteTools.file.Brotlier;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.file.FileServlet;
import libWebsiteTools.file.FileUtil;
import libWebsiteTools.file.Filemetadata;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.file.Gzipper;
import libWebsiteTools.imead.IMEADHolder;
import toilet.bean.EntryRepo;
import toilet.db.Article;

/**
 *
 * @author alpha
 */
public class ArticleShooter extends ArticleProcessor {

    private final static Logger LOG = Logger.getLogger(ArticleShooter.class.getName());
    private final EntryRepo entry;
    private final ExecutorService exec;
    private static final LinkedBlockingQueue<Object> POTATOES = new LinkedBlockingQueue<>(Runtime.getRuntime().availableProcessors() / 2);

    /*static {
        {
            for (int x = 0; x < Runtime.getRuntime().availableProcessors() / 2; x++) {
                POTATOES.add(new Filemetadata(Integer.toString(x), null));
            }
        }
    }*/

    public ArticleShooter(Article art, IMEADHolder imead, FileRepo file, EntryRepo entry, ExecutorService exec) {
        super(art, imead, file);
        this.entry = entry;
        this.exec = exec;
    }

    @Override
    @SuppressWarnings("UseSpecificCatch")
    public Article call() {
        if (art.getSummary().contains("<figure>") && !hasPreview(art)) {
            Matcher imgAttribMatcher = ArticleProcessor.IMG_ATTRIB_PATTERN.matcher(art.getPostedhtml());
            while (imgAttribMatcher.find()) {
                HashMap<String, String> attribs = new HashMap<>();
                Matcher attribMatcher = ArticleProcessor.ATTRIB_PATTERN.matcher(imgAttribMatcher.group(1));
                while (attribMatcher.find()) {
                    attribs.put(attribMatcher.group(1), attribMatcher.group(2));
                }
                Fileupload img = file.getFile(FileServlet.getNameFromURL(attribs.get("src")));
                if (null != img) {
                    String command = "null";
                    File tempfile = null;
                    Object potato = null;
                    try {
                        potato = POTATOES.take();
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(img.getFiledata()));
                        if (image.getWidth() >= 600 && image.getHeight() >= 300) {
                            tempfile = File.createTempFile("articleScreenshot", art.getArticleid().toString());
                            command = String.format("firefox --window-size=608,608 -new-instance -screenshot %s %sarticlePreview/%s",
                                    new Object[]{tempfile.getAbsolutePath(), imead.getValue(GuardRepo.CANONICAL_URL), art.getArticleid().toString()});
                            FileUtil.runProcess(command, null, 0);
                            if (0 == tempfile.length()) {
                                return art;
                            }
                            Date now = new Date();
                            Fileupload f = new Fileupload("articlePreview/" + art.getArticleid() + ".png");
                            f.setAtime(now);
                            f.setFiledata(FileUtil.getByteArray(new FileInputStream(tempfile)));
                            f.setEtag(HashUtil.getSHA256HashAsBase64(f.getFiledata()));
                            f.setMimetype("image/png");
                            f.setUrl(FileServlet.getImmutableURL(imead.getValue(GuardRepo.CANONICAL_URL), new Filemetadata(f.getFilename(), now)));
                            file.deleteFile(f.getFilename());
                            file.addFiles(Arrays.asList(f));
                            exec.submit(new Brotlier(f));
                            exec.submit(new Gzipper(f));
                            art = entry.getArticle(art.getArticleid());
                            art.setImageurl(f.getUrl());
                            HashMap<Article, String> articles = new HashMap<>();
                            articles.put(art, art.getSectionid().getName());
                            entry.addArticles(articles);
                        }
                    } catch (Exception ex) {
                        LOG.log(Level.SEVERE, "Error while screenshotting: " + command, ex);
                        throw new RuntimeException(ex);
                    } finally {
                        while (null != potato) {
                            try {
                                POTATOES.put(potato);
                                break;
                            } catch (InterruptedException ex) {
                            }
                        }
                        if (null != tempfile) {
                            tempfile.delete();
                        }
                    }
                }
            }
        }
        return art;
    }

    private static boolean hasPreview(Article art) {
        return null != art.getImageurl() && art.getImageurl().contains("/articlePreview/");
    }
}
