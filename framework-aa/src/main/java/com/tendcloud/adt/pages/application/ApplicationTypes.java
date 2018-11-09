package com.tendcloud.adt.pages.application;

public enum ApplicationTypes {
	//选择创建的App的类型：
	GAME("game"),
	ECOMERCIAL("ecomercial"),
	OTHERS("others");
	
	private String name;
	ApplicationTypes(String name){
		this.name = name;
	}
}
