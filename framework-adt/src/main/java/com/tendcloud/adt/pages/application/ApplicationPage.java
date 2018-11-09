package com.tendcloud.adt.pages.application;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tendcloud.adt.framework.constants.ApplicationPageConstants;
import com.tendcloud.adt.framework.constants.BasicElementLocators;
import com.tendcloud.adt.pages.common.AbstractBasePage;
import com.tendcloud.adt.widgets.DataTable;

import framework.base.LoggerManager;
import framework.base.exception.StopRunningException;
import framework.base.utils.ReportUtils;
import framework.keyworddriven.executionengine.ExecutionEngine;
import framework.webdriver.TestContext;

public class ApplicationPage extends AbstractBasePage{
	Logger logger = LoggerManager.getLogger(ApplicationPage.class.getSimpleName());
	
	public ApplicationPage(){
		super();
	}

	//选择要创建的应用的类型：游戏/电商/其它：
	@FindBy(xpath=BasicElementLocators.GAME_XPATH)
	WebElement gameElement;
	@FindBy(xpath=BasicElementLocators.ECOMERCIAL_XPAHT)
	WebElement ecomercialElement;
	@FindBy(xpath=BasicElementLocators.OTERS_XPATH)
	WebElement othersElement;	
	
	//Application主页面上的创建的App的类型的单选框：IOS/Adriod。。。
	@FindBy(xpath=BasicElementLocators.ISO_XPATH)
	WebElement iOSPlatform; 
	@FindBy(xpath=BasicElementLocators.ANDROID_XPATH)
	WebElement andriodPlatform;
	@FindBy(xpath=BasicElementLocators.HTML5_XPATH)
	WebElement html5Platform;
	@FindBy(xpath=BasicElementLocators.SMALL_APP_XPATH)
	WebElement smallAppPlatform;
	
	//Application 状态元素：
	@FindBy(xpath=ApplicationPageConstants.APP_STATUS_ONSTOR)
	WebElement onStore;
	@FindBy(xpath=ApplicationPageConstants.APP_STATUS_DEVELOPING)
	WebElement developing;
	
	public enum ApplicationStatus{//应用的状态：开发中、商店中
		ONSTOR,
		DEVELOPING;
	}
	
	//选择要创建的应用的类型：游戏/电商/其它,之后点击进入Application创建页面：
	public ApplicationPage selectAppType(ApplicationTypes appType){
		if(null == appType){
			logger.info("no application type selected, will use 'Game' as the default type.");
			gameElement.click();//如果没有传application name, 默认创建Game；
		}else {
			if (appType.name().equalsIgnoreCase(ApplicationTypes.GAME.name())) {
				logger.info("the selected applicatoin type is:" + wrapSingleQuotes(appType.name()));
				gameElement.click();
			} else if (appType.name().equalsIgnoreCase(ApplicationTypes.ECOMERCIAL.name())) {
				logger.info("the selected application type is: " + wrapSingleQuotes(appType.name()));
				ecomercialElement.click();
			} else if(appType.name().equalsIgnoreCase(ApplicationTypes.OTHERS.name())){
				logger.info("the selected application type is: " + wrapSingleQuotes(appType.name()));
				othersElement.click();
			}
		}
		
		return this;
	}
	
	//选择创建应用的平台，选中前边的单选框：
	public ApplicationPage setAppPlatform(AppPlatformsType platform){
		
		if(null == platform || platform == AppPlatformsType.IOS){//如果不传平台名字，则默认选中IOS
			logger.info("no application platform passed in, take iOS as the default platform.");
			iOSPlatform.click();
		}else{
			
			if(platform == AppPlatformsType.ANDROID){
				logger.info("take Android as the platform..");
				andriodPlatform.click();
			}else if(platform == AppPlatformsType.HTML5){
				logger.info("take H5/Wap as the platform..");
				html5Platform.click();
			}else if(platform == AppPlatformsType.SMALLAPP){//创建小程序时，需要额外添加ID、Key
				logger.info("take Small App as the platform..");
				smallAppPlatform.click();
			}
		}
		
		return this;
	}
	
	//选择Application状态：
	public void setAppStatus(ApplicationStatus appStatus){
		if(appStatus == ApplicationStatus.ONSTOR){
			onStore.click();
			logger.info("the selected Application Status is: " + wrapSingleQuotes(appStatus.name()));
			waitFor(10);
			
		}else if(appStatus == ApplicationStatus.DEVELOPING){
			developing.click();
			logger.info("the selected Application Status is: " + wrapSingleQuotes(appStatus.name()));
			waitFor(10);
		}
	}
	
