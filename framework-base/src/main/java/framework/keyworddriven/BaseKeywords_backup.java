package framework.keyworddriven;

import static framework.base.Verify.verifyContains;
import static framework.base.Verify.verifyEquals;
import static framework.base.Verify.verifyNotContains;
import static framework.base.Verify.verifyNotNull;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;

import classic.pages.AbstractBasePage;
import framework.base.BaseFramework;
import framework.base.LoggerManager;
import framework.base.Validate;
import framework.base.Verify;
import framework.base.exception.NonStopRunningException;
import framework.base.exception.StopRunningException;
import framework.base.utils.PerformanceTimer;
import framework.base.utils.ReportUtils;
import framework.keyworddriven.executionengine.ExecutionEngine;
import framework.webdriver.IFrameHelper;
import framework.webdriver.TestContext;
import framework.webdriver.WebDriverUtils;
import test.base.TestCaseBase;

/**
 * 该类是关键字的公共基类，可用于任意项目中<br/>
 * 继承AbstractBasePage类，可以获取很多有用的方法（如driver的获取 getWebDriver(), 元素获取的方法等）
 * @author James Guo
 */
public class BaseKeywords_backup extends AbstractBasePage {
	protected int stepNum = 1;// 记录测试的第几步；
	
	public final static String StoppingRunning = "stoppingRunning";//如果异常会影响以后的操作，停止进行下边的操作；
	public final static String ContinueRunning = "continueRunning";//如果异常，不影响以后的操作，继续进行

	private static Logger log = LoggerManager.getLogger(BaseKeywords_backup.class.getSimpleName());
	protected WebDriver driver = getWebDriver();

	/**
	 * 用于存储在测试过程中产生的临时变量值，如果这些值在以后的操作步骤中还要用到，就暂时放到该Map中，该测试用例结束时，清空此Map
	 * @author James Guo
	 */
	public Map<String, Object> tempValueMap = new HashMap<>();
	public Map<String, Object> getTempValuMap(){
		return tempValueMap;
	}
	/**
	 * 专用于存放各个窗口句柄的Map，每当调用“ getWindowHandle() ” 方法时，就会将当前的窗口句柄存入此Map，切换不同的窗口时，用之前保存的变量来该Map中取即可；
	 */
	public Map<String, String> windowHandleMap = new HashMap<>();
	public Map<String, String> getWindowHandleMap(){
		return this.windowHandleMap;
	}

	/** --------------定义一般方法（非关键字）开始----------------*/
	/**
	 * 测试步骤文案提示：Step1. xxx的形式, 非关键字定义
	 * @author James Guo
	 * @param stepInfo： 传入当前步骤的描述信息；
	 */
	public void stepInfo(String stepInfo) {
		String step = "";
		if(!ExecutionEngine.isRerun){
			step = "Step" + stepNum + ". ";
			step += stepInfo;
			logger.info(ReportUtils.formatStepInfo(step));
		}else{
			step += stepInfo;
		}
	}
	/**
	 * 每测试一步，测试步骤 +1：非关键字定义
	 * @author James Guo
	 */
	public void increaceStepNum() {
		if(!ExecutionEngine.isRerun){
			stepNum++;
		}
	}

	/**
	 * 重置测试步骤：当一个测试用例结束后，新开始的测试，其步骤重新从1开始. 非关键字定义
	 * @author James Guo
	 */
	public void resetStepNum() {
		stepNum = 1;
	}
	/**
	 * 失败截图：内部或子类调用. 非关键字定义
	 * @author James Guo
	 */
	protected void takeScreenshot(){
		if(!ExecutionEngine.isRerun){
			TestContext.get().captureScreen("失败截图");//截图
		}
	}
	/**
	 * @Description： 判断元素是否出现, 内部调用，非关键字：
	 * @author：James
	 * @return: boolean  
	 */
	protected boolean isElementApeare(String locator){
		//log.info("等待元素出现：" + locator);
		try {
			WebElement e = getElement(locator);
			if(null != e){
				return true;
			}
		} catch (Exception e) {
			log.error(ReportUtils.formatError("元素未出现. " + e.getMessage()));
		}
		return false;
	}
	/**
	 * @Description：判断传入的元素定位信息是否是有效的
	 * @author：James
	 * @return: boolean
	 */
	protected boolean isLocatorValid(String locator){
		String pattern = "^[a-zA-Z]{2,5}={1}.{1,}$";// 如果匹配这个正则，就是定位信息(id,css,xpath...)
		if(StringUtils.isNotBlank(locator) && Pattern.matches(pattern, locator)){
			return true;
		}
		log.error("传入的元素定位信息有误，请检查... [ <font color='red'>" + locator + " </font>]");
		
		return false;
	}
	/**
	 *判断页面是否已成功跳转(比较麻烦，需多方考虑处理遇到的如接口响应慢或网络延迟的情况)：
	 * 	处理流程：
	 * 		1、先判断RUL是否改变，若改变则页面已经跳转，但不代表跳转完全成功，因为新页面很可能未加载或未加载完全，
	 *        此时需等待页面和Ajax加载完成；
	 * 		2、若URL未改变(如果后台使用转发而非重定向，则页面跳转后URL是不会改变的)，此时走下一流程，判断Title是否已改变，
	 *        如果Tile改变则页面跳转，再等待页面和Ajax加载完成即可;
	 *      3、如果以上二步均未实现，则需要判断指定的新页面上的元素是否出现来验证页面是否跳转成功；
	 * @author James Guo
	 * @param: locator, 新页面上目标元素定位信息
	 * @return: boolean 
	 */
	protected boolean isPageNavigated(String locator) {

		int timeout = TestContext.getDomTimeout();
		log.info(ReportUtils.formatAction("页面跳转中，跳转最大超时时间：" + timeout + " ms, 请稍候..."));

		String currUrl = getWebDriver().getCurrentUrl();
		log.debug("当前页面的URL： " + currUrl);

		String currTitle = getWebDriver().getTitle();
		log.debug("当前页面的Tile：" + currTitle);

		boolean isNavigated = false;

		PerformanceTimer timer = new PerformanceTimer(timeout);

		waitFor(500);

		while (!isNavigated && !timer.hasExpired()) {
			try {
				// 先根据当前的Url判断页面是否跳转，获取当前最新的的Url：
				String newUrl = getWebDriver().getCurrentUrl();
				if (!currUrl.equals(newUrl)) {// 当URL不同，且指定的元素出现时，表明页面已跳转
					log.info("页面 url 已改变，页面已跳转，跳转总耗时： " + timer.getElapsedTime() + " ms. 等待新页面加载完成...");
					completeWait(200);

					isNavigated = true;
					break;
				}
				// 如果Url没变，则再根据当前的页面Title判断：
				String currNewTitle = getWebDriver().getTitle();
				if (!currTitle.equals(currNewTitle)) {
					log.info("页面 Title 已改变，页面已跳转，跳转总耗时： " + timer.getElapsedTime() + " ms. 等待新页面加载完成...");
					completeWait(200);

					isNavigated = true;
					break;
				}
				// 如果以上都不成功，如果locator不为空，再根据locator指定新页面的元素来确定页面是否跳转：
				if (isLocatorValid(locator) && isElementApeare(locator)) {// 通过查找目标页面上的标识元素；
					log.info("期望的新页面元素已出现，页面已跳转，跳转总耗时： " + timer.getElapsedTime() + " ms. 等待新页面加载完成...");
					completeWait(200);
					
					isNavigated = true;
					break;
				}
				throw new Exception();// 若以上都不成功，抛出异常，catch住后，再根据是否超时判断是否需要重复执行；

			} catch (Exception e) {
				PerformanceTimer.wait(500);
				if (timer.hasExpired()) {// 等待元素超时失败；
					logger.error("页面跳转超时失败. 跳转等待时间： " + timer.getElapsedTime() + " ms");
				}
			}finally{
				increaceStepNum();
			}
		}
		//根据 isNavigated 的值判断是否跳转成功：
		if(isNavigated){
			return true;
		}
		return false;
	}
	/**
	 * @Description：对异常信息进行格式化
	 * @author：James Guo
	 * @return: String
	 */
	public String getErrorMsg(Throwable t){
		String errorMsg = "";
		
		String throwableMsg = t.getMessage();
		Throwable cause = t.getCause();
		
		if(null != cause){
			errorMsg += cause.getMessage() + "<br/>";
		}
		if(StringUtils.isNotBlank(throwableMsg)){
			errorMsg += throwableMsg;
		}
		if(StringUtils.isNotBlank(errorMsg)){
			log.error(ReportUtils.formatError("错误信息：" + errorMsg));
		}
		return errorMsg;
	}
	/**
	 * @Description：封装一个完全等待的方法:包括指定等待时间、页面加载完成、Ajax加载完成
	 * @author：James Guo
	 * @param waitTime
	 * @return: void
	 */
	public void completeWait(int waitTime){
		waitFor(waitTime);
		waitForAjaxComplete();
		waitForPageLoad();
	}
	/**
	 * 再按“ESC”或“Tab”键使其失去焦点,一般用于验证输入框的错误提示：
	 * @param keyCode
	 * @param locator
	 * @throws Exception
	 */
	protected void loseFocus(int keyCode, WebElement element) throws Exception {

		//Robot robot = new Robot();		
		if (null != element) {
			log.debug(ReportUtils.formatAction("执行使 [" + this.getElementInfo(element) + " ]失去焦点的动作...."));
			/*robot.keyPress(keyCode);// 按Tab/Esc键使目标失去焦点；
			log.debug("模拟按键：" + KeyEvent.getKeyText(keyCode));
			robot.keyRelease(keyCode);// 一定要注意，按下Tab后，要再立即弹开，否则等于 一直按着Tab键，导致后续操作错误
			log.debug("模拟弹起键：" + KeyEvent.getKeyText(keyCode));*/
			
			element.sendKeys(Keys.TAB);
			log.info("[ " + element.toString() + " 成功失去焦点...");
		} else {
			log.error(ReportUtils.formatError("元素未找到，执行元素失去焦点动作失败..."));
		}
	}
	/**
	 * 查找并鼠标移动到目标元素
	 * @param locator
	 * @throws Exception
	 */
	protected void move2Element(String locator) throws Exception{
		WebElement element = getElement(locator);
		report(ReportUtils.formatAction(String.format("移动光标到  %s", this.getElementInfo(element))));
		(new Actions(getWebDriver())).moveToElement(element).build().perform();
		
		waitFor(200);
	}
	/**
	 * 获取传入的元素的Text/value属性/title属性：
	 * @author James Guo
	 * @param element
	 * @return String
	 */
	protected String getElementInfo(WebElement element){
		if(StringUtils.isNotBlank(element.getText())){
			return element.getText();
		}
		if(StringUtils.isNotBlank(element.getAttribute("value"))){
			return element.getAttribute("value");
		}
		if(StringUtils.isNotBlank(element.getAttribute("title"))){
			return element.getAttribute("title");
		}
		return element.toString();
	}
	/**
	 * 断言实际结果与期望结果的包含或不包含关系的公共方法：
	 * @author James Guo
	 * @param actual
	 * @param expected
	 * @param flag 表示要进行断言包含还是不包含：true-断言包含，false-断言不包含
	 * @param descriptions
	 * @return
	 */
	protected void isContainsOrNotContains(String actual, String expected, boolean flag, String descriptions){
		String actualResult = (String)tempValueMap.get(actual);// 先取出上步存入的实际值；	
		String expectedResult = (String)tempValueMap.get(expected);
		
		if(null == expectedResult ^ null == actualResult){//二者不同时为null
			if(null == expectedResult){//如果期望未在map中，而实际值在map中，拿Map中取出的期望与传入的原实际比较：
				if(flag){//要断言“实际结果”与“期望结果”是否是包含关系：
					verifyContains(actualResult, expected, descriptions);
				}else{//要断言“实际结果”与“期望结果”是否是非包含关系：
					verifyNotContains(actualResult, expected, descriptions);
				}
				
			}else if(null == actualResult){
				if(flag){
					verifyContains(actual, expectedResult, descriptions);
				}else{
					verifyNotContains(actual, expectedResult, descriptions);
				}
			}	
		}else{//如果二者都为null,直接比较传入的值：
			if(flag){
				verifyContains(actual, expected, descriptions);
			}else{
				verifyNotContains(actual, expected, descriptions);
			}
		}
		waitFor(100);
	}
	/**
	 * 断言实际结果与期望结果的包含或不包含关系的公共方法：
	 * @author James Guo
	 * @param actual
	 * @param expected
	 * @param flag 表示要进行断言包含还是不包含：true-断言包含，false-断言不包含
	 * @param descriptions
	 * @return
	 */
	protected void isEqualsOrNotEquals(String actual, String expected, boolean flag, String descriptions){
		String actualResult = (String)tempValueMap.get(actual);// 先取出上步存入的实际值；	
		String expectedResult = (String)tempValueMap.get(expected);
		
		if(null == expectedResult ^ null == actualResult){//二者不同时为null
			if(null == expectedResult){//如果期望未在map中，而实际值在map中，拿Map中取出的期望与传入的原实际比较：
				if(flag){//要断言“实际结果”与“期望结果”是否是相同关系：
					verifyEquals(actualResult, expected, descriptions);
				}else{//要断言“实际结果”与“期望结果”是否是非相同关系：
					Assert.assertNotEquals(actualResult, expected, descriptions);
				}
				
			}else if(null == actualResult){
				if(flag){
					verifyEquals(actual, expectedResult, descriptions);
				}else{
					Assert.assertNotEquals(actual, expectedResult, descriptions);
				}
			}
		}else{//如果二者都为null,直接比较传入的值：
			if(flag){
				verifyEquals(actual, expected, descriptions);
			}else{
				Assert.assertNotEquals(actual, expected, descriptions);
			}
		}
		waitFor(100);
	}
	/** -------------定义一般方法结束---------------*/
	
