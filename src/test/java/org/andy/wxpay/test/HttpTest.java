package org.andy.wxpay.test;

import java.util.HashMap;
import java.util.Map;

import org.andy.wxpay.utils.HttpKit;
import org.andy.wxpay.utils.HttpUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * 创建时间：2016年11月7日 上午11:58:55
 * 
 * @author andy
 * @version 2.2
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext.xml",
		"classpath:rest-servlet.xml" })
public class HttpTest {

	public long testGet() {
		Map<String, String> params = new HashMap<>();
		params.put("city", "上海");
		params.put("rtitle", "买一");
		params.put("pno", "1");
		params.put("size", "10");
		Long start =  System.currentTimeMillis();
		HttpKit.get("http://www.huazhi.co/yhm/api/reward/search/list.shtml", params);
		long mid = System.currentTimeMillis() - start;
		System.out.println(mid);
		return mid;
	}
	
	public long testGet1() {
		Map<String, String> params = new HashMap<>();
		params.put("city", "上海");
		params.put("rtitle", "买一");
		params.put("pno", "1");
		params.put("size", "10");
		Long start =  System.currentTimeMillis();
		HttpUtils.get("http://www.huazhi.co/yhm/api/reward/search/list.shtml", params);
		long mid = System.currentTimeMillis() - start;
		System.out.println(mid);
		return mid;
	}
	
	public long testPost() {
		Map<String, String> params = new HashMap<>();
		params.put("city", "上海");
		params.put("rtitle", "买一");
		params.put("pno", "1");
		params.put("size", "10");
		String body = null;
		Long start =  System.currentTimeMillis();
		try {
			body = HttpKit.post("http://www.huazhi.co/yhm/api/reward/search/list.shtml", params);
		} catch (Exception e) {
		 //
		}
		long mid = System.currentTimeMillis() - start;
		System.out.println(mid);
		return mid;
	}
	
	public long testPost1() {
		Map<String, String> params = new HashMap<>();
		params.put("city", "上海");
		params.put("rtitle", "买一");
		params.put("pno", "1");
		params.put("size", "10");
		Long start =  System.currentTimeMillis();
		String body = HttpUtils.post("http://www.huazhi.co/yhm/api/reward/search/list.shtml", params);
		long mid = System.currentTimeMillis() - start;
		System.out.println(mid);
		return mid;
	}
	
	@Test
	public void Testsysch(){
		Long avg = 0l;
		for (int i = 0; i < 30; i++) {
			avg += testPost();
		}
		System.out.println("async-http-client平均请求时间： " + (avg / 30));
	}
	
	@Test
	public void TestAsysch(){
		Long avg = 0l;
		for (int i = 0; i < 30; i++) {
			avg += testPost1();
		}
		System.out.println("httpclient平均请求时间： " + (avg / 30));
	}
	
}
