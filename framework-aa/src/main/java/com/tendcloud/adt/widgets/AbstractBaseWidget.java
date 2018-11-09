package com.tendcloud.adt.widgets;

import com.tendcloud.adt.pages.common.AbstractBasePage;
public class AbstractBaseWidget extends AbstractBasePage {

	public AbstractBaseWidget() {
		super();// 页面元素定位及Webdriver初始化
	}

	public AbstractBaseWidget(String elementId) throws Exception {
		super(elementId);
	}
}