	/**
	 * 以下定义关键字部分：
	 * 
	 * @author James Guo
	 * @param locator: 定位串、测试数据、期望结果
	 * @param testData： 临时变量、预期结果
	 */
	// -----------定义关键字开始-----------------//
	
	/**
	 * 使指定的元素失去焦点:模拟按Tab键：
	 * 用法：传入使失去焦点的目标元素的定位信息 - locator
	 * @author James Guo
	 * @param stepDescription: 步骤信息
	 * @param locator：使失去焦点的目标元素定位信息
	 * @param testData：Null
	 * @throws NonStopRunningException 
	 */
	protected void loseFocusWithTabKey(String stepDescription, String locator, String testData) throws NonStopRunningException {
		stepInfo(stepDescription);
		WebElement targetEle = null;
		try {
			if(isLocatorValid(locator)){
				targetEle = getElement(locator);
			}
			loseFocus(KeyEvent.VK_TAB, targetEle);// 按Tab键使目标失去焦点；	
			waitFor(200);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(ReportUtils.formatError("使元素失去焦点时发生错误： " + this.getElementInfo(targetEle)));
			this.getErrorMsg(e);
			throw new NonStopRunningException(e);
		} finally {
			increaceStepNum();
		}
	}
	/**
	 * 使指定的元素失去焦点:模拟按Esc键：
	 * @author James Guo
	 * @param stepDescription: 步骤信息
	 * @param locator：使失去焦点的目标元素定位信息
	 * @param testData：Null
	 * @throws NonStopRunningException 
	 */
	protected void loseFocusWithEscKey(String stepDescription, String locator, String testData) throws NonStopRunningException {
		stepInfo(stepDescription);
		WebElement targetEle = null;
		try {
			if(isLocatorValid(locator)){
				targetEle = getElement(locator);
			}
			loseFocus(KeyEvent.VK_ESCAPE, targetEle);// 按Tab键使目标失去焦点；
			waitFor(200);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(ReportUtils.formatError("使元素失去焦点时发生错误： " + this.getElementInfo(targetEle)));
			this.getErrorMsg(e);
			throw new NonStopRunningException(e);
		} finally {
			increaceStepNum();
		}
	}
	/**-----------------元素的选中与不选中------------------*/
	/**
	 * @Description： 期望的元素默认被选中，默认选中时通过，否则不通过
	 * @author：James
	 * @return: 
	 * @throws：
	 */
	protected void isChecked(String stepDescription, String locator, String testData) {
		stepInfo(stepDescription);

		try {
			WebElement e = getElement(locator);
			if (null != e) {
				if (e.isSelected()) {
					log.info("期望被选中的元素：[ " + this.getElementInfo(e) + " ] 当前被选中.");
					Validate.validateTrue(e.isSelected(), "[ " + this.getElementInfo(e) + " ] 当前被选中.");
				} else {
					log.error("期望被选中的元素：[ " + this.getElementInfo(e) + " ] 当前未被选中.");
				}
			}
			waitFor(300);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("期望的元素未被选中：" + locator);
			this.getErrorMsg(e);
		}finally{
			increaceStepNum();
		}
	}
	/**
	 * 生成随机的字符串：前缀+随机数：<br/>
	 * 用法：randomStrPrefix(Excel中的locator列)：自定义一个生成随机字符串的“前缀”，如ADT-ATUO-TEST
	 * 		  tempVarible： 自定义一个临时变量用来接收生成的随机字符串
	 * @author James Guo
	 * @param randomStrPrefix： 传入要生成的随机字符串的前缀，如“ADT-ATUO-TEST” ；
	 * @param tempVarible： 定义一个接收生成的随机字符串的变量，在以后的步骤中使用此随机字符串时，填入此变量即可 ；
	 */
	// testData为生成的名字的变量名,如：传递的testData为：appName, locator是名字的前缀；
	protected void createRandomStr(String stepDescription, String randomStrPrefix, String tempVarible) {		
		stepInfo(stepDescription);
		String nameValue = null;
		try {
			if(StringUtils.isBlank(randomStrPrefix)){//locator列为null -> 生成纯随机数；
				nameValue = TestCaseBase.gen8UniqueID();
			}else{//locator列不为null -> 生成如，ADT-AUTO-TEST+纯随机数；
				nameValue = randomStrPrefix + TestCaseBase.gen8UniqueID();
			}			
			log.info("生成随机字符串：" + wrapSingleQuotes(nameValue));
		} catch (Exception e) {
			this.getErrorMsg(e);
		}
		tempValueMap.put(tempVarible, nameValue);// 存放的时候，Key为Excel中的test_data列的值；
		increaceStepNum();
	}

