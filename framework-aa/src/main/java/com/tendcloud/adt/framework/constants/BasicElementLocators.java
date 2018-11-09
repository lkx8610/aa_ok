package com.tendcloud.adt.framework.constants;

public interface BasicElementLocators {
	
	String Basi_Url = 				"http://172.20.7.2:10005/webapp/index/login.html";
	
	//任何页面返回“产品中心”的链接：
	String TO_PRODUCT_CENTER 		= "//div[@class='top']//strong | //a[@class='logo_link']/following-sibling::a/descendant::* | //a[contains(text(),'to home')]/following-sibling::a/strong";
	String TO_PRODUCT_CENTER_LOGO 	= "//a[@class='logo_link']";

	//login page:
	String USERNAME_INPUT_XPATH 	= "//input[@name='user']";//用户名
	String PASSWORD_INPUT_XPATH 	= "//input[@name='password']";//密码
	String LOGIN_BUTTON_XPATH 		= "//div[@class='button_login']/a";//登录按钮
	String REMEMBER_PASS_XPATH 		= "//div[@class='remeber']/descendant::input";//记住密码Checkbox
	String REGISTER_LINK_XPATH		= "//div[@class='regist_now']/a";//注册链接
	
	//产品中心 page:
	String TITLE_XPATH				= "";
	//String TO_HOME_XPATH			= "//div[@class='top']/span/a[1]";//判断登录成功的标志是这个图标显示出来
	String APPLICATION_CENTER_XPTH	= "//div[@class='main']/descendant::a[1]";//产品中心
	String MANAGEMENT_CENTER_XPATH	= "//div[@class='main']/descendant::a[2]";//管理中心
	String CREATE_NEW_APP_XPATH		= "//div[contains(@class, 'btn-add')]/a[1]";//创建新应用按钮
	String SEARCH_INPUT_XPATH		= "//div[@class='chooseTime']/input";//应用搜索框；	
	//今日、昨日、近七日
	String TODAY_XPATH				= "//div[@class='chooseTime']/descendant::li[1]";//今日
	String YESTERDAY_XPATH 			= "//div[@class='chooseTime']/descendant::li[2]";//昨日
	String LAST7DAYS_XPATH			= "//div[@class='chooseTime']/descendant::li[last()]";//近7天
	String APPLICATION_LIST_XPATH   = "//table/tbody/tr[@class='ng-scope']";//产品中心页面中包含的应用列表
	
	//产品 page:
	//应用类型：Game/电商/其它
	String GAME_XPATH				= "//div[@id='addProductPage']/descendant::li/h3[contains(text(), '游戏')]";
	String ECOMERCIAL_XPAHT			= "//div[@id='addProductPage']/descendant::li/h3[contains(text(), '电商')]";
	String OTERS_XPATH 				= "//div[@id='addProductPage']/descendant::li/h3[contains(text(), '其它')]";
	//应用平台：
	String ISO_XPATH 				= "//*[@id='platform1']";
	String ANDROID_XPATH			= "//*[@id='platform2']";
	String HTML5_XPATH				= "//*[@id='platform3']";
	String SMALL_APP				= "//h5[contains(text(),'小程序')]";
	//创建应用与非小应用程序时的“下一步”按钮，完成创建：
	String NEXT_STEP				= "//a[contains(text(), '下一步')]";
	
	//创建应用时输入的应用名称输入框（所有应用公用的，对于小程序与非小程序都适用）：
	String 	APP_NAME_INPUT_XPATH	= "//div[@id='addProductPage']/descendant::input[@placeholder='请输入应用名称']";
	String DOWNLOAD_LINK_XPATH		= "//div[@id='addProductPage']/descendant::input[last()]";
	
	String SMALL_APP_XPATH			= "//*[@id='platform4']";
	
	//选择小应用时多出的二个输入框：
	String SAMLL_APP_ID_INPUT		= "//input[@placeholder='请输入小程序ID']";
	String SMALL_APP_KEY_INPUT		= "//input[@placeholder='请输入小程序密钥']";
	String SAMLL_APP_FINISH_BTN		= "//a[contains(text(),'完成')]";
	
	
	

}
