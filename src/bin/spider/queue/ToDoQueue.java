package bin.spider.queue;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ToDoQueue {

	/**超链接队列*/
    public static ConcurrentLinkedQueue <String> urlQueue = new ConcurrentLinkedQueue <String>();  
         
    /**队列中对应最多的超链接数量*/
    public static final int MAX_SIZE = 10000;
    
    public static void addElem(String url)  
    {
    	synchronized(urlQueue){
    		urlQueue.add(url);
    	}     
    }
    
    public static void addItratorElem(Iterator<String> iterator)  
    {
    	synchronized(urlQueue){
    		while (iterator.hasNext()) {
    			urlQueue.add(iterator.next());			
			}
    		
    	}     
    }
         
    public static String outElem()  
    {
    	synchronized(urlQueue){
    		return urlQueue.poll();
    	}
    }
    
    public static boolean removeElem(String url) {
    	synchronized(urlQueue){
    		return urlQueue.remove(url);
    	}
	}
         
    public static boolean isEmpty()  
    {  
    	synchronized(urlQueue){
    		return urlQueue.isEmpty();  
    	}
    }  
         
    public static int size()  
    {  
    	synchronized(urlQueue){
    		return urlQueue.size();
    	}
    }  
         
    public static boolean isContains(String url)
    {  
    	synchronized(urlQueue){        
    		return urlQueue.contains(url);
    	}
    }
    
}
