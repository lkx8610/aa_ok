package com.tendcloud.adt.widgets;

public class AbstractBaseDropDownList extends AbstractBaseWidget{

	//public static String locator = "";
		public AbstractBaseDropDownList(){
			super();
		}
		
		//有参构造；初始化、定位组件：
		public AbstractBaseDropDownList(String locator) throws Exception{
			super(locator);
		}
}
