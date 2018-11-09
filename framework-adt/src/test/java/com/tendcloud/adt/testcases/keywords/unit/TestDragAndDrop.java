package com.tendcloud.adt.testcases.keywords.unit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.tendcloud.adt.framework.base.AdtTestBase;

import framework.webdriver.TestNGListener;

@Listeners(TestNGListener.class)
public class TestDragAndDrop extends AdtTestBase{
	@Test
	public void test() throws Exception{
		WebDriver driver = getWebDriver();
		driver.get("http://debug.tenddata.com");
		waitForPageLoad();
		getElement("xpath=//input[@placeholder='用户名']").sendKeys("aeplus@talkingdata.com");
		
		getElement("xpath=//input[@placeholder='密码']").sendKeys("a12345");
		
		getElement("xpath=//*[text()='登录']").click();
		
		waitFor(1000);
		getElement("xpath=//a[contains(text(),'查看')]").click();
		
		waitFor(1000);
		
		getElement("xpath=//*[text()='营销']").click();
		getElement("xpath=//*[@title='活动管理']").click();
		
		waitFor(1000);
		getElement("xpath=/html/body/app-root/main/app-activity-center/div[2]/button[2]").click();
		
		getElement("xpath=//input[@placeholder='请输入活动名称']").sendKeys("hello");
		
		getElement("xpath=//input[@class='ant-calendar-range-picker-input ng-star-inserted'][1]").click();
		
		
		Actions ac = (Actions)driver;
		//选择日期
		WebElement e = getElement("xpath=//*[@id='cdk-overlay-6']/div/date-range-popup/div/div/div/div/div[1]/div/inner-popup/div/date-table/table/tbody/tr[5]/td[4]/div");
		//双击选中
		ac.doubleClick(e).perform();
		
		getElement("xpath=//button//span[contains(text(),'确定')]").click();
		waitFor(1000);
		
		getElement("xpath=//*[contains(text(),'权益配比')]/preceding-sibling::span/button").click();
		
		waitFor(1000);
		//拖放元素
		WebElement ee = getElement("xpath=//*[contains(text(),'应用推送')]/preceding-sibling::img");//源
		WebElement target = getElement("xpath=//tspan[contains(text(),'入口')]");
		
		ac.dragAndDrop(ee, target).release().build().perform();
		
		
		
		
		
	}
	
	
}
