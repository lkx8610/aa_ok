
package framework.base;

import java.util.List;

import org.apache.log4j.Logger;

import framework.base.utils.ReportUtils;
import framework.base.utils.TestUtils;

/**
 * 'Soft' verifications that parallel the Verify class.  These will log everything as a Verify would, but will trap
 * exceptions and allow the test to continue.  At the end of the test method the framework will fail the test if 
 * any Validations have failed.
 * 
 */
public class Validate {

	private static Logger logger = LoggerManager.getLogger(Thread.currentThread().getClass().getName());
	
	private static ThreadLocal<Integer> validateFailureCount = new ThreadLocal<Integer>() {
		@Override
		public Integer initialValue() {
			return 0;
		}
	};
	
	public static int getAndResetValidationFailureCount() {
		int count = validateFailureCount.get();
		validateFailureCount.set(0);
		return count;
	}

	private static void incrementFailureCount() {
		validateFailureCount.set(validateFailureCount.get() + 1);
	}
	
	public static void validateContains(String actual, String expected, String message) {
		try {
			Verify.verifyContains(actual, expected, message);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}
	}
	//断言期望不包含在实际中
	public static void validateNotContains(String actual, String expected, String message) {
		try {
			Verify.verifyNotContains(actual, expected, message);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}
	}
	
	public static void validateTrue(boolean condition, String description) {
		try {
			Verify.verifyTrue(condition, description);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}
	}
	
	public static void validateFalse(boolean condition, String description) {
		try {
			Verify.verifyFalse(condition, description);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}
	}

	public static void validateEquals(int actual, int expected, String message) {
		try {
			Verify.verifyEquals(actual, expected, message);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}
	}
	
	public static void validateBetween(int actual, int expectedMin, int expectedMax, String message) {
		try {
			Verify.verifyBetween(actual, expectedMin, expectedMax, message);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}
	}
	
	public static void validateNotEquals(int actual, int expected, String message) {
		try {
			Verify.verifyNotEquals(actual, expected, message);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}
	}
	
	public static void validateMatches(String actual, String regExExpected, String message) {
		try {
			Verify.verifyMatches(actual, regExExpected, message);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}
	}
	
	public static void validateEquals(String actual, String expected, String message) {
		try {
			Verify.verifyEquals(actual, expected, message);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}
	}

	public static void validateNotEquals(String actual, String expected, String message) {
		try {
			Verify.verifyNotEquals(actual, expected, message);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}
	}

	public static void validateEquals(Object actual, Object expected, String message) {
		try {
			Verify.verifyEquals(actual, expected, message);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}
	}

	public static void validateNotEquals(Object actual, Object expected, String message) {
		try {
			Verify.verifyNotEquals(actual, expected, message);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}
	}
	
	public static void validateEquals(List<String> actual, List<String> expected, String message) {
		try {
			Verify.verifyEquals(actual, expected, message);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}
	}
	
	public static void validateFail(String message) {
		try {
			Verify.verifyFail(message);
		} catch (AssertionError ex) {
			logValidationFailure(ex);
		}

	}
	
	private static void logValidationFailure(AssertionError ex) {
		logger.error(ReportUtils.formatError(TestUtils.getFailureMessage(ex)));
		incrementFailureCount();
		//TestContext.get().captureScreen("Screen at time of validation failure.");
	}
	
}
