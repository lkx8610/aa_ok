package com.tendcloud.adt.widgets;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import com.tendcloud.adt.pages.ActiveDebugPage;
import com.tendcloud.adt.pages.common.AbstractBasePage;

import framework.base.LoggerManager;

/** 
*@description 处理激活调试的浮动框或弹出窗口，在登录系统后和创建应用成功后弹出的“激活功能上线啦！”的弹出框。 
*			 	 1. 定位元素；
*			 	 2. 处理弹出窗口：/不再提示/下次再说/立即开始
*@author James Guo
**/
public class ActivationTipBox extends AbstractBasePage{
	String title;
	
	Logger logger = LoggerManager.getLogger(Thread.currentThread().getClass().getName());
	
	//以下Locator先写死在这里，如果页面元素不再改变就这样，如果有改变的可能，就写在外部文件中：TODO
	String tipBoxLocator = "//div[@class='new-remind-background show']";
	String remindNextTimeLoc = "//div[@class='buttons']/button[contains(text(), '下次再说')]";
	String startNowLoc = "//div[@class='buttons']/button[contains(text(), '立即开始')]";
	String neverRemindLoc = "//div[@class='not-remind']/input";
	
	/**
	 * 判断提示框是否出现；
	 */
	public boolean isActiveTipBoxShown(){
		//防止页面刷新而使元素失效，连续尝试3次；
		WebElement activationBox = retryIfPageRefreshed(By.xpath(tipBoxLocator), 2);
		
		return (null != activationBox);
	}
	/*
	 *获取提示框； 
	 */
	public WebElement getActiveTipBox(){
		
		if(isActiveTipBoxShown()){
			logger.info("located the float box with: " + tipBoxLocator );
			return findElement(By.xpath(tipBoxLocator));
		}else {
			logger.error("can not locate the FLOAT BOX by locating with: " + tipBoxLocator);
			return null;
		}
	}
	//通过改变元素class的属性值来使得提示框消失：TODO
	public void dismissTipBox(){
		if(isActiveTipBoxShown()){
			WebElement e = getActiveTipBox();
			((JavascriptExecutor)getWebDriver()).executeScript("", e);
		}
	}
	//针对“下次提醒”的功能的方法：
	public void remindeNextTime(){
		if(isActiveTipBoxShown()){
			WebElement e = findElement(By.xpath(remindNextTimeLoc));
			logger.debug("find Remind Next Time element...");
			waitFor(100);
			logger.debug("sleep 100ms");
			e.click();
		}
	}
	//不再提醒的Checkbox：
	public void neverRemind(){
		if(isActiveTipBoxShown()){
			WebElement checkBox = findElement(By.xpath(neverRemindLoc));
			logger.debug("find element: " + checkBox.getText());
			checkBox.click();
			logger.info("dismiss the Tip Box, and will never remind again...");
		}
	}
	//马上激活：
	public ActiveDebugPage startNow(){
		try{
			waitUntilElement(By.xpath(startNowLoc)).click();
		}catch(Exception e){
			e.printStackTrace();
		}
		return new ActiveDebugPage();
	}

}








