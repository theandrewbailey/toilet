package libWebsiteTools;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author alpha
 * @param <K>
 * @param <V>
 */
public class HashCache<K, V> extends LinkedHashMap<K, V> {

    private final int limit;

    public HashCache(int limit) {
        super(limit, 0.8f, false);
        this.limit = new Float(limit * 0.75f).intValue();
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() >= limit;
    }
}
