package test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import bin.spider.util.FileUtil;
import bin.spider.util.SettingsXMLUtil;

public class FileTest {

	public static void main(String[] args) {
		String url = "http://product.pconline.com.cn/mobile/aux/565520.html";
		mkDir(url);
	}
	
	public static void mkDir(String url) {
		try {
			//AUX 解析异常
			url = URLDecoder.decode(url, "UTF-8");
			url = url.replaceAll("#.*$", "");//替换JavaScript的# 符号后缀
			url = url.replace("http://", "E:/mobile/");//替换成本地文件夹
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		boolean isIndexHtml = url.endsWith("/");
		boolean isImg = false;
		if (isIndexHtml){//包含大量型号的品牌页面
			String path = url+"index.html";
			System.out.println(path);
			FileUtil.transFilePath(path);
		}else if(url.endsWith(".html")){//产品具体页面，包括概览和参数页面
			//由于可能使用了IndexWriter，改变了content，但没改byte需要从content getBytes
			FileUtil.transFilePath(url);
		}else{//除此之外就是图片,
			FileUtil.transFilePath(url);
		}
	}
}
