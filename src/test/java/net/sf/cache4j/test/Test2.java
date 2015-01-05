package net.sf.cache4j.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import net.sf.cache4j.Cache;
import net.sf.cache4j.CacheException;
import net.sf.cache4j.CacheFactory;

public class Test2 {

	public static void main(String[] args) throws CacheException, FileNotFoundException {
		//
		File file=new File("D:\\opensouce\\cache4j-read\\src\\test\\java\\net\\sf\\cache4j\\test\\cache4j_config.xml");
		FileInputStream in = new FileInputStream(file);
		CacheFactory.getInstance().loadConfig(in );
		Cache cache = CacheFactory.getInstance().getCache("cache_id2");
		cache.put("22", "2");
		System.out.println(cache);
		System.out.println(cache.get("22"));
	}

}
