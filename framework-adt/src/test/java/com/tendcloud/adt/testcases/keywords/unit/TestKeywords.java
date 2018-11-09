package com.tendcloud.adt.testcases.keywords.unit;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import framework.keyworddriven.BaseKeywords;

public class TestKeywords extends BaseKeywords{

	@Test
	public void testAssertListSizeEquals(){
		List<String> list = new ArrayList<>();
		list.add("aaa");
		list.add("bbb");
		String key = "key";
		
		tempValueMap.put(key, list);
		
		//BaseKeywords bk = new BaseKeywords();
		String stepDes = "xxx";
		String locator = "ke";
		String testData = "num";
		
		try {
			assertListSizeEquals(stepDes, locator, testData, "");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
