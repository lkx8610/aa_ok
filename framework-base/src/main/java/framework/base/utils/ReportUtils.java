package framework.base.utils;

/**
 * 格式化报告的工具类：根据需要用于在生成的HTML报告中以不同的字体与颜色突出显示报告内容
 * 	
 * @author James Guo
 *
 */
public class ReportUtils {
	
	public static final String ACTION 	= "action";
	public static final String DATA 	= "data";
	public static final String WARN 	= "warn";
	public static final String VERIFY 	= "verify";
	public static final String ERROR 	= "error";
	public static final String STEP 	= "step";
	
	public static String format(String format, String data) {
		return String.format("<span class='%s'>%s</span>", format, data);
	}

	public static String formatData(String data) {
		return format(DATA, data);
	}

	public static String formatWarn(String data) {
		return format(WARN, data);
	}
	
	public static String formatError(String data) {
		return format(ERROR, data);
	}

	public static String formatVerify(String data) {
		return format(VERIFY, data);
	}

	public static String formatStepInfo(String data) {
		return format(STEP, data);	
	}

	public static String formatAction(String action) {
		return String.format("<span class='%s'>%s</span>", ACTION, action);
	}
	
	public static String formatAction(String action, String data) {
		return String.format("%s %s", formatAction(action), formatData(data));
	}
	public static String formatAction(String action, String other, String data) {
		return String.format("%s %s %s", formatAction(action), other, formatData(data));
	}
	
}
