package net.sf.cache4j;

/**
 * 缓存配置对象
 * @author longjia.zt
 *
 */
public interface CacheConfig {
	/**
	 * 缓存ID
	 * @return
	 */
    public Object getCacheId();


    /**
     * 缓存描述
     * @return
     */
    public String getCacheDesc();

    /**
     * 最长存活时间
     * @return
     */
    public long getTimeToLive();

    /**
     * 最大空闲时间
     * @return
     */
    public long getIdleTime();

    /**
     * 最大内存占用
     * @return
     */
    public long getMaxMemorySize();

    /**
     * 最大记录条数
     * @return
     */
    public int getMaxSize();

    /**
     * 缓存类型
     * @return
     */
    public String getType();
    /**
     * 获取淘汰类型
     * @return
     */
    public String getAlgorithm();

   /**
    * 获取连接类型，软连接、强连接
    * @return
    */
    public String getReference();
}

 