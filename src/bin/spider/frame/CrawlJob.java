/* CrawlJob
 *
 * Copyright (C) 2003 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Heritrix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package bin.spider.frame;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import bin.spider.event.CrawlStatusListener;

/**
 * A CrawlJob encapsulates a 'crawl order' with any and all information and
 * methods needed by a CrawlJobHandler to accept and execute them.
 *
 * <p>A given crawl job may also be a 'profile' for a crawl. In that case it
 * should not be executed as a crawl but can be edited and used as a template
 * for creating new CrawlJobs.
 *
 * <p>All of it's constructors are protected since only a CrawlJobHander
 * should construct new CrawlJobs.
 *
 * @author Kristinn Sigurdsson
 *
 * @see org.archive.crawler.admin.CrawlJobHandler#newJob(CrawlJob, String,
 * String, String, String, int)
 * @see org.archive.crawler.admin.CrawlJobHandler#newProfile(CrawlJob,
 *  String, String, String)
 */

public class CrawlJob implements CrawlStatusListener, Serializable {
    /**
     * Eclipse generated serial number.
     */
    private static final long serialVersionUID = 3411161000452525856L;
    
    /*
     * Possible values for Priority
     */
    /** lowest */
    public static final int PRIORITY_MINIMAL = 0;
    /** low */
    public static final int PRIORITY_LOW = 1;
    /** average */
    public static final int PRIORITY_AVERAGE = 2;
    /** high */
    public static final int PRIORITY_HIGH = 3;
    /** highest */
    public static final int PRIORITY_CRITICAL = 4;

    /*
     * Possible states for a Job.
     */
    /** Inital value. May not be ready to run/incomplete. */
    public static final String STATUS_CREATED = "Created";
    /** Job has been successfully submitted to a CrawlJobHandler */
    public static final String STATUS_PENDING = "Pending";
    /** Job is being crawled */
    public static final String STATUS_RUNNING = "Running";
    /** Job was deleted by user, will not be displayed in UI. */
    public static final String STATUS_DELETED = "Deleted";
    /** Job was terminted by user input while crawling */
    public static final String STATUS_ABORTED = "Finished - Ended by operator";
    /** Something went very wrong */
    public static final String STATUS_FINISHED_ABNORMAL =
        "Finished - Abnormal exit from crawling";
    /** Job finished normally having completed its crawl. */
    public static final String STATUS_FINISHED = "Finished";
    /** Job finished normally when the specified timelimit was hit. */
    public static final String STATUS_FINISHED_TIME_LIMIT =
        "Finished - Timelimit hit";
    /** Job finished normally when the specifed amount of 
     * data (MB) had been downloaded */
    public static final String STATUS_FINISHED_DATA_LIMIT =
        "Finished - Maximum amount of data limit hit";
    /** Job finished normally when the specified number of documents had been
     * fetched.
     */
    public static final String STATUS_FINISHED_DOCUMENT_LIMIT =
        "Finished - Maximum number of documents limit hit";
    /** Job is going to be temporarly stopped after active threads are finished. */
    public static final String STATUS_WAITING_FOR_PAUSE = "Pausing - " +
        "Waiting for threads to finish";
    /** Job was temporarly stopped. State is kept so it can be resumed */
    public static final String STATUS_PAUSED = "Paused";
    /**
     * Job is being checkpointed.  When finished checkpointing, job is set
     * back to STATUS_PAUSED (Job must be first paused before checkpointing
     * will run).
     */
    public static final String STATUS_CHECKPOINTING = "Checkpointing";
    /** Job could not be launced due to an InitializationException */
    public static final String STATUS_MISCONFIGURED = "Could not launch job " +
        "- Fatal InitializationException";
    /** Job is actually a profile */
    public static final String STATUS_PROFILE = "Profile";
    
    public static final String STATUS_PREPARING = "Preparing";

    // Class variables
    private String UID;       //A UID issued by the CrawlJobHandler.
    private String name;
    private String status;
    private boolean isReadOnly = false;
    private boolean isNew = true;
    private boolean isProfile = false;
    private boolean isRunning = false;
    private int priority;
    private int numberOfJournalEntries = 0;
    
    private String statisticsFileSave = "";

    private String errorMessage = null;

    private File jobDir = null;

//    private transient CrawlJobErrorHandler errorHandler = null;
//
//    protected transient XMLSettingsHandler settingsHandler;
    
    private transient CrawlController controller = null;
    
    public static final String RECOVERY_JOURNAL_STYLE = "recoveryJournal";
    public static final String CRAWL_LOG_STYLE = "crawlLog";
    
