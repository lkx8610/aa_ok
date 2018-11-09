package com.tendcloud.adt.pages.application;


import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.tendcloud.adt.framework.base.AdtTestBase;
import com.tendcloud.adt.widgets.LeftSideBar;

import framework.base.LoggerManager;

/** 
*@description 应用详情页面，包含左侧栏 
*@author James Guo
**/
public class ApplicationDetailsPage extends AdtTestBase{
	Logger logger = LoggerManager.getLogger(ApplicationDetailsPage.class.getSimpleName());
	public ApplicationDetailsPage(){
		
	}
	
	/*
	 * 通过点击“左侧栏”的链接进入到推广活动页面：
	 */
	public TuiGuanHuoDongPage getTuiGuanHuoDongPage(){
		WebElement ele = getWebDriver().findElement(By.xpath(LeftSideBar.TUIGUANGHUODONG.name));
		logger.debug("found element: '" + ele + "'");
		
		ele.click();
		logger.debug("clicked on '" + ele + " ");
		
		return new TuiGuanHuoDongPage();
	}
	
}
