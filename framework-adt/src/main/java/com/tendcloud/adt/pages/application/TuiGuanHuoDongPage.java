package com.tendcloud.adt.pages.application;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.tendcloud.adt.framework.constants.TuiGuangHuoDongPageConst;
import com.tendcloud.adt.pages.common.AbstractBasePage;
import com.tendcloud.adt.widgets.DataTable;

import framework.base.LoggerManager;
import framework.webdriver.TestContext;

public class TuiGuanHuoDongPage extends AbstractBasePage{
	@FindBy(xpath=TuiGuangHuoDongPageConst.CREATE_NEW_TUIGUANG)
	WebElement createNewBtn;

	Logger logger = LoggerManager.getLogger(TuiGuanHuoDongPage.class.getSimpleName());
	
	public enum AdType{//广告类型枚举
		TEXT_AD("text advertisement type"),
		INFO_STREAM_AD("information stream type");
		
		
		public String adType;
		
		private AdType(String adType){
			this.adType = adType;
		}
		public String getAdType(){
			
			return this.adType;
		}
	}
	
	/**
	 * 获取该应用已有的推广活动：TODO
	 * @throws Exception 
	 */
	
	public List<WebElement> getTuiGuangHuoDongList() throws Exception{
		DataTable dt = new DataTable(TuiGuangHuoDongPageConst.TUIGUANG_LIST);
		
		List<WebElement> dataList = dt.getTableElements();
		
		if(null == dataList)
			return null;
		
		return dataList;
	}
	/**
	 * 创建推广活动：
	 */
	public TuiGuanHuoDongPage createTuiGuangHuoDong(){
		
		return this;
	}
	/**
	 * 关闭重点广告推广活动：
	 */
	public void closeImportantAdHd(String locator){//传入重点推广活动的“X”号的XPath来关闭；
		waitUntilElement(By.xpath(locator)).click();
	}
	//获取非重点推广活动的数量：
	public int getNonImportHdCount() throws Exception{
		List<WebElement> eleList = waitUntilElements(By.xpath(TuiGuangHuoDongPageConst.
								NON_ZHONGDIAN_AD_COUNT), TestContext.getDomTimeout());
		logger.debug("there're '" + eleList.size() + "' none zhondian tui guang huo dong.");
		
		return eleList.size();
	}
}
























