package libWebsiteTools;

import java.io.InputStreamReader;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author alpha
 */
public class Markdowner {

    public static final String LOCAL_NAME = "java:module/Markdowner";
    private static final Logger LOG = Logger.getLogger(Markdowner.class.getName());
    private static final LinkedBlockingQueue<ScriptEngine> SCRIPT_ENGINES = new LinkedBlockingQueue<>(Runtime.getRuntime().availableProcessors());

    static {
        {
            for (int x = 0; x < Runtime.getRuntime().availableProcessors(); x++) {
                ScriptEngine processor = null;
                while (true) {
                    try {
                        if (null == processor) {
                            processor = new ScriptEngineManager().getEngineByName("JavaScript");
                            processor.eval(new InputStreamReader(Markdowner.class.getClassLoader().getResourceAsStream("libWebsiteTools/marked.js")));
                            processor.eval(new InputStreamReader(Markdowner.class.getClassLoader().getResourceAsStream("libWebsiteTools/markdown-it.js")));
                        }
                        SCRIPT_ENGINES.put(processor);
                        break;
                    } catch (InterruptedException | ScriptException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    public static String getMarkdown(String html) {
        ScriptEngine processor = null;
        try {
            processor = SCRIPT_ENGINES.take();
            processor.put("input", html);
            processor.eval("var output=markdownit().render(input);");
            LOG.info("converted HTML to markdown");
            return processor.get("output").toString();
        } catch (InterruptedException | ScriptException ex) {
            throw new RuntimeException(ex);
        } finally {
            while (true) {
                try {
                    SCRIPT_ENGINES.put(processor);
                    break;
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    public static String getHtml(String markdown) {
        ScriptEngine processor = null;
        try {
            processor = SCRIPT_ENGINES.take();
            processor.put("input", markdown);
            processor.eval("var output=marked(input);");
            LOG.info("converted markdown to HTML");
            return processor.get("output").toString();
        } catch (InterruptedException | ScriptException ex) {
            throw new RuntimeException(ex);
        } finally {
            while (true) {
                try {
                    SCRIPT_ENGINES.put(processor);
                    break;
                } catch (InterruptedException ex) {
                }
            }
        }
    }

}