    // OpenMBean support.

    /**
     * Server we registered with. Maybe null.
     */
//    private transient MBeanServer mbeanServer = null;
//    private transient ObjectName mbeanName = null;
//    public static final String CRAWLJOB_JMXMBEAN_TYPE =
//        JmxUtils.SERVICE + ".Job";
//    private transient JEMBeanHelper bdbjeMBeanHelper = null;
    private transient List<String> bdbjeAttributeNameList = null;
    private transient List<String> bdbjeOperationsNameList = null;
    
    
    /**
     * The MBean we've registered ourselves with (May be null
     * throughout life of Heritrix).
     */
//    private transient OpenMBeanInfoSupport openMBeanInfo;
    
    public static final String NAME_ATTR = "Name";
    public static final String UID_ATTR = "UID";
    public static final String STATUS_ATTR = "Status";
    public static final String FRONTIER_SHORT_REPORT_ATTR =
        "FrontierShortReport";
    public static final String THREADS_SHORT_REPORT_ATTR =
        "ThreadsShortReport";
    public static final String TOTAL_DATA_ATTR = "TotalData";
    public static final String CRAWL_TIME_ATTR = "CrawlTime";
    public static final String DOC_RATE_ATTR = "DocRate";
    public static final String CURRENT_DOC_RATE_ATTR = "CurrentDocRate";
    public static final String KB_RATE_ATTR = "KbRate";
    public static final String CURRENT_KB_RATE_ATTR = "CurrentKbRate";
    public static final String THREAD_COUNT_ATTR = "ThreadCount";
    public static final String DOWNLOAD_COUNT_ATTR = "DownloadedCount";
    public static final String DISCOVERED_COUNT_ATTR = "DiscoveredCount";
    public static final String [] ATTRIBUTE_ARRAY = {NAME_ATTR, UID_ATTR,
        STATUS_ATTR, FRONTIER_SHORT_REPORT_ATTR, THREADS_SHORT_REPORT_ATTR,
        TOTAL_DATA_ATTR, CRAWL_TIME_ATTR, DOC_RATE_ATTR,
        CURRENT_DOC_RATE_ATTR, KB_RATE_ATTR, CURRENT_KB_RATE_ATTR,
        THREAD_COUNT_ATTR, DOWNLOAD_COUNT_ATTR, DISCOVERED_COUNT_ATTR};
    public static final List ATTRIBUTE_LIST = Arrays.asList(ATTRIBUTE_ARRAY);
    
    public static final String IMPORT_URI_OPER = "importUri";
    public static final String IMPORT_URIS_OPER = "importUris";
    public static final String DUMP_URIS_OPER = "dumpUris";
    public static final String PAUSE_OPER = "pause";
    public static final String RESUME_OPER = "resume";
    public static final String FRONTIER_REPORT_OPER = "frontierReport";
    public static final String THREADS_REPORT_OPER = "threadsReport";
    public static final String SEEDS_REPORT_OPER = "seedsReport";
    public static final String CHECKPOINT_OPER = "startCheckpoint";
    public static final String PROGRESS_STATISTICS_OPER =
        "progressStatistics";
    public static final String PROGRESS_STATISTICS_LEGEND_OPER =
        "progressStatisticsLegend";
    
    public static final String PROG_STATS = "progressStatistics";
    
    // Same as JEMBeanHelper.OP_DB_STAT
    public static final String OP_DB_STAT = "getDatabaseStats";
    
    /**
     * Don't add the following crawl-order items.
     */
    public static final List ORDER_EXCLUDE;
    static {
        ORDER_EXCLUDE = Arrays.asList(new String [] {"bdb-cache-percent",
            "extract-processors", "DNS", "uri-included-structure"});
    }
    
    /**
     * Sequence number for jmx notifications.
     */
    private static int notificationsSequenceNumber = 1;
    
    /**
     * A shutdown Constructor.
     */
    protected CrawlJob() {
        super();
    }

    public CrawlController getController() {
        return this.controller;
    }

	@Override
	public void crawlStarted(String message) {
		
	}

	@Override
	public void crawlEnding(String sExitMessage) {
		
	}

	@Override
	public void crawlEnded(String sExitMessage) {
		
	}

	@Override
	public void crawlPausing(String statusMessage) {
		
	}

	@Override
	public void crawlPaused(String statusMessage) {
		
	}

	@Override
	public void crawlResuming(String statusMessage) {
		
	}

	@Override
	public void crawlCheckpoint(File checkpointDir) throws Exception {
		
	}

}
