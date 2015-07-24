package bin.httpclient.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharsetUtils;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bin.spider.util.SettingsXMLUtil;
import bin.spider.util.StringUtils;
 
/**<p>
 * 封装HttpClient，加入失败重试HttpRequestRetryHandler
 *</p>
 * @author   Andy Pan    
 */
public class HttpClientWrapper {
	
    private enum VERBTYPE {
        GET, POST
    }

    private Integer socketTimeout            = 1000;//50
    private Integer connectTimeout           = 1000;//50
    private Integer connectionRequestTimeout = 1000;//50
    private String[] userAgents = {"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
			"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:21.0) Gecko/20100101 Firefox/21.0",
			"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0)",
			"Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36",
			"Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9) Gecko Minefield/3.0",
			"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6",
			"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0; MATP; MATP)",
			"Mozilla/5.0 (Windows NT 6.1; rv:22.0) Gecko/20100101 Firefox/22.0"};
    
    private Random random = new Random();
    private static CloseableHttpClient client;
    private RequestConfig requestConfig;
    private List<ContentBody> contentBodies;
    private List<NameValuePair> nameValuePostBodies;
    private static PoolingHttpClientConnectionManager connManager = null;

    private static HttpRequestRetryHandler myRetryHandler;//retry
    private static Registry<CookieSpecProvider> cookieRegistry;//cookie
    Logger logger = LoggerFactory.getLogger(HttpClientWrapper.class);
 
