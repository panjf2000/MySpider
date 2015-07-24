package bin.spider.frame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bin.spider.frame.CrawlController;

/**
* A collection of ToeThreads. The class manages the ToeThreads currently
* running. Including increasing and decreasing their number, keeping track
* of their state and it can be used to kill hung threads.
*
* @author Gordon Mohr
* @author Kristinn Sigurdsson
*
* @see org.archive.crawler.framework.ToeThread
*/
public class ToePool extends ThreadGroup {
   /** run worker thread slightly lower than usual */
   public static int DEFAULT_TOE_PRIORITY = Thread.NORM_PRIORITY - 1;
   
//   Logger logger = LoggerFactory.getLogger(ToePool.class);
   
   protected CrawlController controller;
   protected int nextSerialNumber = 1;
   protected int targetSize = 0; 


   /**
    * Constructor. Creates a pool of ToeThreads. 
    *
    * @param c A reference to the CrawlController for the current crawl.
    */
   public ToePool(CrawlController c) {
       super("ToeThreads");
       this.controller = c;
       setDaemon(true);
   }
   
   public void cleanup() {
       this.controller = null;
   }

   /**
    * @return The number of ToeThreads that are not available (Approximation).
    */
   public int getActiveToeCount() {
       Thread[] toes = getToes();
       int count = 0;
       for (int i = 0; i < toes.length; i++) {
           if((toes[i] instanceof ToeThread) &&
                   ((ToeThread)toes[i]).isActive()) {
               count++;
           }
       }
       return count; 
   }

   /**
    * @return The number of ToeThreads. This may include killed ToeThreads
    *         that were not replaced.
    */
   public int getToeCount() {
       Thread[] toes = getToes();
       int count = 0;
       for (int i = 0; i<toes.length; i++) {
           if((toes[i] instanceof ToeThread)) {
               count++;
           }
       }
       return count; 
   }
   
   private Thread[] getToes() {
       Thread[] toes = new Thread[activeCount()+10];
       this.enumerate(toes);
       return toes;
   }

   /**
    * Change the number of ToeThreads.
    *
    * @param newsize The new number of ToeThreads.
    */
   public void setSize(int newsize)
   {
       targetSize = newsize;
       int difference = newsize - getToeCount(); 
       if (difference > 0) {
           // must create threads
           for(int i = 1; i <= difference; i++) {
               startNewThread();
           }
       } else {
           // must retire extra threads
           int retainedToes = targetSize; 
           Thread[] toes = this.getToes();
           for (int i = 0; i < toes.length ; i++) {
               if(!(toes[i] instanceof ToeThread)) {
                   continue;
               }
               retainedToes--;
               if (retainedToes>=0) {
                   continue; // this toe is spared
               }
               // otherwise:
               ToeThread tt = (ToeThread)toes[i];
               tt.retire();
           }
       }
   }

   /**
    * Kills specified thread. Killed thread can be optionally replaced with a
    * new thread.
    *
    * <p><b>WARNING:</b> This operation should be used with great care. It may
    * destabilize the crawler.
    *
    * @param threadNumber Thread to kill
    * @param replace If true then a new thread will be created to take the
    *           killed threads place. Otherwise the total number of threads
    *           will decrease by one.
    */
   public void killThread(int threadNumber, boolean replace){

       Thread[] toes = getToes();
       for (int i = 0; i< toes.length; i++) {
           if(! (toes[i] instanceof ToeThread)) {
               continue;
           }
           ToeThread toe = (ToeThread) toes[i];
           if(toe.getSerialNumber()==threadNumber) {
               toe.kill();
           }
       }

       if(replace){
           // Create a new toe thread to take its place. Replace toe
           startNewThread();
       }
   }

   private synchronized void startNewThread() {
       ToeThread newThread = new ToeThread(this, nextSerialNumber++);
       newThread.setPriority(DEFAULT_TOE_PRIORITY);
       newThread.start();
   }

   /**
    * @return Instance of CrawlController.
    */
   public CrawlController getController() {
       return controller;
   }
   
}