	/**
	 * @Description： 根据传入的长度值，生成一组固定长度的等于传入的长度的 数字类型的 字符串
	 * 		用法：length： 传入要生成的随机字符串的长度值
	 * 			   tempVarible: 自定义一个临时变量用来接收生成的随机字符串
	 * @author：James
	 * @param:length 要生成的随机字符串的长度
	 * @param:tempVarible 临时存储的变量
	 * @return: void 
	 */
	public void createFixedLengthStr(String stepDescription, String length, String tempVarible){
		stepInfo(stepDescription);
		
		String randStr = "";
		try{
			int len = Integer.valueOf(length);
			log.info("要生成的数字字符串的长度：[ " + length + " ]位");
			Random random = new Random();
			
			for(int i=0; i<len; i++){
				int ran = random.nextInt(9);
				randStr += ran;
			}
			log.info("生成长度为 " + len + " 位的数字字符串：[ " + randStr + " ]");
			tempValueMap.put(tempVarible, randStr);
		}catch(Exception e){
			log.error(ReportUtils.formatError("生成固定长度的随机字符串失败，请确保所输入的长度值为>0的数字."));
			this.getErrorMsg(e);
		}finally{
			increaceStepNum();
		}
	}
	/**
	 * 检查是否有弹窗，如无，不处理，如有，确认弹出窗口 
	 * 对于Windows系统的弹出窗口，确认操作：OK 或  确认
	 * @author James Guo
	 * @param stepDescription: 步骤信息
	 * @param locator：null
	 * @param testData: null
	 * @throws Exception
	 */
	public void acceptAlert(String stepDescription, String locator, String testData) throws Exception{
		stepInfo(stepDescription);

		String alertText = null;
		try {
			Alert alert = getWebDriver().switchTo().alert();
			if (null != alert) {
				alertText = alert.getText();
				alert.accept();
				report(ReportUtils.formatAction("Windows 弹出窗口[ " + alertText + " ]已被确认..."));
			}
			waitFor(200);
		} catch (NoAlertPresentException e) {// 如果捕获的是这个异常，则说明当前并没有windows弹出窗口，无需使用例失败；
			report(ReportUtils.formatData("<font color='red'>当前无弹出窗口</font>"));
		} catch (Exception ex) {
			report(ReportUtils.formatError("弹出窗口  " + alertText + " 确认操作失败..."));
			takeScreenshot();
			this.getErrorMsg(ex);
			throw new StopRunningException();
		} finally {
			getWebDriver().switchTo().defaultContent();
			increaceStepNum();
		}
	}

	/**
	 * 检查是否有弹窗，如无，不处理，如有，取消弹出窗口
	 * 对于Windows 系统的弹出窗口进行取消操作：Cancel 或  取消
	 * @author James Guo
	 * @param stepDescription: 步骤信息
	 * @param locator：null
	 * @param testData: null
	 * @throws Exception
	 */
	public void cancelAlert(String stepDescription, String locator, String testData) throws Exception {
		stepInfo(stepDescription);

		String alertText = null;
		try {
			Alert alert = getWebDriver().switchTo().alert();
			if (null != alert) {
				alertText = alert.getText();
				alert.dismiss();
				report(ReportUtils.formatAction("Windows 弹出窗口[ " + alertText + " ]已被取消..."));
			}

			waitFor(200);
		} catch (NoAlertPresentException e) {// 如果捕获的是这个异常，则说明当前并没有windows弹出窗口，无需使用例失败；
			report(ReportUtils.formatData("<font color='red'>当前无弹出窗口</font>"));
		} catch (Exception ex) {
			report(ReportUtils.formatError("弹出窗口  " + alertText + " 取消操作失败..."));
			takeScreenshot();
			throw new StopRunningException();
		} finally {
			getWebDriver().switchTo().defaultContent();
			increaceStepNum();
		}
	}
	/**
	 * 获取由“createName（）”方法生成的随机字符串, 不作为关键字，只在代码内部调用：
	 * @author James Guo
	 * @param locator: 不传值 ；
	 * @param testData：前边步骤定义的变量名 ；
	 * @return Object
	 */
	protected Object getStringValue(String stepDescription, String locator, String testData) {// Excel表格中该列的值就是要获取的名字
		Object nameValue = null;
		try {
			log.info(stepDescription);
			nameValue = tempValueMap.get(testData);
		} catch (Exception e) {
			log.info(ReportUtils.formatError("获取名字失败， 或要获取的名字不存在，请检查要获取的名字  " + wrapDoubleQuotes(nameValue.toString())
					+ " 是否已正确存储... -> " + e.getMessage()));
		}finally{
			increaceStepNum();
		}
		return nameValue;
	}

	/**
	 *  打开浏览器：
	 * @author James Guo
	 * @param locator
	 * @param testData
	 * @throws StopRunningException
	 */
	protected void openBrowser(String stepDescription, String locator, String testData) throws StopRunningException {
		stepInfo(stepDescription);

		try {
			WebDriverUtils.launchBrowser();
		} catch (Exception e) {
			e.printStackTrace();
			this.getErrorMsg(e);
			takeScreenshot();
			throw new StopRunningException(StoppingRunning);
		}finally{
			increaceStepNum();// 步骤执行成功，增加一步；
		}
	}

	/**
	 * 导航到页面：
	 * @author James Guo
	 * @param locator
	 * @param testData
	 * @throws StopRunningException
	 */
	protected void navigate(String stepDescription, String locator, String testData) throws StopRunningException {
		stepInfo(stepDescription);

		try {
			driver.get(locator);
			log.info("成功导航到页面：" + locator);

		} catch (Throwable t) {
			this.getErrorMsg(t);
			takeScreenshot();
			throw new StopRunningException();
		}finally{
			increaceStepNum();
		}
	}

	 /** 查找元素及查找后相关的操作：--------开始-------------
	 * @throws StopRunningException */
	 
	/**
	 * 查找单个元素,传入一个查找元素的变量放入“testData"列中，查找到后，存入tempValueMap中；
	 * @author James Guo
	 * @param locator: 传入要查找的元素的定位信息，如“xpath=xxxxx” ；
	 * @param testData： 自定义一个变量，用来接收查找到的元素对象 ；
	 * @throws StopRunningException
	 */
	protected void find(String stepDescription, String locator, String testData) throws StopRunningException {
		try{
			stepInfo(stepDescription);
			WebElement e = getElement(locator);
			
			tempValueMap.put(testData, e);//将查找的元素以“testData”中的变量名为Key，Element为value存入；
			
			if(null != e){
				String textValue = e.getText();
				if(StringUtils.isNotBlank(textValue)){
					log.info("查找到元素：" + wrapDoubleQuotes(textValue));
				}else{
					log.info("查找到元素：" + wrapDoubleQuotes(e.toString()));
				}
			}else{
				log.error(ReportUtils.formatError("查找元素：" + locator + "失败... " ));
				takeScreenshot();
				throw new StopRunningException(StoppingRunning);
			}
		}catch(Throwable t){
			this.getErrorMsg(t);
		}finally{
			increaceStepNum();
		}
	}
	
	/**
	 * 根据定位信息，查找多个元素，得到代表多个元素的List集合；
	 * @author James Guo
	 * @param stepDescription
	 * @param locator：要查找的元素的定位信息
	 * @param testData：定义一个变量，用于查找完成后，放入到Map中，以此变量为Key，List为Value；
	 */
	protected void findElements(String stepDescription, String locator, String testData) throws Exception{
		stepInfo(stepDescription);
		
		List<WebElement> list = null;
		
		try {
			list = getElements(locator);
			report("查询出元素列表. 共[ " + list.size() + " ] 条");
			//找到元素列表，存储到map中：
			tempValueMap.put(testData, list);
			
		} catch (Exception e) {
			e.printStackTrace();
			this.getErrorMsg(e);
			takeScreenshot();
			throw new StopRunningException(e);
		}finally{
			increaceStepNum();
		}
	}
	
