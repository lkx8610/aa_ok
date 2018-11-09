package com.tendcloud.adt.pages.common;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tendcloud.adt.framework.constants.BasicElementLocators;
import com.tendcloud.adt.pages.application.ApplicationCenterPage;

import framework.base.utils.PerformanceTimer;
import framework.base.utils.ReportUtils;
import framework.webdriver.TestContext;

//import static base.Verify.*;

public class LoginPage extends AbstractBasePage {

	@FindBy(xpath=BasicElementLocators.USERNAME_INPUT_XPATH)
	private WebElement usernameInput;
	
	@FindBy(xpath=BasicElementLocators.PASSWORD_INPUT_XPATH)
	private WebElement passwordInput;
	
	@FindBy(xpath=BasicElementLocators.LOGIN_BUTTON_XPATH)
	private WebElement loginBtn;
	
	public LoginPage() {
		super();
	}
	
	public void get(){
		String url = TestContext.getServerUrl();
		logger.debug("server URL: " + url);
		
		getWebDriver().get(url);
	}
	
	//什么也不输入的错误登录(登录功能验证：异常方式登录)
	public LoginPage loginWithoutUserAndPass(){
		logger.info("tring to log in without username and password.");
		this.usernameInput.clear();
		this.passwordInput.clear();
		
		this.submit();
		
		return this;
	}
	//只输用户名
	public LoginPage inputUsername(String user){
		logger.info("log in with username: " + user);
		usernameInput.clear();
		usernameInput.sendKeys(user);
		
		return this;
	}
	
	//只输入密码
	public LoginPage inputPassword(String password){
		logger.info("login with password:" + password);
		this.passwordInput.clear();
		this.passwordInput.sendKeys(password);
		
		return this;
	}
	
	public void submit(){
		this.loginBtn.click();
	}
	
	//regular login function:
	public void login(String username, String password) {
		PerformanceTimer timer = new PerformanceTimer();
		
		this.get();//输入server 地址 进入登录页面
		
		report(ReportUtils.formatAction("Login with user: ", username));
		inputUsername(username);		
				
		report(ReportUtils.formatAction("Login with pass: ", password));
		inputPassword(password);
		
		this.submit();
		
		//等待页面跳转后显示“创建新应用按钮”
		waitUntilElement(By.xpath(BasicElementLocators.CREATE_NEW_APP_XPATH), TestContext.getDomTimeout());
		
		//verifyContains(getWebDriver().getTitle(), BasicTestConstants.APP_CENTER_TITLE, "after login the new page titile should contains " + BasicTestConstants.APP_CENTER_TITLE );
		
		logger.info("login completed." + timer.getElapsedTimeString());
		
	}
	
	//basic login function:
	public void basicLogin(){
		PerformanceTimer timer = new PerformanceTimer(TestContext.getDomTimeout());
		
		String url = TestContext.getServerUrl();
		
		getWebDriver().get(url);
		
		String loginPageTitle = getWebDriver().getTitle();//登录页面的Title；
		
		logger.info("login with server url: " + url);
		waitFor(100);
		
		this.usernameInput.sendKeys(TestContext.getUsername());
		logger.info("entered username : " + TestContext.getUsername());
		
		this.passwordInput.sendKeys(TestContext.getPassword());
		logger.info("entered password : " + TestContext.getPassword());
		
		this.loginBtn.click();
		logger.info("pressed on login button, and waitting for page loaded...");
		
		while (true) {//这里判断有标题有点问题，待改进：TODO
			String nextPageTitle = getWebDriver().getTitle();//登录成功后新页面的Title; 

			if (!"".equals(nextPageTitle) && !nextPageTitle.equals(loginPageTitle)) {//根据页面的Title来判断是否登录成功
				if (!timer.hasExpired()) {
					logger.info("login completed..." + timer.getElapsedTimeString());// 如果出现“创建新应用”按钮，则登录成功；
				}else{
					logger.info("login failed for timeing out..." + timer.getElapsedTimeString());// 如果出现“创建新应用”按钮，则登录成功；
				}
				break;
				
			} else {
				if (!timer.hasExpired()) {
					PerformanceTimer.wait(100);
					logger.info("wait 100ms for page redirection...");
				} else {
					logger.info("login failed for timeing out..." + timer.getElapsedTimeString());// 如果出现“创建新应用”按钮，则登录成功；
					break;
				}
			}
		}
	}
	
	/**
	 * 非Login的功能测试直接调用此方法完成登录后跳转：
	 * @author James Guo
	 * @return
	 */
	public ApplicationCenterPage navi2ApplicationCenterPage(){
		
		this.basicLogin();
		//waitFor(300);
		
		return new ApplicationCenterPage();
	}
}











