package bin.httpclient.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.stream.FileCacheImageOutputStream;

import org.apache.http.HttpException;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.EntityUtils;
 
/**
 * HTTP工具类，封装HttpClient4.3.x来对外提供简化的HTTP请求 
 */
public class HttpHelper {
 
    private static Integer socketTimeout            = 6000;//50
    private static Integer connectTimeout           = 6000;
    private static Integer connectionRequestTimeout = 6000;//50
    private static HttpClientWrapper hw;
    
    static{
    	hw = new HttpClientWrapper(connectionRequestTimeout, connectTimeout, socketTimeout);
    }
 
    /**
     * 使用Get方式 根据URL地址，获取ResponseContent对象
     * 
     * @param url
     *            完整的URL地址
     * @return ResponseContent 如果发生异常则返回null，否则返回ResponseContent对象
     */
    public static ResponseContent getUrlRespContent(String url) {
        ResponseContent response = null;
        try {
            response = hw.getResponse(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
 
    /**
     * 使用Get方式 根据URL地址，获取ResponseContent对象
     * 
     * @param url
     *            完整的URL地址
     * @param urlEncoding
     *            编码，可以为null
     * @return ResponseContent 如果发生异常则返回null，否则返回ResponseContent对象
     */
    public static ResponseContent getUrlRespContent(String url, String urlEncoding) {
        ResponseContent response = null;
        try {
            response = hw.getResponse(url, urlEncoding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
    
    /**
     * 使用Get方式 根据URL地址，获取ResponseContent对象
     * 
     * @param url
     *            完整的URL地址
     * @param urlEncoding
     *            编码，可以为null
     * @param contentType
     * 			  header，网页一般为text/html;charset=UTF-8
     * @return ResponseContent 如果发生异常则返回null，否则返回ResponseContent对象
     */
    public static ResponseContent getUrlRespContent(String url, String urlEncoding,String contentType) {
        ResponseContent response = null;
        try {
            response = hw.getResponse(url, urlEncoding,contentType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
    
//    public static byte[] getUrlContentBytes(String url) {
////    	HttpClientWrapper hw = new HttpClientWrapper(connectionRequestTimeout, connectTimeout, socketTimeout);
//    	byte[] contentBytes = null;
//		try {
//			contentBytes =  hw.getResponseBytes(url);
//		} catch (HttpException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return contentBytes;
//	}
 
    /**
     * 将参数拼装在url中，进行post请求。
     * 
     * @param url
     * @return
     */
    public static ResponseContent postUrl(String url) {
        ResponseContent ret = null;
        try {
            setParams(url, hw);
            ret = hw.postNV(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
 
    private static void setParams(String url, HttpClientWrapper hw) {
        String[] paramStr = url.split("[?]", 2);
        if (paramStr == null || paramStr.length != 2) {
            return;
        }
        String[] paramArray = paramStr[1].split("[&]");
        if (paramArray == null) {
            return;
        }
        for (String param : paramArray) {
            if (param == null || "".equals(param.trim())) {
                continue;
            }
            String[] keyValue = param.split("[=]", 2);
            if (keyValue == null || keyValue.length != 2) {
                continue;
            }
            hw.addNV(keyValue[0], keyValue[1]);
           
            
        }
    }
 
    /**
     * 上传文件（包括图片）
     * 
     * @param url
     *            请求URL
     * @param paramsMap
     *            参数和值
     * @return
     */
    public static ResponseContent postEntity(String url, Map<String, Object> paramsMap) {
        HttpClientWrapper hw = new HttpClientWrapper();
        ResponseContent ret = null;
        try {
            setParams(url, hw);
            Iterator<String> iterator = paramsMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                Object value = paramsMap.get(key);
                if (value instanceof File) {
                    FileBody fileBody = new FileBody((File) value);
                    hw.getContentBodies().add(fileBody);
                } else if (value instanceof byte[]) {
                    byte[] byteVlue = (byte[]) value;
                    ByteArrayBody byteArrayBody = new ByteArrayBody(byteVlue, key);
                    hw.getContentBodies().add(byteArrayBody);
                } else {
                    if (value != null && !"".equals(value)) {
                        hw.addNV(key, String.valueOf(value));
                    } else {
                        hw.addNV(key, "");
                    }
                }
            }
            ret = hw.postEntity(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
 
    /**
     * 使用post方式，发布对象转成的json给Rest服务。
     * 
     * @param url
     * @param jsonBody
     * @return
     */
    public static ResponseContent postJsonEntity(String url, String jsonBody) {
        return postEntity(url, jsonBody, "application/json");
    }
 
    /**
     * 使用post方式，发布对象转成的xml给Rest服务
     * 
     * @param url
     *            URL地址
     * @param xmlBody
     *            xml文本字符串
     * @return ResponseContent 如果发生异常则返回空，否则返回ResponseContent对象
     */
    public static ResponseContent postXmlEntity(String url, String xmlBody) {
        return postEntity(url, xmlBody, "application/xml");
    }
 
    private static ResponseContent postEntity(String url, String body, String contentType) {
        HttpClientWrapper hw = new HttpClientWrapper();
        ResponseContent ret = null;
        try {
            hw.addNV("body", body);
            ret = hw.postNV(url, contentType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    
//    private static boolean DownloadFile(String url){
//    	ResponseContent responseContent = getUrlRespContent(url);
//        try {
//			url = URLDecoder.decode(url, "UTF-8");
//			String path = url.replace("http://", "E:/mobile/");
//			//java7 Paths,640
//			Path target = Paths.get(path);
//			if (Files.notExists(target)) {
//				if (Files.notExists(target.getParent()))
//					Files.createDirectories(target.getParent());
//				Files.createFile(target);
//			}
//			//transition create file,686
////	        File storeFile = new File(path);
////	        if (!storeFile.exists()) {
////	        	if (!storeFile.getParentFile().exists())
////					storeFile.getParentFile().mkdirs();
////				storeFile.createNewFile();
////			}
//	        byte b[]= responseContent.getContentBytes();
//	        //traditional I/O
////	        FileOutputStream output = new FileOutputStream(storeFile);
////			output.write(byte);
////			output.flush();  
////	        output.close();
//	        //NIO
//	        //ByteBuffer buffer=ByteBuffer.allocate(256);//256 bit size
//	        FileChannel fChannel= new FileOutputStream(path).getChannel();
//	        ByteBuffer buffer=ByteBuffer.wrap(b);     
//	        buffer.flip();
//	        buffer.clear();
//	        fChannel.write(buffer);
//	        fChannel.close();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		} 
//    	return false;
//    }
    
//    private static boolean copyFile(String oldPath,String newFloader){
//    	try {
//    		String newPath = null;
//    		if (-1!=oldPath.lastIndexOf("/")) {
//    			newPath = newFloader+oldPath.substring(oldPath.lastIndexOf("/"));
//			}else if(-1!=oldPath.lastIndexOf("\\")){
//				newPath = newFloader+oldPath.substring(oldPath.lastIndexOf("\\"));
//			}
//    		//jdk7 便捷写法
//        	Path from = Paths.get(oldPath);
//        	Path to = Paths.get(newPath);
//        	Files.copy(from,to);
//    		//jdk5 NIO写法
////    		FileInputStream fis = new FileInputStream(new File(oldPath));
////        	FileChannel fin = fis.getChannel();
////        	FileOutputStream fos = new FileOutputStream(new File(newPath));
////        	FileChannel fout = fos.getChannel();
////        	ByteBuffer buffer = ByteBuffer.allocate(1024); //1024字节的buffer
////        	while(fin.read(buffer) != -1){
////        	     buffer.flip(); //buffer写满，position重新置0，limit置为结尾，方便使用
////        	     fout.write(buffer);
////        	     buffer.clear();//清空buffer
////        	}
////        	fin.close();
////        	fout.close();
////        	fis.close();
////        	fos.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//    	
//    	return false;
//    }
 
//    public static void main(String[] args) {
//    	Long start = System.currentTimeMillis();
//    	testGet();
//    	testUploadFile();
//    	testDownloadFile();
//    	testCopyFile();
//    	long consume = System.currentTimeMillis()-start;
//		System.out.println(consume);
//        
//    }
 
    //test
//    public static void testGet() {
//        String url = "http://product.pconline.com.cn/mobile/htc/";
//        ResponseContent responseContent = getUrlRespContent(url);
//        System.out.println(responseContent.getContent());
//    }
//    
//    //test
//    public static void testDownloadFile() {
//    	String url = "http://img.pconline.com.cn/images/product/5723/572378/0_sn.jpg";
//        DownloadFile(url);
//	}
//
//    //test
//    public static void testCopyFile() {
//    	String oldPath= "E:/pconline/img.pconline.com.cn/images/product/5723/572378/0_sn.jpg";
//    	String newFloader = "E:/pconline/imgs/";
//    	copyFile(oldPath,newFloader);
//	}
//    //test
//    public static void testUploadFile() {
//        try {
//            String url = "http://localhost:8280/jfly/action/admin/user/upload.do";
//            Map<String, Object> paramsMap = new HashMap<String, Object>();
//            paramsMap.put("userName", "jj");
//            paramsMap.put("password", "jj");
//            paramsMap.put("filePath", new File("C:\\Users\\yangjian1004\\Pictures\\default (1).jpeg"));
//            ResponseContent ret = postEntity(url, paramsMap);
//            System.out.println(ret.getContent());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
