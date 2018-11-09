package com.tendcloud.adt.pages.application;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tendcloud.adt.framework.constants.BasicElementLocators;
import com.tendcloud.adt.pages.common.AbstractBasePage;

public class SmallApp extends AbstractBasePage{

	// 选择“小程序”时的二个输入框：小程序ID、小程序密钥：
	@FindBy(xpath = BasicElementLocators.SAMLL_APP_ID_INPUT)
	private WebElement samllAppId;
	@FindBy(xpath = BasicElementLocators.SMALL_APP_KEY_INPUT)
	private WebElement smallAppKey;

	/**
	 * 获取选择小程序时出现的二个输入框：
	 */
	public WebElement getSmallAppIdInput(){
		
		//WebElement smallAppID = waitUntilElement(By.xpath(locator));
		
		return samllAppId == null ? null : samllAppId;
	}
	/**
	 * 获取小程序密钥输入框：
	 */
	public WebElement getSmallAppKeyInput(){
		//WebElement smallAppKey = waitUntilElement(By.xpath(locator));
		
		return smallAppKey == null ? null : smallAppKey;
	}
	
	/**
	 * 测试点击小程序类型时，二个链接的可用性：
	 */
	
	public void testIdLink(){
		
	}
	public void testKeyLink(){
		
	}
}










