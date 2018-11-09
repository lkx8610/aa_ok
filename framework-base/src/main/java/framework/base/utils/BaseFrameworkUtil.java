package framework.base.utils;

public class BaseFrameworkUtil {

	//为字符串加上双引号：" + value
	public static String wrapDoubleQuotes(String value) {
		return "\"" + value + "\"";
	}
	
	// 为字符串加上单引号：' + value
	public static String wrapSingleQuotes(String value) {
		return "\"" + value + "\"";
	}
	
	/**
	 * 判断字符串是否为空或为Null，若为Null或""，返回 false, 否则返回true;
	 * @author James Guo
	 */
	public static boolean isNotNullOrBlank(String content){
		
		if(null == content || "".equals(content.trim())){
			return false;
		}
		
		return true;
	}

	/**
	 * 检查页面中链接的可用性：TODO
	 */
	public boolean checkLinksAvailable(String url){
		
		return false;
	}
}
