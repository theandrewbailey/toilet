package libWebsiteTools.imead;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 *
 * @author alpha
 */
public class IMEADResource extends ResourceBundle {

    private final Map<String, String> values;
    private final Locale locale;

    public IMEADResource(Locale thisLocale, IMEADResource parent, List<Localization> localizations) {
        super();
        locale = thisLocale;
        Map<String, String> theseValues = new HashMap<>(localizations.size() * 2);
        if (null != parent) {
            for (String k : parent.keySet()) {
                theseValues.put(k, parent.getString(k));
            }
        }
        for (Localization l : localizations) {
            theseValues.put(l.getLocalizationPK().getKey(), l.getValue());
        }
        values = Collections.unmodifiableMap(theseValues);
    }

    @Override
    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public Set<String> keySet() {
        return values.keySet();
    }

    @Override
    protected Set<String> handleKeySet() {
        return values.keySet();
    }

    @Override
    protected Object handleGetObject(String string) {
        return values.get(string);
    }

    @Override
    public Enumeration<String> getKeys() {
        return new Enumeration<String>() {
            Iterator<String> it = values.keySet().iterator();

            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public String nextElement() {
                if (!hasMoreElements()) {
                    throw new NoSuchElementException();
                }
                return it.next();
            }
        };
    }
}
