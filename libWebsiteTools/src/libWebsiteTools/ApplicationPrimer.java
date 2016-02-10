package libWebsiteTools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Automatically makes the web app request itself after a certain period of
 * time after the servlet context is initialized. This can alleviate certain
 * delays that might be encountered when pages are first loaded. 
 *
 * In the web.xml, a few context params are checked:
 *
 * ApplicationPrimer.timeout: delay before starting, in millis. Default: 10000.
 * ApplicationPrimer.url: what to request. Default: http://::1 + context path.
 * ApplicationPrimer.ua: the User-Agent string. Default: JDK version.
 *
 * For multiple URLs, limit them by pipes. The first JSESSIONID is
 * captured and sent with subsequent requests, so these requests should appear
 * as a single session.
 */
//@WebListener("Automatically request the web app.")
public class ApplicationPrimer extends TimerTask implements ServletContextListener {

    private static final Logger log = Logger.getLogger(ApplicationPrimer.class.getName());
    public static final String TIMEOUT_PARAM = "ApplicationPrimer.timeout";
    public static final String URL_PARAM = "ApplicationPrimer.url";
    public static final String UA_PARAM = "ApplicationPrimer.ua";
    private String url;
    private String ua;
    private int timeout = 10000;
    private Timer t = new Timer();

    /**
     * phase one: get init params, schedule request timer
     *
     * @param e
     */
    @Override
    public void contextInitialized(ServletContextEvent e) {
        ServletContext c = e.getServletContext();

        url = c.getInitParameter(URL_PARAM);
        ua = c.getInitParameter(UA_PARAM);

        if (url == null) {
            url = "http://::1" + e.getServletContext().getContextPath();
        }
        try {
            if (c.getInitParameter(TIMEOUT_PARAM) != null) {
                timeout = Integer.parseInt(c.getInitParameter(TIMEOUT_PARAM));
            }
        } catch (NumberFormatException n) {
            log.log(Level.SEVERE, TIMEOUT_PARAM + " context parameter formatted incorrectly.", n);
            throw n;
        }

        if (timeout != 0) {
            t.schedule(this, timeout);
            log.log(Level.INFO, "AppPrimer request scheduled in {0} to {1}", new Object[]{timeout, url});
        }
    }

    /**
     * phase two: request page(s), clean up
     */
    @Override
    public void run() {
        // TODO: use commons.httpclient
        String cookie = null;
        for (String s : url.split("\\|")) // split urls by pipe
        {
            try {
                URL u = new URL(s);
                URLConnection c = u.openConnection();
                if (ua != null) // set user agent
                {
                    c.setRequestProperty("User-Agent", ua);
                }
                if (cookie != null) // set cookie if it exists
                {
                    c.setRequestProperty("Cookie", "JSESSIONID=" + cookie + ';');
                }
                c.connect();
                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                while (br.ready()) // receive everything
                {
                    br.readLine();
                }
                if (cookie == null) // extract cookie for subsequent requests
                {
                    cookie = c.getHeaderField("Set-Cookie").split("JSESSIONID=")[1].split("; Path=/")[0];
                }
                log.log(Level.INFO, "Primed {0} sucessfully", s);
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Priming failed for " + s + ": " + ex.getClass().getSimpleName(), ex);
            }
        }

        // clean up
        t.cancel();
        t.purge();
        t = null;
        log.info("AppPrimer complete");
    }

    @Override
    public void contextDestroyed(ServletContextEvent e) {
    }
}
