package bin.spider.extractor;

import java.util.logging.Level;

import bin.spider.frame.Processor;
//import bin.spider.frame.muCuri.CrawlURI;
import bin.spider.frame.uri.CrawlURI;

public abstract class Extractor extends Processor{

	/**
     * Passthrough constructor.
     * 
     * @param name
     * @param description
     */
    public Extractor(String name, String description) {
        super(name, description);
        // TODO Auto-generated constructor stub
    }

    public void innerProcess(CrawlURI curi) {
        try {
            extract(curi);
        } catch (NullPointerException npe) {
        	npe.printStackTrace();
        } catch (StackOverflowError soe) {
        	soe.printStackTrace();
        } catch (java.nio.charset.CoderMalfunctionError cme) {
        	cme.printStackTrace();
        }
    }

    protected abstract void extract(CrawlURI curi);
}
