package framework.keyworddriven.util;

public class ExcelConstants {
	
	public static final String Result_FAIL = "FAILED";
	public static final String Result_PASS = "PASSED";
	public static final String Result_SKIP = "SKIPPED";//如果未找与之匹配的关键字，就跳过该步（可能是关键字写错了）
	
	public static final int Col_TestCaseID = 0;//Sheet TestCases和TestSteps中 test_case_id所在的列（第一列）；
	public static final int Col_TestStepID = 1;//测试用例 Sheet中的test_step_id
	public static final int Col_RunMode = 2;//TestCases Sheet中的是否Run所在的列
	public static final int Col_TestResult = 3;//Sheet TestCases 测试结果所在的列；
	public static final int Col_Test_Last_Run = 4;//最后执行测试的日期
	/**
	 * 测试用例表格中的各列:
	 */
	public static final int Col_StepNum = 1;
	public static final int Col_Step_Description = 2;
	public static final int Col_Keywords = 4;
	public static final int Col_Locator_Type = 5;
	public static final int Col_Locator = 6;
	public static final int Col_InputData= 7;
	public static final int Col_TestStepResult = 8;
	
	// TestCases和TestSteps二个Sheet：
	public static final String TestCasesList_Sheet = "TestCasesList";
}
