package bin.spider.postprocessor;

import bin.spider.frame.FetchStatusCodes;
import bin.spider.frame.Processor;
import bin.spider.frame.uri.CandidateURI;
//import bin.spider.frame.muCuri.CrawlURI;
import bin.spider.frame.uri.CrawlURI;

public class FrontierScheduler extends Processor
implements FetchStatusCodes{

	/**
     * @param name Name of this filter.
     */
    public FrontierScheduler(String name) {
        super(name, "FrontierScheduler. 'Schedule' with the Frontier " +
            "any CandidateURIs carried by the passed CrawlURI. " +
            "Run a Scoper before this " +
            "processor so links that are not in-scope get bumped from the " +
            "list of links (And so those in scope get promoted from Link " +
            "to CandidateURI).");
    }
    
    protected void innerProcess(final CrawlURI curi) {
//    	synchronized(this) {
//            for (CandidateURI cauri: curi.getOutCandidates()) {
//                schedule(cauri);
//            }
//        }
    	schedule(curi);
    }
    
//    /**
//     * Schedule the given {@link CandidateURI CandidateURI} with the Frontier.
//     * @param caUri The CandidateURI to be scheduled.
//     */
//    protected void schedule(CandidateURI caUri) {
//        getController().getFrontier().schedule(caUri);
//    }
    
    /**
     * Schedule the given {@link CrawlURI CrawlURI}
     * **/
    protected void schedule(CrawlURI curi) {
		
	}

}
