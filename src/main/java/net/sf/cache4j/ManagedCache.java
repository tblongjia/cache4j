package net.sf.cache4j;

import net.sf.cache4j.CacheException;
import net.sf.cache4j.CacheConfig;

/**
 * 缓存管理
 * @author longjia.zt
 *
 */
public interface ManagedCache {
	/**
	 * 设置缓存的配置项
	 * @param config
	 * @throws CacheException
	 */
    public void setCacheConfig(CacheConfig config) throws CacheException;
    /**
     * 实现缓存的清理算法。
     * @throws CacheException
     */
    public void clean() throws CacheException;

 }

 