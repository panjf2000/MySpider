package bin.spider.fetch;

import bin.httpclient.util.HttpHelper;
import bin.httpclient.util.ResponseContent;
import bin.spider.frame.Processor;
import bin.spider.frame.uri.CrawlURI;

public class FetchHttpAndHttps extends Processor{
	
	public FetchHttpAndHttps(String name) {
		super(name, "Fetch Http or Https by HttpClient4.3");
	}


	public FetchHttpAndHttps(String name, String description) {
		super(name, description);
	}
	
	@Override
	protected void innerProcess(final CrawlURI curi)
		    throws InterruptedException {
		ResponseContent rspc = HttpHelper.getUrlRespContent(curi.toString());
		if (null==rspc) {
			curi.setFetchStatus(404);//404 not found
			return;
		}
		curi.setResponseContent(rspc);
	}

}
