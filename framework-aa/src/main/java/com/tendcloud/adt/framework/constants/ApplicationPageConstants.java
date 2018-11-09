package com.tendcloud.adt.framework.constants;

public interface ApplicationPageConstants {
	//产品中心页面：
	String APP_SEARCH_INPUT 	    = "//input[@placeholder='输入关键字进行检索']";//查询应用的输入框；
	String APPLICATION_LIST_XPATH   = "//table/tbody/tr[@class='ng-scope']";//产品中心页面中包含的应用列表
	//选择要检测的产品类型页面：
	String SELECT_APP_TYPE 			= "//div[@id='addProductPage']/descendant::*[contains(text(),'选择您要监测的产品类型')]";

	
	//创建新应用页面元素：
		//Application 状态元素定位信息：
	String APP_STATUS_ONSTOR 		= "//*[@id='publishStatus1']";
	String APP_STATUS_DEVELOPING 	= "//*[@id='publishStatus2']";
	String DOWNLOAD_LINK			= "//*[@id='addProductPage']/descendant::input[last()]";
	
	
	//Save New Application Page:
	String APPL_CREATE_SUCCESS		= "//div[@id='addProductPage']/descendant::h2[contains(text(),'产品创建成功')]";
	String SEND_SDK_TO_EMAIL_ADDR 	= "//div[@id='addProductPage']/descendant::input[@placeholder='请输入邮箱地址']";
	String SEND_EMAIL_BUTTON 		= "//a[contains(text(),'发送')]";
	String FINISH_BUTTON 			= "//a[contains(text(),'完成')]";
	
	
}
