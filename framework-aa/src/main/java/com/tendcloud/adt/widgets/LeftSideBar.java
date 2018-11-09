package com.tendcloud.adt.widgets;

public enum LeftSideBar {
	//页面上的左侧边栏：推广概览、推广活动。。。
	TUIGUANGGAILAN("//div[@class='leftMenu']/descendant::span[@class='icon-TuiGuangGL']"),
	TUIGUANGHUODONG("//*[@class='icon-TuiGuangHD']']"),
	FENBAOTONGJI("//*[@class='icon-package']"),
	FENQUIFENXI("//*[@class='icon-FenQunHD']"),
	YINGYONGPAIMING("//*[@class='icon-app-ranking']"),
	ZUOBIFANGHU("//*[@class='icon-zbfh']"),
	RIZHIDAOCHU("//*[@class='icon-rzdc']"),
	EASYLINK("//*[@class='icon-eLink']");
				
	public String name;
	LeftSideBar(String name){
		this.name = name;
	}
	String getName(){
		return name;
	}
}
