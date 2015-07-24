package bin.spider.writer;

import bin.spider.frame.Processor;
import bin.spider.frame.uri.CrawlURI;
//import bin.spider.frame.muCuri.CrawlURI;
import bin.spider.util.FileUtil;

public class MirrorWriter extends Processor{
	
	public MirrorWriter(String name) {
		super(name, "PcOnlineIndexWriter just used for pconline");
	}

	public MirrorWriter(String name, String description) {
		super(name, description);
	}
	
	@Override
	protected void innerProcess(CrawlURI curi) {
        if (!curi.isSuccess()) {
            return;
        }
        FileUtil.saveTask(curi.getResponseContent());
	}

}
