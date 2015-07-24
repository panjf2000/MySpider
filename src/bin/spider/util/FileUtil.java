package bin.spider.util;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bin.httpclient.util.HttpHelper;
import bin.httpclient.util.ResponseContent;

public class FileUtil {
	static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
	private static String[] ImgType = {".jpg",".jpeg",".png",".gif"};
	private static final String targetEncoding = "UTF-8";
	
//	public static void main(String[] args) {
//		FileUtil util = new FileUtil();
//		ParseThread run1 = util.new ParseThread
//				("http://s.cimg.163.com/i/img2.ph.126.net%2F1r0bEla-ehUCg3ZllO42Mw%3D%3D%2F6608490393934334891.jpg.1000x255.auto.jpg");
//		Thread thread1 = new Thread(run1);
//		thread1.start();
//		ParseThread run2 = util.new ParseThread
//				("http://img.pconline.com.cn/images/product/5700/570024/iPhone6plus_sn.jpgl");
//		Thread thread2 = new Thread(run2);
//		thread2.start();
//		ParseThread run3 = util.new ParseThread
//				("http://product.pconline.com.cn/mobile/21ke/572378_detail.html");
//		Thread thread3 = new Thread(run3);
//		thread3.start();
//		ParseThread run4 = util.new ParseThread
//				("http://product.mobile.163.com/brand/#1ba00");
//		Thread thread4 = new Thread(run4);
//		thread4.start();
//		ParseThread run5 = util.new ParseThread
//				("http://product.mobile.163.com/vivo/000BMWKJ/#B11");
//		Thread thread5 = new Thread(run5);
//		thread5.start();
//		ParseThread run6 = util.new ParseThread
//				("http://product.mobile.163.com/vivo/000BMWKJ/param.html#8B2");
//		Thread thread6 = new Thread(run6);
//		thread6.start();
//	}
	
	static class ParseTask implements Callable<Boolean>{
		ResponseContent rspc;
		
		ParseTask(ResponseContent rspc){
			this.rspc = rspc;
		}

		@Override
		public Boolean call() throws Exception {
			return saveFile(rspc);
		}
		
	}
	
	public static boolean saveTask(ResponseContent rspc) {
		ParseTask task = new ParseTask(rspc);
        FutureTask<Boolean> futureTask = new FutureTask<Boolean>(task);
        Thread thread = new Thread(futureTask);
        thread.start();
        //如果线程内部的事情还未处理完，即还没有返回结果，则会阻塞  
        boolean isDone = futureTask.isDone();  
        while(!isDone){
            isDone = futureTask.isDone();  
        }
        try {
			return futureTask.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/** a method deal with url ,then judge whether it is img or not,
	 * and use  saveFile(String path,byte[] b)
	 * @param rspc --- bean parse by Jsoup
	 * */
	public static boolean saveFile(ResponseContent rspc) {
		String url = rspc.getDecodeUrl();
//		byte b[]= rspc.getContentBytes();
			//AUX 解析异常
		url = url.replaceAll("/(#.*)$", "")//替换JavaScript的#符号后缀
					.replace("http://", SettingsXMLUtil.getSTOREPATH());//替换成本地文件夹
		boolean isIndexHtml = url.endsWith("/");
		boolean isImg = false;
		try{
			if (isIndexHtml){//包含大量型号的品牌页面
				String path = url+"index.html";
				logger.info(path);
				return saveFile(path,rspc.getContent().getBytes());
			}else if(url.endsWith(".html")){//产品具体页面，包括概览和参数页面
				//由于可能使用了IndexWriter，改变了content，但没改byte需要从content getBytes
				return saveFile(url,rspc.getContent().getBytes());
			}else{//除此之外就是图片,
				logger.info(url);
				return saveFile(url,rspc.getContentBytes());
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
//	public static boolean saveFile(String url) {
//		ResponseContent responseContent = HttpHelper.getUrlRespContent(url);
//		byte b[]= responseContent.getContentBytes();
//		try {
//			url = URLDecoder.decode(url, "UTF-8");
//			url = url.replaceAll("#.*$", "")//替换JavaScript的# 符号后缀
//					.replace("http://", "E:/mobile/");//替换成本地文件夹
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		boolean isIndexHtml = url.endsWith("/");
//		boolean isInclude = false;
//		if (isIndexHtml){
//			String path = url+"index.html";
//			logger.info(path);
//			return saveFile(path,b);
//		}else {//除此之外就是.html或者图片,css,js,pdf,word之类的，
//			//检测文件类型是否需要，直接保存
//			for (int i = 0; !isInclude&&i < ImgType.length; i++) {
//				isInclude = url.endsWith(ImgType[i]);
//			}
//			if(isInclude){
//				logger.info(url);
//				return saveFile(url,b);
//			}
//		}	
//		return false;
//	}
	
//	/**专门用正则表达式，抽取出最后/×××/中的字符，用来命名html文件
//	 * **/
//	public static String getPathByRegex(String url) {
//		String regexString = "((.*)+(/[\\w._]+)(/?))";
//		Pattern pattern = Pattern.compile(regexString);		
//		Matcher mc = pattern.matcher(url);
//		Boolean isMatched = mc.matches();
//		int count = mc.groupCount();
//		return url+mc.group(count-1).replaceAll("/", "")+".html";
//	}
	
	public static boolean saveFile(String path,byte[] b) {
		try {
			//jdk7 path
//			path = transFilePath(path);
//			Path target = Paths.get(path);
//			if (Files.notExists(target)) {
//				if (Files.notExists(target.getParent()))
//					Files.createDirectories(target.getParent());
//				Files.createFile(target);
//			}
//			if(Files.size(target)>=b.length)
//				return true;
			File file = new File(path);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			if (file.length()>=b.length) {
				return true;
			}
			//jdk5 nio,会出现乱码（不能直接TXT打开）
//			FileChannel fChannel= new FileOutputStream(path).getChannel();
//	        ByteBuffer buffer=ByteBuffer.wrap(b);
//	        ByteBuffer encodebuffer = ByteBuffer.wrap(Charset.forName(sourceEncoding).decode(buffer).toString().getBytes(targetEncoding));
//	        buffer = null;
//	        encodebuffer.flip();	        
//	        encodebuffer.clear();
//	        while(encodebuffer.hasRemaining()) {
//	        	fChannel.write(encodebuffer);
//	        }       
//	        fChannel.close();
			//writer io
//	        BufferedWriter bw = new BufferedWriter
//					(new OutputStreamWriter
//							(new FileOutputStream(path), sourceEncoding));
//			bw.write(new String(b,sourceEncoding));
//			bw.close();
			//stream io
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
			bos.write(b);
			bos.flush();
			bos.close();
	        return true;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * validate the path in windowsMode,if forbiden,change it
	 * */
	public static String transFilePath(String path){
		String lowerPath = path.toLowerCase();
		String regrex = "/(con|com(\\d?)|prn|aux|nul)/";
		Pattern pattern = Pattern.compile(regrex);
		Matcher matcher = pattern.matcher(lowerPath);
		int count = matcher.groupCount();
		if (count<=0){
			return path;
		}else if (matcher.find()) {
			int mIndex = lowerPath.indexOf(matcher.group(0));
			lowerPath = lowerPath.substring(0,mIndex+1)+lowerPath
					.substring(mIndex+1, lowerPath.length())
					.replace("/", "_mobile/");
			return lowerPath;
		}else {
			return path;
		}			
		
	}
	
}
