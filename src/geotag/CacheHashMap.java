/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotag;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Dario Bertini <berdario@gmail.com>
 */
public class CacheHashMap<K,V> extends LinkedHashMap {
    int maxCachedResults;

    public CacheHashMap(int maxCachedResults) {
        super(Math.round(maxCachedResults/(float) 0.75)+1,(float) 0.75,true);
        this.maxCachedResults = maxCachedResults;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size()>maxCachedResults;
    }

    @Override
    public V get(Object key) {
        return (V) super.get(key);
    }

}
