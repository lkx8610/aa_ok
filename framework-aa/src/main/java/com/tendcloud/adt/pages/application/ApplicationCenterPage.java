package com.tendcloud.adt.pages.application;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tendcloud.adt.framework.constants.ApplicationPageConstants;
import com.tendcloud.adt.framework.constants.BasicElementLocators;
import com.tendcloud.adt.pages.common.AbstractBasePage;
import com.tendcloud.adt.widgets.ActivationTipBox;
import com.tendcloud.adt.widgets.DataTable;

import framework.base.LoggerManager;
import framework.base.utils.ReportUtils;
import framework.webdriver.TestContext;

/**
 * Main Page, 用户登录后进入的页面
 * @author James Guo
 *
 */
public class ApplicationCenterPage extends AbstractBasePage {
	private Logger logger = LoggerManager.getLogger(AbstractBasePage.class.getSimpleName());
	
	@FindBy(xpath=BasicElementLocators.CREATE_NEW_APP_XPATH)
	private WebElement createNewAppBtn;
	@FindBy(xpath=BasicElementLocators.APPLICATION_CENTER_XPTH)
	private WebElement applicationCenterBtn;
	@FindBy(xpath=BasicElementLocators.MANAGEMENT_CENTER_XPATH)
	private WebElement manageCenterBtn;
	
	public ApplicationCenterPage() {		
		super();
		
		//waitForPageLoad();
		//waitUntilElement(By.id(CommonDOMIds.Button_GlobalFinder));
	}
	//点击 "创建新应用"按钮跳转到新建应用页面：
	public ApplicationPage navi2ApplicationPage(){
		if(isCreateNewBtnDisplayed()){
			logger.info("click on Create New Application button...");
			createNewAppBtn.click();
			
			//点击“创建新应用”后，将进入“选择产品类型”页面；等待“选择您要监测的产品类型”元素出现，说明页面跳转成功：
			waitUntilElement(By.xpath(ApplicationPageConstants.SELECT_APP_TYPE), TestContext.getDomTimeout());

			logger.info("nevigate to new Application page...");
		}
		return new ApplicationPage();
	}
	/**
	 * 判断“创建新应用”按钮的可用性
	 */
	public boolean isCreateNewBtnDisplayed() {
		WebElement ele = null;
		try{
			ele = waitUntilElement(By.xpath(BasicElementLocators.CREATE_NEW_APP_XPATH));
		}catch(Exception e){
			//
		}		
		return null != ele;
	}

	/**
	 * 获取Main Page中所有Application的列表：
	 * @throws Exception 
	 */
	public List<WebElement> getApplicationList() throws Exception{
		//List<WebElement> appList = findElements(By.xpath(BasicElementLocators.APPLICATION_LIST_XPATH));
		By by = By.xpath(BasicElementLocators.APPLICATION_LIST_XPATH);
		
		List<WebElement> appList = waitUntilElements(by, TestContext.getDomTimeout());
		
		if(appList.size() > 0){
			logger.debug("there're '" + appList.size() + "' Applications in Application Center Page..");
			return findElements(by);
		}
		
		return null;
	}
	/**
	 * 搜索应用：根据名称查找应用，可能返回0、1或多个；
	 * @return
	 * @throws Exception 
	 */
	private List<WebElement> basicSearchApp(String appName) throws Exception{
		//输入框定位对象：
		By searchInput = By.xpath(BasicElementLocators.SEARCH_INPUT_XPATH);
		
		// 等待获取Search Input组件：
		WebElement searchInputEle = waitUntilElement(searchInput);

		try{
			setTextValueWithEnterKey(searchInputEle, appName);//在输入框中输入应用名称，之后按Tab键完成
		}catch(Exception e){
			//retryAndInput(searchInput, appName);
			e.printStackTrace();
		}
		
		logger.info("application name to be searched: " + appName);
		
		waitForAjaxComplete();
		waitForPageLoad();

		waitFor(1000);//TODO 这里不另外加等待时间，由于网络的不稳定极易造成查找失败，先硬写这里，以后改进；
		//等待搜索结果出现：
		List<WebElement> resultList = waitUntilElements(By.xpath(BasicElementLocators.APPLICATION_LIST_XPATH), TestContext.getDomTimeout());

		if (null != resultList && resultList.size() > 0) {
			logger.info("found applications with name " + wrapSingleQuotes(appName) + " total '" + resultList.size() + "' records.");
			return resultList;// 返回当前搜索的应用,如果List的size>0，返回这个list,否则说明没有查询到结果，返回Null；
		} else {
			report(ReportUtils.formatError("couldnot find the application with name " + wrapSingleQuotes(appName)));
		}
		return null;
	}
	
