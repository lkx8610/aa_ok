package com.tendcloud.adt.testcases.keywords.unit;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;

import org.testng.annotations.Test;

public class TestKeywordDrivenFile {
	@Test
	public void testFilePath() throws Exception{
		URL url = this.getClass().getClassLoader().getResource("test-data/keyword_driven_data/test.txt");
		
		String filePath = this.getClass().getClassLoader().getResource("test-data/keyword_driven_data/test.txt").getPath();
		
		BufferedReader bf = new BufferedReader(new FileReader(filePath));
		String line = null;
		while((line = bf.readLine()) != null){
			line += line;
			System.out.println(line);
		}
		
	}
}
