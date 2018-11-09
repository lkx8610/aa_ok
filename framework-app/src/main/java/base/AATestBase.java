package base;

import keywords.BasicElementLocators;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;

import framework.base.LoggerManager;
import framework.keyworddriven.BaseKeywords;
import framework.webdriver.TestContext;
import framework.webdriver.WebDriverUtils;
import test.base.TestCaseBase;

/**
 * ADT测试基类
 * @author James Guo
 *
 */
@SuppressWarnings("all")
public abstract class AATestBase extends TestCaseBase{
	
	protected Logger logger = LoggerManager.getLogger(AATestBase.class.getSimpleName());

	/**
	 * 为了方便测试，开始测试时，创建Driver并自动登录
	 */
	protected void createBrowserAndAutoLogin() throws Exception {
		WebDriverUtils.launchBrowser();
		loginBeforeClass();
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

	/**
	 * Subclasses can override if they do not want automatic login
	 * Currently only intended for the Login unit tests
	 */
	protected void loginBeforeClass() {
		login(TestContext.getUsername(), TestContext.getPassword());
	}

	protected void login(String user, String password) {
		WebDriver driver = TestContext.getWebDriver();
		String serverAddress = TestContext.getServerUrl();
		if (!StringUtils.isBlank(serverAddress)) {
			logger.debug("Loading " + serverAddress);
			getWebDriver().get(serverAddress);
		}
		waitForAjaxComplete();
		waitForPageLoad();
		try {
			getElement("xpath=" + BasicElementLocators.USERNAME_INPUT_XPATH).sendKeys(user);
			waitFor(300);
			getElement("xpath=" + BasicElementLocators.PASSWORD_INPUT_XPATH).sendKeys(password);
			waitFor(300);
			getElement("xpath=" + BasicElementLocators.LOGIN_BUTTON_XPATH).click();
			waitFor(300);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//LoginPage loginPage = new LoginPage();
		//loginPage.login(user, password);
	}

	

	@Override
	protected void start() {
		// TODO Auto-generated method stub
		
	}
}
