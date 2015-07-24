package bin.spider.frontier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bin.httpclient.util.ResponseContent;
import bin.httpclient3.uri.URIException;
import bin.spider.frame.CrawlController;
import bin.spider.frame.Frontier;
import bin.spider.frame.FrontierJournal;
import bin.spider.frame.FrontierMarker;
import bin.spider.frame.ToePool;
import bin.spider.frame.uri.CandidateURI;
//import bin.spider.frame.muCuri.CrawlURI;
import bin.spider.frame.uri.CrawlURI;
import bin.spider.frame.uri.UURI;
import bin.spider.frame.uri.UURIFactory;
import bin.spider.queue.ToDoQueue;
import bin.spider.queue.VisitedQueue;
import bin.spider.util.SettingsXMLUtil;
import bin.spider.util.StringUtils;

public class QueueFrontier implements Frontier{
	
	Logger logger = LoggerFactory.getLogger(QueueFrontier.class);

	// 列表中保存了还未被抓取的链接
//  List pendingURIs = new ArrayList();
//	ConcurrentLinkedQueue <String> urlQueue = new ConcurrentLinkedQueue <String>();
 
  // 这个列表中保存了一系列的链接，它们的优先级
  // 要高于pendingURIs那个List中的任何一个链接
  // 表中的链接表示一些需要被满足的先决条件
//  List prerequisites = new ArrayList();
 
  // 一个HashMap，用于存储那些已经抓取过的链接
//  Map alreadyIncluded = new HashMap();
//	Set<String> visitedUrlQueue = Collections.synchronizedSet(new HashSet<String>());
 
  // CrawlController对象
  CrawlController controller;
  // 用于标识是否一个链接正在被处理
//  boolean uriInProcess = false;
 
  // 成功下载的数量
  long successCount = 0;
  // 失败的数量
  long failedCount = 0;
  // 抛弃掉链接的数量
  long disregardedCount = 0;
  // 总共下载的字节数
  long totalProcessedBytes = 0;
  //产品总数
  public static long totalMobile = 1;
  
  //已经下载的产品数
  long mobileCount = 0;
  
  int code100 = HttpStatus.SC_CONTINUE;
  int code101 = HttpStatus.SC_SWITCHING_PROTOCOLS;
  int code102 = HttpStatus.SC_PROCESSING;
  
  int code200 = HttpStatus.SC_OK;
  int code201 = HttpStatus.SC_CREATED;
  int code202 = HttpStatus.SC_ACCEPTED;
  int code203 = HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION;
  int code204 = HttpStatus.SC_NO_CONTENT;
  int code205 = HttpStatus.SC_RESET_CONTENT;
  int code206 = HttpStatus.SC_PARTIAL_CONTENT;//部分请求，如断点续传，应重试
  int code207 = HttpStatus.SC_MULTI_STATUS;
  
  int code300 = HttpStatus.SC_MULTIPLE_CHOICES;
  int code301 = HttpStatus.SC_MOVED_PERMANENTLY;
  int code302 = HttpStatus.SC_MOVED_TEMPORARILY;
  int code303 = HttpStatus.SC_SEE_OTHER;
  int code304 = HttpStatus.SC_NOT_MODIFIED;
  int code305 = HttpStatus.SC_USE_PROXY;
  int code307 = HttpStatus.SC_TEMPORARY_REDIRECT;
  
  int code400 = HttpStatus.SC_BAD_REQUEST;
  int code401 = HttpStatus.SC_UNAUTHORIZED;
  int code402 = HttpStatus.SC_PAYMENT_REQUIRED;
  int code403 = HttpStatus.SC_FORBIDDEN;
  int code404 = HttpStatus.SC_NOT_FOUND;
  int code405 = HttpStatus.SC_METHOD_NOT_ALLOWED;
  int code406 = HttpStatus.SC_NOT_ACCEPTABLE;
  int code407 = HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED;
  int code408 = HttpStatus.SC_REQUEST_TIMEOUT;
  int code409 = HttpStatus.SC_CONFLICT;
  int code410 = HttpStatus.SC_GONE;
  int code411 = HttpStatus.SC_LENGTH_REQUIRED;
  int code412 = HttpStatus.SC_PRECONDITION_FAILED;
  int code413 = HttpStatus.SC_REQUEST_TOO_LONG;
  int code414 = HttpStatus.SC_REQUEST_URI_TOO_LONG;
  int code415 = HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE;
  int code416 = HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE;
  int code417 = HttpStatus.SC_EXPECTATION_FAILED;
  int code419 = HttpStatus.SC_INSUFFICIENT_SPACE_ON_RESOURCE;
  int code420 = HttpStatus.SC_METHOD_FAILURE;
  int code422 = HttpStatus.SC_UNPROCESSABLE_ENTITY;
  int code423 = HttpStatus.SC_LOCKED;
  int code424 = HttpStatus.SC_FAILED_DEPENDENCY;

