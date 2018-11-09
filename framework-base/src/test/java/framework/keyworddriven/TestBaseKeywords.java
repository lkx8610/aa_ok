package framework.keyworddriven;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import framework.base.anotation.TestDoc;
import test.base.TestGroups;

public class TestBaseKeywords {
	
	BaseKeywords baseKeywords = null;
	
	@BeforeTest
	public void setUp(){
		baseKeywords = new BaseKeywords();
	}
	
	/**
	 * 测试创建随机字符串方法：由于方法无返回值，需要做Mock
	 * @author James Guo
	 */
	@Test(groups=TestGroups.UNIT)
	@TestDoc(testObjective="验证生成随机字符串的功能是否正常...")
	public void testCreateRandomStr(){
		baseKeywords.createRandomStr(null, null, null, null);
	}
	/**
	 * 断言Null
	 * @author James Guo
	 * @throws Exception 
	 */
	@Test(groups=TestGroups.UNIT)
	public void testAssertNull() throws Exception{
		baseKeywords.assertNull(null, "hello", null, null);
		baseKeywords.assertNull(null, "//div[@class='abc'", null, null);
	}
	/**
	 * 断言非空
	 * @author James Guo
	 */
	@Test
	public void testAssertNotNull(){
		
	}
}
