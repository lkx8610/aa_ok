package com.tendcloud.adt.framework.constants;

public interface TuiGuangHuoDongPageConst {
	String CREATE_NEW_TUIGUANG 		= "//a[contains(text(),'新建推广活动')]";
	String CLOSE_ZHONGDIAN_HD 		= "//i[@class='close']"; //关闭推广活动页面上的“重点推广活动”标签
	String NON_ZHONGDIAN_AD_COUNT	= "//span[@class='number']";//非重点推广活动的数量；
	
	//已有推广活动的列表（Table）：
	String TUIGUANG_LIST			= "//table";
	
	
	
	//推广活动页面：
	String SELECT_PRODUCT_DROPDOWN 	= "//*[@id='productDropDown']";//选择产品的下拉列表对象；
	//根据列表项的名字选择下拉列表项：
	String DROPDOWN_IDTEM 			= "//div[@class='drop-down-result']/following-sibling::ul/descendant::span[contains(text(),'%s')]";
	
	
	
}
