/* =========================================================================
 * File: $Id: $CacheObject.java,v$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package net.sf.cache4j.impl;

import net.sf.cache4j.CacheException;

/**
 * 缓存对象的设计
 * @author longjia.zt
 *
 */
public class CacheObject {
 
	/**
	 * 缓存对象ID
	 */
    private Object _objId;

    /**
     * 缓存对象
     */ 
    protected Object _obj;

    /**
     * 访问次数统计，作为LFU过滤算法。
     */
    private long _accessCount;

    /**
     * FIFO过滤算法使用
     */
    private long _createTime;

    /**
     * 最后被访问时间。LRU过滤算法
     */
    private long _lastAccessTime;

    /**
     * 缓存对象大小
     */
    private int _objSize;

    /**
     * 线程锁定,看的不是特别明白 
     */
    //TODO
    private Thread _lockThread;

    /**
     * 主键ID
     */
    private long _id;

 
    /**
     * 创建新的缓存对象
     * @param objId
     */
    CacheObject(Object objId) {
        _objId = objId;
        _obj = null;

        _createTime = System.currentTimeMillis();
        _accessCount = 1;
        _lastAccessTime = _createTime;
        _objSize = 0;
        _lockThread = null;

        _id = nextId();
    }

// ----------------------------------------------------------------------------- Public 戾蝾潲

  /**
   * 不是特别明白 应该是用来锁定的
   * @throws CacheException
   */
    synchronized void lock() throws CacheException {
        if(_lockThread!=null && Thread.currentThread()==_lockThread){
            return;
        }

        try {
            while (_lockThread!=null) {
                 wait();
             }
             _lockThread = Thread.currentThread(); 
        } catch (InterruptedException ex) {
            notify();     
             throw new CacheException(ex.getMessage());
        }
    }

    /**
     *  
     *  释放锁定
     */
    synchronized void unlock() {
        _lockThread = null;

         notify();      //蜞� � 溻� 疣玎 猁耱疱�
     }

 
    Object getObject() {
        return _obj;
    }

 
    void setObject(Object obj) {
        _obj = obj;
    }

 
    Object getObjectId(){
        return _objId;
    }

 
    long getAccessCount() {
        return _accessCount;
    }

 
    long getCreateTime() {
        return _createTime;
    }

 
    long getLastAccessTime() {
        return _lastAccessTime;
    }

 
    long getObjectSize() {
        return _objSize;
    }

 
    void setObjectSize(int objSize) {
        _objSize = objSize;
    }

    /**
     * 更新统计信息  访问命中缓存  访问增加1  最后访问时间+1
     */
    void updateStatistics() {
        _accessCount++;
        _lastAccessTime = System.currentTimeMillis();

        _id = nextId();
    }

    /**
     * 重置对象
     */
    void reset(){
        _obj = null;
        _objSize = 0;
        _createTime = System.currentTimeMillis();
        _accessCount = 1;
        _lastAccessTime = _createTime;

        _id = nextId();
    }

    long getId() {
        return _id;
    }

    /**
     * 
     */
    public String toString(){
        return "id:"+_objId+
                " createTime:"+_createTime+
                " lastAccess:"+_lastAccessTime+
                " accessCount:"+_accessCount+
                " size:"+_objSize+
                " object:"+_obj;
    }
    /**
     * 全局变量，同时加锁获取唯一主键ID
     */
    private static long ID = 0;
    private static synchronized long nextId(){
        return ID++;
    }
}

 
