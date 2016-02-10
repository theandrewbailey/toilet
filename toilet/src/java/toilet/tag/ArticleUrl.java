package toilet.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.ejb.EJB;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import libOdyssey.bean.GuardHolder;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.imead.IMEADHolder;
import toilet.db.Article;

public class ArticleUrl extends SimpleTagSupport {

    @EJB
    private IMEADHolder imead;
    private Article article;
    private boolean link = true;
    private String anchor;
    private String cssClass;
    private String text;
    private String id;

    @Override
    public void doTag() throws JspException, IOException {
        StringBuilder b = new StringBuilder(200);
        if (link) {
            b.append("<a href=\"");
        };
        b.append(getUrl(imead.getValue(GuardHolder.CANONICAL_URL), article, anchor));
        if (link && id != null) {
            b.append("\" id=\"").append(id);
        }
        if (link && cssClass != null) {
            b.append("\" class=\"").append(cssClass);
        }
        if (link) {
            b.append("\">").append(text == null ? article.getArticletitle() : text).append("</a>");
        }
        getJspContext().getOut().print(b.toString());
    }

    public static String getUrl(String thisURL, Article article, String anchor) {
        return anchor == null ? getUrl(thisURL, article)
                : getUrl(thisURL, article) + "#" + anchor;
    }

    public static String getUrl(String thisURL, Article article) {
        StringBuilder out = new StringBuilder(thisURL).append("article/").append(article.getArticleid()).append('/');
        try {
            String title = URLEncoder.encode(article.getArticletitle(), "UTF-8");
            title = title.replaceAll("%[0-9A-F]{2}", "").replace(":", "").replace("+", "-");
            out.append(title);
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        }
        return out.toString();
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
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