	/**
	 * 获取List的size()，如，上一步查询出来的是一个元素的集合，要做断言时，判断有多少个元素，此方法可取得List的size(); 
	 * @author James Guo
	 * @param stepDescription: Excel中的步骤描述
	 * @param locator： 上一步查询的元素集合存入集合后定义的一个临时变量；
	 * @param testData： 取得List.size()后，将该值存入到一个变量中，此处是定义的该变量；
	 */
	@SuppressWarnings("unchecked")
	protected void getListSize(String stepDescription, String locator, String testData){
		stepInfo(stepDescription);
		//去tempValueMap中取值：
		List<WebElement> list = (List<WebElement>) tempValueMap.get(locator);
		int count = list.size();
		report("获取的元素集合的size = " + wrapSingleQuotes(count + ""));
		
		//将这个size的值存入Map中，以供以后的步骤使用：
		tempValueMap.put(testData, count);
		increaceStepNum();
	}
	
	/**
	 * 判断所得到的List集合的size 是否和期望的相等
	 * @author James Guo
	 * @param stepDescription： 步骤描述
	 * @param locator： 传入上边步骤存入的代表该集合的变量,也可传入一个元素的Locator信息，查找到元素封装到集合再判断size
	 * @param testData：期望结果
	 */
	@SuppressWarnings("unchecked")
	protected void assertListSizeEquals(String stepDescription, String locator, String testData) throws Exception{
		stepInfo(stepDescription);
		
		List<WebElement> list = (List<WebElement>)tempValueMap.get(locator);//先去map中取。
		log.debug("尝试依据 [ " + locator + " ] 信息查找已有数据." + ((null != list && list.size()>0) ? "已查找到." : "未查找到"));
		if(null == list){//在map中没找到，可以是需要根据Locator信息现找元素：
			try {
				list = getElements(locator);
				report("共查找到：" + list.size() + " 条元素记录.");
				
				verifyEquals(list.size(), Integer.parseInt(testData), "实际结果：[ " + list.size() + " ], 期望结果：[ " + testData + " ] -> ");
				
			} catch (Exception e) {
				e.printStackTrace();
				takeScreenshot();
				throw new StopRunningException(e);
			}
		}else{//之前存储过，在tempValueMap中查找到了,直接做断言：
			try {
				verifyEquals(list.size(), Integer.parseInt(testData), "实际结果：[ " + list.size() + " ], 期望结果：[ " + testData + " ] -> ");
			} catch (Exception e) {
				this.getErrorMsg(e);
				e.printStackTrace();
				takeScreenshot();
				throw new StopRunningException(e);
			}
		}
		increaceStepNum();
	}
	
	/**
	 * 向"locator"所代表的元素中，输入“testData"的值。 （二个值都有可能是上一步查找到的，也可能不是，所以要判断）：
	 * @author James Guo
	 * @param locator: 传入前边步骤查找元素时定义的变量，也可传入一个元素的定位信息 ；
	 * @param testData
	 * @throws StopRunningException
	 */
	protected void input(String stepDescription, String locator, String testData) throws StopRunningException{//locator:向目标元素要输入的值； testData: 上一步查找到的目标元素对应的变量名
		stepInfo(stepDescription);
		
		WebElement e = null;
		try {
			e = (WebElement) tempValueMap.get(locator);//先用locator的值去Map中取，看是否有值；
			String value = (String)tempValueMap.get(testData);//向Map中取要输入的值，看是否存在  
			
			if(null == e){//如果Map中没有，就把locator当作是定位信息，查找：
				e = getElement(locator);
			}

			if (StringUtils.isNotBlank(value)) {//临时变量有值：
				BaseFramework.clearAndInput(e, value);
				
			} else {// Map中没有查找到要输入的值，直接将testData列中的数据输入：
				BaseFramework.clearAndInput(e, testData);
			}
			waitForPageLoad();
		} catch (Exception e1) {
			this.getErrorMsg(e1);

			takeScreenshot();
			throw new StopRunningException(StoppingRunning);
		}finally{
			increaceStepNum();
		}
	}
	
	/**
	 * 对查找的元素执行点击操作：先去本地的Map中按变量名查找，如果没有的话就用locator中的值为定位串去获取元素；
	 * @author James Guo
	 * @param locator: 传入一个变量，也可传入元素的定位信息 ；
	 * @param testData： null, 不传参 ；
	 * @throws StopRunningException
	 */
	protected void click(String stepDescription, String locator, String testData) throws StopRunningException{
		stepInfo(stepDescription);
		WebElement e = null;
		try {
			e = (WebElement) tempValueMap.get(testData);
			
			if(null == e){
				e = getElement(locator);
			}
			e.click();
		} catch (Throwable t) {
			this.getErrorMsg(t);
			takeScreenshot();
			throw new StopRunningException(StoppingRunning);
		}finally{
			increaceStepNum();
		}
	}

	/**
	 * 先查找到文本框再输入内容 <==> find() + input(); <br/>
	 * NOTICE: 当输入值时，先去tempValeMap中去根据“testData”中传入的值为Key取值，如果有值就输入取到的Value；<br/>
	 * 	  <pre>如果取不到值，则说明“testData"不是变量，而是直接要输入的内容，提高灵活性；
	 * @author James Guo
	 * @param locator：元素的定位信息 ；
	 * @param testData： 要向元素输入的文本值 ；
	 * @throws StopRunningException 
	 */
	protected void findAndInput(String stepDescription, String locator, String testData) throws StopRunningException {
		WebElement e = null;
		try {
			stepInfo(stepDescription);
			e = getElement(locator);
			//判断要输入的是直接值，还是上步骤存入到Map中的值：
			String value = (String)tempValueMap.get(testData);		
			if(null != value){//value!=null, 则取出之前存入的值，进行下一步操作；
				testData = value;
			}
			BaseFramework.clearAndInput(e, testData);
				this.completeWait(200);

			} catch (Throwable t) {
			this.getErrorMsg(t);
			takeScreenshot();
			throw new StopRunningException(t);
		}finally{
			increaceStepNum();
		}
	}
	/**
	 * 先查找元素再执行点击事件 <==> find() + click() ， 直接以locator为定位信息去获取元素，而不去本地的map中取；
	 * @author James Guo
	 * @param locator：元素的定位信息 ；
	 * @param testData：null, 不传参 ；
	 * @throws StopRunningException
	 */
	protected void findAndClick(String stepDescription, String locator, String testData) throws StopRunningException {
		try {
			stepInfo(stepDescription);

			WebElement e = getElement(locator);
			e.click();
			log.info("成功点击 : '" + locator + "' ");

			this.completeWait(200);
			
		} catch (Exception t) {
			t.printStackTrace();
			this.getErrorMsg(t);
			takeScreenshot();
			throw new StopRunningException(t);
		} finally {
			increaceStepNum();
		}
	}
	/**
	 * 获取元素的属性：
	 * 	 1、   先以locator为Key去本地map中查询是否存在前边步骤通过 find()关键字查出的元素，若存在，再getAttribute()；
	 * 	 2、  若不存在1中的情况，再判断传入的locator是否为定位信息（xpath=xxx),若是，则先查找元素，再取元素的属性，然后放入Map中；
	 * @author James Guo
	 * @param locator: 前边步骤查找到的元素时定义的变量值，或放置定位信息 ；
	 * @param testData： 要获取的元素的某个属性，如， name, href等待属性的名字 或 “属性名=变量名” 的形式，即将取出的属性值存入一个变量中 ；
	 */
	protected void getAttribute(String stepDescription, String locator, String testData) {
		stepInfo(stepDescription);
		
		String attrName = "";
		String attrValue = null;
		//如果之前通过find()关键字取出元素后存放到Map中，则先是否之前存放过这个变量，此时locator是前边步骤通过find()查询元素后的变量名；
		WebElement ele = (WebElement) tempValueMap.get(locator);

		try {
			if (null == ele && isLocatorValid(locator)) {// 存放的是元素定位信息（xpath=xxx)，开始查找元素
				ele = getElement(locator);
			}

			if (testData.indexOf('=') != -1) {// 用的是存变量的形式：value=Var_推广活动URL
				attrName = testData.split("=")[0].trim();
				String attrParam = testData.split("=")[1].trim();
				attrValue = ele.getAttribute(attrName).trim();
				tempValueMap.put(attrParam, attrValue);

			} else {// 未写存入变量的形式,直接使用元素的属性名称：value , 2018-9-6改动，为兼容之前的写法，在此做判断
				attrName = testData;
				attrValue = ele.getAttribute(testData);
				tempValueMap.put(testData, attrValue);
			}
			if(StringUtils.isNotBlank(attrValue)){
				log.info("获取到的元素的属性值：[' " + attrName + " = " + attrValue + " ']");
				// tempValueMap.put(testData.split("=")[1].trim(), attrValue);
				waitFor(100);
			}
			
		} catch (Exception e) {
			log.error(ReportUtils.formatError("无法获取元素 的'" + attrName + "'属性值：" + testData));
			this.getErrorMsg(e);
			takeScreenshot();
		} finally {
			increaceStepNum();
		}
	}
	/**
	 * 获取页面的Title：
	 * @author James Guo
	 * @param locator: null, 不传参， 因为此操作是由Webdriver对象操作页面的，无需任何页面元素参与 ；
	 * @param testData： 定义一个变量（如，pageTitle)，用于接收得到的页面的Title ；
	 */
	protected void getTitle(String stepDescription, String locator, String testData) throws Throwable{
		stepInfo(stepDescription);
		try {
			String title = getWebDriver().getTitle();
			log.info("获取获取的页面Title：" + title );
			
			tempValueMap.put(testData, title);
			waitFor(100);
		} catch (Exception e) {
			log.error(ReportUtils.formatError("无法获取页面Title"));
			this.getErrorMsg(e);
			takeScreenshot();
			throw new StopRunningException(e);
		}finally{
			increaceStepNum();
		}
	}
	/**
	 * 获取元素的文本值（HTML的innerHTML)：
	 * @author James Guo
	 * @param locator: 元素的定位信息，或不传参。若传入定位信息，则先查找元素再获取文本值；若不传参，则取前边步骤存入的变量对应的元素 ；
	 * @param testData：定义一个变量（如，xxxTextValue），用于接收得到的元素的文本值 ；
	 */
	protected void getText(String stepDescription, String locator, String testData) throws Throwable{
		stepInfo(stepDescription);
		WebElement e = null;
		String text = "";
		try {
			if(isLocatorValid(locator)){//如果locator不为空，则先根据locator查询出Element，再取值；
				e = getElement(locator);
				if(null != e){
					text = e.getText();
					report("获取到元素的文本内容：" + wrapDoubleQuotes(text));
				}
			}else{
				e = (WebElement) tempValueMap.get(testData);//testData列放置上一步查找到的元素
				if(null != e){
					text = e.getText();
					report("获取到元素的文本内容：" + wrapDoubleQuotes(text));
				}
			}
			waitFor(100);
			tempValueMap.put(testData, text);//存入临时变量值；
		} catch (Exception e1) {
			log.error(ReportUtils.formatError("无法获取元素的文本值：'" + tempValueMap.get(testData) + "' "));
			this.getErrorMsg(e1);
			takeScreenshot();
			throw new StopRunningException(e1);
		}finally{
			increaceStepNum();
		}
	}
	
