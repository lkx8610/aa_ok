package com.tendcloud.adt.testcases.keywords.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.Test;


public class TestExecutionEngine {

	@Test
	public void testFilePath(){
		filePath("hello/hello");
	}
	
	private void filePath(String path){
		String pathReg = "^[a-zA-Z]{1}:/{1}.{1,}$";
		Pattern p = Pattern.compile(pathReg);
		Matcher m = p.matcher(path);
		
	
		//boolean b = Pattern.matches("a*b", "aaaaab");
		//Pattern.matches("^[a-zA-Z]{1}:/{1}.{1,}$", path
		
		if(m.matches()){
			System.out.println("match");
		}else{
			System.out.println("mismatch");
		}
	}
}
