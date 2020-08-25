package toilet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import libWebsiteTools.bean.SecurityRepo;
import libWebsiteTools.Markdowner;
import libWebsiteTools.db.Repository;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.file.FileServlet;
import libWebsiteTools.file.Fileupload;
import libWebsiteTools.imead.IMEADHolder;
import toilet.db.Article;
import toilet.tag.ArticleUrl;

/**
 * instantiate as needed to process articles in threads
 *
 * @author alpha
 */
public class ArticleProcessor implements Callable<Article> {

    private static final Logger LOG = Logger.getLogger(ArticleProcessor.class.getName());
    public static final Pattern IMG_ATTRIB_PATTERN = Pattern.compile("<img (.+?)\\s?/?>");
    private static final Pattern IMG_X2 = Pattern.compile("^(.+)(\\..+)$");
    public static final Pattern ATTRIB_PATTERN = Pattern.compile("([^\\s=]+)(?:=['|\\\"](.*?)['|\\\"])?");
    private static final Pattern PARA_PATTERN = Pattern.compile(".*?(<p>.*?</p>).*");
    private static final Pattern A_PATTERN = Pattern.compile("</?a(?:\\s.*?)?>");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<.+?>");
    // !?\["?(.+?)"?\]\(\S+?(?:\s"?(.+?)"?)?\)
    private static final Pattern MARKDOWN_LINK_PATTERN = Pattern.compile("!?\\[\"?(.+?)\"?\\]\\(\\S+?(?:\\s\"?(.+?)\"?)?\\)");

    public Repository<Fileupload> file;
    public Article art;
    public IMEADHolder imead;

    public ArticleProcessor(Article art, IMEADHolder imead, Repository<Fileupload> file) {
        this.art = art;
        this.file = file;
        this.imead = imead;
    }

    @Override
    public Article call() {
        if (null == art.getPostedhtml() || null == art.getPostedmarkdown()) {
            convert(art);
        }
        return processArticle();
    }

    /**
     * will refresh the posted HTML in the article if possible
     *
     * @param art article to convert
     * @return said article
     */
    public static Article convert(Article art) {
        if (null != art.getPostedmarkdown()) {
            art.setPostedhtml(Markdowner.getHtml(art.getPostedmarkdown()));
        } else if (null != art.getPostedhtml()) {
            art.setPostedmarkdown(Markdowner.getMarkdown(art.getPostedhtml()));
            LOG.log(Level.INFO, "The markdown for article {0} had to be created from HTML.", art.getArticletitle());
        } else {
            throw new IllegalArgumentException(String.format("The text for article %s cannot be recovered, because it has no HTML or markdown.", art.getArticletitle()));
        }
        return art;
    }

