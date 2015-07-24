package bin.spider.util;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bin.spider.extractor.Extractor;
import bin.spider.frame.CrawlController;
import bin.spider.frame.Processor;

public class SettingsXMLUtil {
	
	static String DEPTH;
	static String WIDTH;
	static int MAXTHREADS;
	static String STOREPATH;
	
	static String DRIVER;
	static String DBURI;
	static String USERNAME;
	static String PASSWORD;
	static ArrayList<String> PRIFETCH = new ArrayList<String>();
	static ArrayList<String> FETCH = new ArrayList<String>();
	static ArrayList<String> EXTRACTOR = new ArrayList<String>();
	static ArrayList<String> WRITER = new ArrayList<String>();
	static ArrayList<String> POSTPROCESSOR = new ArrayList<String>();
	
	private static CrawlController controller;
	private static Logger logger = LoggerFactory.getLogger(SettingsXMLUtil.class);
	protected final static Map<String,ArrayList<Processor>> definitionMap = new HashMap<String,ArrayList<Processor>>();

//	public static void main(String[] args) {
//		Parse();	
////		logger.info(getProcessorList("fetchs"));
//	}
	
	public static List<Processor> getProcessorList(String attributeName) {
		if(definitionMap.size()==0)
			initDefinition();
	    return definitionMap.get(attributeName);
	}
	
	public static Processor getProcessor(String attributeName) {
		if(definitionMap.size()==0)
			initDefinition();
	    return definitionMap.get(attributeName).get(0);
	}
	
//	public static void getAttribute(String attr){
//		if(definitionMap.size()==0)
//			initDefinition();
//	}
	
