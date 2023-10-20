package toilet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.Markdowner;
import libWebsiteTools.file.BaseFileServlet;
import libWebsiteTools.file.Filemetadata;
import libWebsiteTools.file.Fileupload;
import toilet.db.Article;
import toilet.tag.ArticleUrl;

/**
 * instantiate as needed to process articles in threads
 *
 * @author alpha
 */
public class ArticleProcessor implements Callable<Article> {

    public static final String FORMAT_PRIORITY = "entry_imagePriority";
    public static final Pattern IMG_ATTRIB_PATTERN = Pattern.compile("<img (.+?)\\s?/?>");
    // "toilet.css" -> ["toilet", null, "css"]
    // "post/2019_MEAndromedaCombat×2.avif" -> ["post/2019_MEAndromedaCombat", "2", "avif"]
    public static final Pattern IMG_X2 = Pattern.compile("^(.+?)(?:×(\\d+))?\\.(.+)$");
    public static final Pattern ATTRIB_PATTERN = Pattern.compile("([^\\s=]+)(?:=['|\\\"](.*?)['|\\\"])?");
    public static final Pattern PARA_PATTERN = Pattern.compile(".*?(<p>.*?</p>).*");
    public static final Pattern A_PATTERN = Pattern.compile("</?a(?:\\s.*?)?>");
    public static final Pattern HTML_TAG_PATTERN = Pattern.compile("<.+?>");
    // !?\["?(.+?)"?\]\(\S+?(?:\s"?(.+?)"?)?\)
    public static final Pattern MARKDOWN_LINK_PATTERN = Pattern.compile("!?\\[\"?(.+?)\"?\\]\\(\\S+?(?:\\s\"?(.+?)\"?)?\\)");

    private static final Logger LOG = Logger.getLogger(ArticleProcessor.class.getName());
    private final AllBeanAccess beans;
    private final Article art;

