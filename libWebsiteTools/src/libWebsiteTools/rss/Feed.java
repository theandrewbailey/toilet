package libWebsiteTools.rss;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * allows feeds to specify their own name (URLs), and MIME type (RSS, Atom) in
 * the class file
 *
 * @author: Andrew Bailey (praetor_alpha) praetoralpha 'at' gmail.com
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Feed {

    public static final String MIME_RSS = "application/rss+xml";
    public static final String MIME_ATOM = "application/atom+xml";

    /**
     * specifies the mime type of the feed
     * @return
     */
    String MIME() default MIME_RSS;

    /**
     * specifies the rss feed name
     * @return
     */
    String value();
}