	/** 动作相关： ----------动作相关方法开始----------------*/	
	/**
	 * 测试时，如果要查找的元素在页面下方，被页面遮挡而不可见时，需要用此方法，将页面拖动以使元素可见 ；
	 * @author James Guo
	 * @param locator： 元素的定位信息 ；
	 * @param testData：null, 无需传值 ；
	 */
	protected void scrollIntoView(String stepDescription, String locator, String testData) throws Exception{
		stepInfo(stepDescription);
		WebElement elem =null;//先查找
		try {
			elem = getElement(locator);//先查找	
			super.scrollIntoView(locator);//拖动滚动条使元素可见；
			waitFor(300);
			
		} catch (Exception e) {
			if(null != elem){
				log.error(ReportUtils.formatError("拖动滚动条，使元素： [ " + this.getElementInfo(elem) + " ]可见"));
			}else{
				log.error(ReportUtils.formatError("未查找到元素：[ " + locator + " ]" ));
			}
			this.getErrorMsg(e);
			takeScreenshot();
			throw new StopRunningException(e);
		}finally{
			increaceStepNum();
		}
	}
	/**
	 * 模拟悬浮光标于元素上的动作 ；
	 * @author James Guo
	 * @param locator：元素的定位信息 ；
	 * @param testData
	 */
	protected void moveTo(String stepDescription, String locator, String testData) throws Throwable{
		stepInfo(stepDescription);
		try{
			this.move2Element(locator);
		}catch(Exception e){
			log.error(ReportUtils.formatError("移动光标到元素操作失败..."));
			this.getErrorMsg(e);
			takeScreenshot();
			throw new StopRunningException(e);
		}finally{
			increaceStepNum();
		}
	}
	/**
	 * 模拟在元素上右击动作 ；
	 * @author James Guo
	 * @param locator： 元素的定位信息 ；
	 * @param testData: null
	 */
	protected void rightClick(String stepDescription, String locator, String testData) throws Throwable{
		stepInfo(stepDescription);
		try {
			this.move2Element(locator);
			
			new Actions(getWebDriver()).contextClick().perform();
			waitFor(200);
		} catch (Exception e) {
			log.error(ReportUtils.formatError("无法完成鼠标右击操作..."));
			this.getErrorMsg(e);
			takeScreenshot();
			throw new StopRunningException(e);
		}finally{
			increaceStepNum();
		}
	}
	
	/** ----------动作相关方法结束----------------*/
	
	/** 断言相关：Assert - 硬断言：如果该测试步骤失败会对以后的测试步骤直接影响，则用此断言抛出运行时异常，中止测试的继续进行：----断言方法开始-------------*/
	 /**----硬断言开始---【NOTE】: TestNG提供的断言方法，在断言失败时抛出的是“Error” 而不是Exception，需要用Throwable类型捕获错误，否则对于Error，Exception无法捕获。
	 * @throws StopRunningException */
	/**
	 * 硬断言， 判断期望结果与实际结果是否相同 （文本类型）；
	 * @author James Guo
	 * @param actual： 实际结果，由上边步骤得到的值（如，执行getTitle()方法得到的实际的页面Title）；
	 * @param expected： 期望结果，我们手写传入期望的结果（如，传入我们已知的期望页面的Titile值）；
	 * @throws StopRunningException
	 */
	protected void assertEquals(String stepDescription, String actual, String expected) throws StopRunningException {//第一个参数是locator列中是传入的实际值， 第二个参数是testData列传入的预期值；
		stepInfo(stepDescription);
		
		try {
			this.isEqualsOrNotEquals(actual, expected, true, "断言 期望值 与 实际值 相同:");
			
		} catch (Throwable t) {
			log.error(ReportUtils.formatError(t.getMessage()));
			this.getErrorMsg(t);
			takeScreenshot();//截图
			throw new StopRunningException(StoppingRunning);
		}finally{
			increaceStepNum();
		}
	}
	
	/**
	 * 断言期望与实际不相等,参考 {@code assertEquals(String actual, String expected) throws StopRunningException}
	 * @author James Guo
	 * @param actual
	 * @param expected
	 * @throws StopRunningException
	 */
	protected void assertNotEquals(String stepDescription, String actual, String expected) throws StopRunningException{
		stepInfo(stepDescription);

		try {
			this.isEqualsOrNotEquals(actual, expected, false, "断言 期望值 与 实际值 不相同:");
			
		} catch (Throwable t) {
			log.error(ReportUtils.formatError(t.getMessage()));
			this.getErrorMsg(t);
			takeScreenshot();//截图
			throw new StopRunningException(StoppingRunning);
		}finally{
			increaceStepNum();
		}
	}
	/**
	 * 断言字符串的包含，期望值包含在实际值中：
	 * @author James Guo
	 * @param actual：实际得到的值,是由前边步骤执行后存入到Map中的值；
	 * @param expected：传入我们期望的值，可以是期望值字符串，也可以是变量，程序会先按变量取，若取不到变量对应的值，再按期望字符串去和实际值比较；
	 * @throws Throwable 
	 */
	protected void assertContains(String stepDescription, String actual, String expected) throws StopRunningException{
		stepInfo(stepDescription);

		try {
			this.isContainsOrNotContains(actual, expected, true, "断言期望结果包含于实际结果中: ");
			
		} catch (Throwable e) {
			this.getErrorMsg(e);
			takeScreenshot();
			log.error("断言失败：" + e.getMessage());
			throw new StopRunningException(StoppingRunning);
		}finally{
			increaceStepNum();
		}
	}

