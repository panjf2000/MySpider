package bin.spider.frame;

import java.util.HashMap;



import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

//import bin.spider.frame.muCuri.CrawlURI;
import bin.spider.frame.uri.CrawlURI;;
/**
* One "worker thread"; asks for CrawlURIs, processes them,
* repeats unless told otherwise.
*
* @author Gordon Mohr
*/
public class ToeThread extends Thread implements FetchStatusCodes{
	
	Logger logger = LoggerFactory.getLogger(ToePool.class);
	
	private static final String STEP_NASCENT = "NASCENT";
    private static final String STEP_ABOUT_TO_GET_URI = "ABOUT_TO_GET_URI";
    private static final String STEP_FINISHED = "FINISHED";
    private static final String STEP_ABOUT_TO_BEGIN_CHAIN =
        "ABOUT_TO_BEGIN_CHAIN";
    private static final String STEP_ABOUT_TO_BEGIN_PROCESSOR =
        "ABOUT_TO_BEGIN_PROCESSOR";
    private static final String STEP_DONE_WITH_PROCESSORS =
        "DONE_WITH_PROCESSORS";
    private static final String STEP_HANDLING_RUNTIME_EXCEPTION =
        "HANDLING_RUNTIME_EXCEPTION";
    private static final String STEP_ABOUT_TO_RETURN_URI =
        "ABOUT_TO_RETURN_URI";
    private static final String STEP_FINISHING_PROCESS = "FINISHING_PROCESS";

   private CrawlController controller;
   private int serialNumber;
   
   /**
    * Each ToeThead has an instance of HttpRecord that gets used
    * over and over by each request.
    * 
    * @see org.archive.util.HttpRecorderMarker
    */
//   private HttpRecorder httpRecorder = null;
   
   private HashMap<String,Processor> localProcessors
    = new HashMap<String,Processor>();
   private String currentProcessorName = "";

   private String coreName;
   private CrawlURI currentCuri;
//   private ResponseContent currentRspc;
   private long lastStartTime;
   private long lastFinishTime;

   // activity monitoring, debugging, and problem detection
   private String step = STEP_NASCENT;
   private long atStepSince;
   
   // default priority; may not be meaningful in recent JVMs
   private static final int DEFAULT_PRIORITY = Thread.NORM_PRIORITY-2;
   
   // indicator that a thread is now surplus based on current desired
   // count; it should wrap up cleanly
   private volatile boolean shouldRetire = false;
   
   /**
    * Create a ToeThread
    * 
    * @param g ToeThreadGroup
    * @param sn serial number
    */
   public ToeThread(ToePool g, int sn) {
       // TODO: add crawl name?
       super(g,"ToeThread #" + sn);
       coreName="ToeThread #" + sn + ": ";
       controller = g.getController();
       serialNumber = sn;
       setPriority(DEFAULT_PRIORITY);
//       int outBufferSize = ((Integer) controller
//               .getOrder()
//               .getUncheckedAttribute(null,CrawlOrder.ATTR_RECORDER_OUT_BUFFER))
//                       .intValue();
//       int inBufferSize = ((Integer) controller
//               .getOrder()
//               .getUncheckedAttribute(null, CrawlOrder.ATTR_RECORDER_IN_BUFFER))
//               .intValue();  
//       httpRecorder = new HttpRecorder(controller.getScratchDisk(),
//           "tt" + sn + "http", outBufferSize, inBufferSize);
       lastFinishTime = System.currentTimeMillis();
   }

   /** (non-Javadoc)
    * @see java.lang.Thread#run()
    */
   public void run() {
//       String name = controller.getOrder().getCrawlOrderName();

       try {
           controller.getLoopingToes().incrementAndGet();
           
           while ( true ) {
               // TODO check for thread-abort? or is waiting for interrupt enough?
               continueCheck();
               
               setStep(STEP_ABOUT_TO_GET_URI);
               CrawlURI curi = controller.getFrontier().next();
//               CrawlURI curi = controller.getFrontier().next();    
        	   synchronized(this){
        		   while (null==curi) {
        			   logger.info(coreName+"is waiting 1000ms");
        			   wait(1000);
        			   curi = controller.getFrontier().next();
        		   }
            	   
               }
//               ResponseContent currentRspc = controller.getFrontier().next();
               synchronized(this) {
                   continueCheck();
                   setCurrentCuri(curi);
               }
               
               processCrawlUri();
               
               setStep(STEP_ABOUT_TO_RETURN_URI);
               continueCheck();

               synchronized(this) {
                   controller.getFrontier().finished(currentCuri);
                   setCurrentCuri(null);
//                   setCurrentRspc(null);
               }
               
               setStep(STEP_FINISHING_PROCESS);
               lastFinishTime = System.currentTimeMillis();
               controller.releaseContinuePermission();
               if(shouldRetire) {
                   break; // from while(true)
               }
           }
       } catch (Exception e) {
           // everything else (including interruption)
           e.printStackTrace();
       } finally {
           controller.getLoopingToes().decrementAndGet();
           controller.releaseContinuePermission();
       }
       setCurrentCuri(null);
//       setCurrentRspc(null);
       // Do cleanup so that objects can be GC.
//       this.httpRecorder.closeRecorders();
//       this.httpRecorder = null;
       localProcessors = null;

       setStep(STEP_FINISHED);
       controller.toeEnded();
       controller = null;
   }

