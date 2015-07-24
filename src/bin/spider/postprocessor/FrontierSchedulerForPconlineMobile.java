package bin.spider.postprocessor;

//import bin.spider.frame.muCuri.CrawlURI;
import bin.spider.frame.uri.CandidateURI;
import bin.spider.frame.uri.CrawlURI;
import bin.spider.queue.ToDoQueue;
import bin.spider.queue.VisitedQueue;

public class FrontierSchedulerForPconlineMobile extends FrontierScheduler{
	
	
	public FrontierSchedulerForPconlineMobile(String name) {
		super(name);
	}

//	public static void main(String[] args) {
//		SettingsXMLUtil.Parse();
//		System.out.println(this.getClass().getName());
//		System.out.println(SettingsXMLUtil.getPOSTPROCESSOR().get(0));
//		System.out.println(SettingsXMLUtil.isInPostprocessor(FrontierSchedulerForPconlineMobile.class.getName()));
//	}

	// 一次抓取结束后所进行的操作，该操作由线程池
    // 中的线程来进行调用
	/** addOutLink(),将所有链接暂时存在curi里面，在Frontier中处理。
	 * 原来此方法是用来过滤链接用的，由于使用Jsoup，这里不做任何事情
	**/
	@Override
	protected void schedule(CrawlURI cduri) {
//		Boolean bool = false;
//	    if (200==cduri.g) {
//			VisitedQueue.addElem(curi.toString());
//		}else {
//			ToDoQueue.addElem(curi.toString());
//		}
	}
}