	/**
	 * 断言实际值不为空：
	 * @author James Guo
	 * @param actualVarible
	 * @param testData
	 * @throws Exception 
	 */
	protected void assertNotNull(String stepDescription, String actualVarible, String testData) throws Exception{
		stepInfo(stepDescription);
		int index = actualVarible.indexOf('=');//判断是传的是已查找的元素还是一个定位信息；
		
		Object actualResult = null;
		if(-1 == index){//如果传入的值不是以“=”连接的，则说明传入的是是一个变量，直接去Map中取；
			log.debug("尝试从本地临时容器中查找 ' " + actualVarible + " '对应的值...");
			actualResult = tempValueMap.get(actualVarible);//取出存放在Map中的实际值；
		}else{//如果传入的是一个定位信息，则去查找元素,先判断是不是一个合法的定位信息（有时可能在Excel中忘记选定位类型而导致失败）；
			log.debug("传入了元素定位信息，将查找元素：' " + actualVarible + " '...");
			if(! isLocatorValid(actualVarible)){//不符合定位信息时，报错提示
				log.error(ReportUtils.formatError("传入的元素定位信息不合法：" + actualVarible));
				throw new StopRunningException("传入的元素定位信息不合法：" + actualVarible);
			}
			actualResult = getElement(actualVarible);
		}
		try {
			Verify.verifyNotNull(actualResult, " 断言实际值不为null: ");
			waitFor(100);
		} catch (Throwable e) {
			log.error(ReportUtils.formatError("断言失败，实际值 '" + actualResult + "' Is  Null..."));
			this.getErrorMsg(e);
			takeScreenshot();
			throw new StopRunningException(StoppingRunning);
		}finally{
			increaceStepNum();
		}
	}
	/**
	 * 断言实际值为空：可以先查找元素，存入变量Map中，然后再断言是否为空；也可以直接传入一个定位信息，查找元素，再直接判断是否为空；
	 * @author James Guo
	 * @param actualVarible
	 * @param expected
	 * @throws Exception 
	 */
	protected void assertNull(String stepDescription, String actualVarible, String expected) throws Exception{
		stepInfo(stepDescription);
		
		int index = actualVarible.indexOf("=");//判断是传的是已查找的元素还是一个定位信息；
		
		Object actualResult = null;
		if(-1 == index){//如果传入的值不是以“=”连接的，则说明传入的是是一个变量，直接去Map中取；
			actualResult = tempValueMap.get(actualVarible);//取出存放在Map中的实际值；
		}else{//如果传入的是一个定位信息，则去查找元素；
			actualResult = getElement(actualVarible);
		}
		try {
			Verify.verifyNull(actualResult, " 断言实际值为null: ");
			waitFor(100);
		} catch (Throwable e) {
			log.error(ReportUtils.formatError("断言失败，实际值 [ " + actualResult + " ] Is Not null..."));
			this.getErrorMsg(e);
			takeScreenshot();
			throw new StopRunningException(StoppingRunning);
		}finally{
			increaceStepNum();
		}
	}
	
	/**
	 * 断言页面的”Title”和期望的Title相同，包含二个功能：1、先判断二者是否相同，若不同，再判断二者是否包含关系；
	 * 		<pre>该方法的作用是：先判断当前获取的页面Title是否为Null，若是，则抛出异常；
	 * 		<pre>作断言时，先判断当前页面的Title是否和期望的Title完全一样（用verifyEquals断言）：
	 * 			 <pre>若相同，则断言成功；
	 * 			 <pre>若不相同，此步是要抛出异常的，用try..catch 接收，在上一步抛出异常后，再判断实际Title是否包含期望Title，若包含，成功。否则，再次抛出异常，结束断言；
	 * @author James Guo
	 * @param actaul
	 * @param expected
	 * @throws StopRunningException
	 */
	protected void assertTitle(String stepDescription, String actaul, String expected) throws StopRunningException{
		stepInfo(stepDescription);
		String title = "";
		try {
			waitForPageLoad();
			title = getWebDriver().getTitle();
			if(StringUtils.isBlank(title)){
				report(ReportUtils.formatError("无法获取当前页面的Title."));
				throw new StopRunningException(StoppingRunning);
			}
			
			report("获取到当前页面的Title为：" + wrapSingleQuotes(title));
			try {
				verifyEquals(title, expected, "判断页面的 'Title' 是否和期望Title值相同");
			} catch (Throwable e) {
				verifyContains(title, expected, "判断页面的 'Title' 是否和期望Title值相同或实际Title值包含期望Title值");
				this.getErrorMsg(e);
			}
			report(ReportUtils.formatData(">> 当前页面的Title: [ " + title + " ] 期望页面的Title： [ " + expected + " ]"));
			waitFor(100);
		} catch (Throwable e) {
			report(ReportUtils.formatError("当前页面的Title为：[ " + title + " ] 期望的页面Title为：[ " + expected + " ], 二者不相同也非包含."));
			this.getErrorMsg(e);
			takeScreenshot();
			throw new StopRunningException(StoppingRunning);
		}finally{
			increaceStepNum();
		}
	}
	
	/**----------------硬断言结束-------------*/
	/**
	 * validate: - 软断言，如果该测试步骤的失败无关后续的测试步骤，则用此断言，可以让测试继续进行
	 * 由于是软断言，异常抛出后，后边的代码还要执行，所以此时的增加步骤操作必须执行；
	 * @author James Guo
	 */
	/**
	 * validateIsDisplayed: 用来断言页面上的元素是否出现，如，输入框在不满足输入条件时会出现错误提示，此时判断提示是否出现等; 
	 * 	分二种情况对待：
	 * 	  1、该类的提示元素提前已存在于HTML中，只是配置了相关的属性(style="display:none")使其默认不显示在页面上，在一定条件下才显示(style="display:inline" / style="display:block")；
	 * 	  2、该类元素默认不在HTML中，只有当满足显示条件时才出现；
	 * 
	 * @author James Guo
	 * @param stepDescription：步骤描述
	 * @param locator：要判断是否出现的目标元素的定位信息，如xpath=xxxx
	 * @param testData：Null
	 * @throws NonStopRunningException 
	 */
	@SuppressWarnings("all")
	protected void validateIsDisplayed(String stepDescription, String locator, String testData) throws NonStopRunningException  {
		stepInfo(stepDescription);
		WebElement ele = null;
		try {
			 ele = getElement(locator);
			 if(null != ele){
				String attrValue =  ele.getAttribute("style");//获取得到元素的"style"属性；
				if(null != attrValue){
					attrValue = attrValue.replaceAll("\\s*", "");
					validateNotContains(attrValue, "display:none", "元素的style属性：" + attrValue);
					return;
				}
				report(ReportUtils.formatVerify("要查找的元素[ " + ele.toString() + " ] 出现在页面上"));
			 }else{
				 report(ReportUtils.formatVerify("要查找的元素[ " + ele.toString() + " ] 未出现"));
			 }
			 waitFor(100);
		} catch (Exception e) {
			
			e.printStackTrace();
			this.getErrorMsg(e);
			throw new NonStopRunningException(e);
		}finally{
			increaceStepNum();
		}
	}
	/**
	 * 判断是否为Null的软断言 ，如果为Null，则通过，不为null，该步骤失败，但测试还可以继续运行，影响的是最终的测试结果
	 * @author James Guo
	 * @param stepDescription
	 * @param actualVarible
	 * @param expected
	 * @throws Exception
	 */
	protected void validateNull(String stepDescription, String actualVarible, String expected) throws Exception{
		stepInfo(stepDescription);
		
		int index = actualVarible.indexOf("=");//判断是传的是已查找的元素还是一个定位信息；
		
		Object actualResult = null;
		if(-1 == index){//如果传入的值不是以“=”连接的，则说明传入的是是一个变量，直接去Map中取；
			actualResult = tempValueMap.get(actualVarible);//取出存放在Map中的实际值；
		}else{//如果传入的是一个定位信息，则去查找元素；
			actualResult = getElement(actualVarible);
		}
		try {
			Verify.verifyNull(actualResult, "软断言，断言实际结果为null: ");
			waitFor(100);
		} catch (Throwable e) {
			log.error(ReportUtils.formatError("断言失败，实际值 [ " + actualResult + " ] Is Not null..."));
			this.getErrorMsg(e);
			takeScreenshot();
			throw new NonStopRunningException(e);
		}finally{
			increaceStepNum();
		}
	}
	/**
	 * 断言实际结果不为null, 若实际结果不为null时，测试通过，为null时，该步骤失败，影响的只是最后测试的结果
	 * @author James Guo
	 * @param stepDescription：Excel中步骤的信息
	 * @param actualVarible：传入的实际参数
	 * @param testData:null
	 * @throws Exception: 实际抛出的是NonStopRunningException 
	 */
	protected void validateNotNull(String stepDescription, String actualVarible, String testData) throws Exception{
		stepInfo(stepDescription);
		int index = actualVarible.indexOf("=");//判断是传的是已查找的元素还是一个定位信息；
		
		Object actualResult = null;
		if(-1 == index){//如果传入的值不是以“=”连接的，则说明传入的是是一个变量，直接去Map中取；
			actualResult = tempValueMap.get(actualVarible);//取出存放在Map中的实际值；
		}else{//如果传入的是一个定位信息，则去查找元素；
			actualResult = getElement(actualVarible);
		}
		try {
			Verify.verifyNotNull(actualResult, "软断言，断言实际结果不为null: ");
			waitFor(100);
		} catch (Throwable e) {
			log.info(ReportUtils.formatError("断言失败，实际值 '" + actualResult + "' Is  Null..."));
			this.getErrorMsg(e);
			takeScreenshot();
			throw new NonStopRunningException(e);
		}finally{
			increaceStepNum();
		}
	}
	
