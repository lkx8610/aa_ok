package framework.webdriver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Level;

public class LogLevel {

	private static Level Info 	= Level.INFO;
	private static Level Debug = Level.DEBUG;
	private static Level Warn 	= Level.WARN;
	private static Level Error = Level.ERROR;
	private static Level Fatal = Level.FATAL;
	private static Level Off 	= Level.OFF;
	
	static Map<String, Level> levelMap = new HashMap<>();
	
	static Level[] level = { Info, Debug, Warn, Error, Fatal, Off };
	
	static{
		for(int i=0; i<level.length; i++){
			levelMap.put(level[i].toString().toLowerCase(), level[i]);
		}
	}
	public static void main(String[] args) {
		Iterator<String> it = levelMap.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			String value = levelMap.get(key).toString();
			System.out.println(key + " = " + value);
		}
		
	}
}