	//创建“非小程序”的新应用的方法,二者创建方式略有不同：
	public ApplicationPage createNewApp(String appName, AppPlatformsType platform, ApplicationStatus appStatus, String downloadLink){
		
		//设置“应用名称” + “选择应用平台”：
		this.setAppNameAndSelectPlatformType(appName, platform);
		
		logger.info("the selected Application Status: " + wrapSingleQuotes(appStatus.name()));
		this.setAppStatus(appStatus);
		
		//当AppStatus是“开发中”时，是没有邮箱输入框的，只有“在商店中”时才有邮箱输入框：
		if(appStatus != ApplicationStatus.DEVELOPING){
			WebElement downLoadLinkInput = waitUntilElement(By.xpath(BasicElementLocators.DOWNLOAD_LINK_XPATH));
		
			//由于页面的遮挡，邮箱输入框不可见，所以要先让其可见：
			scrollIntoView(downLoadLinkInput);
			
			logger.info("input downloadlink: " + wrapSingleQuotes(downloadLink));
			downLoadLinkInput.sendKeys(downloadLink);
		}
		
		//创建完成，并检验是否成功且跳转页面：
		this.finishCreationAndVerify();
		
		return this;
	}
	
	//创建“小应用”的方法：不需要再传平台类型，只需传递“名称 + ID + Key
	public ApplicationPage createSmallApp(String appName,  String appId, String appKey){
		//设置小应用的名字 + 平台类型选择：
		//this.setAppNameAndSelectPlatformType(appName, platform);
		
		//输入小程序名字：
		logger.info("创建小应用：" + wrapSingleQuotes(appName));
		findElement(By.xpath(BasicElementLocators.APP_NAME_INPUT_XPATH)).sendKeys(appName);
		//选择平台；
		findElement(By.xpath(BasicElementLocators.SMALL_APP)).click();
		
		SmallApp sap = new SmallApp();
		//输入appID:
		sap.getSmallAppIdInput().sendKeys(appId);
		logger.info("input appID: " + wrapSingleQuotes(appId));
		//输入appKey:
		sap.getSmallAppKeyInput().sendKeys(appKey);
		logger.info("input appKey: " + wrapSingleQuotes(appKey));
		
		//下一步，且跳转创建成功页面：
		this.finishCreationAndVerify();		
		
		return this;
	}
	
	//创建小应用与“非小应用”程序的公共部分：输入名字 + 选择平台类型 ：
	private void setAppNameAndSelectPlatformType(String appName, AppPlatformsType platformType){
		//创建小应用与“非小应用”程序的公共部分：输入名字 + 选择平台类型 
		logger.info("input application name: " + wrapSingleQuotes(appName));
		WebElement e = waitUntilElement(By.xpath(BasicElementLocators.APP_NAME_INPUT_XPATH), TestContext.getDomTimeout());
		e.clear();
		e.sendKeys(appName);
		
		logger.info("the selected platform: " + wrapSingleQuotes(platformType.name()));
		this.setAppPlatform(platformType);
	}
	
	//创建小应用与“非小应用”程序的公共部分：输入名字 + 选择平台类型 : 下一步 + 判断创建成功：
	private void finishCreationAndVerify(){
		scrollIntoView(getWebDriver().findElement(By.xpath(BasicElementLocators.NEXT_STEP)));
		
		logger.info("click on the Next Step button");
		waitUntilElement(By.xpath(BasicElementLocators.NEXT_STEP), TestContext.getDomTimeout()).click();
		
		//等待进入“创建应用成功”页面，判断“创建应用成功”的状态栏信息是否出现：
		waitUntilElement(By.xpath(ApplicationPageConstants.APPL_CREATE_SUCCESS), TestContext.getDomTimeout());
		
		logger.info("create Application successfully.... "/* + timer.getElapsedTimeString()*/);
	}
	
