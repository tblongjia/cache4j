
package net.sf.cache4j;

import net.sf.cache4j.Cache;
import net.sf.cache4j.CacheConfig;
import net.sf.cache4j.CacheException;
import net.sf.cache4j.impl.Configurator;

import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
 
public class CacheFactory {
	/**
	 * 所有实例化的cache
	 */
    private Map _cacheMap;

    private CacheCleaner _cleaner;

    /**
     * 单例模式，
     */
    private static final CacheFactory _cacheFactory = new CacheFactory();

    public CacheFactory() {
        _cacheMap = new HashMap();
        _cleaner = new CacheCleaner(30000); //default 30sec
        _cleaner.start();
    }

 
    public static CacheFactory getInstance(){
        return _cacheFactory;
    }

    /**
     * 加载配置项，全部初始化缓存数据
     * @param in
     * @throws CacheException
     */
    public void loadConfig(InputStream in) throws CacheException {
        Configurator.loadConfig(in);
    }

    /**
     * 增加一个缓存到缓存工厂
     * @param cache
     * @throws CacheException
     */
    public void addCache(Cache cache) throws CacheException {
        if(cache==null){
            throw new NullPointerException("cache is null");
        }
        CacheConfig cacheConfig = cache.getCacheConfig();
        if(cacheConfig==null) {
            throw new NullPointerException("cache config is null");
        }
        if(cacheConfig.getCacheId()==null) {
            throw new NullPointerException("config.getCacheId() is null");
        }
        if(!(cache instanceof Cache)) {
            throw new CacheException("cache not instance of "+ManagedCache.class.getName());
        }

        synchronized(_cacheMap){
            if(_cacheMap.containsKey(cacheConfig.getCacheId())) {
                throw new CacheException("Cache id:"+cacheConfig.getCacheId()+" exists");
            }

            _cacheMap.put(cacheConfig.getCacheId(), cache);
        }
    }

    /**
     * 从工厂获取缓存实例
     * @param cacheId
     * @return
     * @throws CacheException
     */
    public Cache getCache(Object cacheId) throws CacheException {
        if(cacheId==null) {
            throw new NullPointerException("cacheId is null");
        }

        synchronized(_cacheMap){
            return (Cache)_cacheMap.get(cacheId);
        }
    }

    /**
     * 根据ID从工厂删除缓存
     * @param cacheId
     * @throws CacheException
     */
    public void removeCache(Object cacheId) throws CacheException {
        if(cacheId==null) {
            throw new NullPointerException("cacheId is null");
        }

        synchronized(_cacheMap){
            _cacheMap.remove(cacheId);
        }
    }

   /**
    * 获取所有缓存的ID
    * @return
    */
    public Object[] getCacheIds() {
        synchronized(_cacheMap) {
            return _cacheMap.keySet().toArray();
        }
    }

    /**
     * 设置清理的时间
     * @param time
     */
    public void setCleanInterval(long time) {
        _cleaner.setCleanInterval(time);
    }
 
}
