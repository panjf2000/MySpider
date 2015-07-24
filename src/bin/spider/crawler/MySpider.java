package bin.spider.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

import bin.spider.frame.CrawlController;
import bin.spider.util.SettingsXMLUtil;

public class MySpider {
	static Logger logger = LoggerFactory.getLogger(MySpider.class);

	public static void main(String[] args) {
		CrawlController controller = new CrawlController();
		//controller初始化，包含Pool、Settings、Frontier、Processor
		controller.initialize();
		while (!controller.atFinish()) {
			//do nothing
		}
		controller.toeEnded();
		controller = null;
		logger.info("CrawlController has Finished");
	}
}
