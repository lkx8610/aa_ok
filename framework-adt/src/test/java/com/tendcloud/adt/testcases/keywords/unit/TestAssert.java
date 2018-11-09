package com.tendcloud.adt.testcases.keywords.unit;

import static framework.base.Verify.*;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class TestAssert {

	@Test
	public void vE(){
		
		String a = "精确_002";
		String b = "精确_002";
		
		verifyEquals(b, a, "");
	}
}
