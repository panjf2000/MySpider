package bin.spider.extractor;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import bin.httpclient.util.HttpHelper;
import bin.httpclient.util.ResponseContent;
import bin.httpclient3.uri.URIException;
import bin.spider.frame.uri.CrawlURI;
import bin.spider.frame.uri.UURIFactory;

public class PcOnlineExtractorTest {
	
	PcOnlineExtractor extractor;

	@Before
	public void setUp() throws Exception {
		extractor = new PcOnlineExtractor("Extractor");
	}

	@Test
	public void test() {
		CrawlURI curi = null;
		try {
			curi = new CrawlURI(UURIFactory.getInstance
					("http://product.pconline.com.cn/mobile/"));
		} catch (URIException e1) {
			e1.printStackTrace();
		}
		ResponseContent rspc = HttpHelper.getUrlRespContent(curi.toString());
		if (null==rspc) {
			curi.setFetchStatus(404);//404 not found
		}
		curi.setResponseContent(rspc);
		extractor.extract(curi);
		return;
//		fail("Not yet implemented");
	}

}
