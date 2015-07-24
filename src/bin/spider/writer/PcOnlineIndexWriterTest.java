package bin.spider.writer;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

import bin.httpclient.util.HttpHelper;
import bin.httpclient.util.ResponseContent;
import bin.httpclient3.uri.URIException;
import bin.spider.frame.uri.CrawlURI;
import bin.spider.frame.uri.UURIFactory;
import bin.spider.queue.ToDoQueue;
import bin.spider.util.FileUtil;
import bin.spider.util.SettingsXMLUtil;

public class PcOnlineIndexWriterTest {
	
	private static final String imgRegrex = "(http:|https:)([\\w/.-]+)(.JPEG|.jpeg|.JPG|.jpg|.GIF|.gif|.BMP|.bmp|.PNG|.png)$";
	PcOnlineIndexWriter writer = null;

	@Before
	public void setUp() throws Exception {
		writer = new PcOnlineIndexWriter("PcOnlineWriter");
		SettingsXMLUtil.Parse();
	}

	@Test
	public void test() {
		CrawlURI curi = null;
		try {
			curi = new CrawlURI(UURIFactory.getInstance
					("http://product.pconline.com.cn/mobile/samsung/576986.html"));
		} catch (URIException e) {
			e.printStackTrace();
		}
		String url = curi.toString();
		ResponseContent rspc = HttpHelper.getUrlRespContent(url);
		if (null==rspc) {
			curi.setFetchStatus(404);//404 not found
		}
		curi.setResponseContent(rspc);
		writer.innerProcess(curi);
//		if (!curi.isSuccess())
//            return;
//		boolean isImg = url.matches(imgRegrex);
//		if(isImg){
//			System.out.println("just download");
//		}else{
//			Document doc = null;
//			try {//GB2312的网页编码，但是Jsoup默认parse的时候使用UTF8编码，会乱码
//				ByteArrayInputStream in = new ByteArrayInputStream(rspc.getContentBytes());
//				doc = Jsoup.parse(in, rspc.getEncoding(), rspc.getFromUrl());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
////			System.out.println(doc.toString());
//			boolean isParam=url.endsWith("_detail.html");
//			boolean isProductIndex = url.matches("(http:|https:)([\\w/.]+)\\d+.html");
////			boolean isIndex=url.endsWith(".html");
//			//XXX.html,Introduction of the phone
//			Element title = doc.select("h1").first();
//			
//			if (null==doc||doc.equals(""))
//				return;
//			if(isParam){
//				Element paramTable = doc.select("table#JparamTable").first();
//				rspc.setContent(title.toString()+"\r\n"+paramTable.toString());
//				System.out.println("isParam"+rspc.getContent());
//				return;
//			}else if (isProductIndex) {//is Product Introduction
//				Element bImg = doc.select("div.largerPic img").first();
//				rspc.setContent(title.toString()+"\r\n"+bImg.toString());
//				ToDoQueue.addElem(bImg.attr("abs:src"));
//				System.out.println("isProductIndex"+rspc.getContent());
//				return;
//			}else {//Index or ProductList
//				Element product = doc.select("div.main").first();
//				rspc.setContent(title.toString()+"\r\n"+product.toString());
//				System.out.println("Index or ProductList"+rspc.getContent());
//				return;
//			}
//		}
	}

}
