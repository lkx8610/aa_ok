package com.tendcloud.adt.testcases.keywords.unit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import framework.keyworddriven.util.KeywordDrivenUtil;

public class TestReg {

	boolean flag = false;
	
	public static void main(String[] args) {
		String pattern = "^[a-zA-Z]{2,5}={1}.{1,}$";
		String s = "xpath=//abc[@class='cdfefee']";
		//Matcher m = Pattern.compile(pattern).matcher(s);
		
		
		
		if(Pattern.matches(pattern, s)){
			System.out.println("Matched");
			
		}else{
			System.out.println("Not Matched");
		}
	}
	
	@Test
	public void ttt(){
		if(flag){
			System.out.println("hello" + flag);
			
		}else{
			System.out.println("hi" + flag);
		}
	}
	@Test
	public  void testtest() throws Exception {
		KeywordDrivenUtil.setExcelFile("推广活动管理\\testCreateOnlineSpreads.xlsx");
	}
}