	/**
	 * 硬断言， 判断期望结果与实际结果是否相同 （文本类型）；
	 * @author James Guo
	 * @param actual： 实际结果，由上边步骤得到的值（如，执行getTitle()方法得到的实际的页面Title）；
	 * @param expected： 期望结果，我们手写传入期望的结果（如，传入我们已知的期望页面的Titile值）；
	 * @throws StopRunningException
	 * @throws NonStopRunningException 
	 */
	protected void validateEquals(String stepDescription, String actual, String expected) throws NonStopRunningException {//第一个参数是locator列中是传入的实际值， 第二个参数是testData列传入的预期值；
		stepInfo(stepDescription);

		try {
			this.isEqualsOrNotEquals(actual, expected, true, "软断言，断言实际结果与期望结果相同.");
			
		} catch (Throwable t) {
			log.error(ReportUtils.formatError(t.getMessage()));
			this.getErrorMsg(t);
			takeScreenshot();//截图
			throw new NonStopRunningException(ContinueRunning);
		}finally{
			increaceStepNum();
		}
	}
	
	/**
	 * 软断言字符串的包含，期望值包含在实际值中：
	 * @author James Guo
	 * @param actual：实际得到的值,是由前边步骤执行后存入到Map中的值；
	 * @param expected：传入我们期望的值，可以是期望值字符串，也可以是变量，程序会先按变量取，若取不到变量对应的值，再按期望字符串去和实际值比较；
	 * @throws NonStopRunningException 
	 * @throws Throwable 
	 */
	protected void validateContains(String stepDescription, String actual, String expected) throws NonStopRunningException{
		stepInfo(stepDescription);
		
		try {
			this.isContainsOrNotContains(actual, expected, true, "软断言，断言实际结果包含期望结果：");
		} catch (Throwable e) {
			log.error(ReportUtils.formatError(e.getMessage()));
			this.getErrorMsg(e);
			takeScreenshot();
			
			throw new NonStopRunningException(e);
		}finally{
			increaceStepNum();
		}
	}
	protected void validateNotContains(String stepDescription, String actual, String expected) throws NonStopRunningException{
		stepInfo(stepDescription);
		
		try {
			this.isContainsOrNotContains(actual, expected, false, "软断言，断言实际结果不包含期望结果：");
		} catch (Throwable e) {
			log.error(ReportUtils.formatError(e.getMessage()));
			this.getErrorMsg(e);
			takeScreenshot();
			
			throw new NonStopRunningException(e);
		}finally{
			increaceStepNum();
		}
	}
	/**----------------断言方法结束---------------------------------*/
	
