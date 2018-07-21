package libWebsiteTools.tag;

import java.io.IOException;
import java.text.DecimalFormat;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

/**
 *
 * @author alpha
 */
public class FileSize extends SimpleTagSupport {

    private final static String[] FORMATS = new String[]{"#,##0' bytes'", "#,##0.#' KB'", "#,##0.#' MB'", "#,##0.#' GB'", "#,##0.#' TB'", "#,##0.#' PB'", "#,##0.#' EB'", "#,##0.#' ZB'", "#,##0.#' YB'"};
    private Long length;

    @Override
    public void doTag() throws JspException, IOException {
        getJspContext().getOut().print(readableFileSize(length));
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0 bytes";
        }
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat(FORMATS[digitGroups]).format(size / Math.pow(1024, digitGroups));
    }

    /**
     * @param bytes the bytes to set
     */
    public void setLength(Long bytes) {
        this.length = bytes;
    }
}
