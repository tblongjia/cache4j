
package net.sf.cache4j;

/**
 * 缓存实现
 * @author longjia.zt
 *
 */
public interface Cache {
 
    public void put(Object objId, Object obj) throws CacheException;
 
    public Object get(Object objId) throws CacheException;
 
    public void remove(Object objId) throws CacheException;
 
    public int size();
 
    public void clear() throws CacheException;
 
    public CacheConfig getCacheConfig();
 
    public CacheInfo getCacheInfo();
 }
