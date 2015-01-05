package net.sf.cache4j.impl;

import java.lang.ref.SoftReference;
/**
 * 软连接
 * @author longjia.zt
 *
 */
public class SoftCacheObject extends CacheObject {
 
    SoftCacheObject(Object objId) {
        super(objId);
    }

 
    Object getObject() {
        return _obj==null ? null : ((SoftReference)_obj).get();
    }

 
    void setObject(Object obj) {
        _obj = obj==null ? null : new SoftReference(obj);
    }

 }

 
