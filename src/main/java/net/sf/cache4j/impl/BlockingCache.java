
package net.sf.cache4j.impl;

import net.sf.cache4j.CacheException;
import net.sf.cache4j.Cache;
import net.sf.cache4j.CacheConfig;
import net.sf.cache4j.CacheInfo;
import net.sf.cache4j.ManagedCache;

import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.io.IOException;
/**
 * 阻塞的缓存
 * @author longjia.zt
 *
 */

public class BlockingCache implements Cache, ManagedCache {
	/**
	 * 真正的数据
	 */
    private Map _map;

    /**
     * 执行算法的数据
     */
    private TreeMap _tmap;
    /**
     * 配置
     */
    private CacheConfigImpl _config;

   
    private long _memorySize;
 
    private CacheInfoImpl _cacheInfo;
    /**
     * 线程变量，每个线程不同
     */
    private ThreadLocal _tl = new ThreadLocal();

 
    public void put(Object objId, Object obj) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }

       
        CacheObject tlCO = (CacheObject)_tl.get();
        CacheObject co = null;

        int objSize = 0;
        try {
            objSize = _config.getMaxMemorySize()>0 ? Utils.size(obj) : 0;
        } catch (IOException e) {
            throw new CacheException(e.getMessage());
        }
 
        checkOverflow(objSize);

        if (tlCO==null) {
            co = getCacheObject(objId);
        } else {
            if(tlCO.getObjectId().equals(objId)){
                co = tlCO;
                _tl.set(null);
            } else {
                tlCO.unlock();
                throw new CacheException("Cache:"+_config.getCacheId()+" wait for call put() with objId="+tlCO.getObjectId());
            }
        }

        co.lock();
        _cacheInfo.incPut();
        try {
 
            tmapRemove(co);
 
            resetCacheObject(co);

            co.setObject(obj);
            co.setObjectSize(objSize);

            synchronized(this) {
                _memorySize = _memorySize + objSize;
            }

            tmapPut(co);
        } finally {
            co.unlock();
        }
    }

 
    public Object get(Object objId) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }

        CacheObject tlCO = (CacheObject)_tl.get();
        if (tlCO!=null) {
            throw new CacheException("Cache:"+_config.getCacheId()+" wait for call put() with objId="+tlCO.getObjectId());
        }

        CacheObject co = getCacheObject(objId);
        co.lock();
        Object o = co.getObject();
        if(o!=null){
            tmapRemove(co);

            if(!valid(co)) {
                resetCacheObject(co);
                _tl.set(co);

                _cacheInfo.incMisses();
                return null;
            } else {
                co.updateStatistics();
                tmapPut(co);

                 co.unlock();
                _cacheInfo.incHits();

                 return o;
            }
        } else {
             _tl.set(co);
            _cacheInfo.incMisses();
             return null;
        }
    }

 
    public void remove(Object objId) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }
 
        CacheObject tlCO = (CacheObject)_tl.get();
        if (tlCO!=null) {
            throw new CacheException("Cache:"+_config.getCacheId()+" wait for call put() with objId="+tlCO.getObjectId());
        }

        CacheObject co = null;//getCacheObject(objId);
        synchronized (this) {
            co = (CacheObject)_map.get(objId);
        }

        if (co==null) {
            return;
        }

        co.lock();
        _cacheInfo.incRemove();
        try {
            synchronized (this) {
                tmapRemove(co);
 
                CacheObject co2 = (CacheObject)_map.get(co.getObjectId());
                if(co2!=null && co2==co){
                    _map.remove(co.getObjectId());
                    resetCacheObject(co);
                }
            }
        } finally {
            co.unlock();
        }
    }

 
    public int size() {
        return _map.size();
    }

 
    public void clear() throws CacheException {
        Object[] objArr = null;
        synchronized (this) {
            objArr = _map.values().toArray();
        }

         for (int i = 0, indx = objArr==null ? 0 : objArr.length; i<indx; i++) {
            remove(((CacheObject)objArr[i]).getObjectId());
        }
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
        _tl.set(null);
    }

 
    public void clean() throws CacheException {
         if(_config.getTimeToLive()==0 && _config.getIdleTime()==0){
            return;
        }

        Object[] objArr = null;
        synchronized (this) {
            objArr = _map.values().toArray();
        }

        for (int i = 0, indx = objArr==null ? 0 : objArr.length; i<indx; i++) {
            CacheObject co = (CacheObject)objArr[i];
             if ( !valid(co) ) {
                remove(co.getObjectId());
            }
        }
    }
    /**
     * 检测超限制，如果超出限制，直接过期数据，一次过期一个key。
     * @param objSize
     */
    private void checkOverflow(int objSize) {
        synchronized(this) {
            while( (_config.getMaxSize() > 0 &&  _map.size()+1>_config.getMaxSize() ) ||
                    (_config.getMaxMemorySize()  > 0 && _memorySize+objSize > _config.getMaxMemorySize())  ) {
 
                Object firstKey = _tmap.size()==0 ? null : _tmap.firstKey();
                CacheObject fco = firstKey==null ? null : (CacheObject)_tmap.remove(firstKey);

                if(fco!=null) {
                    CacheObject co = (CacheObject)_map.get(fco.getObjectId());
                    if(co!=null && co==fco){
                        _map.remove(fco.getObjectId());
                        resetCacheObject(fco);
                    }
                }
            } //while
        }
    }
 
    private void tmapRemove(CacheObject co){
        synchronized(this) {
            _tmap.remove(co);
        }
    }
 
    private void tmapPut(CacheObject co){
        synchronized(this) {
 
            Object mapO = _map.get(co.getObjectId());
            if(mapO!=null && mapO==co){
                _tmap.put(co, co);
            }
        }
    }


 
    private CacheObject getCacheObject(Object objId) {
        synchronized (this) {
            CacheObject co = (CacheObject)_map.get(objId);
            if (co == null) {
                co = _config.newCacheObject(objId);
                _map.put(objId, co);
            }
            return co;
        }
    }
 
    private boolean valid(CacheObject co){
        long curTime = System.currentTimeMillis();
        return  (_config.getTimeToLive()==0 || (co.getCreateTime()  + _config.getTimeToLive()) >= curTime) &&
                (_config.getIdleTime()==0 || (co.getLastAccessTime() + _config.getIdleTime()) >= curTime) &&
 
                co.getObject()!=null;
    }
 
    private void resetCacheObject(CacheObject co){
        synchronized(this) {
            _memorySize = _memorySize - co.getObjectSize();
        }
        co.reset();
    }
     private class CacheInfoImpl implements CacheInfo {
        private long _hit;
        private long _miss;
        private long _put;
        private long _remove;

        synchronized void incHits(){
            _hit++;
        }
        synchronized void incMisses(){
            _miss++;
        }
        synchronized void incPut(){
            _put++;
        }
        synchronized void incRemove(){
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
            //return "hit:"+_hit+" miss:"+_miss+" memorySize:"+_memorySize+" size:"+_map.size()+" tsize:"+_tmap.size();
        }
    }
}

