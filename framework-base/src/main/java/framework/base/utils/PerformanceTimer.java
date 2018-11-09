package framework.base.utils;

import org.apache.log4j.Logger;

import framework.base.LoggerManager;
import framework.base.exception.TimeoutException;

/**
 * 测试执行的计时器工具
 */
public class PerformanceTimer {

	private static Logger logger = LoggerManager.getLogger(Thread.currentThread().getName() + ".PerformanceTimer");

	private long startTime;
	private long lastTime;
	private long waitTime = -1;
	
	public PerformanceTimer() {
		start();
	}
	
	public PerformanceTimer(int maxWaitTimeInMilliseconds) {
		this();
		waitTime = maxWaitTimeInMilliseconds;
	}

	public boolean hasExpired() {
		return waitTime > -1 && getElapsedTime() > waitTime;
	}
	
	public static void wait(int milliseconds) {
		try {
			logger.debug("sleep " + milliseconds + " ms");
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			//
		}
	}
	
	/**
	 * "restart" for instance re-use
	 */
	public void start() {
		startTime = lastTime = System.currentTimeMillis();
	}
	
	/**
	 * Convenience method for logging 
	 */
	public String getElapsedTimeString() {
		return getElapsedTime() + " (ms)";
	}

	/**
	 * Returns the total elapsed time since the PerformanceTimer was started as well as the time since
	 * the last call to this method.   Used for Performance timing of sub-sections within a larger test.
	 *  
	 * @return		"Total: 23770 (ms), Since Last Call:   754 (ms)"
	 */
	public String getRunningTimeString() {
		long last = lastTime;
		lastTime = System.currentTimeMillis();
		long now = lastTime;
		return String.format("Total:%6d (ms), Since Last Call:%6d (ms)", now-startTime, now-last);
	}
	
	public void logRunningTime(String message) {
		logger.info(getRunningTimeString() + " : " + message);
	}
	
	/**
	 * @return The elapsed time in milliseconds
	 */
	public long getElapsedTime() {
		return System.currentTimeMillis() - startTime;
	}
	
	public void assertLessThan(int milliseconds) throws Exception {
		long elapsed = getElapsedTime();
		if (elapsed > milliseconds) {
			throw new TimeoutException("Exceeded maximum time of " + milliseconds + " , actual was " + elapsed);
		}
	}
}
