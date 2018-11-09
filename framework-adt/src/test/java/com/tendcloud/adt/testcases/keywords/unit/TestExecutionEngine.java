package com.tendcloud.adt.testcases.keywords.unit;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import framework.webdriver.TestNGListener;
import test.base.TestGroups;

@Listeners(TestNGListener.class)
public class TestExecutionEngine {

	/**
	 * 验证：
	 * 	FilePath为绝对路径时
	 * 	FilePath为相对路径时
	 * 	FilePath为非法时（系统找不到指定的文件，此时退出程序，报告的结果置为Failed
	 * @author James Guo
	 */
	@Test(groups=TestGroups.UNIT)
	public void testVerifyFilePath(){
		
	}
	
	String[] filePaths = {
			"C:\\testcases\\推广活动管理\\testBasicTGHDFunc.xlsx",//绝对路径，正确的路径；
			"C:\\testcases\\testBasicTGHDFunc.xlsx",//非示的路径，系统找不到指定文件；
			"testBasicTGHDFunc.xlsx" //相对路径，完整的路径信息：test-data/keyword_driven_data/testBasicTGHDFunc.xlsx
	};
}
