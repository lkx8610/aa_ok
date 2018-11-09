package com.tendcloud.adt.framework.base;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterClass;

import com.tendcloud.adt.framework.constants.BasicElementLocators;
import com.tendcloud.adt.pages.application.ApplicationCenterPage;
import com.tendcloud.adt.pages.application.BasicAppPage;
import com.tendcloud.adt.pages.common.LoginPage;

import framework.base.LoggerManager;
import framework.webdriver.TestContext;
import framework.webdriver.WebDriverUtils;
import test.base.TestCaseBase;


/**
 * ADT测试基类
 * @author James Guo
 *
 */
@SuppressWarnings("all")
public abstract class AdtTestBase extends TestCaseBase{
	
	protected Logger logger = LoggerManager.getLogger(AdtTestBase.class.getSimpleName());

	protected BasicAppPage applicationMenu;
	
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
/*	@BeforeClass(alwaysRun=true)
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
		
		//createBrowserAndAutoLogin();//自动登录到系统
		createBrowser();
	}*/

	//首选验证测试服务的连通性：
/*	@SuppressWarnings("all")
	private void verifyAppServer() throws Exception {
		String serverAddress = TestContext.getServerUrl();
		if("http:///".equals(serverAddress)){
			logger.error("no application server address configured, exit testing abnormally, pls check the .properties file.");
			System.exit(-1);
		}
		logger.debug("Testing appserver availability: " + serverAddress);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(new HttpGet(serverAddress));
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
        	throw new Exception(String.format("Could not load %s : %d - %s", serverAddress, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
        }
        logger.info("accessed server successfully: " + serverAddress);
	}*/

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
		String serverAddress = TestContext.getServerUrl();
		if (!StringUtils.isBlank(serverAddress)) {
			logger.debug("Loading " + serverAddress);
			getWebDriver().get(serverAddress);
		}

		LoginPage loginPage = new LoginPage();
		loginPage.login(user, password);
	}

	/**
	 * 在任何页面都可返回到ApplicationCenterPage的方法
	 * @author James Guo
	 * @return ApplicationCenterPage
	 */
	public ApplicationCenterPage back2ApplicationCenter(){
		WebElement e = retryIfPageRefreshed(By.xpath(BasicElementLocators.TO_PRODUCT_CENTER));
		//WebElement e = getWebDriver().findElement(By.xpath("//a[contains(text(),'to home')]/following-sibling::a/strong"));//TODO 此值可配置在外部。
		
		e.click();
		logger.info("clicked on ' " + e.toString() + " 'and navigating..." );
		
		waitForPageLoad();
		
		return new ApplicationCenterPage();
	}
	

	@Override
	protected void start() {
		// TODO Auto-generated method stub
		
	}
}
