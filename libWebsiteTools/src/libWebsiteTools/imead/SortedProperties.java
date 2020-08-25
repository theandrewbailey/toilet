package libWebsiteTools.imead;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author alpha
 */
public class SortedProperties extends Properties {

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        TreeMap<Object, Object> output = new TreeMap<>();
        for (Map.Entry prop : super.entrySet()) {
            output.put(prop.getKey(), prop.getValue());
        }
        return output.entrySet();
    }
}
