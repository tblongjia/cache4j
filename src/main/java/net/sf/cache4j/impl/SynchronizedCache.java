
package net.sf.cache4j.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import net.sf.cache4j.Cache;
import net.sf.cache4j.CacheConfig;
import net.sf.cache4j.CacheException;
import net.sf.cache4j.CacheInfo;
import net.sf.cache4j.ManagedCache;
/**
 * Synchronized实现的缓存
 * @author longjia.zt
 *
 */
@SuppressWarnings("rawtypes")
public class SynchronizedCache implements Cache, ManagedCache {
	/**
	 * 实际的缓存数据
	 */
	private Map _map;

    //设置配置项的时候设置treeMap
    private TreeMap _tmap;

    /**
     * 缓存配置项
     */
    private CacheConfigImpl _config;

    /**
     * 占用内存大小
     */
    private long _memorySize;

    /**
     * 缓存的一些命中信息
     */
    private CacheInfoImpl _cacheInfo;

    
    public synchronized void put(Object objId, Object obj) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }

        int objSize = 0;
        try {
            objSize = _config.getMaxMemorySize()>0 ? Utils.size(obj) : 0;
        } catch (IOException e) {
            throw new CacheException(e.getMessage());
        }

         checkOverflow(objSize);

        CacheObject co = (CacheObject)_map.get(objId);

        if(co!=null) {
            _tmap.remove(co);
            resetCacheObject(co);
        } else {
            co = newCacheObject(objId);
        }

        _cacheInfo.incPut();

        co.setObject(obj);
        co.setObjectSize(objSize);
        _memorySize = _memorySize + objSize;

        _tmap.put(co, co);
    }

 
    public synchronized Object get(Object objId) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }

        CacheObject co = (CacheObject)_map.get(objId);
        Object o = co==null ? null : co.getObject();
        if(o!=null){
            if(!valid(co)) {
                remove(co.getObjectId());
                _cacheInfo.incMisses();
                return null;
            } else {
                _tmap.remove(co);
                co.updateStatistics();
                _tmap.put(co, co);

                _cacheInfo.incHits();
                return o;
            }
        } else {
            _cacheInfo.incMisses();
            return null;
        }
    }

 
    public synchronized void remove(Object objId) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }

        CacheObject co = (CacheObject)_map.remove(objId);

        _cacheInfo.incRemove();

        if(co!=null) {
            _tmap.remove(co);
            resetCacheObject(co);
        }
    }

 
    public int size() {
        return _map.size();
    }

  
    public synchronized void clear() throws CacheException {
        _map.clear();
        _tmap.clear();
        _memorySize = 0;
    }

   
    public CacheInfo getCacheInfo() {
        return _cacheInfo;
    }

  
    public CacheConfig getCacheConfig() {
        return _config;
    }
 
    public synchronized void setCacheConfig(CacheConfig config) throws CacheException {
        if(config==null) {
            throw new NullPointerException("config is null");
        }

        _config = (CacheConfigImpl)config;

        _map = _config.getMaxSize()>1000 ? new HashMap(1024) : new HashMap();
        _memorySize = 0;
        _tmap = new TreeMap(_config.getAlgorithmComparator());
        _cacheInfo = new CacheInfoImpl();
    }
    public void clean() throws CacheException {
         if(_config.getTimeToLive()==0 && _config.getIdleTime()==0){
            return;
        }

        Object[] objArr = null;
        synchronized(this) {
            objArr = _map.values().toArray();
        }

        for (int i = 0, indx = objArr==null ? 0 : objArr.length; i<indx; i++) {
            CacheObject co = (CacheObject)objArr[i];
            if ( !valid(co) ) {
                remove(co.getObjectId());
            }
        }
    }

 
    private void checkOverflow(int objSize) {
        while ( (_config.getMaxSize() > 0 && _map.size()+1   > _config.getMaxSize()) ||
                (_config.getMaxMemorySize()  > 0 && _memorySize+objSize > _config.getMaxMemorySize()) ) {
 
            CacheObject co = _tmap.size()==0 ? null : (CacheObject)_tmap.remove(_tmap.firstKey());

            if(co!=null) {
                _map.remove(co.getObjectId());
                resetCacheObject(co);
            }
        }
    }


 
    private CacheObject newCacheObject(Object objId) {
        CacheObject co = _config.newCacheObject(objId);
        _map.put(objId, co);
        return co;
    }
 
    private boolean valid(CacheObject co) {
        long curTime = System.currentTimeMillis();
        return  (_config.getTimeToLive()==0 || (co.getCreateTime()  + _config.getTimeToLive()) >= curTime) &&
                (_config.getIdleTime()==0 || (co.getLastAccessTime() + _config.getIdleTime()) >= curTime) &&
             
                co.getObject()!=null;
    }
 
    private void resetCacheObject(CacheObject co){
        _memorySize = _memorySize - co.getObjectSize();
        co.reset();
    }

    private class CacheInfoImpl implements CacheInfo {
        private long _hit;
        private long _miss;
        private long _put;
        private long _remove;

        void incHits(){
            _hit++;
        }
        void incMisses(){
            _miss++;
        }
        void incPut(){
            _put++;
        }
        void incRemove(){
            _remove++;
        }
        public long getCacheHits(){
            return _hit;
        }
        public long getCacheMisses(){
            return _miss;
        }
        public long getTotalPuts() {
            return _put;
        }
        public long getTotalRemoves() {
            return _remove;
        }
        public synchronized void reset() {
            _hit = 0;
            _miss = 0;
            _put = 0;
            _remove = 0;
        }
        public long getMemorySize() {
            return _memorySize;
        }
        public String toString(){
            return "hit:"+_hit+" miss:"+_miss+" memorySize:"+_memorySize;
         }
    }
}
