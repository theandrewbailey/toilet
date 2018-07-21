package libWebsiteTools;

import java.util.Enumeration;
import java.util.Iterator;

/**
 *
 * @author alpha
 * @param <T>
 */
public class EnumerationWrapper<T> implements Enumeration<T> {
    private final Iterator<T> internal;

    public EnumerationWrapper(Iterator<T> toBeWrapped) {
        internal = toBeWrapped;
    }

    @Override
    public boolean hasMoreElements() {
        return internal.hasNext();
    }

    @Override
    public T nextElement() {
        return internal.next();
    }

}