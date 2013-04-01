package libWebsiteTools.tag;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import libWebsiteTools.RequestTokenBucket;
import libWebsiteTools.RequestTokenChecker;

/**
 *
 * @author alpha
 */
public class RequestToken extends Hidden {
    public static final String ID_NAME="$_REQUEST_TOKEN";
    public static final String DISABLE_REFERRER_CHECK="$_DISABLE_REFERRER_CHECK";
    private boolean disableReferrerCheck=false;

    @Override
    public void doTag() throws JspException, IOException {
        getJspContext().getOut().print(generateTag(getJspContext()));
    }

    public String generateTag(JspContext jspc){
        PageContext context=((PageContext)jspc);
        RequestTokenBucket bucket=RequestTokenBucket.getRequestTokenBucket((HttpServletRequest)context.getRequest());
        HttpServletRequest req=((HttpServletRequest)context.getRequest());
        String url=req.getAttribute(RequestTokenChecker.ORIGINAL_REQUEST_URL).toString();
        setValue(bucket.generateToken(url));
        return this.generateTag();
    }

    @Override
    public String getId(){
        return ID_NAME;
    }

    public boolean isDisableReferrerCheck() {
        return disableReferrerCheck;
    }

    public void setDisableReferrerCheck(boolean disableReferrerCheck) {
        this.disableReferrerCheck = disableReferrerCheck;
    }
}
