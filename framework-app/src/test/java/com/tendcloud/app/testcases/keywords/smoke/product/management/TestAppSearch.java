package com.tendcloud.app.testcases.keywords.smoke.product.management;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import base.AATestBase;
import framework.base.anotation.TestDoc;
import framework.keyworddriven.BaseKeywords;
import framework.keyworddriven.executionengine.ExecutionEngine;
import framework.webdriver.TestNGListener;
import test.base.TestGroups;

//@Listeners(TestNGListener.class)
public class TestAppSearch extends AATestBase{
	@Test(groups=TestGroups.SMOKE)
//	@TestDoc(testCaseID="T497088", testObjective="测试应用的查询功能：精确查询 + 模糊查询 + 无符合条件的记录...")
	public void testAppSearch() throws Exception{
		String file = "d:/testAppSearch.xlsx";
		
		new ExecutionEngine(file, BaseKeywords.class);
	}
}
