package test.base;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.google.common.base.Function;

import framework.base.LoggerManager;
import framework.base.dataparser.xml.DataSetBean;
import framework.base.dataparser.xml.XMLFinder;
import framework.base.dataparser.xml.XMLParser;
import framework.base.utils.BaseFrameworkUtil;
import framework.base.utils.PerformanceTimer;
import framework.base.utils.ReportUtils;
import framework.base.utils.TestUtils;
import framework.webdriver.TestContext;
import framework.webdriver.WebDriverUtils;

/**
 * 测试基类
 * @author James Guo
 *
 */
@SuppressWarnings("deprecation")
public abstract class TestCaseBase{
	
	protected Logger logger = LoggerManager.getLogger(TestCaseBase.class.getSimpleName());

	//protected BasicAppPage applicationMenu;
	
	/**
	 * 测试基类中开始测试前的操作，完成：
	 * 	1、检测远程被测服务器的连通性，若服务器无响应，则抛出RuntimeException，结束测试；
	 *  2、根据配置文件中的设置初始化Webdriver，并将其设置到对应的TestContext中；
	 *  
	 *  所有继承了该类的测试子类，都可以覆盖这个方法，完成特定的操作，如：
	 *   @Override
	 *   public void startOfTesting() throws Exception(){
	 *   	verifyServer();
	 *      createBrowser();//覆盖父类中自动登录的方法；
	 *   }
	 * @author James Guo
	 * @throws Exception
	 */
	/*@BeforeSuite(alwaysRun=true)
	private void checkConnectionStatus(){
		
	}*/
	
	@BeforeClass(alwaysRun=true)
	protected void startOfTesting() throws Exception {
		if (! TestContext.isInitialized()) {
			throw new IllegalStateException("TestContext has not been initialized, make sure to set Preferences/TestNG Template XML File to TestNG.xml");
		}
		//控制在测试开始前是否校验服务状态（判断TestContext中getIsVerifyServerStateFirst=ture && TestContext.getVerifyServerStatus=false)
		if(TestContext.getIsVerifyServerStateFirst() && ! TestContext.getVerifyServerStatus()){
			verifyAppServer();//首选检测远程Server是否可用；
			//校验之后，设置已校验状态为true: 
			TestContext.setVerifyServerStatus(true);
		}
		
		createBrowser();
	}

