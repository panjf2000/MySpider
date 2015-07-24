package bin.spider.prefetch;

import bin.spider.frame.Processor;
import bin.spider.frame.uri.CrawlURI;

public class PreconditionEnforcer extends Processor{
	

	public PreconditionEnforcer(String name) {
		super(name, "PreFetch");
	}
	
	public PreconditionEnforcer(String name, String description) {
		super(name, description);
	}

	@Override
	protected void innerProcess(CrawlURI curi) throws InterruptedException {
		super.innerProcess(curi);
	}
	
	

}
