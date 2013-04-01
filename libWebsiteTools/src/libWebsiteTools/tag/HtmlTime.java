package libWebsiteTools.tag;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 *
 * @author alpha
 */
public class HtmlTime extends SimpleTagSupport {

    public static final String FORMAT_VAR = "$_LIBWEBSITETOOLS_DATETIME_FORMAT";
    private String pattern;
    private Date datetime;
    private Boolean pubdate = false;
    private SimpleDateFormat htmlFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public void doTag() throws JspException, IOException {
        StringBuilder out = new StringBuilder("<time datetime=\"");
        out.append(htmlFormat.format(datetime));
//        if (pubdate)
//            out.append("\" pubdate=\"pubdate");
        out.append("\" >");
        Object uniFormat = getJspContext().findAttribute(FORMAT_VAR);
        if (pattern != null) {
            out.append(new SimpleDateFormat(pattern).format(datetime));
        } else if (uniFormat != null) {
            out.append(new SimpleDateFormat(uniFormat.toString()).format(datetime));
        } else {
            out.append(new SimpleDateFormat().format(datetime));
        }
        out.append("</time>");
        getJspContext().getOut().print(out.toString());
    }

    /**
     * @return the format
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @param format the format to set
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * @return the datetime
     */
    public Date getDatetime() {
        return datetime;
    }

    /**
     * @param datetime the datetime to set
     */
    public void setDatetime(Date datetime) {
        this.datetime = datetime;
    }

    /**
     * @return the pubdate
     */
    public Boolean getPubdate() {
        return pubdate;
    }

    /**
     * @param pubdate the pubdate to set
     */
    public void setPubdate(Boolean pubdate) {
        this.pubdate = pubdate;
    }
}