  int code500 = HttpStatus.SC_INTERNAL_SERVER_ERROR;
  int code501 = HttpStatus.SC_NOT_IMPLEMENTED;
  int code502 = HttpStatus.SC_BAD_GATEWAY;
  int code503 = HttpStatus.SC_SERVICE_UNAVAILABLE;
  int code504 = HttpStatus.SC_GATEWAY_TIMEOUT;    
  int code505 = HttpStatus.SC_HTTP_VERSION_NOT_SUPPORTED;    
  int code507 = HttpStatus.SC_INSUFFICIENT_STORAGE;
  
  private ConcurrentHashMap<String, Integer> failedMap = new ConcurrentHashMap<String, Integer>();
  
	@Override
	public void initialize(CrawlController c) throws IOException {
		// 注入
        this.controller = c;
        
        // 把种子文件中的链接加入到pendingURIs中去
//        this.controller.getScope().refreshSeeds();
//        List seeds = this.controller.getScope().getSeedlist();
        loadSeeds();
	}

	@Override
	public CrawlURI next() throws InterruptedException {
		synchronized(this){
//			if (!uriInProcess && !isEmpty()) {
//	            uriInProcess = true;
			if (!isEmpty()) {
	            String url = ToDoQueue.outElem();
//	            logger.info("CrawlURi next():"+url);
	//            curi.setServer(controller.getServerCache().getServerFor(curi));
	            try {
					return new CrawlURI(UURIFactory.getInstance(url));
				} catch (Exception e) {
					e.printStackTrace();
					logger.info(url);
				}
	            
	        } else {
	        	//达到目标数量，停止抓取
	        	if(mobileCount==totalMobile){
	        		controller.getToePool().setSize(0);
	        		//set state to stopping
	        		controller.sendCrawlStateChangeEvent(controller.STOPPING,"stop");
//	        		ToePool
	        	}else {
	        		wait(1000);
				}
	        }
		}
		return null;
	}

	@Override
	public boolean isEmpty() {
		return ToDoQueue.isEmpty();
	}

//	@Override
//	public void schedule(CrawlURI curi) {
////		String url = curi.getResponse().getFromUrl();
//		String url = curi.toString();
//        if (!visitedUrlQueue.contains(url)) {
//            urlQueue.add(url);
//            // HashMap中使用url的字符串来做为key
//            // 而将实际的CadidateURI对象做为value
//            visitedUrlQueue.add(url);
//        }
//	}

	@Override
	public void finished(CrawlURI curi) {
		synchronized(this){
//			uriInProcess = false;
		    int statusCode = curi.getFetchStatus();
	        // 成功下载
	        if (statusCode==code200||statusCode==code203) {
	            successCount++;
	          //网页才算totalMobile,用于PCOnline
	            if (curi.toString().matches("(http:|https:)([\\w/.]+)\\d+.html")) {
					mobileCount++;
				}
	            // 统计下载总数
//	            totalProcessedBytes += curi.getResponseContent().getContentLength();
	            // 如果成功，则触发一个成功事件
	            // 比如将Extractor解析出来的新URL加入队列中
	            failedMap.remove(curi.toString());
	            VisitedQueue.addElem(curi.toString());
	            controller.fireCrawledURISuccessfulEvent(curi);
	            curi.stripToMinimal();
	        }// 需要推迟下载
	        else if (statusCode == code408 || statusCode == code304
	        		|| statusCode == code409 || statusCode == code413
	        		|| statusCode == 421 || statusCode == code424 
	        		|| statusCode == code502 || statusCode == code504
	        		|| statusCode == code507 || statusCode == 404) {
	        	addFialedMapCountAndRetry(curi.toString());
//	        	visitedUrlQueue.remove(curi.getResponse().getFromUrl());
//	        	VisitedQueue.removeElem(curi.toString());
//	            schedule(curi);
	        }// 抛弃当前URI
	        else {
	            controller.fireCrawledURIDisregardEvent(curi);
	            disregardedCount++;
	            curi.stripToMinimal();
	        }
	        curi.processingCleanup();
		}
	}

