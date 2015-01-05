
package net.sf.cache4j;

 
public class CacheCleaner extends Thread {
	/**
	 * 清理间隔
	 */
    private long _cleanInterval;
    /**
     * 是不是在睡眠中
     */
    private boolean _sleep = false;

 
    public CacheCleaner(long cleanInterval) {
        _cleanInterval = cleanInterval;

        setName(this.getClass().getName());
        setDaemon(true);
 
    }

 
    /**
     * 重新设置缓存马上唤醒重新清理
     * @param cleanInterval
     */
    public void setCleanInterval(long cleanInterval) {
        _cleanInterval = cleanInterval;

        synchronized(this){
            if(_sleep){
                interrupt();
            }
        }
    }

 
    public void run() {
        while(true)  {
            try {
                CacheFactory cacheFactory = CacheFactory.getInstance();
                Object[] objIdArr = cacheFactory.getCacheIds();
                for (int i = 0, indx = objIdArr==null ? 0 : objIdArr.length; i<indx; i++) {
                    ManagedCache cache = (ManagedCache)cacheFactory.getCache(objIdArr[i]);
                    if(cache!=null){
                        cache.clean();
                    }
                    yield();
                }
            } catch (Throwable t){
                t.printStackTrace();
            }

            _sleep = true;
            try {
                sleep(_cleanInterval);
            } catch (Throwable t){
            } finally {
                _sleep = false;
            }
        }
    }

 
}

 
