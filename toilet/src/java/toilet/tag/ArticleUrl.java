package toilet.tag;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.ejb.EJB;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import libWebsiteTools.imead.IMEADHolder;
import toilet.bean.UtilBean;
import toilet.db.Article;

public class ArticleUrl extends SimpleTagSupport {

    @EJB
    private IMEADHolder imead;
    private Article article;
    private Boolean link = true;
    private String anchor;
    private String text;
    private String id;

    @Override
    public void doTag() throws JspException, IOException {
        StringBuilder b = new StringBuilder(link ? "<a href=\"" : "");
        b.append(getUrl(imead.getValue(UtilBean.THISURL), article));
        if (anchor != null) {
            b.append('#');
            b.append(anchor);
        }
        if (link && id!=null){
            b.append("\" id=\"");
            b.append(id);
        }
        if (link) {
            b.append("\">");
            b.append(text == null ? article.getArticletitle() : text);
            b.append("</a>");
        }
        getJspContext().getOut().print(b.toString());
    }

    public static String getUrl(String thisURL, Article article, String anchor) {
        return getUrl(thisURL, article) + "#" + anchor;
    }

    public static String getUrl(String thisURL, Article article) {
        StringBuilder out = new StringBuilder(thisURL);
        out.append("article/");
        out.append(article.getArticleid());
        out.append('/');
        try {
            String title = URLEncoder.encode(article.getArticletitle(), "UTF-8");
            title = title.replaceAll("%[0-9A-F]{2}", "");
            title = title.replace(":", "");
            title = title.replace("+", "-");
            out.append(title);
        } catch (UnsupportedEncodingException ex) {
            // not gonna happen
            throw new RuntimeException(UtilBean.UTF8_UNSUPPORTED, ex);
        }   // F U JAVA
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

    public void setLink(Boolean link) {
        this.link = link;
    }

    public void setId(String id) {
        this.id = id;
    }
}