	//首选验证测试服务的连通性：
	@SuppressWarnings("all")
	private void verifyAppServer() throws Exception {
		String serverAddress = TestContext.getServerUrl();
		if("http:///".equals(serverAddress)){
			logger.error("no application server address configured, exit testing abnormally, pls check the .properties file.");
			System.exit(-1);
		}
		logger.info(ReportUtils.formatAction("Testing appserver availability: " + serverAddress));
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = null;
        try {
			response = httpClient.execute(new HttpGet(serverAddress));

        } catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			/*if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        	
			}*/
			logger.error("连接服务器失败，请检查网络/VPN连接是否正常或RemoteWebDriver驱动已启动...");
        	throw new RuntimeException(String.format("Could not load %s : %d - %s", serverAddress, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
		}
        
        logger.info("accessed server successfully: " + serverAddress);
	}

	/**
	 * 只创建Driver不自动登录，子类覆盖时可采用这个方法，单独测试登录的功能
	 * @author James Guo
	 * @throws Exception
	 */
	protected void createBrowser() throws Exception {
		WebDriverUtils.launchBrowser();
	}
	/**
	 * Execute javascript in the browser window
	 */
	protected Object executeJavaScript(String script) {
		return WebDriverUtils.executeJavaScript(script);
	}
	/**
	 * 测试结束，退出、关闭浏览器, 测试子类可以覆盖此方法，以完成相应的操作
	 */
	@AfterClass(alwaysRun=true)
	public void endOfTesting() {
		logger.info("--CLASS WILL CLEANUP--");
		if (null != TestContext.getWebDriver()) {
			// Set this to false for debugging
			if (TestContext.getPropertyBoolean("closeBowserOnEnd")) {//根据需要，在配置文件中配置是否最后关闭浏览器
				try {
					logout(TestContext.getWebDriver());
				} catch (Exception ex) {
						logger.error(ex,ex);
					}
				}
		}
	}

	
	protected void captureScreen(String description) {
		TestContext.get().captureScreen(description);
	}

	protected void logout(WebDriver driver) {
		logger.info("Logout");
		// String serverAddress = TestContext.getServerUrl();
		//driver.switchTo().defaultContent();
		try {
			// driver.close();
			driver.quit();

			waitFor(200);
			logger.info("Logout complete");
		} catch (Exception ex) {
			logger.warn(ex, ex);
		} finally {
			if (null != driver)
				driver = null;
		}
	}
	
	//Format测试步骤：
	protected void stepInfo(String stepDescription) {
		logger.info(ReportUtils.formatStepInfo(stepDescription));
	}

	/**
	 * Execute a test method the specified number of times and log the elapsed time for each execution as well as the average time.
	 * 
	 * @param methodName
	 * @param testClassInstance
	 * @param invocationCount
	 */
	public void time(String methodName, Object testClassInstance, int invocationCount) throws Exception {
		Method method = getClass().getMethod(methodName, new Class<?>[] {});
		long times[] = new long[invocationCount];
		long total = 0;
		for (int i=0; i<invocationCount; i++) {
			PerformanceTimer pt = new PerformanceTimer();
			Object[] args = { };
			method.invoke(testClassInstance, args);
			times[i] = pt.getElapsedTime();
			total += times[i];
		}
		long average = total / invocationCount;
		for (int i=0; i<invocationCount; i++) {
			logger.info(String.format("Run %d  : %6d", i+1, times[i]));
		}
		logger.info(String.format("Average : %6d", average));
	}
	//生成13位唯一字符串：
	public static String gen13UniqueId(){
		return TestUtils.Generate13UniqueId();
	}
	//生成8位唯一的字符串：
	public static String gen8UniqueID(){
		return TestUtils.Generate8UniqueId();
	}

	// 生成诸如截图唯一名字等需要唯一字符串的地方
	public static String uniquify(String str) {
		return str + "_" + System.currentTimeMillis();
	}

	// 生成UUID
	public static String getUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	//-------------------------对元素操作的通用方法封装-------------------------//
	//拉动滚动使被遮挡的元素可见：
	protected void scrollIntoView(WebElement element){
		logger.info((ReportUtils.formatAction(String.format("Scroll screen to make the element '%s' available into Views ",element.getText()))));
		JavascriptExecutor jsExecutor = (JavascriptExecutor) TestContext.getWebDriver();		
		jsExecutor.executeScript("arguments[0].scrollIntoView(true);",element);
	}

	//获取页面的Title：
	protected String getTitle(){
		return TestContext.getWebDriver().getTitle();
	}
	
	//------- 定义几种等待的方式 --------//
	//等待页面加载完毕
	  public void waitForPageLoad() {
	        WebDriverWait wait = new WebDriverWait(getWebDriver(), TestContext.getDomTimeout());
	        wait.until(isPageLoaded());
	    }
	  //判断页面是否加载完成,如，登录系统后或页面跳转后，判断页面是否加载完成再继续其它的操作：
	  protected Function<WebDriver, Boolean> isPageLoaded() {
	        return new Function<WebDriver, Boolean>() {
	            @Override
	            public Boolean apply(WebDriver driver) {
	                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
	            }
	        };
	    }
	  
	  
		//简单等待,对Thread.sleep()方法进行处理，在其它地方调用时不再需要单独的try...catch处理：
		public void waitFor(long milles) {
			try {
				Thread.sleep(milles);
			}catch(Exception e) {
				e.printStackTrace();
				logger.info("\n time out with exception: " + e.getMessage());
			}
		}

	/**
	 *判断页面的Ajax是否完成.
	 */
	protected void waitForAjaxComplete() {
		logger.debug("waitForAjaxComplete");
		PerformanceTimer timer = new PerformanceTimer(TestContext.getAjaxTimeout());

		while (isAjaxExecuting() && ! timer.hasExpired()) {
			PerformanceTimer.wait(100);
		}
		if (timer.hasExpired()) {
			throw new TimeoutException("Timed out waiting for ajax call to complete, exceeded " + TestContext.getAjaxTimeout() + " ms");			
		}
		logger.debug("waitForAjaxComplete elapsed time " + timer.getElapsedTimeString());
	}

	protected boolean isAjaxExecuting() {
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
	 *写日志的简单实现
	 */
	protected void report(String message) {
		logger.info(message);
	}
	protected WebDriver getWebDriver() {
		return TestContext.getWebDriver();
	}
	
	// 出现这个异常 stale element reference，
	// 是因为之前获取的元素，因为JS等对页面的刷新，导致元素找不到，下面的方法是尝试多次获取：
	public void retryIfPageRefreshed(By by, String contents) {
		int retryTimes = 0;
		while (retryTimes <= 3) {
			try {
				WebElement e = getWebDriver().findElement(by);
				if (null != e) {
					e.sendKeys(contents);
				} else {
					logger.info("page has been refreshed, and try to locate element again. '" + retryTimes + "' times");
					
					retryTimes++;
				}
			} catch (Exception e) {
				logger.info("the page may refresh too frequently, cannot locate element after refreshing...");
				// throw exception....
			}
		}
	}
	
	public WebElement retryIfPageRefreshed(By by) {
		int retryTimes = 0;
		int timeout = TestContext.getDomTimeout();

		while (timeout > 0) {
			try {
				WebElement e = getWebDriver().findElement(by);
				if (null != e) {
					logger.debug("found element: '" + e + "'");
					return e;
				} else {
					// if中找不到元素时，会直接抛出异常，所有进不到else.
				}
			} catch (Exception e) {
				retryTimes++;
				logger.info("try to re-locate element. '" + retryTimes + "' times");

				waitFor(300);

				timeout -= 300;
				if (retryTimes > 6) {
					break;
				}
			}
		}
		return null;
	}

	protected WebElement findElement(By by) {
		return findElement(null, by);
	}
	//根据传入的：xpath=xxx, id=xxx定位元素的方法: 定位单个元素
	protected WebElement getElement(String locator) throws Exception{
		List<WebElement> list = this.getElements(locator);
		
		if(null != list && list.size() > 0 ){
			return list.get(0);
		}
		return null;
	}
	
	//定位多个元素：需要传入的Locaotr格式： xpath=xxx, id=xxx定位元素的方法
	protected List<WebElement> getElements(String locator) throws Exception{
		
		List<WebElement> eleList = null;
		
		if(BaseFrameworkUtil.isNotNullOrBlank(locator)){
			int index = locator.indexOf("=");//xpath=//div[]的格式；
			if(index != -1){
				String key = locator.substring(0, index).trim();
				String value = locator.substring(index + 1).trim();
				//try{
					switch(key){
						case "id":
							eleList = waitUntilElements(By.id(value), TestContext.getDomTimeout());
							break;
						case "xpath":
							eleList = waitUntilElements(By.xpath(value), TestContext.getDomTimeout());
							break;
						case "class":
							eleList = waitUntilElements(By.className(value), TestContext.getDomTimeout());
							break;
						case "css":
							eleList = waitUntilElements(By.cssSelector(value), TestContext.getDomTimeout());
							break;
						case "tagName":
							eleList = waitUntilElements(By.tagName(value), TestContext.getDomTimeout());
							break;
						case "link":
							eleList = waitUntilElements(By.linkText(value), TestContext.getDomTimeout());
							break;
						case "partialLink":
							eleList = waitUntilElements(By.partialLinkText(value), TestContext.getDomTimeout());
							break;
						
					}
					return eleList;
				/*}catch(Exception e){
					
				}*/
			}
		}else{
			logger.error("locator cannot be blank, and format should be: 'xpath=xxx / id=xxx / class=xxx / tagName=xxx / link=xxx'");
				throw new RuntimeException("locator's format error, should be: 'xpath=xxx / id=xxx / class=xxx / tagName=xxx / link=xxx'");
		}
		
		return null;
	}
	
	protected WebElement findElement(WebElement container, By by) {
		PerformanceTimer timer = new PerformanceTimer();
		try {
			WebElement element = container != null ? container.findElement(by) : getWebDriver().findElement(by);  
			logger.debug("findElement " + by.toString() + " " + timer.getElapsedTimeString());
			return element;			
		} catch (NoSuchElementException ex) {
			throw new NoSuchElementException("No Element in the DOM matching " + by.toString(), ex);
		}
	}

	protected List<WebElement> findElements(By by) {
		return findElements(null, by);
	}

	protected List<WebElement> findElements(WebElement container, By by) {
		PerformanceTimer timer = new PerformanceTimer();
		List<WebElement> elements = container != null ? container.findElements(by) : getWebDriver().findElements(by);
		logger.debug("findElements " + by.toString() + " " + timer.getElapsedTimeString());
		return elements;
	}


	protected WebElement waitUntilElement(final By by) {
		return waitUntilElement(by, TestContext.getDomTimeout());
	}

	protected WebElement waitUntilElement(final WebElement container, final By by) {
		return waitUntilElement(container, by, TestContext.getDomTimeout());
	}

	protected WebElement waitUntilElement(final By by, int timeoutMilliseconds) {
		return waitUntilElement(null, by, timeoutMilliseconds);
	}
	
	/**
	 * Wait for an element to appear in the DOM relative to a parent container, returns the WebElement if found.
	 */
	protected WebElement waitUntilElement(final WebElement container, final By by, int timeoutMilliseconds) {
		PerformanceTimer perfTimer = new PerformanceTimer(timeoutMilliseconds);
		boolean found = false;
		WebElement webElement = null;
		logger.debug("waitUntilElement: " + by.toString());
		while (! found && ! perfTimer.hasExpired()) {
			try {
				if (container == null) {
					webElement = getWebDriver().findElement(by);
				} else {
					webElement = container.findElement(by);
				}
				found = (webElement != null);
			} catch (NoSuchElementException ex) {
				PerformanceTimer.wait(300);
			}
		}
		if (perfTimer.hasExpired()) {//等待元素超时失败；
			logger.error("timeout waiting for element: " + by.toString());
			throw new NoSuchElementException("Timeout waiting for " + by.toString());
		}
		logger.debug("waitUntilElement " + by.toString() + " " + perfTimer.getElapsedTimeString());
		return webElement;
		
		//return this.waitUntilElements(container, by, timeoutMilliseconds).get(0);
	}
	/**
	 * 查找的结果是多个元素组成的List
	 */
	protected List<WebElement> waitUntilElements(WebElement container, By by, int timeoutMilliseconds) {
		PerformanceTimer perfTimer = new PerformanceTimer(timeoutMilliseconds);
		boolean found = false;
		WebDriver driver = getWebDriver();
		
		List<WebElement> webElements = null;
		logger.debug("waitUntilElements " + by.toString());
		//当未超时，且!found=true;时一直循环：while(true && !timer.hasExpired()){......}
		while (! found && ! perfTimer.hasExpired()) {//found=false; while(!found) => while(true)死循环
			try {
				if (container == null) {
					webElements = driver.findElements(by);
				} else {
					webElements = container.findElements(by);
				}
				found = (webElements != null);
			} catch (NoSuchElementException ex) {
				PerformanceTimer.wait(300);
			}
		}
		if (perfTimer.hasExpired()) {//等待元素超时失败；
			logger.error("timeout waiting for element: " + by.toString());
			logger.debug("waitUntilElement " + by.toString() + " totalTime: " + perfTimer.getElapsedTimeString());
			throw new NoSuchElementException("Timeout waiting for " + by.toString());
		}
		logger.debug("waitUntilElement " + by.toString() + " totalTime: " + perfTimer.getElapsedTimeString());
		return webElements;
	}

	protected List<WebElement> waitUntilElements(By by, int timeoutMilliseconds) throws Exception{
		PerformanceTimer perfTimer = new PerformanceTimer(timeoutMilliseconds);
		boolean found = false;
		List<WebElement> webElements = null;
		logger.info("waitUntilElements " + by.toString());
		
		//当未超时，且!found=true;时一直循环：while(true && !timer.hasExpired()){......}
		while (! found && ! perfTimer.hasExpired()) {//found=false; while(!found) => while(true)死循环
			try {
				webElements = getWebDriver().findElements(by);
				found = (webElements != null);
			} catch (Exception ex) {
				PerformanceTimer.wait(300);
				logger.debug(ReportUtils.formatData("尚未找到元素，等待 300ms 后重新尝试查找..."));
			}
		}
		if (perfTimer.hasExpired()) {//等待元素超时失败；
			logger.error("timeout waiting for element: " + by.toString());
			throw new NoSuchElementException("Timeout waiting for " + by.toString());
		}
		logger.debug("waitUntilElement " + by.toString() + " " + perfTimer.getElapsedTimeString());
		return webElements;
	}
    /**
     * Wait for an element to be *removed* from the DOM
     */
    protected void waitUntilElementNotPresent(By by) {
        PerformanceTimer perfTimer = new PerformanceTimer(TestContext.getAjaxTimeout());
        logger.debug("waitUntilElementGone " + by.toString());
        try {        	
        	getWebDriver().findElement(by);
        	// Found it, now wait for it to be removed
        	boolean found = true;
        	do {
				try {
					getWebDriver().findElement(by);
					PerformanceTimer.wait(100);
				} catch (NoSuchElementException nsee) {
					found = false;
				}
			} while (found && !perfTimer.hasExpired());

        } catch (NoSuchElementException nsee) {
        	// 
        }

        if (perfTimer.hasExpired()) {
            throw new TimeoutException("Timed out waiting for " + by.toString() + " to disappeared.");
        }

        logger.debug("waitUntilElementGone " + by.toString() + " " + perfTimer.getElapsedTimeString());
    }

    protected void waitUntilElementNotDisplayed(By by) {
        PerformanceTimer perfTimer = new PerformanceTimer(TestContext.getAjaxTimeout());
        logger.debug("waitUntilElementNotDisplayed " + by.toString());
        boolean displayed = false;
       	try {
       		WebElement element = waitUntilElement(by, 500);
       		displayed = element.isDisplayed();
       	} catch (NoSuchElementException ex) {
       		// 
       	} catch (StaleElementReferenceException ex) {
       		//
       	}
        	
    	// Found it, now wait for it to be removed wait
    	while (displayed && ! perfTimer.hasExpired()) {
			try {
				WebElement element = getWebDriver().findElement(by);
				displayed = element.isDisplayed();
				if (displayed) {
					PerformanceTimer.wait(50);
				}
			} catch (NoSuchElementException ex) {
				displayed = false;
			} catch (StaleElementReferenceException ex) {//TODO
				displayed = false;
			}
    	}

        if (perfTimer.hasExpired()) {
            throw new TimeoutException("Timed out waiting for " + by.toString() + " to go away");
        }

        logger.debug("waitUntilElementNotDisplayed " + by.toString() + " " + perfTimer.getElapsedTimeString());
    }

	protected boolean isElementEnabled(WebElement webElement, String disabledClassAttribute) {
		String classAttribute = webElement.getAttribute("class");
		return webElement.isEnabled() && ! classAttribute.contains(disabledClassAttribute);
	}
	
	/**
	 * Wait until an element is displayed.  
	 */
	protected WebElement waitUntilElementDisplayed(final WebElement webElement) {
		return waitUntilElementDisplayed(webElement, TestContext.getDomTimeout());
	}
	protected WebElement waitUntilElementDisplayed(final WebElement webElement, int timeoutMilliseconds) {
		PerformanceTimer perfTimer = new PerformanceTimer(timeoutMilliseconds);
		logger.debug("waitUntilElementDisplayed");
		while (! webElement.isDisplayed() && ! perfTimer.hasExpired()) {
			PerformanceTimer.wait(100);
		}
		if (perfTimer.hasExpired()) {
			throw new IllegalStateException("Element is still not displayed");
		}
		logger.debug("waitUntilElementDisplayed " + perfTimer.getElapsedTimeString());
		return webElement;
	}
	
	/**
	 * 检查和确认弹出窗口 
	 */
	public static String acceptAlert() {
		String alertText = null;
		WebDriver webDriver = TestContext.getWebDriver();
		if (webDriver != null) {
			try {
				Alert alert = webDriver.switchTo().alert();
				alertText = alert.getText();
				alert.accept();
				Thread.sleep(200);
			} catch (Exception ex) {
				// 如果走到这里， 可以不做任何处理，表示当前没有任何的弹出窗口需要处理
			}
			webDriver.switchTo().defaultContent();
		}
		return alertText;
	}
	/**
	 * 检查和取消弹出窗口 
	 */
	public static String cancelAlert() {
		String alertText = null;
		WebDriver webDriver = TestContext.getWebDriver();
		if (webDriver != null) {
			try {
				Alert alert = webDriver.switchTo().alert();
				alertText = alert.getText();
				alert.dismiss();
				Thread.sleep(200);
			} catch (Exception ex) {
				// 如果走到这里， 可以不做任何处理，表示当前没有任何的弹出窗口需要处理
			}
			webDriver.switchTo().defaultContent();
		}
		return alertText;
	}
	
	//-------------------------对元素操作的通用方法封装结束-------------------------//
	
	// -------------- 对XML文件的操作 -----------------------//
	/**
	 * dataSetBeanMap:封装的数据格式如下：
	 * {loginLocators:DataSetBean, loginWithoutPassword:DataSetBean}, 
	 */
	private Map<String, DataSetBean> dataSetBeanMap = null;

	//@BeforeTest
	public void setup() {
		//装载与类同名的XML文件数据；
		//initializeDataSet();是否需要一开始就去初始化XML数据，留存。。
		
		start();//调用start()完成初始化等工作。
	}
	
	/*
	 * 抽象方法start()：如果要实现WebDriver的自动化，在子类中实现此方法，可完成Driver初始化及浏览器启动等。
	 */
	protected abstract void start();
	
/*	@AfterTest
	public void tearDown() throws Exception{

	}
*/
	/**
	  *开始装载XML测试数据： 如果查找到有与用例类同名的XML文件,就调用方法，完成解析，填充数据；
	*/
	@SuppressWarnings("unused")
	private void initializeDataSet() {
		try {
			
			InputStream in = XMLFinder.getInputFileAsStream(this.getClass());  
			if (in != null) {
				logger.info(" found test data…" + this.getClass().getPackage().getName() + this.getClass().getSimpleName() + ".xml. digesting file…");
				
				XMLParser parser = new XMLParser(in);
				dataSetBeanMap = parser.parseXml();// 解析整个XML文件后得到List；
			} else {
				logger.info(" no test data file found...");//
			}
		} catch (Exception e) {
			logger.error("error while parsing input file…", e);
		}
	}

	public Map<String, DataSetBean> getData() {//获取解析后的整个Xml数据，封装到Map中；
		if (null != dataSetBeanMap) {
			return dataSetBeanMap;
		} 
		return null;
	}
	
	/**
	 * 
	 * @Description: 获取locator的DataSetBean，其中封装的是locatorName, by, locatorValue
	  * @return DataSetBean 
	 * @author James Guo
	 *
	 */
	public DataSetBean getLocatorsBean(String locatorsName) {
		return dataSetBeanMap.get(locatorsName);
	}
	
	/**
	 * 
	 * @Description: 获取dataset的DataSetBean，封装的是var, varList
	  * @return DataSetBean 
	 * @author James Guo
	 *
	 */
	public DataSetBean getDataSetBean(String dataSetName) {
		return dataSetBeanMap.get(dataSetName);
	}
	/**
	 * 
	 * @Description: 获取dataset中的 var 的值
	  * @return String 
	 * @author James Guo
	 *
	 */
	public String getVar(String dataSetName, String varName) {
		return getDataSetBean(dataSetName).getVarValue(varName);
	}
	/**
	 * 
	 * @Description: 获取dataset 中var list的值
	  * @return List<String> 
	 * @author James Guo
	 *
	 */
	public List<String> getVarList(String dataSetName, String listName){
		return getDataSetBean(dataSetName).getVarValues(listName);
	}
	/**
	 * 
	 * @Description: 获取locator的by的值
	  * @return String 
	 * @author James Guo
	 *
	 */
	public String getLocatorByTypeVal(String locatorsName, String locatorName) {
		return getLocatorsBean(locatorsName).getLocator(locatorName).get(0);
	}
	/**
	 * 
	 * @Description: 获取locator的的值
	  * @return String 
	 * @author James Guo
	 *
	 */
	public String getLocatorVal(String locatorsName, String locatorName) {
		return getLocatorsBean(locatorsName).getLocator(locatorName).get(1);
	}
}
