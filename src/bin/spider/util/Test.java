package bin.spider.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Test {

	public static void get(String url) {
		try {
			Document doc = Jsoup.connect(url).get();
			Elements imgs = doc.select("img[src$=.jpg]");
			ArrayList<String> urls = new ArrayList<String>();
			String imgRegex = "http://img.pconline.com.cn/images/product/";
			Pattern imgPattern = Pattern.compile(imgRegex);
			Matcher m;
			for (Element element : imgs) {
				String imgUrl = element.attr("abs:src");
				m = imgPattern.matcher(imgUrl);
				if (m.find()) {
					urls.add(imgUrl);
				}
			}
			String aRegex = "http://product.pconline.com.cn/mobile/.*\\.html";
			Pattern aPattern = Pattern.compile(aRegex);
			Elements aTags = doc.select("a");
			
			ArrayList<String> links = new ArrayList<String>();
			for (Element element : aTags) {
				String link = element.attr("abs:href");
				m = aPattern.matcher(link);
				if (m.find()) {
					links.add(link);
				}
			}
			//test
//			String uri = "http://product.pconline.com.cn/mobile/21ke/0531/index.html";
//			m = aPattern.matcher(uri);
//			System.out.println(m);
//			if (m.find()) {
//				links.add(uri);
//			}
			System.out.println(imgs);
			System.out.println(links);
//			System.out.println(doc.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		String url = "http://product.pconline.com.cn/mobile/21ke/";
//		get(url);
		getConstructor();
	}

	private static void getConstructor() {
		Class classType;
		try {
//			bin.spider.extractor.PcOnlineExtractor
			classType = Class.forName("bin.spider.extractor.PcOnlineExtractor");
			Constructor<?> cons[]=classType.getConstructors();
			System.out.println(cons[0].getParameterCount());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
		
	}
}
