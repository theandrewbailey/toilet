package toilet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import libWebsiteTools.bean.GuardRepo;
import libWebsiteTools.Markdowner;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.file.FileServlet;
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

    public FileRepo file;
    public Article art;
    public IMEADHolder imead;

    public ArticleProcessor(Article art, IMEADHolder imead, FileRepo file) {
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
        while (imgAttribMatcher.find()) {
            HashMap<String, String> attribs = new HashMap<>();
            Matcher attribMatcher = ATTRIB_PATTERN.matcher(imgAttribMatcher.group(1));
            while (attribMatcher.find()) {
                attribs.put(attribMatcher.group(1), attribMatcher.group(2));
            }
            HashMap<String, String> ampAttribs = new HashMap<>(attribs);
            String imgName = attribs.get("src");
            ampAttribs.put("src", imgName);
            BufferedImage image;
            try {
                Matcher doubleMatcher = IMG_X2.matcher(imgName);
                Fileupload img = null;
                if (doubleMatcher.find()) {
                    imgName = URLDecoder.decode(doubleMatcher.group(1) + "×2" + doubleMatcher.group(2), "UTF-8");
                    img = file.get(FileServlet.getNameFromURL(imgName));
                }
                if (null == img) {
                    imgName = URLDecoder.decode(attribs.get("src"), "UTF-8");
                    img = file.get(FileServlet.getNameFromURL(imgName));
                }
                image = ImageIO.read(new ByteArrayInputStream(img.getFiledata()));
                ampAttribs.put("width", Integer.toString(image.getWidth()));
                ampAttribs.put("height", Integer.toString(image.getHeight()));
                ampAttribs.put("src", FileServlet.getImmutableURL(imead.getValue(GuardRepo.CANONICAL_URL), img));
                if (!imgName.equals(attribs.get("src"))) {
                    img = file.get(FileServlet.getNameFromURL(attribs.get("src")));
                    image = ImageIO.read(new ByteArrayInputStream(img.getFiledata()));
                }
                attribs.put("width", Integer.toString(image.getWidth()));
                attribs.put("height", Integer.toString(image.getHeight()));
                art.setImageurl(FileServlet.getImmutableURL(imead.getValue(GuardRepo.CANONICAL_URL), img));
                attribs.put("src", art.getImageurl());
            } catch (IOException | NullPointerException ex) {
                Logger.getLogger(ArticleServlet.class
                        .getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            StringBuilder imgTagAttribs = new StringBuilder(imgAttribMatcher.group().length());
            for (Map.Entry<String, String> attribute : attribs.entrySet()) {
                imgTagAttribs.append(" ").append(attribute.getKey());
                if (null != attribute.getValue()) {
                    imgTagAttribs.append("=\"").append(attribute.getValue()).append("\"");
                }
            }
            StringBuilder ampImgTagAttribs = new StringBuilder(imgAttribMatcher.group().length());
            for (Map.Entry<String, String> attribute : ampAttribs.entrySet()) {
                ampImgTagAttribs.append(" ").append(attribute.getKey());
                if (null != attribute.getValue()) {
                    ampImgTagAttribs.append("=\"").append(attribute.getValue()).append("\"");
                }
            }
            StringBuilder imgTag = new StringBuilder(imgTagAttribs.length() + 10).append("<img").append(imgTagAttribs).append("/>");
            StringBuilder replacement = new StringBuilder(ampImgTagAttribs.length() * 2 + 100)
                    .append("<amp-img layout=\"responsive\"").append(ampImgTagAttribs)
                    .append("><noscript><img").append(ampImgTagAttribs).append("></noscript></amp-img>");
            html = html.replace(imgAttribMatcher.group(0), imgTag);
            ampHtml = ampHtml.replace(imgAttribMatcher.group(0), replacement);
            if (null == art.getSummary() && image.getWidth() >= 600 && image.getHeight() >= 300) {
                art.setSummary(String.format(
                        "<article class=\"article%s\"><a class=\"withFigure\" href=\"%s\"><figure>%s<figcaption><h1>%s</h1></figcaption></figure></a>%s</article>",
                        art.getArticleid(), ArticleUrl.getUrl(imead.getValue(GuardRepo.CANONICAL_URL), art), imgTag, art.getArticletitle(), paragraph
                ));
            }
        }
        art.setPostedhtml(html);
        art.setPostedamp(ampHtml);
        if (null == art.getSummary()) {
            art.setSummary(String.format("<article class=\"article%s\"><header><a href=\"%s\"><h1>%s</h1></a></header>%s</article>",
                    art.getArticleid(), ArticleUrl.getUrl(imead.getValue(GuardRepo.CANONICAL_URL), art), art.getArticletitle(), paragraph));
        }
        return art;
    }

    /*public Article processArticle() {
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
                break;
            }
        }
        String ampHtml = html;
        Matcher imgAttribMatcher = IMG_ATTRIB_PATTERN.matcher(html);
        art.setSummary(null);
        while (imgAttribMatcher.find()) {
            HashMap<String, String> imgTagAttribsMap = new HashMap<>();
            Matcher attribMatcher = ATTRIB_PATTERN.matcher(imgAttribMatcher.group(1));
            while (attribMatcher.find()) {
                imgTagAttribsMap.put(attribMatcher.group(1), attribMatcher.group(2));
            }
            String imgName = imgTagAttribsMap.get("src");
            BufferedImage image;
            ArrayList<Fileupload> sources = new ArrayList<>();
            try {
                Fileupload img = file.getFile(FileServlet.getNameFromURL(imgName));
                image = ImageIO.read(new ByteArrayInputStream(img.getFiledata()));
                imgTagAttribsMap.put("width", Integer.toString(image.getWidth()));
                imgTagAttribsMap.put("height", Integer.toString(image.getHeight()));
                art.setImageurl(FileServlet.getImmutableURL(imead.getValue(GuardRepo.CANONICAL_URL), new Filemetadata(img.getFilename(), img.getAtime())));
                imgTagAttribsMap.put("src", art.getImageurl());
            } catch (IOException | NullPointerException ex) {
                Logger.getLogger(ArticleServlet.class
                        .getName()).log(Level.SEVERE, null, ex);
                continue;
            }
            Matcher doubleMatcher = IMG_X2.matcher(imgName);
            if (doubleMatcher.find()) {
                Fileupload img = file.getFile(FileServlet.getNameFromURL(doubleMatcher.group(1) + "×2" + doubleMatcher.group(2)));
                if (null != img) {
                    sources.add(img);
                }
                img = file.getFile(FileServlet.getNameFromURL(doubleMatcher.group(1) + "×2.avif"));
                if (null != img) {
                    sources.add(img);
                }
            }
            StringBuilder imgTagAttribs = new StringBuilder(imgAttribMatcher.group().length());
            for (Map.Entry<String, String> attribute : imgTagAttribsMap.entrySet()) {
                imgTagAttribs.append(" ").append(attribute.getKey());
                if (null != attribute.getValue()) {
                    imgTagAttribs.append("=\"").append(attribute.getValue()).append("\"");
                }
            }
            StringBuilder imgTag = new StringBuilder(imgTagAttribs.length() + 10).append("<img").append(imgTagAttribs).append("/>");
            if (sources.size() > 0) {
                String origImgTag = imgTag.toString();
                imgTag = new StringBuilder(1000).append("<picture>");
                Collections.reverse(sources);
                for (Fileupload imageSource : sources) {
                    imgTag.append("<source srcset=\"").append(imageSource.getUrl()).
                            append("\" type=\"").append(imageSource.getMimetype()).
                            //append("\" media=\"(min-width: ").append(Integer.toString(new Float(image.getWidth() * 1.2).intValue())).append("px)\"/>");
                            append("\" media=\"(min-resolution: 1.2 dppx)\"/>");
                }
                imgTag.append(origImgTag).append("</picture>");
            }
            StringBuilder replacement = new StringBuilder(imgTagAttribs.length() * 2 + 100)
                    .append("<amp-img layout=\"responsive\"").append(imgTagAttribs)
                    .append("><noscript><img").append(imgTagAttribs).append("></noscript></amp-img>");
            html = html.replace(imgAttribMatcher.group(0), imgTag);
            ampHtml = ampHtml.replace(imgAttribMatcher.group(0), replacement);
            if (null == art.getSummary() && image.getWidth() >= 600 && image.getHeight() >= 300) {
                art.setSummary(String.format(
                        "<article class=\"article%s\"><a class=\"withFigure\" href=\"%s\"><figure>%s<figcaption><h1>%s</h1></figcaption></figure></a>%s</article>",
                        art.getArticleid(), ArticleUrl.getUrl(imead.getValue(GuardRepo.CANONICAL_URL), art), imgTag, art.getArticletitle(), paragraph
                ));
            }
        }
        art.setPostedhtml(html);
        art.setPostedamp(ampHtml);
        if (null == art.getSummary()) {
            art.setSummary(String.format("<article class=\"article%s\"><header><a href=\"%s\"><h1>%s</h1></a></header>%s</article>",
                    art.getArticleid(), ArticleUrl.getUrl(imead.getValue(GuardRepo.CANONICAL_URL), art), art.getArticletitle(), paragraph));
        }
        return art;
    }*/
}
