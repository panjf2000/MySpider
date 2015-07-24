package bin.spider.writer;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bin.httpclient.util.HttpHelper;
import bin.httpclient.util.ResponseContent;
import bin.spider.frame.Processor;
import bin.spider.frame.uri.CrawlURI;
import bin.spider.queue.ToDoQueue;
//import bin.spider.frame.muCuri.CrawlURI;
import bin.spider.util.FileUtil;
import bin.spider.util.SettingsXMLUtil;

public class PcOnlineIndexWriter extends Processor{
	
	final Logger logger = LoggerFactory.getLogger(PcOnlineIndexWriter.class);
	
	public PcOnlineIndexWriter(String name) {
		super(name, "");
	}

	public PcOnlineIndexWriter(String name, String description) {
		super(name, description);
	}

//	private static String[] imgType = {".jpg",".jpeg",".png",".gif"};
	private static final String imgRegrex = ".*(.JPEG|.jpeg|.JPG|.jpg|.GIF|.gif|.BMP|.bmp|.PNG|.png)$";
	
//	public static void main(String[] args) {
//		PcOnlineIndexWriter writer = new PcOnlineIndexWriter();
//		String url1 = "http://product.pconline.com.cn/mobile/htc/548941.html";
//		String url2 = "http://product.pconline.com.cn/mobile/htc/548941_detail.html";
////		ResponseContent rc1 = HttpHelper.getUrlRespContent(url1);
////		ResponseContent rc2 = HttpHelper.getUrlRespContent(url2);
//		writer.write(new ResponseContent(url1));
//		writer.write(new ResponseContent(url2));
//	}
	
	@Override
	protected void innerProcess(CrawlURI curi) {
		if (!curi.isSuccess())
            return;
		write(curi);
	}
	
	public void write(CrawlURI curi) {
//		String url = curi.toString();
//		String url = rspc.getFromUrl();
		ResponseContent rspc = curi.getResponseContent();
		String url = rspc.getDecodeUrl();//一般图片会encode
		
		boolean isImg = url.matches(imgRegrex);
//		String type=null;
//		int size = imgType.length;
//		for (int i=0;!isImg&&i<size;i++) {
//			type = imgType[i];
//			if(url.endsWith(type))
//				isImg = true;
//		}	
		if(isImg){
//			System.out.println("isImg");
			FileUtil.saveTask(rspc);
		}else{
			FileUtil.saveTask(jsoupParse(rspc));
		}
		//test
//		jsoupParse(rspc);
	}
	
	private ResponseContent jsoupParse(ResponseContent rspc){
//		System.out.println(rspc.getContent());
		String url = rspc.getDecodeUrl();
		Document doc = null;
		try {//GB2312的网页编码，但是Jsoup默认parse的时候使用UTF8编码，会乱码
			ByteArrayInputStream in = new ByteArrayInputStream(rspc.getContentBytes());
			doc = Jsoup.parse(in, rspc.getEncoding(), rspc.getFromUrl());
		} catch (Exception e) {
			e.printStackTrace();
		}
//		System.out.println(doc.toString());
		boolean isParam=url.endsWith("_detail.html");
		boolean isProductIndex = url.matches("(http:|https:)(.+)\\d+.html");
//		boolean isIndex=url.endsWith(".html");
		//XXX.html,Introduction of the phone
		Element title = doc.select("h1").first();
		
		if (null==doc||doc.equals(""))
			return null;
		if(isParam){
			Element paramTable = doc.select("table#JparamTable").first();
			rspc.setContent(title.toString()+"\r\n"+paramTable.toString());
//			System.out.println("isParam"+rspc.getContent());
			return rspc;
		}else if (isProductIndex) {//is Product Introduction
			Element bImg = doc.select("div.largerPic img").first();
			rspc.setContent(title.toString()+"\r\n"+bImg.toString());
			ToDoQueue.addElem(bImg.attr("abs:src"));
//			System.out.println("isProductIndex"+rspc.getContent());
			return rspc;
		}else {//Index or ProductList
			logger.info(url);
			Element product = doc.select("div.main").first();
			rspc.setContent(title.toString()+"\r\n"+product.toString());
//			System.out.println("Index or ProductList"+rspc.getContent());
			return rspc;
		}
		
	}
}