    static {
        try {
            SSLContext sslContext = SSLContexts.custom().useTLS().build();
            sslContext.init(null, new TrustManager[] { new X509TrustManager() {
 
                public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }
 
                public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }
 
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            } }, null);
            //retry
            myRetryHandler = new HttpRequestRetryHandler() {
            	@Override
                public boolean retryRequest(
                        IOException exception,
                        int executionCount,
                        HttpContext context) {
                    if (executionCount >= 4) {
                        // Do not retry if over max retry count
                        return false;
                    }
                    if (exception instanceof NoHttpResponseException) {
                        // Retry if the server dropped connection on us
                        return true;
                    }
                    if (exception instanceof SSLHandshakeException) {
                        // Do not retry on SSL handshake exception
                        return false;
                    }
                    HttpRequest request = (HttpRequest) context.getAttribute(
                            ExecutionContext.HTTP_REQUEST);
                    boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
                    if (idempotent) {
                        // Retry if the request is considered idempotent
                        return true;
                    }
                    return false;
                }
             
            };
            //cookie
            CookieSpecProvider easySpecProvider = new CookieSpecProvider() {
            	public BrowserCompatSpec create(HttpContext context) {
	            	return new BrowserCompatSpec() {
		            	@Override
		            	public void validate(Cookie cookie, CookieOrigin origin)
		            			throws MalformedCookieException {
		            		// Oh, I am easy
		            	}
	            	};
            	}

            };
            cookieRegistry = RegistryBuilder
            		 .<CookieSpecProvider> create()
            		 .register(CookieSpecs.BEST_MATCH, new BestMatchSpecFactory())
            		 .register(CookieSpecs.BROWSER_COMPATIBILITY,
            				 new BrowserCompatSpecFactory())
            		 .register("easy", easySpecProvider).build();
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                    .<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslContext))
                    .build();
            connManager                           = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            SocketConfig socketConfig             = SocketConfig.custom().setTcpNoDelay(true).build();
            connManager.setDefaultSocketConfig(socketConfig);
            MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(200)
                    .setMaxLineLength(2000).build();
            ConnectionConfig connectionConfig     = ConnectionConfig.custom()
                    .setMalformedInputAction(CodingErrorAction.IGNORE)
                    .setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8)
                    .setMessageConstraints(messageConstraints)
                    .build();
            connManager.setDefaultConnectionConfig(connectionConfig);
            connManager.setMaxTotal(200);
            connManager.setDefaultMaxPerRoute(20);
            
        } catch (KeyManagementException e) {
        	e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
        	e.printStackTrace();
        }
    }
    public HttpClientWrapper() {
        super();
        BasicCookieStore cookieStore = new BasicCookieStore();
        //client                 = HttpClientBuilder.create().build();//不使用连接池
        client                   = HttpClients.custom()
        							.setConnectionManager(connManager)
        							.setRetryHandler(myRetryHandler)
        							.setDefaultCookieSpecRegistry(cookieRegistry)
        							.setDefaultCookieStore(cookieStore)
        							.build();
        client.getParams().setParameter(DirSeparator, nameValuePostBodies);
        this.contentBodies       = new ArrayList<ContentBody>();
        this.nameValuePostBodies = new LinkedList<NameValuePair>();
        this.requestConfig       = RequestConfig.custom().setConnectionRequestTimeout(this.connectionRequestTimeout)
                .setConnectTimeout(this.connectTimeout).setSocketTimeout(this.socketTimeout).build();
    }
 
    public HttpClientWrapper(Integer connectionRequestTimeout, Integer connectTimeout, Integer socketTimeout) {
        super();
        this.socketTimeout            = socketTimeout;
        this.connectTimeout           = connectTimeout;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.contentBodies            = new ArrayList<ContentBody>();
        this.nameValuePostBodies      = new LinkedList<NameValuePair>();
        //client                      = HttpClientBuilder.create().build();//不使用连接池
        client                        = HttpClients.custom().setConnectionManager(connManager).build();
        this.requestConfig            = RequestConfig.custom().setConnectionRequestTimeout(this.connectionRequestTimeout)
                .setConnectTimeout(this.connectTimeout).setSocketTimeout(this.socketTimeout).build();
    }
 
    /**
     * Get方式访问URL
     * 
     * @param url
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public ResponseContent getResponse(String url) throws HttpException, IOException {
        return this.getResponse(url, "UTF-8", VERBTYPE.GET, null);
    }
 
    /**
     * Get方式访问URL
     * 
     * @param url
     * @param urlEncoding
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public ResponseContent getResponse(String url, String urlEncoding) throws HttpException, IOException {
        return this.getResponse(url, urlEncoding, VERBTYPE.GET, null);
    }
    
    /**
     * Get方式访问URL
     * 
     * @param url
     * @param urlEncoding
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public ResponseContent getResponse(String url, String urlEncoding,String contentType) throws HttpException, IOException {
        return this.getResponse(url, urlEncoding, VERBTYPE.GET, contentType);
    }
 
    /**
     * POST方式发送名值对请求URL
     * 
     * @param url
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public ResponseContent postNV(String url) throws HttpException, IOException {
        return this.getResponse(url, "UTF-8", VERBTYPE.POST, null);
    }
 
    public ResponseContent postNV(String url, String contentType) throws HttpException, IOException {
        return getResponse(url, "UTF-8", VERBTYPE.POST, contentType);
    }
 
    /**
     * 根据url编码，请求方式，请求URL
     * 
     * @param urlstr
     * @param urlEncoding
     * @param bodyType
     * @return
     * @throws HttpException
     * @throws IOException
     */
    @SuppressWarnings("null")
    public ResponseContent getResponse(String urlstr, String urlEncoding, VERBTYPE bodyType, String contentType)
            throws HttpException, IOException {
 
        if (urlstr == null)
            return null;
 
        String url = urlstr;
        if (urlEncoding != null)
            url = HttpClientWrapper.encodeURL(url.trim(), urlEncoding);
 
        HttpEntity entity = null;
        HttpRequestBase request = null;
        CloseableHttpResponse response = null;
        try {
            if (VERBTYPE.GET == bodyType) {
                request = new HttpGet(url);
            } else if (VERBTYPE.POST == bodyType) {
                this.parseUrl(url);
                HttpPost httpPost = new HttpPost(toUrl());
                List<NameValuePair> nvBodyList = this.getNVBodies();
                httpPost.setEntity(new UrlEncodedFormEntity(nvBodyList, urlEncoding));
                request = httpPost;
            }
 
            if (contentType != null) {
                request.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
            }
 
            request.setConfig(requestConfig);
         // 模拟谷歌 爬虫  
//            request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
            request.addHeader(HttpHeaders.USER_AGENT,userAgents[random.nextInt(9)]);
//            request.addHeader(HttpHeaders.USER_AGENT,
//                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)");
            // 如果参数是中文，需要进行转码
            response = client.execute(request);      
            if (null==response) {
//            	SettingsXMLUtil.getController().getFrontier().addFialedMapCountAndRetry(url);
				logger.error("reponse null,retry "+urlstr+",clean up the thread");
				return null;
			}
            entity = response.getEntity(); // 获取响应实体
            if (null==entity){
//            	SettingsXMLUtil.getController().getFrontier().addFialedMapCountAndRetry(url);
				logger.error("entity null,retry "+urlstr+",clean up the thread");
				return null;
            }
	        ResponseContent ret = new ResponseContent(url);
	        ret.setStatusCode(response.getStatusLine().getStatusCode());
	        String encoding = EntityUtils.getContentCharSet(entity);
	        if(null==encoding)//没有默认编码，就用GBK
	        	encoding = "GBK";
	        ret.setEncoding(encoding);
	        ret.setContentBytes(EntityUtils.toByteArray(entity));
//	        ret.setContent(StringUtils.InputStreamTOString(entity.getContent(),encoding));
	        ret.setContent(new String(ret.getContentBytes(),encoding));
	        return ret;
    } catch (SocketTimeoutException e) {
//		SettingsXMLUtil.getController().getFrontier().addFialedMapCountAndRetry(url);
		logger.error("time out,retry "+urlstr+",clean up the thread");
//		Thread.currentThread().interrupt();
		return null;
	} catch(UnsupportedEncodingException e){
    	logger.error("post error,UnsupportedEncoding "+urlstr);
    } catch (IllegalStateException e) {
    	logger.error("setContent() trans inputStream to String "+urlstr);
    	e.printStackTrace();
	} catch (ParseException e) {
		logger.error("ParseException"+urlstr);
		e.printStackTrace();
	} catch (Exception e) {
		e.printStackTrace();
	}finally {
        close(entity, request, response);
    }
	return null;
    }
    
