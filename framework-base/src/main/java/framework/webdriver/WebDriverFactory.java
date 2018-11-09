package framework.webdriver;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import framework.base.LoggerManager;


/***
 * WebDriver工厂类
 */
public class WebDriverFactory {
	/*private static final Logger log = LoggerManager.getLogger(Thread.currentThread().getClass().getName());
	private static RemoteWebDriver driver;
	static {
		log.debug("setting WebDrivers in webdriver factory...");
		setDriver();
	}

	public static void setDriver() {
		String browserName = System.getProperty("browserName"); // 获取浏览器名,通过Maven配置文件传入；
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setBrowserName(browserName);
		driver = setDriver(capabilities);
	}

	private static RemoteWebDriver setDriver(DesiredCapabilities capabilities) {
		String browserType = capabilities.getBrowserName();
		if(null == browserType || "".equals(browserType)) {
			browserType = "chrome";
			log.info("there's no browser type defined in System Property, will use chrome browser as default...");
			return setCrhomeDriver(capabilities);
			
		} else if (browserType.equals(BrowserType.FIREFOX)) {
			log.info("using 【firefox】 as the test browser...");
			return setDriverFirefox(capabilities);
		} else if (browserType.equals(BrowserType.SAFARI)) {
			log.info("using 【safari】 as the test browser...");
			return setDriverSofari(capabilities);
		} else if (browserType.equals(BrowserType.CHROME)) {
			log.info("using 【chrome】 as the test browser...");
			return setCrhomeDriver(capabilities);
		} 
		return null;
	}

	*//**
	 * set different webdriver instances;
	 * 
	 * @Description:
	 * @return RemoteWebDriver
	 * @author James Guo
	 *
	 *//*
	@SuppressWarnings("all")
	private static RemoteWebDriver setCrhomeDriver(DesiredCapabilities capabilities) {
		System.setProperty("webdriver.chrome.driver", "src/test/resources/browser-driver/chromedriver.exe");
		log.debug("\n setting up chrome driver...");
		return new ChromeDriver(capabilities);
	}

	private static RemoteWebDriver setDriverFirefox(DesiredCapabilities capabilities) {
        log.debug("\n Set Firefox driver...");
        //如果FF不是默认安装的，要设置这个，指向FF的EXE文件所在的位置；
      		System.setProperty("webdriver.firefox.bin", "test/resources/browser-driver/firefox.exe"); 
      		
      			return new FirefoxDriver(capabilities);
    }

	private static RemoteWebDriver setDriverSofari(DesiredCapabilities capabilities) {
		// TODO Auto-generated method stub
		return null;
	}
	// 获取Driver:
	public static WebDriver getDriver() {
		return driver;
	}*/
}
