package bin.spider.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import bin.httpclient.util.HttpHelper;
import bin.httpclient.util.ResponseContent;
import bin.spider.dbutils.JDBCHelper;
import bin.spider.frame.uri.CrawlURI;
import bin.spider.frontier.QueueFrontier;
//import bin.spider.frame.muCuri.CrawlURI;
import bin.spider.queue.ToDoQueue;
import bin.spider.util.SettingsXMLUtil;

public class PcOnlineExtractor extends Extractor{

	private final String indexList = "http://product.pconline.com.cn/mobile/";
	
	public PcOnlineExtractor(String name) {
		super(name, "PcOnlineExtractor,just used for product.pconline.com.cn/mobile/");
	}
	
	public PcOnlineExtractor(String name, String description) {
		super(name, description);
	}

	
//	public static void main(String[] args) {
//		String url = "http://product.pconline.com.cn/mobile/";
//		extract(url);
//	}
	@Override
	protected void extract(CrawlURI curi) {
		if (!curi.isSuccess())
            return;
		String url = curi.toString();
//		Pattern pt = Pattern.compile("(http://)?([\\w.]+/)");
		
//		if (url.endsWith(".html")) {
//			// 将链接加入到待处理列表中
//			addLinkFromString(url);
//		}else 
		if (indexList.equals(url)) {
//			System.out.println("is Index");
			extractIndex(curi);
		}else if (url.endsWith(".shtml")||url.matches("(http:|https:)//(.+/){3}")) {
			extractBrand(curi);
		}else {//pictures like jpg/png,or XX.html
			//do noting,just wait for download and then write it to disk
			//有了Fetcher,就不需要了
//			extractHtmlAndImg(curi);
		}
	}
	
//	public void extract(CrawlURI curi){
//		String url = curi.getResponse().getFromUrl();
//		Pattern pt = Pattern.compile("(http://)?([\\w.]+/)");
//		
//		if (url.endsWith(".html")) {
//			// 将链接加入到待处理列表中
//			addLinkFromString(url);
//		}else if (indexList.equals(url)) {
//			extractIndex(url);
//		}else if (url.matches(".*/([\\w]+/){2}")) {
//			extractBrand(url);
//		}else {//pictures like jpg/png
//			addLinkFromString(url);
//		}		
//	}
	
	//原来没有Fetcher，现在有了，就不需要getResponseContent(),statusCode已经设置好
//	public static void extractHtmlAndImg(CrawlURI curi) {
//		//先执行下载，getResponseContent()方法会设置statusCode
//		ResponseContent response =  curi.getResponseContent();
//	}

//	public static void extractBrand(CrawlURI curi) {
//		ResponseContent response =  curi.getResponseContent();
//		extractBrand(curi.toString());
//	}
	
	/**处理初始mobile页面，抽取出各品牌的产品页面链接
	 * **/
	private void extractBrand(CrawlURI curi) {
		ResponseContent response =  curi.getResponseContent();	
		Document document = Jsoup.parse(response.getContent(),response.getFromUrl());
		Element next = document.select("div.content a.next").first();
		ArrayList<String> list = new ArrayList<String>();
		if(null!=next&&!next.equals(""))
			list.add(next.attr("abs:href").trim());
		Elements urlsElements = document.select("div.content li div.product-pic>a");
//		System.out.println(urlsElements.size()*2);
		String link = "";
		for (int i = 0; i < urlsElements.size(); i++) {
//			System.out.println(urlsElements.toString());
			link = urlsElements.get(i).attr("abs:href").trim();//index.html
//			paramLink= link.replaceAll(".html", "_detail.html");
			//javascript:void(0);这种href解析出来是空串""
//			boolean isunder = link.matches("http://product.pconline.com.cn/mobile/zte/"+"\\w+\\.html");
			if (link!=""&&null!=link){
				list.add(link);//index.html
				list.add(link.replaceAll(".html", "_detail.html"));//param.html
			}		
		}
		// 将链接加入到待处理列表中
		addLinkFromList(list);
	}
	