	/** -------- 获取Window、切换 Window 和 切换 Frame的方法定义 开始-------*/
	/**
	 * 获取当前打开的窗口：原理，当客户端调用此方法时，传递一个变量用于标识当前窗口的句柄，然后将此句柄以“该变量为Key，对应的句柄为Value，存入Map。
	 * 					当需要再切换回该窗口时，用此变量取回对应的句柄，切换即可；
	 * @author James Guo
	 * @param stepDescription
	 * @param locator： null
	 * @param currentWindownHandler: 传递的代表当前窗口句柄的变量；
	 * @throws Exception
	 */
	private String getCurrentWindow() throws StopRunningException{
		//stepInfo(stepDescription);
		
		String currentWindow = null;
		try{
			currentWindow = getWebDriver().getWindowHandle();
			report("当前所在的窗口句柄：********* " + currentWindow + " *********");
			return currentWindow;
			
		//	windowHandleMap.put(currentWindownHandler, currentWindow);//将当前的窗口句柄存入句柄专用Map
			
		}catch(Exception e){
			report(ReportUtils.formatError("不能正确获取当前的窗口... ' " + e.getMessage() + " '"));
			
			throw new StopRunningException(e);
		}
	}
	/**
	 * 切换到目标窗口：支持的场景：
	 * 				1、用例端没有传参（参数列未写参数）：
	 * 					判断Map中是否有“firstWindow”值：
	 * 						a>. 无，输出错误信息，页面保持不变，无切换窗口动作；
	 * 
	 * 						b>. 有, 再判断firstWindow是否和当前窗口相同:
	 * 							I>. 若相同，则不切换;
	 * 							II>. 不相同，切换到firstWindow；
	 * 						
	 * 				2、用例端传递了窗口参数：
	 * 					先去map中查这个参数有无对应的窗口句柄：
	 * 						a>. 有对应的句柄：再判断该句柄是否与当前窗口句柄相同:
	 * 							I>. 相同不切换.
	 * 							II>. 不相同，则切换到该窗口；
	 * 
	 * 						b>. Map中无对应的句柄：
	 * 							I>. 先获取所有的句柄
	 * 							II>.遍历这些句柄，找出不在当前map中存在的句柄就是最新打开的窗口句柄
	 * 							III>.切换到该窗口句柄，再将该句柄以“传递的参数为Key，以真实的窗口句柄为Value存入Map
	 * @author James Guo
	 * @param stepDescription： 步骤信息
	 * @param locator：null
	 * @param currentWindowHandle: 调用 "getCurrentWindow" 方法后存入的变量名称；
	 * @throws StopRunningException：如果切换失败，抛出自定义异常，中止当前测试用例的执行；
	 */
	protected void switch2Window(String stepDescription, String locator, String windowToBeSwitched) throws StopRunningException {
		stepInfo(stepDescription);

		final String firstWindow = "firstWindow";// 代表第一个打开的窗口

		try {
			this.getCurrentWindow();
			
			if (StringUtils.isBlank(windowToBeSwitched)) {// 未传参，且Map中没有“ firstWindow ” 对应的值：报错，将当前的Window存入Map，Key = firstWindow
				if (null == windowHandleMap.get(firstWindow)) {
					report(ReportUtils.formatError("请传入需要切换的窗口名称..."));
					windowHandleMap.put(firstWindow, this.getCurrentWindow());//将当前的窗口句柄保存起来；
					return;
				} else {// 未传参，但Map中存在 firstWindow ” 对应的值：日志输出，且切换到" firstWindow ":
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
			report(ReportUtils.formatError("切换到目标窗口：" + (StringUtils.isNotBlank(windowToBeSwitched) ? windowToBeSwitched : "") + " 失败."));
			this.getErrorMsg(e);
			takeScreenshot();
			throw new StopRunningException(e);
		}finally{
			increaceStepNum();
		}
	}
	/**
	 * 关闭driver所在的当前的窗口：
	 * @author James Guo
	 * @param stepDescription
	 * @param frameIndicator
	 * @param testData
	 */
	protected void closeCurrrentWindow(String stepDescription, String frameIndicator, String testData){
		stepInfo(stepDescription);
		
		try {
			report(ReportUtils.formatAction("将关闭当前窗口..."));
			getWebDriver().close();
			report("当前窗口关闭成功...");
		} catch (Exception e) {
			report(ReportUtils.formatError("关闭当前窗口失败..."));
			this.getErrorMsg(e);
			takeScreenshot();
		}finally{
			increaceStepNum();
		}
	}
	
	/**
	 * 根据传入的frame的ID或List切换Frame：支持 单层 / 多层 切换 / 回到DefaultContent（如果未传入切换到目标IFrame的值，则默认是回到DefaultContent）；
	 * @author James Guo
	 * @param stepDescription
	 * @param locator：传入的要切换到目标Frame的ID或ID的List, 如果是切换多层Frame，则传入的Frame值用","隔开。
	 * @param testData：null
	 */
	protected void switch2Frame(String stepDescription, String frameIndicator, String testData) throws Exception{
		stepInfo(stepDescription);
		
		int index = frameIndicator.indexOf(',');
		try {
			if(StringUtils.isBlank(frameIndicator)){// 未传入目标Frame，执行 DefaultContent
				
				report(ReportUtils.formatError("未传入要切换的目标Frame / iframe 标识，将执行' SwitchToDefaultContent ' 操作..."));
				getWebDriver().switchTo().defaultContent();
				report("成功回到顶层 Iframe ...");
				
			}else if(index == -1){//切换单层 Frame；
				
				report(ReportUtils.formatAction("将切换到 IFrame： ' " + frameIndicator + " '"));
				IFrameHelper helper = new IFrameHelper((RemoteWebDriver)getWebDriver(), frameIndicator);
				helper.switchFrame();
				report(ReportUtils.formatAction("成功切换到Frame / IFrame: " + frameIndicator));
				
			}else{//多层切换：
				report("将要切换的Frame / IFrame的列表信息：" + frameIndicator.replace(",", " -> "));
				
				List<String> framePathList = Arrays.asList(frameIndicator);
				IFrameHelper helper = new IFrameHelper((RemoteWebDriver)getWebDriver(), framePathList);
				report(ReportUtils.formatAction("进行逐层的Frame / IFrame的切换..."));
				helper.switchFrame();
				report("成功完成 IFrame / Frame 切换： ' " + frameIndicator.replace(",", " -> "));
			}
		} catch (Exception e) {
			report(ReportUtils.formatError("切换Frame失败 '" + frameIndicator + " '"));
			this.getErrorMsg(e);
			takeScreenshot();
			throw new StopRunningException(e);
		}finally {
			increaceStepNum();
		}
	}
	
	/** 以下是等待相关的关键字：---------等待关键字开始---------*/
	 
	/**
	 *  简单等待：
	 * @author James Guo
	 * @param time: 等待时间值（如，300），单位：ms 
	 * @param testData: null
	 * @throws NonStopRunningException
	 */
	protected void waitFor(String stepDescription, String time, String testData) throws NonStopRunningException {
		
		stepInfo(stepDescription);
		try {
			long waitTime = Long.parseLong(time);
			super.waitFor(waitTime);
			log.info("等待 '" + waitTime + "' ms");
		} catch (Exception e) {
			log.error(ReportUtils.formatError("等待时出错，请检查... 等待时间：[ '" + time + "' ]"));
			this.getErrorMsg(e);
			takeScreenshot();
			throw new NonStopRunningException(ContinueRunning, e);
		}finally{
			increaceStepNum(); //由于此异常抛出后，不影响后边的代码执行，所以此时的增加步骤必须执行，放finally块中。；
		}
	}

	/**
	 *  等待页面加载完成：该方法取值为配置文件“fromework.properties"中的值，不需要在外部传(locator, testData都为null)；
	 *  如需修改时间，就修改此配置文件对应的“waitDomTimeout=” 的值
	 * @author James Guo
	 * @param locator: null
	 * @param testData: null
	 * @throws NonStopRunningException
	 */
	protected void waitForPageLoaded(String stepDescription, String locator, String testData) throws NonStopRunningException{
		stepInfo(stepDescription);
		long start = System.currentTimeMillis();

		try {
			super.waitForPageLoad();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(ReportUtils.formatError("等待页面加载完成失败... '" ));
			this.getErrorMsg(e);
			takeScreenshot();
			throw new NonStopRunningException(ContinueRunning, e);
		}finally{
			increaceStepNum();
		}
		long end = System.currentTimeMillis();
		log.info("页面加载完毕，最大设置的等待时间：" + TestContext.getDomTimeout() + " - " + "实际等待时间：" + (end - start) + " ms");
	}

	/**
	 *  等待Ajax请求完成：此方法的取值来自于配置文件“framework.propertes) ；
	 *  如需修改时间，就修改此配置文件对应的“waitAjaxTimeout=” 的值
	 * @author James Guo
	 * @param locator: null
	 * @param testData: null 
	 * @throws NonStopRunningException
	 */
	protected void waitForAjaxComplete(String stepDescription, String locator, String testData) throws NonStopRunningException{
		stepInfo(stepDescription + ": 设置的最大等待时间： " + TestContext.getAjaxTimeout() + " ms");
		long start = System.currentTimeMillis();
		try {
			super.waitForAjaxComplete();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(ReportUtils.formatError("等待Ajax完成时失败... '"));
			this.getErrorMsg(e);
			takeScreenshot();
			throw new NonStopRunningException(ContinueRunning, e);
		} finally{
			increaceStepNum();
		}
		long end = System.currentTimeMillis();
		log.info("Ajax加载完毕，实际等待时间：" + (end - start) + " ms");
	}
	
	/**
	 * 判断页面是否已成功跳转(比较麻烦，需多方考虑处理遇到接口响应慢或网络延迟的情况)：
	 * 	处理流程：
	 * 		1、先判断RUL是否改变，若改变则页面已经跳转，但不代表跳转完全成功，因为新页面很可能未加载或未加载完全，
	 *        此时需等待页面和Ajax加载完成；
	 * 		2、若URL未改变(如果后台使用转发而非重定向，则页面跳转后URL是不会改变的)，此时走下一流程，判断Title是否已改变，
	 *        如果Tile改变则页面跳转，再等待页面和Ajax加载完成即可;
	 *      3、如果以上二步均未实现，则需要判断指定的新页面上的元素是否出现来验证页面是否跳转成功；
	 * @author James Guo
	 * @param stepDescription: 步骤信息
	 * @param locator：传入跳转后的目标页面上的标识元素			
	 * @param testData：null
	 * @throws Exception
	 */
	protected void waitForPageNavigated(String stepDescription, String locator, String testData) throws Exception {
		stepInfo(stepDescription);
		
		boolean isPageNavigated = this.isPageNavigated(locator);
		
		if(! isPageNavigated){
			throw new StopRunningException("页面跳转超时失败...");
		}
	}
	
	/**
	 * 等待元素出现：用在页面跳转等场景下
	 * @throws StopRunningException 
	 * @author：James
	 * @locator: 等待出现的元素的定位信息
	 * @testData: Null
	 */
	protected void waitForElementApeare(String stepDescription, String locator, String testData) throws StopRunningException{
		stepInfo(stepDescription);
		log.info("等待元素出现：" + locator);
		try {
			WebElement e = getElement(locator);
			verifyNotNull(e, "");
		} catch (Exception e) {
			log.error(ReportUtils.formatError("元素未出现. "));
			this.getErrorMsg(e);
			throw new StopRunningException(e);
		}finally{
			increaceStepNum();
		}
	}
	/**
	 * 等待页面指定元素消失，可用于判断页面是否已跳转成功（当指定的元素消失后，则说明页面已跳转，不完全可靠）
	 * @author James Guo
	 * @param stepDescription: 步骤信息
	 * @param locator：指定的元素的定位信息
	 * @param testData：null
	 */
	protected void waitForDisapeared(String stepDescription, String locator, String testData){
		stepInfo(stepDescription);
		
		WebElement target = null;
		try {
			target = getElement(locator);
			boolean displayed = target.isDisplayed();
			
		} catch (Exception e) {
			e.printStackTrace();
			this.getErrorMsg(e);
		}
	}
	/** --------------------等待关键字方法结束---------------------------*/
	/**
	 *  关闭浏览器：
	 * @author James Guo
	 * @param locator： null
	 * @param testData: null
	 * @throws NonStopRunningException
	 */
	protected void closeBrowser(String stepDescription, String locator, String testData) throws NonStopRunningException{
		stepInfo(stepDescription);
		try {
			log.info("将关闭浏览器与Webdriver");
			if (null != driver) {
				driver.quit();
			}

		} catch (Exception e) {
			log.info(ReportUtils.formatError("关闭浏览器失败："));
			this.getErrorMsg(e);
			takeScreenshot();
		} finally {
			driver = null;
		}
	}

	/**
	 * endTestCase关键字的作用： 重置测试条目； 如果失败，将当前的测试结果置为'false'; 清空 tempValueMap;
	 * 将测试结果置为'true', 为下条测试用例做准备；
	 * 
	 * NOTE: 此方法需要在程序遇到“StopRunningException，或在每个用例执行完成后，必须执行的，无需在Excel中填写，
	 * 		   故此方法需要写在ExecutionEngine的方法调用中；
	 * @author James Guo
	 * @param locator: null
	 * @param testData: null
	 */
	public void endTestCase(String stepDescription, String locator, String testData) {
		log.info(ReportUtils.formatAction("---------当前测试用例执行完成---------"));
		if(ExecutionEngine.result){
			//根据“result”中的结果值，在生成的报告中显示“Passed” 或 “Failed” 的结果；
			report("<font size='4' color='blue'><b>测试结果：</b></font><font size='4' color='green'><i>PASSED</i></font>");
		}else{
			report("<font size='4' color='blue'><b>测试结果：</b></font><font size='4' color='red'><i>FAILED</i></font>");
		}
		
		this.resetStepNum();// 重置测试步骤；
		getWebDriver().switchTo().defaultContent();//退出Iframe；
		
	}
	public void clearDataMap(String excelFileName){
		report(ReportUtils.formatData("[<font color='green'>Post Testing Actions</font>]：" + wrapDoubleQuotes(excelFileName) + "中所有测试用例执行完成...."));
		report(ReportUtils.formatAction("程序将清空测试过程中产生的临时数据...."));
		this.tempValueMap.clear();
		report(ReportUtils.formatAction("临时数据清除完成，共清除 [ " + tempValueMap.size() + " ] 条数据."));
	}

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}
}