	@Override
	public long discoveredUriCount() {
		return ToDoQueue.size()+VisitedQueue.size();
	}

	@Override
	public long queuedUriCount() {
		return ToDoQueue.size();
	}

	@Override
	public long deepestUri() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long averageDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float congestionRatio() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long finishedUriCount() {
		return successCount + failedCount + disregardedCount;
	}

	@Override
	public long succeededFetchCount() {
		return successCount;
	}

	@Override
	public long failedFetchCount() {
		return failedCount;
	}

	@Override
	public long disregardedUriCount() {
		return disregardedCount;
	}

	@Override
	public long totalBytesWritten() {
		return totalProcessedBytes;
	}

	@Override
	public void importRecoverLog(String pathToLog, boolean retainFailures)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FrontierMarker getInitialMarker(String regexpr, boolean inCacheOnly) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<String> getURIsList(FrontierMarker marker,
			int numberOfMatches, boolean verbose) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long deleteURIs(String match) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long deleteURIs(String uriMatch, String queueMatch) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void deleted(CrawlURI curi) {
//		String url = curi.getResponse().getFromUrl();
		String url = curi.toString();
		if(ToDoQueue.isContains(url))
			ToDoQueue.removeElem(url);
		
	}

//	@Override
//	public void considerIncluded(CrawlURI curi) {
////		String url = curi.getResponse().getFromUrl();
//		String url = curi.toString();
//		if (visitedUrlQueue.contains(url))
//			return;
//		else
//			urlQueue.add(url);
//	}

	@Override
	public void kickUpdate() {
		this.controller.kickUpdate();
	}

	@Override
	public void pause() {
		synchronized(this){
			try {
				logger.info("wait 1000");
				this.wait(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void unpause() {
		synchronized(this){
			this.notify();
		}
		
	}

	@Override
	public void terminate() {
		
	}

	@Override
	public FrontierJournal getFrontierJournal() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public String getClassKey(CrawlURI curi) {
////		return curi.getResponse().getFromUrl();
//		return curi.getClassKey();
//	}

	@Override
	public void loadSeeds() {
		List<String> seeds = StringUtils.getSedds();
		for(String seed:seeds){
			logger.info(seed);
			ToDoQueue.addElem(seed);
		}
		
//        synchronized(seeds) {
//        	for(String seed:seeds){
//        		CrawlURI curi;
//				try {
//					curi = new CrawlURI(UURIFactory.getInstance(seed));
//					schedule(curi);
//				} catch (URIException e) {
//					e.printStackTrace();
//				}
////                CrawlURI curi = new CrawlURI(seed);            
//            }
//        }
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public FrontierGroup getGroup(CrawlURI curi) {
		return null;		
	}

	@Override
	public void finalTasks() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void schedule(CandidateURI caURI) {
		String url = caURI.toString();
		synchronized (this) {
			if (!VisitedQueue.isContains(url)) {
	            ToDoQueue.addElem(url);
	            // HashMap中使用url的字符串来做为key
	            // 而将实际的CadidateURI对象做为value
//	            visitedUrlQueue.add(url);
	        }
		}
	}

	@Override
	public void considerIncluded(UURI u) {
//		String url = u.toString();
//		if (!visitedUrlQueue.contains(u.toString())) {
//			urlQueue.add(url);
//            // HashMap中使用url的字符串来做为key
//            // 而将实际的CadidateURI对象做为value
//            visitedUrlQueue.add(url);
//        }
		synchronized (this) {
			String url = u.toString();
			if (VisitedQueue.isContains(url))
				return;
			else
				ToDoQueue.addElem(url);
		}
		
	}

	@Override
	public String getClassKey(CandidateURI cauri) {
		return cauri.getClassKey();
	}

	@Override
	public void removeFromFailedMap(String url) {
		failedMap.remove(url);
	}

	@Override
	public void addFialedMapCountAndRetry(String url) {
		synchronized (failedMap) {
			int count = 1;
			if (null==failedMap.get(url)) {
				failedMap.put(url, count);
			}else {
				count = failedMap.get(url)+1;
				failedMap.put(url, count);	
			}				
			if (count<=3){
				try {
					Thread.currentThread().sleep(2000);
					//网络状况在一定时间内不变，所以不立即重试
					schedule(new CrawlURI(UURIFactory.getInstance(url)));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URIException e) {
					e.printStackTrace();
				}	
			}else {
				failedCount++;
			}
		}
	}	

}
