package framework.base.dataparser.properties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import framework.base.LoggerManager;

/**
 * @description TODO
 * @author James Guo
 **/
public class PropertiesParser {
	private Logger logger = LoggerManager.getLogger(PropertiesParser.class.getSimpleName());

	private Properties prop = new Properties();
	
	private Map<String, String> pramsMap = null; //data map
	
	public PropertiesParser(String propFileName){
		this.parseProperties(propFileName);
	}

	private void parseProperties(String propFilePath){
		try {
			if (null == propFilePath || "".equals(propFilePath.trim())) {
				logger.error("invalid Properties File Path input...");

				throw new FileNotFoundException("");
			}
			InputStream in = PropertiesParser.class.getClassLoader().getResourceAsStream(propFilePath);
			logger.debug("parsing Properties file: " + propFilePath);

			try {
				prop.load(in);
				
				// 加载文件完成后，设置数据
				setPropParamsMap();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getPropParam(String propKey){
		logger.debug("searching propties with propKey:" + propKey);
		return prop.getProperty(propKey);
	}
	
	@SuppressWarnings("all")
	private void setPropParamsMap(){
		pramsMap = new HashMap<>();
		
		Enumeration e = prop.keys();
		while(e.hasMoreElements()){
			String key = (String)e.nextElement();
			String value = prop.getProperty(key);
			
			pramsMap.put(key, value);
		}
		
	}
	
	public Map<String, String> getPropDatMap(){
		return this.pramsMap;
	}
}







