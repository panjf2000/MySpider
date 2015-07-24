# Myspider

MySpider项目，由MySpider类启动，但核心调度类是CrawlController

CrawlController是最中心的调度类，其在initialize方法中，先调用SettingXMl类，读取Settings.xml配置文件。其次，通过SettingXMl类，向CrawlController注入特定的Frontier，此次使用的是QueueFrontier，并读取种子文件seed.txt，加入ToDoQueue得到初始抓取的URL。然后，初始化ProcessorChainList，通过SettingXML得到特定的处理器链。最后，调用ToePool，根据配置创建制定数量的ToeThread线程，线程从ToDOQueue中得到URL，开始抓取工作。最后，MySpider通过while循环，得到CrawlController的信号，抓取完成则退出整个爬虫程序。
 

CrawlController是抓取的调度类，主要负责URL队列的维护。其在intialize方法中注入了对应的CrawlController，并通过loadSeeds方法读取种子文件，将URL加入ToDOQueue。AddFialedMapCountAndRetry是失败重试的方法，失败3次后，抛弃该URL。Schedule方法是将URL处理，加入ToDoQueue。Next方法则是返回ToDOQueue最上层的URL，finished方法在抓取成功后调用，将URL加入VisitedQueue。
 

处理器链:
1.	PreFetchs链，继承Processor基类，用于在抓取前的相关处理。本系统未做任何处理，使用PreconditionEnforcer类

2.	Fetchs链，继承Processor基类，用于抓取URL对应的网页。这里会使用到bin.httpclient.util包中的类，利用HttpClient进行请求，并将Response封装到ResponseContent实体类。为了防止封IP，自动切换代理。对于异常情况，自动加入ToDoQueue重试。失败3次，则抛弃该链接。
使用的是FetchHttpAndHttps，innerProcess中实现的伪代码如下所示：
protected void innerProcess(final CrawlURI curi){
String url = curi.toString();    //将封装的CrawlURI转换成对应的字符串
ResponseContent rspc = HttpHelper.getUrlRespContent(url);
//得到封装的Response
		if (null==rspc) {
			curi.setFetchStatus(404);//404 not found，找不到相关资源
			return;
		}
curi.setResponseContent(rspc);
}
3.	Extractors链，继承Extractor基类，用于对网页信息的过滤处理，抽取相关链接。针对太平洋手机频道，定制了PcOnlineExtractor，使用Jsoup对网页特定链接进行抽取，加入到ToDoQueue中。
使用的是PcOnlineExtractor，innerProcess中实现的伪代码如下所示：
protected void innerProcess(final CrawlURI curi){
if (!curi.isSuccess())//如果抓取失败则退出该方法
        	return;
		String url = curi.toString();
		if (isIndex()) {//如果是主页，则进入对应的主页分析方法
			extractIndex(curi);
		}else if (isBrand()) {//如果是
			extractBrand(curi);
		}else {//图片、或具体手机页面，不用进行链接抽取
		}
}
4.	Writers链，继承Processor基类，将网页、图片保存到磁盘上，默认采用镜像方式保存。针对太平洋手机频道，定制了PcOnlineIndexWriter，使用Jsoup抽取信息，调用bin.spider.util中的FileUtil保存。
使用的是PcOnlineIndexWriter，innerProcess中实现的伪代码如下所示：
protected void innerProcess(CrawlURI curi) {
			if (!curi.isSuccess())
            	return;
			write(curi);
	}
public void write(CrawlURI curi) {
	if(isImg()){//如果是图片，则直接保存
		FileUtil.saveTask(rspc);
	}else{//如果是网页，用Jsoup抽取需要的内容，只保存抽取的内容
		FileUtil.saveTask(jsoupParse(rspc));
//JsoupParse解析网页，抽取需要的内容。而FileUtil.saveTask负责开启线程写入磁盘
		}
}
5.	PostProcessors链，继承Processor基类，用于抓取后的处理。本系统未做任何处理,执行采集子系统，在4M的网速下，5线程抓取，1小时内抓取太平洋手机频道完毕。直接使用镜像保存，和使用IndexWriter，其对比明显，证明爬虫取得初步成功。其存储结果如下图所示（mobile是IndexWriter保存的，pconline_mobile是镜像保存的）：
