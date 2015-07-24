package test;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import bin.httpclient.util.HttpClientWrapper;

public class UTFTest {

	public static void main(String[] args) {
		String url = 
//				"http://product.pconline.com.cn/mobile/21ke/";
		"http://product.mobile.163.com/SONY/000BMWNB/";
		utfGet(url);
	}
	
	private static void utfGet(String url) {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		RequestConfig requestConfig = RequestConfig.custom()  
			    .setConnectionRequestTimeout(500).setConnectTimeout(500)
			    .setSocketTimeout(500).build();
//		url = HttpClientWrapper.encodeURL(url.trim(), "UTF-8");
		HttpGet request = new HttpGet(url);
		CloseableHttpResponse response = null;
		HttpEntity entity = null;
		request.addHeader(HttpHeaders.CONTENT_TYPE, "text/html;charset=UTF-8");
		request.setConfig(requestConfig);
		request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        entity = response.getEntity(); // 获取响应实体
        StatusLine statusLine = response.getStatusLine();
        System.out.println(statusLine);
        String charset = EntityUtils.getContentCharSet(entity);
        try {
        	byte[] b = EntityUtils.toByteArray(entity);
        	String content = new String(b,charset);
//			EntityUtils.consume(entity);
			File file = new File("F:/utf.html");
//			BufferedWriter bw = new BufferedWriter
//					(new OutputStreamWriter
//							(new FileOutputStream(file), charset));
//			bw.write(content);
//			bw.close();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			bos.write(b);
			bos.flush();
			bos.close();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
//        try {
//			byte[] bytes = EntityUtils.toByteArray(entity);
//			String content = new String(bytes, "UTF-8");
//			System.out.println(content);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
