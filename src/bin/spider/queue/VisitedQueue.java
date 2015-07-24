package bin.spider.queue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VisitedQueue {

	public static Set<String> visitedUrlQueue = Collections.synchronizedSet(new HashSet<String>());
	
	public static void addElem(String url)  
    {  
		synchronized(visitedUrlQueue){
			visitedUrlQueue.add(url);
		}
    }  
	
	public static void outElem(String url) {
		synchronized(visitedUrlQueue){
			visitedUrlQueue.remove(url);
		}
	}
	
	public static void removeElem(String url) {
		synchronized(visitedUrlQueue){
			visitedUrlQueue.remove(url);
		}
	}
     
    public static boolean isContains(String url)  
    {
    	synchronized(visitedUrlQueue){
    		return visitedUrlQueue.contains(url);
    	}
    }  
     
    public static int size()  
    {  
    	synchronized(visitedUrlQueue){
    		return visitedUrlQueue.size();  
    	}
    }
    
}