    public ArticleProcessor(AllBeanAccess beans, Article art) {
        this.art = art;
        this.beans = beans;
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
        } else if (null == art.getSummary() || null == art.getImageurl()) {
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
            Matcher imgAttribMatcher = IMG_ATTRIB_PATTERN.matcher(html);
            art.setSummary(null);
            art.setImageurl(null);
            while (imgAttribMatcher.find()) {
                try {
                    HashMap<String, String> origAttribs = new HashMap<>();
                    Matcher attribMatcher = ATTRIB_PATTERN.matcher(imgAttribMatcher.group(1));
                    while (attribMatcher.find()) {
                        origAttribs.put(attribMatcher.group(1), attribMatcher.group(2));
                    }
                    getImageInfo(URLDecoder.decode(origAttribs.get("src"), "UTF-8"), origAttribs);
                    StringBuilder pictureTag = new StringBuilder(500).append("<picture>");
                    String tempImageURL = URLDecoder.decode(origAttribs.get("src"), "UTF-8").replaceAll(beans.getImeadValue(SecurityRepo.BASE_URL), "");
                    Matcher stemmer = IMG_X2.matcher(tempImageURL);
                    if (stemmer.find()) {
                        String name = BaseFileServlet.getNameFromURL(stemmer.group(1));
                        List<Filemetadata> files = beans.getFile().search(name);
                        for (String mime : beans.getImeadValue(FORMAT_PRIORITY).replaceAll("\r", "").split("\n")) {
                            List<String> srcset = new ArrayList<>();
                            Map<String, String> attribs = new HashMap<>();
                            files.stream().filter((file) -> {
                                if (mime.equals(file.getMimetype())) {
                                    Matcher sorter = IMG_X2.matcher(file.getFilename());
                                    if (sorter.find()) {
                                        return name.equals(sorter.group(1));
                                    }
                                }
                                return false;
                            }).sorted((file1, file2) -> {
                                Matcher sort1 = IMG_X2.matcher(file1.getFilename());
                                Matcher sort2 = IMG_X2.matcher(file2.getFilename());
                                if (sort1.find() && sort2.find()) {
                                    int x1 = null != sort1.group(2) ? Integer.parseInt(sort1.group(2)) : 1;
                                    int x2 = null != sort2.group(2) ? Integer.parseInt(sort2.group(2)) : 1;
                                    return x1 - x2;
                                }
                                return 0;
                            }).forEach((file) -> {
                                Integer width = UtilStatic.parseInt(origAttribs.get("width"), 0);
                                try {
                                    attribs.put("type", file.getMimetype());
                                    Matcher namer = IMG_X2.matcher(file.getFilename());
                                    namer.find();
                                    Integer multiplier = UtilStatic.parseInt(namer.group(2), 1);
                                    if (0 != width) {
                                        //srcset.add(file.getUrl() + " " + (width * multiplier) + "w");
                                        srcset.add(file.getUrl() + " " + Double.valueOf(Math.floor(width * (multiplier + 0.5))).intValue() + "w");
                                    } else {
                                        srcset.add(file.getUrl() + " " + multiplier + "x");
                                    }
                                } catch (Exception x) {
                                    srcset.add(file.getUrl() + (0 != width ? " " + width + "w" : " 1x"));
                                }
                            });
                            if (!srcset.isEmpty()) {
                                attribs.clear();
                                attribs.put("type", mime);
                                attribs.put("srcset", String.join(", ", srcset));
                                pictureTag.append(createTag("source", attribs).append("/>"));
                            }
                        }
                    }
                    origAttribs.remove("type");
                    html = html.replace(imgAttribMatcher.group(0),
                            new StringBuilder(500).append(pictureTag).append(createTag("img", origAttribs).append("/>")).append("</picture>"));
                    if (null == art.getSummary() && Integer.parseInt(origAttribs.get("width")) >= 600 && Integer.parseInt(origAttribs.get("height")) >= 300) {
                        art.setImageurl(origAttribs.get("src"));
                        origAttribs.put("loading", "lazy");
                        art.setSummary(String.format("<article class=\"article%s\"><a class=\"withFigure\" href=\"%s\"><figure>%s<figcaption><h1>%s</h1></figcaption></figure></a>%s</article>",
                                art.getArticleid(), ArticleUrl.getUrl("", art, null),
                                pictureTag.append(createTag("img", origAttribs).append("/>")).append("</picture>").toString(), art.getArticletitle(), paragraph
                        ));
                    }
                } catch (UnsupportedEncodingException enc) {
                    throw new JVMNotSupportedError(enc);
                }
            }
            art.setPostedhtml(html);
            if (null == art.getSummary()) {
                art.setSummary(String.format("<article class=\"article%s\"><header><a href=\"%s\"><h1>%s</h1></a></header>%s</article>",
                        art.getArticleid(), ArticleUrl.getUrl("", art, null), art.getArticletitle(), paragraph));
            }
        }
        return art;
    }

    @SuppressWarnings("UseSpecificCatch")
    private Map<String, String> getImageInfo(String url, Map<String, String> attributes) {
        if (null == attributes) {
            attributes = new HashMap<>();
        }
        try {
            Fileupload fileUpload = beans.getFile().get(BaseFileServlet.getNameFromURL(url));
            attributes.put("src", BaseFileServlet.getImmutableURL(beans.getImeadValue(SecurityRepo.BASE_URL), fileUpload));
            attributes.put("type", fileUpload.getMimetype());
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileUpload.getFiledata()));
            attributes.put("width", Integer.toString(image.getWidth()));
            attributes.put("height", Integer.toString(image.getHeight()));
            //} catch (NoResultException nre) {            return null;
        } catch (IOException e) {
        }
        return attributes;
    }

    /**
     *
     * @param tagname
     * @param attributes
     * @return an unterminated opening tag with the given tagname and attributes
     */
    private StringBuilder createTag(String tagname, Map<String, String> attributes) {
        StringBuilder tag = new StringBuilder(200).append("<").append(tagname);
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            tag.append(" ").append(attribute.getKey());
            if (null != attribute.getValue()) {
                tag.append("=\"").append(attribute.getValue()).append("\"");
            }
        }
        return tag;
    }
}
