package test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import bin.httpclient.util.HttpClientWrapper;

public class EncodeTest {

	public static void main(String[] args) {
		String url = "http://img.pconline.com.cn/images/product/4519/451928/sj_duowei_S630%282%29_m.jpg";
		testUrl(url);
	}

	private static void testUrl(String url) {
		String enUrl = HttpClientWrapper.encodeURL(url, "UTF-8");
		System.out.println(enUrl);
		try {
			String decodeUrl = URLDecoder.decode(url, "UTF-8");
			System.out.println(decodeUrl);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
	}
}
