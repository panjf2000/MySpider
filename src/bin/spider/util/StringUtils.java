package bin.spider.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringUtils {
	
	static Logger logger = LoggerFactory.getLogger(StringUtils.class);
//	public static void main(String[] args) {
//		getSedds();
//	}

	public static List<String> getSedds() {
		List<String> seeds = new ArrayList<String>();
		try {
        	InputStream in = SettingsXMLUtil.class.getClassLoader()
    				.getResourceAsStream("seed.txt");
            InputStreamReader read = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while((lineTxt = bufferedReader.readLine()) != null){
                seeds.add(lineTxt);
            }
            read.close();
	    } catch (Exception e) {
	    	logger.error("读取文件内容出错:{}",e.toString());
//	        System.out.println("读取文件内容出错");
//	        e.printStackTrace();
	    }
		System.out.println(seeds);
		return seeds;
	}
	
	/** 
     * 将InputStream转换成某种字符编码的String 
     * @param in 
     * @param encoding 
     * @return 
     * @throws Exception 
     */  
     public static String InputStreamTOString(InputStream in,String encoding) throws Exception{           
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
        byte[] data = new byte[1024];  
        int count = -1;  
        while((count = in.read(data,0,1024)) != -1)  
            outStream.write(data, 0, count);         
        data = null;  
        return new String(outStream.toByteArray(),encoding);  
    }
    
}
