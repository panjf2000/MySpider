package bin.httpclient.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**<p>
 * 封装HttpClient返回数据
 *</p>
 * @author   Andy Pan
 */
public class ResponseContent {
	
	private String fromUrl;
	
	private String decodeUrl;

    private String encoding;
 
    private byte[] contentBytes;
    
    private long contentLength;
    
    private String content;
 
    private int statusCode;
 
//    private String contentType;
    
//    private boolean linkExtractorFinished = false;
 
//    private String contentTypeString;
    
    /**
     * 设置fromUrl,同时得到decodeUrl
     * **/
    public ResponseContent(String fromUrl){
    	this.fromUrl = fromUrl;
    	try {
			decodeUrl = URLDecoder.decode(fromUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }
    
    public String getEncoding() {
        return encoding;
    }
 
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
 
//    public String getContentType() {
//        return this.contentType;
//    }
 
//    public void setContentType(String contentType) {
//        this.contentType = contentType;
//    }
 
    public Long getContentLength() {
    	//lazy-load
		if (0==contentLength)
			contentLength = getContentBytes().length;
		return contentLength;
	}
    
    public void setContentLength(long contentLength) {
		this.contentLength = contentLength;
	}
//    public String getContentTypeString() {
//        return this.contentTypeString;
//    }
// 
//    public void setContentTypeString(String contenttypeString) {
//        this.contentTypeString = contenttypeString;
//    }
    
    public byte[] getContentBytes() {
        return contentBytes;
    }
 
    public void setContentBytes(byte[] contentBytes) {
        this.contentBytes = contentBytes;
    }
 
    public int getStatusCode() {
        return statusCode;
    }
 
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public void setContent(String content) {
		this.content = content;
	}

    /**
     * getContent()只在extractor中调用，用于产生String供Jsoup解析，所以中文乱码没关系
     * 为加快速度，不对其进行转码(encoding不重要)。保存时候用的是byte,不会乱码的
     * **/
	public String getContent() {
		return content;
	}

	public String getFromUrl() {
		return fromUrl;
	}
	
	public String getDecodeUrl() {
		return decodeUrl;		
	}
	
//	public void setFromUrl(String fromUrl) {
//		this.fromUrl = fromUrl;
//	}
	
	public void processingCleanup() {
        this.encoding = null;
        this.contentBytes = null;
//        this.contentType = null;
        this.encoding = null;
        this.content = null;
        this.statusCode = 0;
//        this.fetchStatus = S_UNATTEMPTED;
//        this.setPrerequisite(false);
//        // Clear 'links extracted' flag.
//        this.linkExtractorFinished = false;
//        // Clean the alist of all but registered permanent members.
//        setAList(getPersistentAList());
    }
 
	public void stripToMinimal(){
		this.fromUrl = null;
	}
}