   /**
    * Set currentCuri, updating thread name as appropriate
    * @param curi
    */
//   private void setCurrentRspc(ResponseContent rspc) {
//       if(rspc==null) {
//           setName(coreName);
//       } else {
//           setName(coreName+rspc);
//       }
//       currentRspc = rspc;
//   }
   
   /**
    * Set currentCuri, updating thread name as appropriate
    * @param curi
    */
   private void setCurrentCuri(CrawlURI curi) {
       if(curi==null) {
           setName(coreName);
       } else {
           setName(coreName+curi);
       }
       currentCuri = curi;
   }

   /**
    * @param s
    */
   private void setStep(String s) {
       step=s;
       atStepSince = System.currentTimeMillis();
   }

//	private void seriousError(Error err) {
//	    // try to prevent timeslicing until we have a chance to deal with OOM
//       // TODO: recognize that new JVM priority indifference may make this
//       // priority-jumbling pointless
//       setPriority(DEFAULT_PRIORITY+1);  
//       if (controller!=null) {
//           // hold all ToeThreads from proceeding to next processor
//           controller.singleThreadMode();
//           // TODO: consider if SoftReferences would be a better way to 
//           // engineer a soft-landing for low-memory conditions
//           controller.freeReserveMemory();
//           controller.requestCrawlPause();
//           if (controller.getFrontier().getFrontierJournal() != null) {
//               controller.getFrontier().getFrontierJournal().seriousError(
//                   getName() + err.getMessage());
//           }
//       }
//       
//       // OutOfMemory etc.
////       String extraInfo = DevUtils.extraInfo();
//       System.err.println("<<<");
//       System.err.println(ArchiveUtils.getLog17Date());
//       System.err.println(err);
////       System.err.println(extraInfo);
//       err.printStackTrace(System.err);
//       
//       if (controller!=null) {
//           PrintWriter pw = new PrintWriter(System.err);
//           controller.getToePool().compactReportTo(pw);
//           pw.flush();
//       }
//       System.err.println(">>>");
////       DevUtils.sigquitSelf();
//       
//       String context = "unknown";
//		if(currentCuri!=null) {
//           // update fetch-status, saving original as annotation
//           currentCuri.addAnnotation("err="+err.getClass().getName());
//           currentCuri.addAnnotation("os"+currentCuri.getFetchStatus());
//			currentCuri.setFetchStatus(S_SERIOUS_ERROR);
//           context = currentCuri.singleLineReport() + " in " + currentProcessorName;
//		}
//       String message = "Serious error occured trying " +
//           "to process '" + context + "'\n" + extraInfo;
//       logger.log(Level.SEVERE, message.toString(), err);
//       setPriority(DEFAULT_PRIORITY);
//	}

	/**
    * Perform checks as to whether normal execution should proceed.
    * 
    * If an external interrupt is detected, throw an interrupted exception.
    * Used before anything that should not be attempted by a 'zombie' thread
    * that the Frontier/Crawl has given up on.
    * 
    * Otherwise, if the controller's memoryGate has been closed,
    * hold until it is opened. (Provides a better chance of 
    * being able to complete some tasks after an OutOfMemoryError.)
    *
    * @throws InterruptedException
    */
   private void continueCheck() throws InterruptedException {
       if(Thread.interrupted()) {
           throw new InterruptedException("die request detected");
       }
       controller.acquireContinuePermission();
   }
   
   