	//【精确查找】Application，在adt系统上，搜索功能是输入文本到输入框后异步请求得到结果的，所以不用再额外的点击按钮。
	public WebElement searchSingleApplication(String appName) throws Exception{//精确查询，用于在测试创建新应用后的查询（创建的新应用的名字是唯一的，所以只能查询出1条或0条记录）；
		
		List<WebElement> searchedApp = this.basicSearchApp(appName);
		if(null != searchedApp){
			logger.debug("found application with name: " + wrapSingleQuotes(appName));
			return getApplicationList().get(0);//返回当前搜索的应用
		}else{
			report(ReportUtils.formatError("couldnot found the application with name " + wrapSingleQuotes(appName)));
		}
		return null;
	}
	// 根据部分名称进行的模糊查询，或者可能存在多个同名的应用，返回的可能为null/一个/多个；
	public List<WebElement> searchMultiApplication(String partialAppName) throws Exception {
		List<WebElement> appList = this.basicSearchApp(partialAppName);
		if(null != appList){
			logger.debug("found applications with name: " + wrapSingleQuotes(partialAppName) + " total " + "'" + appList.size() + "'记录..");
			return appList;
		}else{
			report(ReportUtils.formatError("couldnot found the application with name: " +  wrapSingleQuotes(partialAppName)));
		}
		return null;
	}

	/**
	 * 进入“产品中心”页面前，先检查是否有激活相关的弹出框
	 * 检查是否有激活弹出窗口：
	 */
	ActivationTipBox tipBox = null;//定义弹出框对象；
	
	public boolean isActivationTipShown(){
		tipBox = new ActivationTipBox();
		
		return tipBox.isActiveTipBoxShown();
	}
	
	//Remind Next Time:
	public void remindNextTime(){
		if(isActivationTipShown()){
			 tipBox.remindeNextTime();
		}
	}
	
	//激活：
	public void activateNow(){
		if(isActivationTipShown()){
			tipBox.startNow();
		}
	}
	
	/**
	 * 根据传入的应用名称，点击后跳转到“推广概览”页面：
	 * @author James Guo
	 * @param: appName - 应用名称
	 */
	public void navi2TuiGuangGaiLanPage(String appName){
		String baseLocator = "//a[contains(text(), '%s')]" ;//所有的Name的链接都是: //a[contains(text(), "ADT-AUTO-TEST7395432")]的形式；
		String appNameLocator = formatString(baseLocator, appName);
		logger.info("search element: ' " + appNameLocator + " '");
		
		WebElement e = waitUntilElement(By.xpath(appNameLocator));
		
		if(null != e){
			logger.info("found element '" + appName + "' and click the Name link for navigation.");
			e.click();
			waitForPageLoad();
		}else{
			logger.error("cannot find element: '" + e + "'");
		}
	}
	
	
	/**
	 * //修改Application
	 * 点击应用的“设置”链接，进入应用设置页面；
	 * @author James Guo
	 * @param appName
	 * @return
	 * @throws Exception 
	 */
	public ApplicationPage navi2AppSetting(String appName) throws Exception{
		WebElement app = this.searchSingleApplication(appName);//先搜索要设置的App，如存在，页面停留在搜索结果处，再查找“设置”元素：
		
		if(null != app){
			String locator = formatString("//a[contains(text(),'%s')]", "设置");
			logger.info("search element: ' " + locator + " '");
			
			WebElement e = waitUntilElement(By.xpath(locator));
			logger.info("found elemnt: ' " + e.getText() + " '");
			e.click();
			
		}else{
			logger.error("cannot find element '" + appName + "'");
			throw new NoSuchElementException();
		}
		return new ApplicationPage();
	}
	
	public String formatString(String formater, String name){
		return String.format(formater, name);
	}
}
