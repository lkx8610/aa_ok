
package framework.base;

import java.util.List;

import org.apache.log4j.Logger;
import org.testng.Assert;

import framework.base.utils.ReportUtils;
/**
 *  这是 硬断言，当有些关键步骤需要在出错时就没必须再进行其它步骤时，用Verify，
 * 当一些无关紧要的步骤失败时，仍希望测试进行后边的断言，则需要用Velidate类中的方法（软断言）
 * @author James Guo
 */
public class Verify {

	private static Logger logger = LoggerManager.getLogger(Thread.currentThread().getClass().getName());
	
	// Keep a running tally of how many verifies are executed during a test
	private static ThreadLocal<Integer> verifyCount = new ThreadLocal<Integer>() {
		@Override
		public Integer initialValue() {
			return 0;
		}
	};
	
	public static int getAndResetVerifyCount() {
		int count = verifyCount.get();
		verifyCount.set(0);
		return count;
	}
	
	private static void incrementVerifyCount() {
		verifyCount.set(verifyCount.get() + 1);
	}
	/**
	 * Verify that the 'expected' substring is contained in the 'actual' string
	 */
	public static void verifyContains(String actual, String expected, String message){
		incrementVerifyCount();
		//((null == actual) ^ (null == expected): 表示二个不同时为 null,因为，如果二者中有null时，actual.contains(expected)抛异常，进不到 if{}中。
		if ((null != actual && null != expected) && ! actual.contains(expected) || 
				(null == actual) ^ (null == expected) ) {
			Assert.fail(getVerifyText(message) + " Did not find expected text '" + expected + "' contained in '" + actual + "'");
		}
		logger.info(ReportUtils.formatVerify(getVerifyText(message) + "Actual Result-[" + actual + "] contains Expected Result-[" + expected + "]"));
	}
	
	public static void verifyNotContains(String actual, String expected, String message){
		incrementVerifyCount();
		
		if ((null != actual && null != expected) && actual.contains(expected) || 
				(null == actual) && (null == expected) ) {
			Assert.fail(getVerifyText(message) + " the expected text '" + expected + "' contained in '" + actual + "'");
		}
		logger.info(ReportUtils.formatVerify(getVerifyText(message)  + "Actual Result-[" + actual + "] Not contains Expected Result-[" + expected + "]"));
	}

	public static void verifyTrue(boolean condition, String description) {
		incrementVerifyCount();
		Assert.assertTrue(condition, getVerifyText(description));
		logger.info(ReportUtils.formatVerify(getVerifyText(description)));
	}
	
	public static void verifyFalse(boolean condition, String description) {
		incrementVerifyCount();
		Assert.assertFalse(condition, getVerifyText(description));
		logger.info(ReportUtils.formatVerify(getVerifyText(description)));
	}
	
	public static void verifyFail(String description) {
		incrementVerifyCount();
		Assert.fail(getVerifyText(description));
		logger.info(ReportUtils.formatVerify(getVerifyText(description)));
	}

	public static void verifyEquals(int actual, int expected, String message) {
		incrementVerifyCount();
		Assert.assertEquals(actual, expected, getVerifyText(message));
		logger.info(ReportUtils.formatVerify(getVerifyText(message) + " [" + actual + "] equals [" + expected + "]"));
	}
	
	public static void verifyBetween(int actual, int expectedMin, int expectedMax, String message) {
		incrementVerifyCount();
		String verifyMessage = String.format("%s %d <= %d <= %d", getVerifyText(message), expectedMin, actual, expectedMax);
		Assert.assertTrue(actual >= expectedMin && actual <= expectedMax, verifyMessage);
		logger.info(ReportUtils.formatVerify(verifyMessage));		
	}
	
	public static void verifyNotEquals(int actual, int expected, String message) {
		incrementVerifyCount();
		Assert.assertNotEquals(actual, expected, getVerifyText(message));
		logger.info(ReportUtils.formatVerify(getVerifyText(message)));
	}
	
