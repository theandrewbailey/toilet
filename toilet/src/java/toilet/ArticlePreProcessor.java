package toilet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import libOdyssey.bean.GuardHolder;
import libWebsiteTools.Markdowner;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.imead.IMEADHolder;
import toilet.db.Article;
import toilet.servlet.ArticleServlet;
import toilet.tag.ArticleUrl;

/**
 * instantiate as needed to process articles in threads
 *
 * @author alpha
 */
public class ArticlePreProcessor implements Callable<Article> {

    private static final Logger LOG = Logger.getLogger(ArticlePreProcessor.class.getName());
    private static final Pattern IMG_ATTRIB_PATTERN = Pattern.compile("<img (.+?)\\s?/?>");
    private static final Pattern IMG_X2 = Pattern.compile("^(.+)(\\..+)$");
    private static final Pattern ATTRIB_PATTERN = Pattern.compile("([^\\s=]+)(?:=['|\\\"](.*?)['|\\\"])?");
    private static final Pattern PARA_PATTERN = Pattern.compile(".*?(<p>.*?</p>).*");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<.+?>");
    // !?\["?(.+?)"?\]\(\S+?(?:\s"?(.+?)"?)?\)
    private static final Pattern MARKDOWN_LINK_PATTERN = Pattern.compile("!?\\[\"?(.+?)\"?\\]\\(\\S+?(?:\\s\"?(.+?)\"?)?\\)");

    private FileRepo file;
    private final Article art;
    private IMEADHolder imead;

    public ArticlePreProcessor(Article art, IMEADHolder imead, FileRepo file) {
        this.art = art;
        this.file = file;
        this.imead = imead;
    }

    @Override
    public Article call() {
        return preprocessArticle();
    }

    public Article preprocessArticle() {
        if (null == art) {
            throw new IllegalArgumentException("Can't preprocess an article when YOU DON'T PASS IT!");
        }
        if (null == imead) {
            imead = UtilStatic.getBean(IMEADHolder.LOCAL_NAME, IMEADHolder.class);
        }
        if (null == file) {
            file = UtilStatic.getBean(FileRepo.LOCAL_NAME, FileRepo.class);
        }
        String html = Markdowner.getHtml(art.getPostedmarkdown());
        String searchable = HTML_TAG_PATTERN.matcher(art.getPostedmarkdown()).replaceAll("");
        Matcher linkMatcher = MARKDOWN_LINK_PATTERN.matcher(searchable);
        while (linkMatcher.find()) {
            String replacement = 2 == linkMatcher.groupCount() ? linkMatcher.group(1) + " " + linkMatcher.group(2)
                    : 1 == linkMatcher.groupCount() ? linkMatcher.group(1) : "";
            searchable = linkMatcher.replaceFirst(replacement);
            linkMatcher.reset(searchable);
        }
        art.setSearchabletext(searchable);
        String paragraph = "";
        Matcher paraMatcher = PARA_PATTERN.matcher(html);
        while (paraMatcher.find()) {
            if (!paraMatcher.group(1).startsWith("<p><img ")) {
                paragraph = paraMatcher.group(1);
                break;
            }
        }
        String ampHtml = html;
        Matcher imgAttribMatcher = IMG_ATTRIB_PATTERN.matcher(html);
        art.setSummary(null);
        while (imgAttribMatcher.find()) {
            HashMap<String, String> attribs = new HashMap<>();
            Matcher attribMatcher = ATTRIB_PATTERN.matcher(imgAttribMatcher.group(1));
            while (attribMatcher.find()) {
                attribs.put(attribMatcher.group(1), attribMatcher.group(2));
            }
            HashMap<String, String> ampAttribs = new HashMap<>(attribs);
            String imgName = attribs.get("src");
            BufferedImage image;
            try {
                Matcher doubleMatcher = IMG_X2.matcher(imgName);
                Fileupload img = null;
                if (doubleMatcher.find()) {
                    imgName = doubleMatcher.group(1) + "×2" + doubleMatcher.group(2);
                    img = file.getFile(FileRepo.getFilename(imgName));
                }
                if (null == img) {
                    imgName = attribs.get("src");
                    img = file.getFile(FileRepo.getFilename(imgName));
                }
                image = ImageIO.read(new ByteArrayInputStream(img.getFiledata()));
                ampAttribs.put("width", Integer.toString(image.getWidth()));
                ampAttribs.put("height", Integer.toString(image.getHeight()));
                if (!imgName.equals(attribs.get("src"))) {
                    image = ImageIO.read(new ByteArrayInputStream(file.getFile(FileRepo.getFilename(attribs.get("src"))).getFiledata()));
                }
                attribs.put("width", Integer.toString(image.getWidth()));
                attribs.put("height", Integer.toString(image.getHeight()));
            } catch (IOException | NullPointerException ex) {
                Logger.getLogger(ArticleServlet.class.getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            StringBuilder imgTagAttribs = new StringBuilder(imgAttribMatcher.group().length());
            for (Map.Entry<String, String> attribute : attribs.entrySet()) {
                imgTagAttribs.append(" ").append(attribute.getKey());
                if (null != attribute.getValue()) {
                    imgTagAttribs.append("=\"").append(attribute.getValue()).append("\"");
                }
            }
            ampAttribs.put("src", imgName);
            StringBuilder ampImgTagAttribs = new StringBuilder(imgAttribMatcher.group().length());
            for (Map.Entry<String, String> attribute : ampAttribs.entrySet()) {
                ampImgTagAttribs.append(" ").append(attribute.getKey());
                if (null != attribute.getValue()) {
                    ampImgTagAttribs.append("=\"").append(attribute.getValue()).append("\"");
                }
            }
            StringBuilder imgTag = new StringBuilder(imgTagAttribs.length() + 10).append("<img").append(imgTagAttribs).append("/>");
            StringBuilder replacement = new StringBuilder(ampImgTagAttribs.length() * 2 + 100).append("<amp-img layout=\"responsive\"").append(ampImgTagAttribs).append("><noscript><img").append(ampImgTagAttribs).append("></noscript></amp-img>");
            html = html.replace(imgAttribMatcher.group(0), imgTag);
            ampHtml = ampHtml.replace(imgAttribMatcher.group(0), replacement);
            if (null == art.getSummary() && image.getWidth() >= 600 && image.getHeight() >= 200) {
                art.setSummary(String.format("<a href=\"%s\" class=\"withFigure\"><figure>%s<figcaption><h1>%s</h1></figcaption></figure></a>%s",
                        ArticleUrl.getUrl(imead.getValue(GuardHolder.CANONICAL_URL), art), imgTag, art.getArticletitle(), paragraph
                ));
            }
        }
        art.setPostedhtml(html);
        art.setPostedamp(ampHtml);
        if (null == art.getSummary()) {
            art.setSummary(String.format("<header><a href=\"%s\"><h1>%s</h1></a></header>%s",
                    ArticleUrl.getUrl(imead.getValue(GuardHolder.CANONICAL_URL), art), art.getArticletitle(), paragraph));
        }
        return art;
    }
}
