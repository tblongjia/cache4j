package net.sf.cache4j;
 
/**
 * 缓存的统计信息
 * @author longjia.zt
 *
 */
public interface CacheInfo {
    public long getCacheHits();
    public long getCacheMisses();
    public long getTotalPuts();
    public long getTotalRemoves();
    public void reset();
    public long getMemorySize();
 }
 