    /**
     * article MUST have ID set, or else your homepage won't have links that go
     * anywhere!
     *
     * @return processed article (same object as passed in constructor)
     */
    public Article processArticle() {
        if (null == art) {
            throw new IllegalArgumentException("Can't process an article when YOU DON'T PASS IT!");
        }
        if (null == art.getPostedhtml() || null == art.getPostedmarkdown()) {
            convert(art);
        }
        if (null == imead) {
            imead = UtilStatic.getBean(IMEADHolder.LOCAL_NAME, IMEADHolder.class);
        }
        if (null == file) {
            file = UtilStatic.getBean(FileRepo.LOCAL_NAME, FileRepo.class);
        }
        String html = art.getPostedhtml();
        String paragraph = "";
        Matcher paraMatcher = PARA_PATTERN.matcher(html);
        while (paraMatcher.find()) {
            if (!paraMatcher.group(1).startsWith("<p><img ")) {
                paragraph = paraMatcher.group(1);
                paragraph = A_PATTERN.matcher(paragraph).replaceAll("");
                break;
            }
        }
        String ampHtml = html;
        Matcher imgAttribMatcher = IMG_ATTRIB_PATTERN.matcher(html);
        art.setSummary(null);
        art.setImageurl(null);
        while (imgAttribMatcher.find()) {
            HashMap<String, String> origAttribs = new HashMap<>();
            Matcher attribMatcher = ATTRIB_PATTERN.matcher(imgAttribMatcher.group(1));
            while (attribMatcher.find()) {
                origAttribs.put(attribMatcher.group(1), attribMatcher.group(2));
            }
            String imageURL = origAttribs.get("src");
            Map<String, String> baseAttribs = getImageInfo(imageURL, new HashMap<>(origAttribs));
            Map<String, String> doubleAttribs = null;
            Map<String, String> avifAttribs = null;
            try {
                Matcher doubleMatcher = IMG_X2.matcher(imageURL);
                if (doubleMatcher.find()) {
                    doubleAttribs = getImageInfo(URLDecoder.decode(doubleMatcher.group(1) + "×2" + doubleMatcher.group(2), "UTF-8"), new HashMap<>(origAttribs));
                    avifAttribs = getImageInfo(URLDecoder.decode(doubleMatcher.group(1) + "×2.avif", "UTF-8"), new HashMap<>(origAttribs));
                }
            } catch (UnsupportedEncodingException | NullPointerException ex) {
            }
            Map<String, String> ampAttribs = new HashMap<>(null != doubleAttribs ? doubleAttribs : baseAttribs);

            StringBuilder pictureTag = new StringBuilder(500).append("<picture>");
            if (null != avifAttribs) {
                String srcset = avifAttribs.remove("src");
                String type = avifAttribs.remove("type");
                avifAttribs = new HashMap<>();
                avifAttribs.put("srcset", srcset);
                avifAttribs.put("type", type);
                pictureTag.append(createTag("source", avifAttribs).append("/>"));
            }
            {
                Map<String, String> baseset = new HashMap<>();
                baseset.put("type", baseAttribs.get("type"));
                String srcset = baseAttribs.get("src") + " 1x";
                if (null != doubleAttribs) {
                    srcset += ", " + doubleAttribs.get("src") + " 2x";
                    baseset.put("data-highres", doubleAttribs.get("src"));
                }
                baseset.put("srcset", srcset);
                pictureTag.append(createTag("source", baseset).append("/>"));
            }

            baseAttribs.remove("type");
            String imgTag = createTag("img", baseAttribs).append("/>").toString();
            pictureTag.append(imgTag).append("</picture>");
            String pictureString = pictureTag.toString();

            ampAttribs.remove("type");
            ampAttribs.put("layout", "responsive");
            String ampTag = createTag("amp-img", ampAttribs).append("><noscript>").append(imgTag).append("></noscript></amp-img>").toString();
            ampHtml = ampHtml.replace(imgAttribMatcher.group(0), ampTag);

            html = html.replace(imgAttribMatcher.group(0), pictureString);
            if (null == art.getSummary() && Integer.parseInt(baseAttribs.get("width")) >= 600 && Integer.parseInt(baseAttribs.get("height")) >= 300) {
                if (null == art.getImageurl()) {
                    art.setImageurl(baseAttribs.get("src"));
                }
                art.setSummary(String.format("<article class=\"article%s\"><a class=\"withFigure\" href=\"%s\"><figure>%s<figcaption><h1>%s</h1></figcaption></figure></a>%s</article>",
                        art.getArticleid(), ArticleUrl.getUrl(imead.getValue(SecurityRepo.CANONICAL_URL), art, null, null), pictureString, art.getArticletitle(), paragraph
                ));
            }
        }
        art.setPostedhtml(html);
        art.setPostedamp(ampHtml);
        if (null == art.getSummary()) {
            art.setSummary(String.format("<article class=\"article%s\"><header><a href=\"%s\"><h1>%s</h1></a></header>%s</article>",
                    art.getArticleid(), ArticleUrl.getUrl(imead.getValue(SecurityRepo.CANONICAL_URL), art, null, null), art.getArticletitle(), paragraph));
        }
        return art;
    }

    @SuppressWarnings("UseSpecificCatch")
    private Map<String, String> getImageInfo(String url, Map<String, String> attribs) {
        if (null == attribs) {
            attribs = new HashMap<>();
        }
        Fileupload fileUpload = file.get(FileServlet.getNameFromURL(url));
        attribs.put("src", FileServlet.getImmutableURL(imead.getValue(SecurityRepo.CANONICAL_URL), fileUpload));
        attribs.put("type", fileUpload.getMimetype());
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileUpload.getFiledata()));
            attribs.put("width", Integer.toString(image.getWidth()));
            attribs.put("height", Integer.toString(image.getHeight()));
        } catch (Exception e) {
        }
        return attribs;
    }

    private StringBuilder createTag(String tagname, Map<String, String> attribs) {
        StringBuilder tag = new StringBuilder(200).append("<").append(tagname);
        for (Map.Entry<String, String> attribute : attribs.entrySet()) {
            tag.append(" ").append(attribute.getKey());
            if (null != attribute.getValue()) {
                tag.append("=\"").append(attribute.getValue()).append("\"");
            }
        }
        return tag;
    }
}
