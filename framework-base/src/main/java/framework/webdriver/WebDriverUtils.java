package framework.webdriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import framework.base.LoggerManager;
import framework.webdriver.TestContext;
import framework.webdriver.WebDriverUtils;

/**
 * 获取和设置Webdriver实例及元素操作的方法封装（可考虑将Webdriver的获取与设置单独拿出来封装）
 */
public class WebDriverUtils {

	private static Logger logger = LoggerManager.getLogger(Thread.currentThread().getClass().getName());

	public static final String CHROME = "chrome";
	public static final String FIREFOX = "firefox";
	public static final String SOFORI = "safari";
	//public static final String IE = "ie"; //暂时不在IE上测试；

	@SuppressWarnings("deprecation")
	public static void launchBrowser() throws Exception {
		//从配置文件中获取的BrowserType：chrome/firfox/safari
		String browserType = TestContext.getBrowserType().trim();
		logger.debug("Create Browser driver for " + browserType);
		
		//从配置文件中取浏览器Driver的name:chromedriver.exe/geckodriver.exe..
		String driverName = TestContext.getProperty("browser.driver.name").trim().toLowerCase();//去空格+小写
		logger.debug("getting browser driver's name from properties file: "  + driverName);
		
		//在System.setProperties("webdriver.chrome.driver", driverPath);
		String driverPath = TestContext.getProperty("browser.driver.path");//从配置文件读取browser.driver.path的值；
		
		//查看是否配置Grid，以决定是本地还是远程：
		String gridServer = TestContext.getProperty("grid.server"); 
		WebDriver webDriver = null; 
		DesiredCapabilities capabilities = TestContext.getCapabilities();
		// driverType=firefox
		capabilities.setBrowserName(browserType);
		
		if (FIREFOX.equals(browserType)) {
			try {
				//driverName=webdriver.gecko.driver
				if (StringUtils.isBlank(driverName)) {
					//如果配置文件中未配置browser.driver.name属性，将用Default名字拼接路径：
					driverPath = WebDriverUtils.class.getClassLoader().getResource(driverPath + "/geckodriver.exe").getPath();
					logger.debug("no browser's driver name configured, using default: " + driverPath);
				}else{
					//如果在配置文件中配置了browser.driver.name属性，则有如下的拼接路径， driver所在目录 + driverName:
					driverPath = WebDriverUtils.class.getClassLoader().getResource(driverPath + "/" + driverName).getPath();
				}
				
				logger.debug("will load browser's driver from: " + driverPath + " - driver name: " + driverName);
				
				System.setProperty("webdriver.gecko.driver", driverPath);
				//capabilities.setBrowserName(driverName);
				//System.setProperty("webdriver.firefox.profile", "default");
				//webDriver = new FirefoxDriver(capabilities);
				
				if (StringUtils.isBlank(gridServer) || StringUtils.isBlank(gridServer.trim())) {
					webDriver = new FirefoxDriver(capabilities);//未设置Grid属性，在本地跑

				} else {
					// Selenium Grid 
					webDriver = sartGrid(gridServer, capabilities, webDriver);//有Grid的配置，则启动Grid
					//webDriver = new RemoteWebDriver(new URL("http://192.168.234.129:8888/wd/hub/"), capabilities);
				}
				
			}catch(IllegalStateException ex) {
				throw new IllegalStateException("You must have the firefox driver(geckodriver.exe) installed correctlly first.", ex);
			}

		}/* else if (IE.equals(driverType)) { //项目暂时不用IE
			System.setProperty("webdriver.ie.driver", "browserdrivers\\IEDriverServer.exe");
			DesiredCapabilities ieCapabilities = TestContext.getCapabilities();
			// http://jimevansmusic.blogspot.ca/2012/08/youre-doing-it-wrong-protected-mode-and.html
			ieCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			webDriver = new InternetExplorerDriver(ieCapabilities);

		} */
		else if (CHROME.equals(browserType)) {
			try {
				if (StringUtils.isBlank(driverName)) {
					//如果未配置browser.driver.name, 将使用默认的名字：
					driverPath = WebDriverUtils.class.getClassLoader().getResource("browser-drivers/chromedriver.exe").getPath();
				}else{
					//如果配置文件中配置了browser.driver.name属性，则拼接成完整地址：目录 + driverName:
					driverPath = WebDriverUtils.class.getClassLoader().getResource("browser-drivers/" + driverName).getPath();
				}
				
				System.setProperty("webdriver.chrome.driver", driverPath);
				capabilities.setCapability("chrome.switches", Arrays.asList("--start-maximized", "--disable-hang-monitor"));
				
				if (StringUtils.isBlank(gridServer)) {
					webDriver = new ChromeDriver(capabilities);
					
				} else {
					// Selenium Grid 
					webDriver = sartGrid(gridServer, capabilities, webDriver);//有Grid的配置，则启动Grid
				}
				
			} catch (IllegalStateException ex) {
				throw new IllegalStateException("You must have the chromedriver installed correctlly firstly.", ex);
			}

		} else {
			throw new RuntimeException("Invalid value in base.automation.properties, driver type, must be 'firefox', 'chrome' or 'safari' : " +browserType);
		}
		webDriver.manage().window().maximize();
		
		//获取当前测试线程的TestContext对象，并将创建的WebDriver设置到当前测试线程的TestContext中：
		TestContext.get().setWebDriver(webDriver);
		//TestContext.get().setCurrentIFrame(null);
		
		String userAgent = WebDriverUtils.getUserAgent();
		logger.debug(userAgent);
		TestContext.setProperty("userAgent", userAgent);
	}
	
	//启动Grid Server
	private static WebDriver sartGrid(String gridServerUrl, DesiredCapabilities caps, WebDriver webDriver ){
		
		gridServerUrl = "http://" + gridServerUrl + "/wd/hub/";//指向要启动浏览器的远程的Server
		caps.setBrowserName(caps.getBrowserName());
		caps.setPlatform(Platform.WINDOWS);
		caps.setVersion("67");
		try {
			webDriver = new RemoteWebDriver(new URL(gridServerUrl), caps);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return webDriver;
	}
	
	/**
	 * 在浏览器中执行JS脚本
	 */
	public static Object executeJavaScript(String script) {
		Object returnValue = null;
		JavascriptExecutor jsExecutor = (JavascriptExecutor) TestContext.getWebDriver();
		logger.info("execute Javascript " + script);
		if (jsExecutor != null) {
			returnValue = jsExecutor.executeScript(script);
		}
		return returnValue;
	}

	public static String getUncaughtException() {
		String uncaughtException = null;
		try {
			uncaughtException = (String) executeJavaScript("return uncaughtException;");
			executeJavaScript("uncaughtException = null;");
		} catch (Exception ex) {
			logger.warn(ex, ex);
		}
		return uncaughtException;
	}

	/**
	 * 获取浏览器的User Agent，比较不同浏览器上测试执行的情况 （如执行时间。。。）
	 */
	public static String getUserAgent() {
		return (String) executeJavaScript("return navigator.userAgent;");
	}
}