	public static void verifyMatches(String actual, String regExExpected, String message) {
		incrementVerifyCount();
		Assert.assertTrue(actual.matches(regExExpected), getVerifyText(message) + " [" + actual + "] matches [" + regExExpected + "]");
		logger.info(ReportUtils.formatVerify(getVerifyText(message) + " [" + actual + "] matches [" + regExExpected + "]"));
	}
	
	public static void verifyEquals(String actual, String expected, String message) {
		incrementVerifyCount();
		Assert.assertEquals(actual, expected, getVerifyText(message));
		logger.info(ReportUtils.formatVerify(getVerifyText(message) + " [" + actual + "] equals [" + expected + "]"));
	}

	public static void verifyNotEquals(String actual, String expected, String message) {
		incrementVerifyCount();
		Assert.assertNotEquals(actual, expected, getVerifyText(message));
		logger.info(ReportUtils.formatVerify(getVerifyText(message) + " [" + actual + "] does NOT equal [" + expected + "]"));
	}

	public static void verifyEquals(Object actual, Object expected, String message) {
		incrementVerifyCount();
		Assert.assertEquals(actual, expected, getVerifyText(message));
		logger.info(ReportUtils.formatVerify(getVerifyText(message) + " [" + actual + "] equals [" + expected + "]"));
	}

	public static void verifyNotEquals(Object actual, Object expected, String message) {
		incrementVerifyCount();
		Assert.assertNotEquals(actual, expected, getVerifyText(message));
		logger.info(ReportUtils.formatVerify(getVerifyText(message) + " [" + actual + "] does NOT equal [" + expected + "]"));
	}
	
	/**
	 * Compare two Lists of String for equality
	 */
	public static void verifyEquals(List<String> actual, List<String> expected, String message) {
		StringBuilder expectedSb = new StringBuilder();
		for (String s : expected) {
			expectedSb.append(s).append(",");
		}
		StringBuilder actualSb = new StringBuilder();
		for (String item : actual) {
			actualSb.append(item).append(",");
		}
		verifyEquals(actualSb.toString(), expectedSb.toString(), message);
	}
	
	/**
	 * VerifyNull:
	 */
	public static void verifyNull(Object actual, String msg){
		incrementVerifyCount();
		Assert.assertNull(actual, msg);
		logger.info(ReportUtils.formatVerify(getVerifyText(msg) + " [" + actual + "] is Null "));
	}
	/**
	 * VerifyNotNull:
	 */
	public static void verifyNotNull(Object actual, String msg){
		incrementVerifyCount();
		Assert.assertNotNull(actual, msg);
		logger.info(ReportUtils.formatVerify(getVerifyText(msg) + " [" + actual + "] is Not Null "));
	}
	
	/**
	 * Prepends "Verify: " to the report/log entries for verification entries.
	 */
	public static String getVerifyText(String description) {
		return "Assertion : " + description + "\n\n";
	}
	
	/*public static void verifyOutput(String controlFileName, int expectedCountMin, int expectedCountMax) {
		File controlFile = TestUtils.getControlFile(controlFileName);
		int actualCount = -1;
		if (controlFile.exists()) {
			actualCount = TestUtils.getCountFromControlFile(controlFileName);	
		} else {
			actualCount = DatabaseUtils.getTableCount(controlFileName);
		}
		if (expectedCountMin == expectedCountMax) {
			verifyEquals(actualCount, expectedCountMin, "Output count matches expected count for " + controlFileName);
		} else {
			verifyBetween(actualCount, expectedCountMin, expectedCountMax, "Output count matches expected count for " + controlFileName);
		}		
	}
	
	public static void verifyFileCount(String controlFileName, int expectedCount){
		File controlFile = TestUtils.getControlFile(controlFileName);
		int actualCount = -1;
		if (controlFile.exists()) {
			actualCount = TestUtils.getFileCountFromControlFile(controlFileName);
			verifyEquals(actualCount, expectedCount, "Output count matches expected count for " + controlFileName);
		} else {
			Assert.fail("File does not exist");
		}
	}*/
}
