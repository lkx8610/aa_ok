package com.tendcloud.adt.testcases.keywords.unit;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.tendcloud.adt.framework.base.AdtTestBase;

import framework.base.LoggerManager;
import framework.base.exception.StopRunningException;
import framework.base.utils.ReportUtils;
import framework.keyworddriven.BaseKeywords;
import framework.webdriver.TestNGListener;
import test.base.TestGroups;

@Listeners(TestNGListener.class)
public class TestSwitchWindowAndFrame extends AdtTestBase{
	private Logger log = LoggerManager.getLogger(this.getClass().getName());
	BaseKeywords baseKey = new BaseKeywords();
	
	@Test(groups = TestGroups.UNIT)
	public void testSwitch2tWin() throws Exception {
		getWebDriver().get("http://172.20.7.2:10005/webapp/index/login.html");

		// bk.getCurrentWindow("", null, "param");

		Map<String, Object> map = baseKey.getTempValuMap();
		String winHandler = (String) map.get("param");
		System.out.println(winHandler);

		try {
			Method swiTchWindow = baseKey.getClass().getMethod("switch2Window");
			swiTchWindow.invoke(baseKey, "切换窗口", null, null);// 不传值，抛出异常
		} catch (Exception e) {
			report(ReportUtils.formatError(e.getMessage()));
			report((e instanceof StopRunningException) ? "Yes" : "No");
		}
	}

	@Test(groups = TestGroups.UNIT, expectedExceptions = StopRunningException.class)
	public void testSwitchFrame() throws Exception {
		BaseKeywords bk = new BaseKeywords();
		/*
		 * String framePath = "abc";//单层切换 bk.switchFrame("swith to frame",
		 * framePath, null);
		 */

		String framePathList = "a, b, c, d";
		//bk.switch2Frame("多层切换...", framePathList, null);

	}

	// ---------------------------------
	private String getCurrentWindow() throws StopRunningException {
		// stepInfo(stepDescription);

		String currentWindow = null;
		try {
			currentWindow = getWebDriver().getWindowHandle();
			report("获取到的当前窗口句柄：********* " + currentWindow + " *********");
			return currentWindow;

			// windowHandleMap.put(currentWindownHandler,
			// currentWindow);//将当前的窗口句柄存入句柄专用Map

		} catch (Exception e) {
			report(ReportUtils.formatError("不能正确获取当前的窗口... ' " + e.getMessage() + " '"));

			throw new StopRunningException(e);
		} finally {
			// increaceStepNum();
		}
	}

	private Map<String, String> windowHandleMap = new HashMap<>();

	public void switch2Window(String stepDescription, String locator, String windowToBeSwitched) throws StopRunningException {
		stepInfo(stepDescription);

		final String firstWindow = "firstWindow";// 代表第一个打开的窗口

		try {
			if (StringUtils.isBlank(windowToBeSwitched)) {// 未传参，且Map中没有“
															// firstWindow ”
															// 对应的值：报错，将当前的Window存入Map，Key
															// = firstWindow
				if (null == windowHandleMap.get(firstWindow)) {
					report(ReportUtils.formatError("请传入需要切换的窗口名称..."));
					windowHandleMap.put(firstWindow, this.getCurrentWindow());
					return;
				} else {// 未传参，但Map中存在 firstWindow ” 对应的值：日志输出，且切换到" firstWindow
						// ":
					report(ReportUtils
							.formatAction("未指定要切换的窗口名称，默认将切换到当前第一个窗口... [ " + windowHandleMap.get(firstWindow) + " ]"));
					getWebDriver().switchTo().window(windowHandleMap.get(firstWindow));// 切回第一个窗口；
				}
			} else {// 传递了窗口参数：
				String targetWindow = windowHandleMap.get(windowToBeSwitched);// 根据参数去Map中取；
				if (null != targetWindow) {// 取到
					if (targetWindow == this.getCurrentWindow()) {// 取到了已存放的Window，但与当前所在的Window是同一个;
						report(ReportUtils.formatError("要切换的目标窗口与当前所在的窗口相同，无需切换，请检查... 目标窗口句柄 [ " + targetWindow
								+ " ], 当前窗口句柄 [ " + this.getCurrentWindow() + " ]"));
						return;
					} else {// 传递了参数，且Map中有该参数对应的值，且该值与当前窗口不同，则说明，是要切换回原来已打开的老窗口：
						report(ReportUtils.formatAction("将切换窗口到 [ " + targetWindow + " ]"));
						getWebDriver().switchTo().window(targetWindow);
						report("成功切换到窗口：[ " + targetWindow + " ]");
					}
				} else {// 传递了参数，但在Map中不存在，则说明要切换的是一个新窗口，需得到全部句柄，再遍历：
					Set<String> windowsSet = getWebDriver().getWindowHandles();
					report("当前所有的窗口句柄：[ " + windowsSet.toString() + " ]");

					for (String window : windowsSet) {
						if (!windowHandleMap.containsValue(window)) {// 遍历当前所有窗口，如果当前Map中不包含的窗口，就是要切换的最新打开的目标窗口，
							report("将切换到新窗口： [ " + window + " ]");
							getWebDriver().switchTo().window(window);

							// 将当前已切换的窗口放入Map：
							windowHandleMap.put(windowToBeSwitched, window);
							report(ReportUtils.formatAction("成功切换到目标窗口：[ " + window + " ]"));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new StopRunningException(e);
		}finally{
			//
		}
	}

}