   /**
    * Pass the CrawlURI to all appropriate processors
    *
    * @throws InterruptedException
    */
   private void processCrawlUri() throws InterruptedException {
       currentCuri.setThreadNumber(this.serialNumber);
       currentCuri.setNextProcessorChain(controller.getFirstProcessorChain());
       lastStartTime = System.currentTimeMillis();
//       logger.info(currentCuri);
       try {
           while (currentCuri.nextProcessorChain() != null) {
               setStep(STEP_ABOUT_TO_BEGIN_CHAIN);
               // Starting on a new processor chain.
               currentCuri.setNextProcessor(currentCuri.nextProcessorChain().getFirstProcessor());
               currentCuri.setNextProcessorChain(currentCuri.nextProcessorChain().getNextProcessorChain());
               
               while (currentCuri.nextProcessor() != null) {
                   setStep(STEP_ABOUT_TO_BEGIN_PROCESSOR);
                   Processor currentProcessor = getProcessor(currentCuri.nextProcessor());
                   currentProcessorName = currentProcessor.getName();
                   continueCheck();
                   
//                   long memBefore = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024;
                   currentProcessor.process(currentCuri);
//                   long memAfter = (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1024;
//                   logger.info((memAfter-memBefore)+"K in "+currentProcessorName);
               }
           }
           setStep(STEP_DONE_WITH_PROCESSORS);
           currentProcessorName = "";
       } catch (AssertionError ae) {
           // This risks leaving crawl in fatally inconsistent state, 
           // but is often reasonable for per-Processor assertion problems 
           recoverableProblem(ae);
       } catch (RuntimeException e) {
           recoverableProblem(e);
       } catch (StackOverflowError err) {
           recoverableProblem(err);
       }
//       } catch (Error err) {
//           // OutOfMemory and any others
//           seriousError(err); 
//       }
   }


   /**
    * Handling for exceptions and errors that are possibly recoverable.
    * 
    * @param e
    */
   private void recoverableProblem(Throwable e) {
       Object previousStep = step;
       setStep(STEP_HANDLING_RUNTIME_EXCEPTION);
       e.printStackTrace(System.err);
       currentCuri.setFetchStatus(S_RUNTIME_EXCEPTION);
       // store exception temporarily for logging
//       currentCuri.addAnnotation("err="+e.getClass().getName());
//       currentCuri.putObject(A_RUNTIME_EXCEPTION, e);
       String message = "Problem " + e + 
               " occured when trying to process '"
               + currentCuri.toString()
               + "' at step " + previousStep 
               + " in " + currentProcessorName +"\n";
   }

   private Processor getProcessor(Processor processor) {
       if(!(processor instanceof InstancePerThread)) {
           // just use the shared Processor
            return processor;
       }
       // must use local copy of processor
       Processor localProcessor = (Processor) localProcessors.get(
                   processor.getName());
       if (localProcessor == null) {
           localProcessor = processor.spawn(this.getSerialNumber());
           localProcessors.put(processor.getName(),localProcessor);
       }
       return localProcessor;
   }

   /**
    * @return Return toe thread serial number.
    */
   public int getSerialNumber() {
       return this.serialNumber;
   }

   /**
    * Used to get current threads HttpRecorder instance.
    * Implementation of the HttpRecorderMarker interface.
    * @return Returns instance of HttpRecorder carried by this thread.
    * @see org.archive.util.HttpRecorderMarker#getHttpRecorder()
    */
//   public HttpRecorder getHttpRecorder() {
//       return this.httpRecorder;
//   }
   
   /** Get the CrawlController acossiated with this thread.
    *
    * @return Returns the CrawlController.
    */
   public CrawlController getController() {
       return controller;
   }

   /**
    * Terminates a thread.
    *
    * <p> Calling this method will ensure that the current thread will stop
    * processing as soon as possible (note: this may be never). Meant to
    * 'short circuit' hung threads.
    *
    * <p> Current crawl uri will have its fetch status set accordingly and
    * will be immediately returned to the frontier.
    *
    * <p> As noted before, this does not ensure that the thread will stop
    * running (ever). But once evoked it will not try and communicate with
    * other parts of crawler and will terminate as soon as control is
    * established.
    */
   protected void kill(){
       this.interrupt();
       synchronized(this) {
           if (currentCuri!=null) {
               currentCuri.setFetchStatus(S_PROCESSING_THREAD_KILLED);
               controller.getFrontier().finished(currentCuri);
            }
//    	   if (currentRspc!=null) {
//    		   currentRspc.setFetchStatus(S_PROCESSING_THREAD_KILLED);
//               controller.getFrontier().finished(currentRspc);
//           }
       }
   }

	/**
	 * @return Current step (For debugging/reporting, give abstract step
    * where this thread is).
	 */
	public Object getStep() {
		return step;
	}

   /**
    * Is this thread validly processing a URI, not paused, waiting for 
    * a URI, or interrupted?
    * @return whether thread is actively processing a URI
    */
   public boolean isActive() {
       // if alive and not waiting in/for frontier.next(), we're 'active'
       return this.isAlive() && (currentCuri != null) && !isInterrupted();
//	   return this.isAlive() && (currentRspc != null) && !isInterrupted();
   }
   
   /**
    * Request that this thread retire (exit cleanly) at the earliest
    * opportunity.
    */
   public void retire() {
       shouldRetire = true;
   }

   /**
    * Whether this thread should cleanly retire at the earliest 
    * opportunity. 
    * 
    * @return True if should retire.
    */
   public boolean shouldRetire() {
       return shouldRetire;
   }
   
   public String getCurrentProcessorName() {
       return currentProcessorName;
   }
}

