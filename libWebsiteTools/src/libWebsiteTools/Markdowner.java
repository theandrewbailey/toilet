package libWebsiteTools;

import java.io.InputStreamReader;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author alpha
 */
@Startup
@Singleton
public class Markdowner {

    private ScriptEngine processor;
//    private CompiledScript marked;
//    private CompiledScript remarked;

    private ScriptEngine getProcessor() {
        if (processor == null) {
            try {
                processor = new ScriptEngineManager().getEngineByName("JavaScript");
                InputStreamReader markedjs = new InputStreamReader(Markdowner.class.getClassLoader().getResourceAsStream("libWebsiteTools/marked.js"));
                InputStreamReader remarkedjs = new InputStreamReader(Markdowner.class.getClassLoader().getResourceAsStream("libWebsiteTools/reMarked.js"));
//                if (processor instanceof Compilable){
//                    Compilable cPro = (Compilable)processor;
//                    marked = cPro.compile(markedjs);
//                    remarked = cPro.compile(remarkedjs);
//                } else {
                    processor.eval(markedjs);
                    processor.eval(remarkedjs);
//                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return processor;
    }

    public String getHtml(String markdown){
        //return new MarkdownProcessor().markdown(markdown);
        try {
            processor=getProcessor();
            processor.put("input", markdown);
            processor.eval("var output=marked(input);");
            return processor.get("output").toString();
        } catch (ScriptException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getMarkdown(String html){
        try {
            processor=getProcessor();
            processor.put("input", html);
            processor.eval("var output=new reMarked().render(input);");
            return processor.get("output").toString();
        } catch (ScriptException ex) {
            throw new RuntimeException(ex);
        }
    }

}
