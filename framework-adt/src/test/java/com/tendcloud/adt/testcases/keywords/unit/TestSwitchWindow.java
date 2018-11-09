package com.tendcloud.adt.testcases.keywords.unit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.tendcloud.adt.framework.base.AdtTestBase;
import com.tendcloud.adt.pages.common.LoginPage;

import framework.webdriver.TestNGListener;

@Listeners(TestNGListener.class)
public class TestSwitchWindow extends AdtTestBase {

	@Test
	public void testSwitchWindow() throws Exception{
		WebDriver driver = getWebDriver();
		LoginPage p = new LoginPage();
		p.basicLogin();
		
		String windowName = driver.getWindowHandle();
		System.out.println(windowName);
		
		WebElement e = getElement("xpath=//input[@placeholder='输入关键字进行检索']");
		e.clear();
		e.sendKeys("ADT-推广活动-IOS");
		
		WebElement e1 = getElement("xpath=//a[contains(text(),'设置')] ");
		e1.click();
		
		WebElement e2 = getElement("xpath=//*[contains(text(),'推广活动管理')]");
		e2.click();
		
		WebElement e3 = getElement("xpath=//*[contains(text(),'新建推广活动')]");
		e3.click();
		
		String windowName1 = driver.getWindowHandle();
		System.out.println(windowName1);
		
		WebElement e4 = getElement("xpath=//input[@placeholder='请输入推广活动名称']");
		e4.sendKeys("自定义渠道...");
		
		WebElement e5 = getElement("xpath=//label[contains(text(),'推广渠道')]");
		e5.click();
		
		WebElement e6 = getElement("xpath=//a[contains(text(),'自定义渠道')]");
		e6.click();
		
		String windowName2 = driver.getWindowHandle();
		System.out.println(windowName2);
		
		WebElement e7 = getElement("xpath=//*[@id='channelname']");
		e7.sendKeys("渠道名称：自定义");
		
		
		
	}
}








