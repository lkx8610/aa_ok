package com.tendcloud.adt.widgets;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * 页面中的下拉列表对象：适用于 “推广活动 - 选择产品 & 推广活动组”
 * @author James Guo
 *
 */
public class DropDownList extends AbstractBaseWidget {
	public String dropDownLocator = "";
	
	private DropDownList(){
		super();
	}
	
	//有参构造；初始化、定位组件：
	public DropDownList(String locator){
		this();
		this.dropDownLocator = locator;
	}
	
	//获取DropDownList：
	public WebElement getDropDownList(){
		return waitUntilElement(By.xpath(dropDownLocator));
	}
	
	//推广活动 - 选择产品 - 根据名称选择下拉列表项：
	////*[@id='productDropDown']/ancestor::div[@class='drop-down-result']/following-sibling::ul/descendant::span[contains(text(),'dffdfdf')]
	public WebElement getItemByText(String text){//根据文本内容查找
		String textLocator = "//div[@class='drop-down-result']/following-sibling::ul/descendant::span[contains(text(),'%s')]";
		
		return findElement(By.xpath(String.format(textLocator, text)));		
	}

	/*	public String getCurrentSelect(){
		return getItem(l)
	}*/
	
	/*
	public WebElement getRootItem(){
		return getItem("");
	}*/
	
	//打开DropDownList：
/*	public DropDownList openDropDownList(String locator){
		WebElement e = this.getDropDownList(locator);
		if(null != e){
			e.click();
			
			return this;
		}
		logger.error("could not be able to locate the dropdown list.");
		return null;
		
	}*/
	/*public boolean isDropOpen(){
		String classString = getRootItem().getAttribute("class");
		return classString.contains(with_dropString);
	}*/
	/*public List<WebElement> getDropList(){
		this.openDropDownList(locator);
		return getItems(SelectsString);
	}
	public WebElement getItemSelected(){
		return getItem(selectedString);
	}
	public DropDownList openDropAndSelect(String text){
		openDrop();
		getItemBytext(text);
		return this;
	}*/
}
