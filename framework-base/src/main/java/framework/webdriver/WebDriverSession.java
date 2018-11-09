package framework.webdriver;

import org.apache.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

import framework.base.LoggerManager;
import framework.webdriver.WebDriverSession;


public class WebDriverSession {

	/**
	 * 保存Webdriver的实例的ThreadLocal对象：
	 */
    private static ThreadLocal<RemoteWebDriver> rwd = new ThreadLocal<>();
    private static Logger log = LoggerManager.getLogger(WebDriverSession.class);

    public static RemoteWebDriver get() {
    	RemoteWebDriver driver = rwd.get();
    	log.info("get Remote webdriver instance:" + driver);
    	
        return driver;
    }

    public static void set(RemoteWebDriver driver) {
    	log.info("set Remote webdriver instance: " + driver + "to ThreadLocal");
        rwd.set(driver);
    }
}
