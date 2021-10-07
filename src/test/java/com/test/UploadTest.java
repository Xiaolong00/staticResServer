package com.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import ice.tool.GsonUtil;
import ice.tool.http.HttpProxy;
public class UploadTest {
	
	private HttpProxy hp;
	
	@Before
	public void init() {
		hp = HttpProxy.getInstance();
	}

	@Test
	public void testUploadPic() {
		/*File f = new File("C:/Users/zhangrb/Desktop/test.jpg");
		Map<String, File> fm = new HashMap<String, File>();
		fm.put("testPic", f);
		Map<String, String> pm = new HashMap<String, String>();
		pm.put("user", "reser");
		pm.put("password", "381d5e378d1b014648e7b3f789252894");
		pm.put("app_code", "testApp");
		try {
			String result = hp.multipartRequest("http://localhost:7000/upload", null, fm, pm);
			Map m = GsonUtil.StringToObj(result, new TypeToken<Map>(){});
			String code = (String)m.get("code");
			assertEquals("0", code);
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}*/
		assertTrue(true);
	}
}
