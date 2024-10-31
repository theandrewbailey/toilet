package libWebsiteTools.turbo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author alpha
 */
public class PerfStats {

    private static final int LIMIT = 300;
    private final Map<String, LinkedList<RequestTimes>> timeStore = Collections.synchronizedMap(new LinkedHashMap<>(LIMIT));

    public void evict() {
        timeStore.clear();
    }

    public void submit(RequestTimes timings, String urlGroup) {
        String url = urlGroup;
        if (null == url) {
            timings.getUrl();
        }
        LinkedList<RequestTimes> list = timeStore.get(url);
        if (null == list) {
            list = new LinkedList<>();
            if (LIMIT <= timeStore.size()) {
                timeStore.remove(timeStore.keySet().iterator().next());
            }
            timeStore.put(url, list);
        } else if (LIMIT <= list.size()) {
            synchronized (list) {
                list.sort(null);
                list.poll();
            }
        }
        synchronized (list) {
            list.add(timings);
        }
    }

    public Map<String, List<RequestTimes>> getAll() {
        Map<String, List<RequestTimes>> out = new LinkedHashMap<>(timeStore.size() * 2);
        for (Map.Entry<String, LinkedList<RequestTimes>> e : timeStore.entrySet()) {
            synchronized (e.getValue()) {
                out.put(e.getKey(), List.copyOf(e.getValue()));
            }
        }
        return Collections.unmodifiableMap(out);
    }
}
