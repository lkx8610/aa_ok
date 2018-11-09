package framework.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

import framework.base.LoggerManager;
import framework.base.utils.PerformanceTimer;
import framework.webdriver.TestContext;
import framework.webdriver.WebDriverUtils;

public class FrameworkUtil {
	private static Logger logger = LoggerManager.getLogger(FrameworkUtil.class.getSimpleName());

	//// 为字符串加上双引号：" + value
	public static String wrapDoubleQuotes(String value) {
		return "\"" + value + "\"";
	}

	// 为字符串加上单引号：' + value
	public static String wrapSingleQuotes(String value) {
		return "\"" + value + "\"";
	}

	/**
	 * 判断字符串是否为空或为Null，若为Null或""，返回"", 否则返回content.trim();
	 * 
	 * @author James Guo
	 */
	public static String isNullOrBlank(String content) {

		if (null == content || "".equals(content.trim())) {
			return "";
		}

		return content.trim();
	}

	// 生成随机的“下载链接：
	public static String genRandomDownloadLink() {
		String[] links = { "http://www.baiddu.com", "http://www.qq.com", "http://www.google.com",
				"http://www.tendcloud.com" };
		Random random = new Random();
		int randomNum = random.nextInt(links.length);

		return links[randomNum];
	}

	/**
	 * 检查页面中链接的可用性：TODO
	 */
	public boolean checkLinksAvailable(String url) {

		return false;
	}

	//格式化日期：用于给testcase打完结果后，标明最后跑的日期
	public static String dateFormater(){
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
		String dateStr = df.format(new Date());
		return dateStr;
	}
	// ------- 定义几种等待的方式 --------//
	// 等待页面加载完毕
	public static void waitForPageLoad() {
		WebDriverWait wait = new WebDriverWait(TestContext.getWebDriver(), TestContext.getDomTimeout());
		wait.until(isPageLoaded());
	}

	// 判断页面是否加载完成,如，登录系统后或页面跳转后，判断页面是否加载完成再继续其它的操作：
	public static Function<WebDriver, Boolean> isPageLoaded() {
		return new Function<WebDriver, Boolean>() {
			@Override
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		};
	}

	// 简单等待,对Thread.sleep()方法进行处理，在其它地方调用时不再需要单独的try...catch处理：
	public static void waitFor(long milles) {
		try {
			Thread.sleep(milles);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("\n time out with exception: " + e.getMessage());
		}
	}

	/**
	 * 判断页面的Ajax是否完成.
	 */
	public static void waitForAjaxComplete() {
		logger.debug("waitForAjaxComplete");
		PerformanceTimer timer = new PerformanceTimer(TestContext.getAjaxTimeout());

		while (isAjaxExecuting() && !timer.hasExpired()) {
			PerformanceTimer.wait(100);
		}
		if (timer.hasExpired()) {
			throw new TimeoutException(
					"Timed out waiting for ajax call to complete, exceeded " + TestContext.getAjaxTimeout() + " ms");
		}
		logger.debug("waitForAjaxComplete elapsed time " + timer.getElapsedTimeString());
	}

	public static boolean isAjaxExecuting() {
		try {
			Long ajaxInProgressIndicator = (Long) WebDriverUtils.executeJavaScript("return ajaxInProgressIndicator;");
			logger.debug("ajaxInProgressIndicator = "
					+ (ajaxInProgressIndicator == null ? " null" : ajaxInProgressIndicator));
			return ajaxInProgressIndicator != null && ajaxInProgressIndicator > 0;
		} catch (WebDriverException e) {
			return false;
		}
	}

	/**
	 * 写日志的简单实现
	 */
	protected static void report(String message) {
		logger.info(message);
	}
}
