package framework.base.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;

import framework.base.LoggerManager;

/*
 * 读取工程下的 src/test/resources/conf/config.properties文件：
 * */
public class ConfigReader {
	private static Logger logger = LoggerManager.getLogger(ConfigReader.class);
	private static ConfigReader configReader;
	//和TestNGRetry相关的配置信息：
	private int retryCount = 0;
	private String sourceCodeDir = "src";
	private String sourceCodeEncoding = "UTF-8";

	//和TestNGRetry相关的配置信息：
	private static final String RETRYCOUNT = "retrycount";
	private static final String SOURCEDIR = "sourcecodedir";
	private static final String SOURCEENCODING = "sourcecodeencoding";
	

	private ConfigReader() {
		//readConfig(CONFIGFILE);
	}

	public static ConfigReader getInstance() {
		if (configReader == null) {
			configReader = new ConfigReader();
		}
		return configReader;
	}

	private void readConfig(String fileName) {
		Properties properties = getConfig(fileName);
		if (properties != null) {
			String sRetryCount = null;

			Enumeration<?> en = properties.propertyNames();
			while (en.hasMoreElements()) {
				String key = ((String) en.nextElement()).trim().toLowerCase();
				if (key.equals(RETRYCOUNT) && (!"".equals(properties.getProperty(key)))) {//如果配置文件中有配置的值就取配置文件中的该Key的值，如果该Key没有被配置值就取本类中设置的初始值；
					sRetryCount = properties.getProperty(key);
				}
				if (key.equals(SOURCEDIR) && (!"".equals(properties.getProperty(key)))) {
					sourceCodeDir = properties.getProperty(key);
				}
				if (key.equals(SOURCEENCODING) && (!"".equals(properties.getProperty(key)))) {
					sourceCodeEncoding = properties.getProperty(key);
				}
				
			}
			if (sRetryCount != null) {
				sRetryCount = sRetryCount.trim();
				try {
					retryCount = Integer.parseInt(sRetryCount);
				} catch (final NumberFormatException e) {
					throw new NumberFormatException(
							"Parse " + RETRYCOUNT + " [" + sRetryCount + "] from String to Int Exception");
				}
			}
		}
	}

	/**
	 * 
	 * @param propertyFileName
	 * 
	 * @return
	 */
	private Properties getConfig(String propertyFileName) {
		Properties properties = new Properties();
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(propertyFileName);
		try {
			properties.load((new InputStreamReader(in, "utf-8")));//Properties的默认编码是“iso8859-1”，防止Properties乱码，这里用InputStreamReader进行编码转换
		} catch (FileNotFoundException e) {
			properties = null;
			logger.warn("FileNotFoundException:" + propertyFileName);
		} catch (IOException e) {
			properties = null;
			logger.warn("IOException:" + propertyFileName);
		}
		return properties;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public String getSourceCodeDir() {
		return this.sourceCodeDir;
	}

	public String getSrouceCodeEncoding() {
		return this.sourceCodeEncoding;
	}


	/*public static void main(String[] args) {
		ConfigReader r = new ConfigReader();
		System.out.println(r.app_url + ":" + r.report_path + ":" + r.getLog_path() + ":" + r.getReport_dir() + 
		r.retryCount + ":" + r.sourceCodeDir + ":" + r.sourceCodeEncoding);
	}*/
}