	/**处理初始mobile页面，一次循环抽取出品牌的所有产品页面链接
	 * 注意，这样不利于充分抓取，会一次性加很多链接到内存中
	 * 最终可能导致内存溢出，不推荐使用
	 * **/
	private void extractBrandLoop(String url) {
		ResponseContent response = HttpHelper.getUrlRespContent(url);	
		Document document = Jsoup.parse(response.getContent(),url);
		Element next = document.select("div.content a.next").first();
		ArrayList<String> list = new ArrayList<String>();
		if(null!=next&&!next.equals(""))
			extractBrandLoop(next.attr("abs:href"));
		Elements urlsElements = document.select("div.content li div.product-pic>a");
//		System.out.println(urlsElements.size()*2);
		String link = "";
		for (int i = 0; i < urlsElements.size(); i++) {
//			System.out.println(urlsElements.toString());
			link = urlsElements.get(i).attr("abs:href").trim();//index.html
//			paramLink= link.replaceAll(".html", "_detail.html");
			//javascript:void(0);这种href解析出来是空串""
//			boolean isunder = link.matches("http://product.pconline.com.cn/mobile/zte/"+"\\w+\\.html");
			if (link!=""&&null!=link){
				list.add(link);//index.html
				list.add(link.replaceAll(".html", "_detail.html"));//param.html
			}		
		}
		// 将链接加入到待处理列表中
		addBatchLinkFromList(list);

	}
	/**处理各品牌页面，抽取出各型号的手机主页，并根据ID得到对应的参数页面
	 * 注意，这里同时得到了MobileCount
	 * **/
	private void extractIndex(CrawlURI curi) {
		ResponseContent response =  curi.getResponseContent();
		Document document = Jsoup.parse(response.getContent(),curi.toString());
		Element element = document.select("i#J-number").first();
//		System.out.println(element.text());
		QueueFrontier.totalMobile =Integer.valueOf(element.text());
		Elements urlsElements = document.select("div#J-allBrandList dl a");
//		String sql = "INSERT INTO TOCRAWL (geturl) VALUES(?)";
		ArrayList<String> list = new ArrayList<String>();
		String link = null;
		for (int i = 0; i < urlsElements.size(); i++) {
			link = urlsElements.get(i).attr("abs:href").trim();	
			//javascript:void(0);这种href解析出来是空串""
			if (link!=""&&null!=link){
				list.add(link);
				// 将链接加入到待处理列表中
//				addLinkFromString(link);
			}		
		}
//		for (String string : list) {
//			System.out.println(string);
//		}
		addLinkFromList(list);
		
	}
	
	public static void addBatchLinkFromList(List<String> list) {
		ToDoQueue.addItratorElem(list.iterator());
	}
	
	public static void addLinkFromList(List<String> list) {
		for (String string : list) {
			addLinkFromString(string);
		}
//		String[][] params = new String[list.size()][1];
//		String sql = "INSERT INTO TOCRAWL (geturl) VALUES(?)";
//		//只有一个参数，故二维数组为size行，1列
//		for (int i = 0; i < list.size(); i++) {
//			params[i][0]=list.get(i);
//			if(ToDoQueue.size()<20)
//				ToDoQueue.addElem(list.get(i));
//		}
//		//If parameters aren't need, pass an empty array.
//		//存在BUG，传入空的数组时，不会报错，但最后没有成功Insert
//		boolean bool = JDBCHelper.updateWithBatch(sql, params);
//		while (!ToDoQueue.isEmpty()) {
//			System.out.println(ToDoQueue.outElem());
//		}
//		System.out.println(bool);
	}
	
	private static void addLinkFromString(String uri) {
		 // 通过该方法将URL加入
//		String sql = "INSERT INTO TOCRAWL (geturl) VALUES(?)";
//		boolean bool = JDBCHelper.update(sql, uri);
//		if(ToDoQueue.size()<20)
		ToDoQueue.addElem(uri);
	}

}
