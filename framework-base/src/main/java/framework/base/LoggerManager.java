package framework.base;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/** 
*@description 用于提供统一的Log4j记录器； 
*@author James Guo
**/
public class LoggerManager{
		
		//获取Logger记录器：
		public static Logger getLogger(Class<?> clazz) {
			return getLogger(clazz.getName());
		}
		public static Logger getLogger(String name) {
			return LogManager.getLogger(name);
		}
}