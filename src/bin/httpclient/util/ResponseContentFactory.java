package bin.httpclient.util;

import java.util.concurrent.ConcurrentHashMap;

public class ResponseContentFactory {
	
	public static ConcurrentHashMap<String, ResponseContent> classMap = new ConcurrentHashMap<String, ResponseContent>();

	public synchronized static ResponseContent getByUrl(String url){
		ResponseContent rspc = null;
		if(classMap.containsKey(url))
			return classMap.get(url);
		rspc = new ResponseContent(url);
		classMap.put(url, rspc);
		return rspc;
	}
	
	public synchronized static boolean removeByUrl(String url){
		ResponseContent rspc = null;
		if(classMap.containsKey(url)){
			rspc = classMap.get(url);
			return classMap.remove(url, rspc);
		}//does not exist object by given url
		return false;
	}
	
}
