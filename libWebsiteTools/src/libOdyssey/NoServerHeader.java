package libOdyssey;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 *
 * @author alpha
 */
public class NoServerHeader extends HttpServletResponseWrapper {

    public NoServerHeader(HttpServletResponse res) {
        super(res);
    }

    @Override
    public void setHeader(String name, String value) {
        if ("X-Powered-By".equals(name) || "Server".equals(name)) {
            return;
        }
        super.setHeader(name, value);
    }
}
