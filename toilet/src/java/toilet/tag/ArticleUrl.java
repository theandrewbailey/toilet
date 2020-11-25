package toilet.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.security.SecurityRepo;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.imead.Local;
import toilet.db.Article;

public class ArticleUrl extends SimpleTagSupport {

    private Article article;
    private boolean link = true;
    private String anchor;
    private String cssClass;
    private String target;
    private String text;
    private String id;

    @Override
    public void doTag() throws JspException, IOException {
        HttpServletRequest req = ((HttpServletRequest) ((PageContext) getJspContext()).getRequest());
        Locale locale = (Locale) req.getAttribute(Local.OVERRIDE_LOCALE_PARAM);
        StringBuilder b = new StringBuilder(200);
        if (link) {
            b.append("<a href=\"");
        }
        b.append(getUrl(((HttpServletRequest) ((PageContext) getJspContext()).getRequest()).getAttribute(SecurityRepo.BASE_URL).toString(), article, locale, anchor));
        if (link && id != null) {
            b.append("\" id=\"").append(id);
        }
        if (link && cssClass != null) {
            b.append("\" class=\"").append(cssClass);
        }
        if (link && target != null) {
            b.append("\" target=\"").append(target);
        }
        if (link) {
            b.append("\">").append(text == null ? article.getArticletitle() : text).append("</a>");
        }
        getJspContext().getOut().print(b.toString());
    }

    public static String getUrl(String baseURL, Article article, Locale lang, String anchor) {
        StringBuilder url = new StringBuilder(baseURL).append("article/").append(article.getArticleid()).append('/').append(getUrlArticleTitle(article));
        //StringBuilder url = new StringBuilder("article/").append(article.getArticleid()).append('/').append(getUrlArticleTitle(article));
//        if (null != lang) {
//            url.append("?lang=").append(lang.toLanguageTag());
//        }
        if (null != anchor) {
            url.append("#").append(anchor);
        }
        return url.toString();
    }

    @Deprecated
    public static String getAmpUrl(String baseURL, Article article, Locale lang) {
        StringBuilder url = new StringBuilder(baseURL);
        if (null != lang) {
            url.append(lang.toLanguageTag()).append("/");
        }
        return url.append("amp/").append(article.getArticleid()).append('/').append(getUrlArticleTitle(article)).toString();
    }

    private static String getUrlArticleTitle(Article article) {
        try {
            String title = URLEncoder.encode(article.getArticletitle(), "UTF-8");
            title = title.replaceAll("%[0-9A-F]{2}", "").replace(":", "").replace("+", "-").replace("--", "-");
            return title;
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        }
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setLink(boolean link) {
        this.link = link;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }
}
