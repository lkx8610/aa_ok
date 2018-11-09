package framework.restapi.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import framework.base.LoggerManager;

/**
 * 将Json中的双引号 " 转换成 \"，或转成单引号，以便在代码中使用
 * @author James
 *
 */
public class ConvertDoubleQuotesUtil {
	private static Logger log = LoggerManager.getLogger(ConvertDoubleQuotesUtil.class.getSimpleName());

	/**
	 * @Description： 将Json串中的“ " ” 转化成“ \" ” <pre>
	 * @author：James <pre>
	 * @return: <pre>  
	 * @throws： <pre>
	 */
	public static String convertDoubleQuotes2SlashQuotes(String code){
		if(StringUtils.isBlank(code)){
			log.info("The code to be converted is null or blank, convertion failed...");
			throw new RuntimeException("The code to be converted is null or blank, convertion failed...");
		}
		return code.replace("\"", "\\\"");
	}
}