	public static void initDefinition() {
		if(null==FETCH)
			Parse();
		Class<?> classType = null;
		Constructor<?>[] cons = null;
//		for (String string : PRIFETCH) {
//			
//		}
//		for (String string : FETCH) {
//			
//		}
		try{
			ArrayList<Processor> Fetch = new ArrayList<Processor>();
			for (String string : FETCH) {
				classType = Class.forName(string); 
				cons=classType.getConstructors();
				//String name
				Fetch.add((Processor)cons[0].newInstance("Fetch"));
			}
			definitionMap.put("fetchs", Fetch);
			
			ArrayList<Processor> Extrator = new ArrayList<Processor>();
			for (String string : EXTRACTOR) {
				classType = Class.forName(string); 
				cons=classType.getConstructors();
				//String name
				Extrator.add((Extractor)cons[0].newInstance("Extrator"));
			}
			definitionMap.put("extractors", Extrator);
			
			ArrayList<Processor> Writer = new ArrayList<Processor>();
			for (String string : WRITER) {
				classType = Class.forName(string); 
				cons = classType.getConstructors();
				//String name
				Writer.add((Processor)cons[0].newInstance("Writer"));
			}
			definitionMap.put("writers", Writer);
			
			ArrayList<Processor> Postprocessor = new ArrayList<Processor>();
			for (String string : POSTPROCESSOR) {
				classType = Class.forName(string); 
				cons = classType.getConstructors();
				//String name,由于FrontierScheduler有name参数方法，所以是0
				Postprocessor.add((Processor)cons[0].newInstance("Postprocessor"));
			}
			definitionMap.put("postprocessors", Postprocessor);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void Parse() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {  
            DocumentBuilder builder = dbf.newDocumentBuilder();
            InputStream in = SettingsXMLUtil.class.getClassLoader()
				.getResourceAsStream("settings.xml");
            Document doc = builder.parse(in);
            // root <settings>  
            Element root = doc.getDocumentElement();
            if (root == null) return;
            // all college node  
            NodeList settingNodes = root.getElementsByTagName("setting");
            if (settingNodes == null) return;  
            //crawl setting
            Element crawlElement = (Element) settingNodes.item(0);
            NodeList crawlChild = crawlElement.getChildNodes();
            for(int j=0;j<crawlChild.getLength();j++){  
                if(crawlChild.item(j).getNodeType()==Node.ELEMENT_NODE){  
                    if("depth".equals(crawlChild.item(j).getNodeName())){
                    	DEPTH = crawlChild.item(j).getFirstChild().getNodeValue();
                    }else if("width".equals(crawlChild.item(j).getNodeName())){ 
                    	WIDTH = crawlChild.item(j).getFirstChild().getNodeValue();
                    }else if("maxthreads".equals(crawlChild.item(j).getNodeName())){ 
                    	MAXTHREADS = Integer.valueOf
                    			(crawlChild.item(j).getFirstChild().getNodeValue());
                    }else if("storepath".equals(crawlChild.item(j).getNodeName())){
                    	STOREPATH = crawlChild.item(j).getFirstChild().getNodeValue();;
					}
                }  
            }
            logger.info("crawl setting:"+DEPTH+"---"+WIDTH);
            //jdbc setting
            Element jdbcElement = (Element) settingNodes.item(1);
            NodeList jdbcChild = jdbcElement.getChildNodes();
            for(int j=0;j<jdbcChild.getLength();j++){  
                if(jdbcChild.item(j).getNodeType()==Node.ELEMENT_NODE){  
                    if("driver".equals(jdbcChild.item(j).getNodeName())){
                    	DRIVER = jdbcChild.item(j).getFirstChild().getNodeValue();
                    }else if("dbUrl".equals(jdbcChild.item(j).getNodeName())){
                    	DBURI = jdbcChild.item(j).getFirstChild().getNodeValue();
                    }else if ("username".equals(jdbcChild.item(j).getNodeName())) {
						USERNAME = jdbcChild.item(j).getFirstChild().getNodeValue();
					}else if ("password".equals(jdbcChild.item(j).getNodeName())) {
						PASSWORD = jdbcChild.item(j).getFirstChild().getNodeValue();
					}
                }  
            }
            logger.info("jdbc setting:"+DRIVER+"---"+DBURI+"---"
            		+USERNAME+"---"+PASSWORD);
            //prefetch chain List
            Element prefetchElement = (Element) settingNodes.item(2);
            NodeList prefetchChild = prefetchElement.getChildNodes();
            for(int j=0;j<prefetchChild.getLength();j++){
            	if(prefetchChild.item(j).getNodeType()==Node.ELEMENT_NODE)
            		PRIFETCH.add(prefetchChild.item(j).getFirstChild().getNodeValue().trim());
            }
            logger.info(PRIFETCH.toString());
            //fetch chain List
            Element fetchElement = (Element) settingNodes.item(3);
            NodeList fetchChild = fetchElement.getChildNodes();
            for(int j=0;j<fetchChild.getLength();j++){
            	if(fetchChild.item(j).getNodeType()==Node.ELEMENT_NODE)
            		FETCH.add(fetchChild.item(j).getFirstChild().getNodeValue().trim());
            }
            logger.info(FETCH.toString());
            //extractor chain List
            Element extractorElement = (Element) settingNodes.item(4);
            NodeList extractorChild = extractorElement.getChildNodes();
            for(int j=0;j<extractorChild.getLength();j++){
            	if(extractorChild.item(j).getNodeType()==Node.ELEMENT_NODE)
            		EXTRACTOR.add(extractorChild.item(j).getFirstChild().getNodeValue().trim());
            }
            logger.info(EXTRACTOR.toString());
            //writer chain List
            Element writerElement = (Element) settingNodes.item(5);
            NodeList writerChild = writerElement.getChildNodes();
            for(int j=0;j<writerChild.getLength();j++){
            	if(writerChild.item(j).getNodeType()==Node.ELEMENT_NODE)
            		WRITER.add(writerChild.item(j).getFirstChild().getNodeValue().trim());
            }
            logger.info(WRITER.toString());
            //postprocessorChild chain List
            Element postprocessorElement = (Element) settingNodes.item(6);
            NodeList postprocessorChild = postprocessorElement.getChildNodes();
            for(int j=0;j<postprocessorChild.getLength();j++){
            	if(postprocessorChild.item(j).getNodeType()==Node.ELEMENT_NODE)
            		POSTPROCESSOR.add(postprocessorChild.item(j).getFirstChild().getNodeValue().trim());
            }
            logger.info(POSTPROCESSOR.toString());
            
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static boolean isInProcessor(String name) {
		return isInExtrator(name)||isInWriter(name)||isInPostprocessor(name)||
				isInPrefetch(name)||isInFetch(name);
	}
	
	public static boolean isInPrefetch(String name){
		if(PRIFETCH.indexOf(name)!=-1)
			return true;
		return false;
	}
	
	public static boolean isInFetch(String name){
		if(FETCH.indexOf(name)!=-1)
			return true;
		return false;
	}
	
	public static boolean isInExtrator(String name){
		if(EXTRACTOR.indexOf(name)!=-1)
			return true;
		return false;
	}
	
	public static boolean isInWriter(String name){
		if(WRITER.indexOf(name)!=-1)
			return true;
		return false;
	}

	public static boolean isInPostprocessor(String name){
		if(POSTPROCESSOR.indexOf(name)!=-1)
			return true;
		return false;
	}
	
	public static String getDEPTH() {
		return DEPTH;
	}

	public static String getWIDTH() {
		return WIDTH;
	}

	public static int getMAXTHREADS() {
		return MAXTHREADS;
	}

	public static String getDRIVER() {
		return DRIVER;
	}

	public static String getDBURI() {
		return DBURI;
	}

	public static String getUSERNAME() {
		return USERNAME;
	}

	public static void setUSERNAME(String uSERNAME) {
		USERNAME = uSERNAME;
	}

	public static String getPASSWORD() {
		return PASSWORD;
	}

	public static ArrayList<String> getPRIFETCH() {
		return PRIFETCH;
	}

	public static ArrayList<String> getFETCH() {
		return FETCH;
	}
	
	public static ArrayList<String> getEXTRACTOR() {
		return EXTRACTOR;
	}

	public static ArrayList<String> getWRITER() {
		return WRITER;
	}

	public static ArrayList<String> getPOSTPROCESSOR() {
		return POSTPROCESSOR;
	}

	public static CrawlController getController() {
		return controller;
	}

	public static void setController(CrawlController controller) {
		SettingsXMLUtil.controller = controller;
	}

	public static String getSTOREPATH() {
		return STOREPATH;
	}
	
}