	/**
	 * Send Email的方法：
	 */
	public void sendSDK2Email(String emailAddr){
		WebElement emailInput = getWebDriver().findElement(By.xpath(ApplicationPageConstants.SEND_SDK_TO_EMAIL_ADDR));
		logger.info("find email input: " + emailInput.toString());
		
		emailInput.sendKeys(emailAddr);
		logger.info("the entered email address: " + emailAddr);
		
		WebElement sendEmailBtn = getWebDriver().findElement(By.xpath(ApplicationPageConstants.SEND_EMAIL_BUTTON));
		//失败的原因是由于该按钮元素处理页面的下半部分，在页面上被遮挡住了，所有在操作这个元素之前，要先把页面的滚动条向下拉动，首先使元素可见，然后点击：
		scrollIntoView(sendEmailBtn);
		
		sendEmailBtn.click();
		waitFor(20);
		logger.debug("SDK sent to: " + emailAddr + " pls check.");
		
	}
	/**
	 * 点击“完成”按钮操作：
	 * @author James Guo
	 */
	public void saveApp(){
		findElement(By.xpath(ApplicationPageConstants.FINISH_BUTTON)).click();
		logger.info("clicked on " +  By.xpath(ApplicationPageConstants.FINISH_BUTTON).toString() + "to save App..");
		
		logger.info("saved successfully, and navigate to Application Center Page...");
		//waitUntilElement(By.xpath(BasicElementLocators.CREATE_NEW_APP_XPATH), TestContext.getDomTimeout());
		waitForPageLoad();
	}
	
	/**
	 * update application：根据应用名称修改 TODO
	 * @throws Exception 
	 */
	public ApplicationPage editApplication(String appName) throws Exception{
		ApplicationCenterPage acp = new ApplicationCenterPage();
		
		ApplicationPage app = acp.navi2AppSetting(appName);
		logger.info("navi to Application Page...");
		
		return this;
	}
	
	/**
	 * delete application: 删除单个应用：
	 */
	ApplicationCenterPage acp = new ApplicationCenterPage();
	public void delApplication(String appName) throws Exception{
		report("the application to be deleted is: " + appName);
		
		acp.navi2AppSetting(appName);
		report("navigate to Application Settings Page");
		
		String locator = "//a[contains(text(),'删除')]";//TODO 需要提取出去
		try{
			WebElement e = waitUntilElement(By.xpath(locator));
			logger.debug("found delete element: '" + e.getText() + "'");

			e.click();
			logger.info("click on Delete button...");

			logger.debug("Alert appears, click on OK to confirm...");
			String alertText = dismissAlert();
			logger.info("text on Alert: " + alertText);
		}catch(Exception e){
			report(ReportUtils.formatError("删除应用失败：[" + e.getMessage() + "] \n" + e.getCause()));
			throw new StopRunningException(e);
		}
	}
	/**
	 * 批量删除：
	 * @throws Exception 
	 */
	public void batchDel(String appBaseName) throws Exception{//locator -> 要删除的一系列应用的基名；
		List<WebElement> eleList = null;
		
		DataTable dt = new DataTable("//div[@id='data-table']");
		int count = dt.getRowCout();
		//String name = dt.getCellData(appBaseName);
		try {
			if(StringUtils.isNotBlank(appBaseName)){
				report("要删除的应用基名/应用名：" + appBaseName);
				//先在查找应用输入框中输入应用的名字，查询出结果，得到结果List，遍历删除：
				WebElement searchInput = findElement(By.xpath(ApplicationPageConstants.APP_SEARCH_INPUT));
				searchInput.clear();
				waitFor(300);
				searchInput.sendKeys(appBaseName);
			}else{
				report(ReportUtils.formatData("未传入要删除的应用基名/名称，将执行默认的 '全部删除' 动作..."));
			}
			waitForAjaxComplete();
			//得到搜索的结果List:
			//eleList = waitUntilElements(By.xpath(ApplicationPageConstants.APPLICATION_LIST_XPATH), TestContext.getDomTimeout());
			eleList = getWebDriver().findElements(By.xpath(ApplicationPageConstants.APPLICATION_LIST_XPATH));
			int i = 0;
			int size = eleList.size();
			report(ReportUtils.formatData("共查找到： " + size + "条应用记录."));
			if(size > 0){
				for(; i < size; i++){
					this.delApplication(appBaseName);
					report(ReportUtils.formatData("成功删除一条应用记录. 还有 ' " + size + "条记录."));
				}
			}else{
				report("未查询出符合条件的应用，请检查...");
			}
		} catch (Exception e) {
			report(ReportUtils.formatError("删除应时失败.. " + "[" + e.getMessage() + "]"));
			ExecutionEngine.result = false;
			throw new Exception(e);
		}
	}
}
