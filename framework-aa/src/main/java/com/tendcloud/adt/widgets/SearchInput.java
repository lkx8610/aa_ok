package com.tendcloud.adt.widgets;

import org.openqa.selenium.By;

import com.tendcloud.adt.pages.common.AbstractBasePage;

/** 
*@description 页面上的“查找”输入框组件 
*@author James Guo
**/
public class SearchInput extends AbstractBasePage{
	private String locator;
	
	private SearchInput(){
		super();
	}
	public SearchInput(String locator) throws Exception{
		super(locator);
		this.locator = locator;
	}
	
	
	public By getSearchInput() {//带参构造，完成元素获取；
		return By.xpath(locator);
		
	}
}
