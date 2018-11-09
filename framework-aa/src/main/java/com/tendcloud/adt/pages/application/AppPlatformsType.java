package com.tendcloud.adt.pages.application;

public enum AppPlatformsType {
	IOS("iso"),
	ANDROID("android"),
	HTML5("html5"),
	SMALLAPP("smallapp");

	private String name;
	AppPlatformsType(String name) {
		this.name = name;
	}
}