//    public byte[] getResponseBytes(String urlstr)
//       	 throws HttpException,IOException {
//    	return getResponseBytes(urlstr, "UTF-8", VERBTYPE.GET, null);
//    }
//    
//    private byte[] getResponseBytes(String urlstr, String urlEncoding, VERBTYPE bodyType, String contentType)
//    	 throws HttpException,IOException {
//    		 
//	        if (urlstr == null)
//	            return null;
//	 
//	        String url = urlstr;
//	        if (urlEncoding != null)
//	            url = HttpClientWrapper.encodeURL(url.trim(), urlEncoding);
//	 
//	        HttpEntity entity = null;
//	        HttpRequestBase request = null;
//	        CloseableHttpResponse response = null;
//	        try {
//	            if (VERBTYPE.GET == bodyType) {
//	                request = new HttpGet(url);
//	            } else if (VERBTYPE.POST == bodyType) {
//	                this.parseUrl(url);
//	                HttpPost httpPost = new HttpPost(toUrl());
//	                List<NameValuePair> nvBodyList = this.getNVBodies();
//	                httpPost.setEntity(new UrlEncodedFormEntity(nvBodyList, urlEncoding));
//	                request = httpPost;
//	            }
//	 
//	            if (contentType != null) {
//	                request.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
//	            }
//	 
//	            request.setConfig(requestConfig);
//	         // 模拟谷歌 爬虫  
////	            request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
//	            request.addHeader(HttpHeaders.USER_AGENT,userAgents[random.nextInt(9)]);
////	            request.addHeader(HttpHeaders.USER_AGENT,
////    	                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)");
//	 
//	            response = client.execute(request);
//	            entity = response.getEntity(); // 获取响应实体
//	            return EntityUtils.toByteArray(entity);
//        } finally {
//            close(entity, request, response);
//        } 
//    }
    
//    public ResponseContent getResponseContent(String urlstr,String urlEncoding,VERBTYPE bodyType, String contentType) 
//    		throws IOException {
//    	if (urlstr == null)
//            return null;
// 
//        String url = urlstr;
//        if (urlEncoding != null)
//            url = HttpClientWrapper.encodeURL(url.trim(), urlEncoding);
//        else {
//        	url = HttpClientWrapper.encodeURL(url.trim(), "UTF-8");
//		}
//        if(null==bodyType)
//        	bodyType = VERBTYPE.GET;
//        HttpEntity entity = null;
//        HttpRequestBase request = null;
//        CloseableHttpResponse response = null;
//        try{ 	
//            if (VERBTYPE.GET == bodyType) {
//                request = new HttpGet(url);
//            } else if (VERBTYPE.POST == bodyType) {
//                this.parseUrl(url);
//                HttpPost httpPost = new HttpPost(toUrl());
//                List<NameValuePair> nvBodyList = this.getNVBodies();
//                httpPost.setEntity(new UrlEncodedFormEntity(nvBodyList, urlEncoding));
//                request = httpPost;
//            }
// 
//            if (contentType != null) {
//                request.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
//            }
// 
//            request.setConfig(requestConfig);
//         // 模拟谷歌 爬虫  
////            request.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
//            request.addHeader(HttpHeaders.USER_AGENT,userAgents[random.nextInt(9)]);
////            request.addHeader(HttpHeaders.USER_AGENT,
////                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)");
//            // 如果参数是中文，需要进行转码
//            try {
//            	response = client.execute(request);
//			} catch (SocketTimeoutException e) {
//				SettingsXMLUtil.getController().getFrontier().addFialedMapCountAndRetry(url);
//				logger.error("time out,retry "+urlstr+",clean up the thread");
////				Thread.currentThread().interrupt();
//				return null;
//			} catch (Exception e) {
//				SettingsXMLUtil.getController().getFrontier().addFialedMapCountAndRetry(url);
//				logger.error("unknown error,retry urlstr,clean up the thread");
//				return null;
//			}     
//            if (null==response) {
//            	SettingsXMLUtil.getController().getFrontier().addFialedMapCountAndRetry(url);
//				logger.error("reponse null,retry urlstr,clean up the thread");
//				return null;
//			}
//            entity = response.getEntity(); // 获取响应实体
//            ResponseContent ret = new ResponseContent(url);
//            ret.setStatusCode(response.getStatusLine().getStatusCode());
//            String encoding = EntityUtils.getContentCharSet(entity);
//            ret.setEncoding(encoding);
//            ret.setContent(StringUtils.InputStreamTOString(entity.getContent(),encoding));
//            ret.setContentBytes(ret.getContent().getBytes());
//            return ret;
//	    } catch(UnsupportedEncodingException e){
//	    	logger.error("post error,UnsupportedEncoding");
//	    } catch (IllegalStateException e) {
//	    	logger.error("setContent() trans inputStream to String");
//	    	e.printStackTrace();
//		} catch (ParseException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally {
//	        close(entity, request, response);
//	    }
//		return null;
//	}
 
//    private void getResponseContent(HttpEntity entity, ResponseContent ret) throws IOException {
//        Header enHeader = entity.getContentEncoding();
//        if (enHeader != null) {
//            String charset = enHeader.getValue().toLowerCase();
//            ret.setEncoding(charset);
//        }
////        String contenttype = this.getResponseContentType(entity);
////        ret.setContentType(contenttype);//text/html
//        //text/html; charset=GBK
////        ret.setContentTypeString(this.getResponseContentTypeString(entity));  
//        ret.setContentBytes(EntityUtils.toByteArray(entity));
//    }
 
    public ResponseContent postEntity(String url) throws HttpException, IOException {
        return this.postEntity(url, "UTF-8");
    }
 
    /**
     * POST方式发送名值对请求URL,上传文件（包括图片）
     * 
     * @param url
     * @return
     * @throws HttpException
     * @throws IOException
     */
    public ResponseContent postEntity(String url, String urlEncoding) throws HttpException, IOException {
        if (url == null)
            return null;
 
        HttpEntity entity = null;
        HttpRequestBase request = null;
        CloseableHttpResponse response = null;
        try {
            this.parseUrl(url);
            HttpPost httpPost = new HttpPost(toUrl());
 
            //对请求的表单域进行填充  
            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            for (NameValuePair nameValuePair : this.getNVBodies()) {
                entityBuilder.addPart(nameValuePair.getName(),
                        new StringBody(nameValuePair.getValue(), ContentType.create("text/plain", urlEncoding)));
            }
            for (ContentBody contentBody : getContentBodies()) {
                entityBuilder.addPart("file", contentBody);
            }
            entityBuilder.setCharset(CharsetUtils.get(urlEncoding));
            httpPost.setEntity(entityBuilder.build());
            request = httpPost;
            response = client.execute(request);
 
            //响应状态
            StatusLine statusLine = response.getStatusLine();
            // 获取响应对象
            entity = response.getEntity(); // 获取响应实体
            ResponseContent ret = new ResponseContent(url);
            ret.setStatusCode(response.getStatusLine().getStatusCode());
            String encoding = EntityUtils.getContentCharSet(entity);
            ret.setEncoding(encoding);
            ret.setContent(StringUtils.InputStreamTOString(entity.getContent(),encoding));
            ret.setContentBytes(ret.getContent().getBytes());
            return ret;
	    } catch(UnsupportedEncodingException e){
	    	logger.error("post error,UnsupportedEncoding");
	    } catch (IllegalStateException e) {
	    	logger.error("setContent() trans inputStream to String");
	    	e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
	        close(entity, request, response);
	    }
		return null;
    }
 
    private void close(HttpEntity entity, HttpRequestBase request, CloseableHttpResponse response) throws IOException {
        if (request != null)
            request.releaseConnection();
        if (entity != null)
            entity.getContent().close();
        if (response != null)
            response.close();
    }
 
    public NameValuePair[] getNVBodyArray() {
        List<NameValuePair> list = this.getNVBodies();
        if (list == null || list.isEmpty())
            return null;
        NameValuePair[] nvps = new NameValuePair[list.size()];
        Iterator<NameValuePair> it = list.iterator();
        int count = 0;
        while (it.hasNext()) {
            NameValuePair nvp = it.next();
            nvps[count++] = nvp;
        }
        return nvps;
    }
 
    public List<NameValuePair> getNVBodies() {
        return Collections.unmodifiableList(this.nameValuePostBodies);
    }
 
    static Set<Character> BEING_ESCAPED_CHARS = new HashSet<Character>();
    static {
        char[] signArray = { ' ', '\\', '‘', ']', '!', '^', '#', '`', '$', '{', '%', '|', '}', '(', '+', ')', '<', '>',
                ';', '[' };
        for (int i = 0; i < signArray.length; i++) {
            BEING_ESCAPED_CHARS.add(new Character(signArray[i]));
        }
    }
 
    public static String encodeURL(String url, String encoding) {
        if (url == null)
            return null;
        if (encoding == null)
            return url;
 
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c == 10) {
                continue;
            } else if (BEING_ESCAPED_CHARS.contains(new Character(c)) || c == 13 || c > 126) {
                try {
                    sb.append(URLEncoder.encode(String.valueOf(c), encoding));
                } catch (Exception e) {
                    sb.append(c);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString().replaceAll("\\+", "%20");
    }
 
    private String protocol;
    private String host;
    private int port;
    private String dir;
    private String uri;
    private final static int DefaultPort = 80;
    private final static String ProtocolSeparator = "://";
    private final static String PortSeparator = ":";
    private final static String HostSeparator = "/";
    private final static String DirSeparator = "/";
 
    private void parseUrl(String url) {
        this.protocol = null;
        this.host = null;
        this.port = DefaultPort;
        this.dir = "/";
        this.uri = dir;
 
        if (url == null || url.length() == 0)
            return;
        String u = url.trim();
        boolean MeetProtocol = false;
        int pos = u.indexOf(ProtocolSeparator);
        if (pos > 0) {
            MeetProtocol = true;
            this.protocol = u.substring(0, pos);
            pos += ProtocolSeparator.length();
        }
        int posStartDir = 0;
        if (MeetProtocol) {
            int pos2 = u.indexOf(PortSeparator, pos);
            if (pos2 > 0) {
                this.host = u.substring(pos, pos2);
                pos2 = pos2 + PortSeparator.length();
                int pos3 = u.indexOf(HostSeparator, pos2);
                String PortStr = null;
                if (pos3 > 0) {
                    PortStr = u.substring(pos2, pos3);
                    posStartDir = pos3;
                } else {
                    int pos4 = u.indexOf("?");
                    if (pos4 > 0) {
                        PortStr = u.substring(pos2, pos4);
                        posStartDir = -1;
                    } else {
                        PortStr = u.substring(pos2);
                        posStartDir = -1;
                    }
                }
                try {
                    this.port = Integer.parseInt(PortStr);
                } catch (Exception e) {
                }
            } else {
                pos2 = u.indexOf(HostSeparator, pos);
                if (pos2 > 0) {
                    this.host = u.substring(pos, pos2);
                    posStartDir = pos2;
                } else {
                    this.host = u.substring(pos);
                    posStartDir = -1;
                }
            }
 
            pos = u.indexOf(HostSeparator, pos);
            pos2 = u.indexOf("?");
            if (pos > 0 && pos2 > 0) {
                this.uri = u.substring(pos, pos2);
            } else if (pos > 0 && pos2 < 0) {
                this.uri = u.substring(pos);
            }
        }
 
        if (posStartDir >= 0) {
            int pos2 = u.lastIndexOf(DirSeparator, posStartDir);
            if (pos2 > 0) {
                this.dir = u.substring(posStartDir, pos2 + 1);
            }
        }
 
    }
 
    private String toUrl() {
        StringBuffer ret = new StringBuffer();
        if (this.protocol != null) {
            ret.append(this.protocol);
            ret.append(ProtocolSeparator);
            if (this.host != null)
                ret.append(this.host);
            if (this.port != DefaultPort) {
                ret.append(PortSeparator);
                ret.append(this.port);
            }
        }
        ret.append(this.uri);
        return ret.toString();
    }
 
    public void addNV(String name, String value) {
        BasicNameValuePair nvp = new BasicNameValuePair(name, value);
        this.nameValuePostBodies.add(nvp);
    }
 
    public void clearNVBodies() {
        this.nameValuePostBodies.clear();
    }
 
    public List<ContentBody> getContentBodies() {
        return contentBodies;
    }
 
}
