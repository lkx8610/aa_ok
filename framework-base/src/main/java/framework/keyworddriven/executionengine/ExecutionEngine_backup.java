package framework.keyworddriven.executionengine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.ITestResult;
import framework.keyworddriven.BaseKeywords_backup;
import framework.base.LoggerManager;
import framework.base.exception.StopRunningException;
import framework.base.utils.ReportUtils;
import framework.keyworddriven.util.ExcelConstants;
import framework.keyworddriven.util.ExcelUtils;
import framework.keyworddriven.util.KeywordDrivenUtil;
import framework.util.FrameworkUtil;
import framework.webdriver.TestContext;
/**
 * 解析Excel中的测试用例，利用反射方式调用关键字类中定义的关键字；
 * 
 * @author James Guo
 */
public class ExecutionEngine_backup {

	private BaseKeywords_backup keywordsObj;
	private Method methods[];

	private String stepDescription;
	private String keywords;
	private String locatorType;
	private String locator;
	private String testCaseID;
	private String runMode;
	private String testData;
	public static boolean result;// 默认=true;
	
	private int reRuntCounOnFailure = TestContext.getPropertyInt("reRunCountOnFailure");//读配置文件中的重跑次数；
	public static boolean isRerun = false;//标志当前正在执行的动作是不是重跑；
	public static int currentRunCount = 0; //当前已跑的次数；

	private Logger log = LoggerManager.getLogger(ExecutionEngine_backup.class.getSimpleName());

	String excelFile = "";// TestCase的Excel文件路径；
	
	ITestResult wholeTestResult = TestContext.get().getCurrentTestResult(); // 记录整个@Test 用例的测试结果，如果该值是 true,最终的报告结果是Passed，反之，Failed；

	/**
	 * 有参构造，传入Excel文件信息，完成文件初始化，并开始测试；
	 * @author James Guo
	 * @param excelFile
	 * @throws Exception
	 */
	public ExecutionEngine_backup(String excelFile, Class<? extends BaseKeywords_backup> clazz) {
		try {
			/*int index = excelFile.indexOf(":");
			if (index != -1) {*/
			if(StringUtils.isBlank(excelFile)){
				log.info(ReportUtils.formatError("当前测试无效 --- 传入的文件路径不合法：为 '' 或 null. 请检查..."));
				throw new RuntimeException("传入的文件路径非法：" + excelFile);
			}
			if(Pattern.matches("^[a-zA-Z]{1}:{1}.{1,}$", excelFile)) {
				this.excelFile = excelFile;// 直接把绝对路径赋值给路径变量；
			} else {// 相对路径：在固定位置找文件, testcases/ 下
				URL url = this.getClass().getClassLoader().getResource("testcases/" + excelFile);

				if (null != url) {
					this.excelFile = URLDecoder.decode(url.getPath(), "utf-8");
				} else {
					throw new RuntimeException("传入的文件路径不合法，找不到指定的文件: <i><font color='blue'>" +  excelFile  + "</font></i> 请检查....");
				}
			}

			this.startTesting(clazz);
			
		} catch (Exception e) {//如果读文件错误，最终结果也是Failed
			log.info(ReportUtils.formatError(e.getMessage())); 
			wholeTestResult.setStatus(2); //2: Failure;	
		}
	}

	private void startTesting(Class<? extends BaseKeywords_backup> clazz) throws Exception {
		
		keywordsObj = clazz.newInstance();//根据传入的具体Keywords类的类型，生成反射调用对象；

		// 获取当前类和父类中的所有方法；
		methods = KeywordDrivenUtil.getAllMethods(clazz);
		
		try {
			ExcelUtils.setExcelFile(excelFile);
		} catch (Exception e) {
			log.info(ReportUtils.formatError(e.getMessage()));
			//设置文件出错，让报告结果为Failed:
			wholeTestResult.setStatus(2);
			throw new RuntimeException(e);
		}

		try {
			executeTestCase();
		} catch (Exception e) {
			log.info(ReportUtils.formatError(e.getMessage())); 
		}
	}

	// 执行指定的Sheet
	private void executeTestCase() throws Exception {
		
		// 在TestCases的Sheet中, 得到共有多少条Case，；9-14注释；
		int testCaseListInTestCasesSheet = ExcelUtils.getRowCount(ExcelConstants.TestCasesList_Sheet);
		//把当前Sheet中的Case数目记录到TestContext中的“TotalCaseCount”中，在原数上加上此Sheet中的数目：
		int currentCaseCount = TestContext.get().getTotalCaseCount();//先获取当前的测试用例数目，再相加；
		
		TestContext.get().setTotalCaseCount(currentCaseCount + (testCaseListInTestCasesSheet -1));
		
		log.info(ReportUtils.formatData("当前WorkBook中共有：[ " + (testCaseListInTestCasesSheet -1) + " ] 条测试用例"));//日志中记录测试用例的条数；
		
		TestContext.testCaseCountFromExcel = testCaseListInTestCasesSheet -1;//为TestContext设置测试用例总数，用于在报告中显示；

		// 执行测试用例开始 - 遍历Case列表：首行（第0行）是标题行，不计入；
		for (int testcase = 1; testcase < testCaseListInTestCasesSheet; testcase++) {

			result = true;// 每条用例执行前，先重置 result=true;
			// TestCases中的test_case_id 列；9-14注
			testCaseID = ExcelUtils.getCellData(testcase, ExcelConstants.Col_TestCaseID, ExcelConstants.TestCasesList_Sheet);
			// TestCases中的runmode 列；
			runMode = ExcelUtils.getCellData(testcase, ExcelConstants.Col_RunMode, ExcelConstants.TestCasesList_Sheet);
			// yes代表当前有效的case, 如果设置为yes, 则去TestSteps中找相同TestCaseId的TestSteps：
			if (runMode.trim().equalsIgnoreCase("yes")) {

				// 报告中用来分隔各条TestCase名字：
				log.info("<font color='#76E11F'><h3> &nbsp;&nbsp; >>>开始执行第 [<b> " + testcase + " </b>] 条测试用例 - [ " + testCaseID + " ]<<< </h3></font> </br>");
				
				// 这里要求TestCase的名字与TestCasesList中对应的Case的名字一致；
				int testCaseSheetRows = ExcelUtils.getRowCount(testCaseID);//如果传入的测试用例的Sheet不存在，调用方法后返回 -1：
				if(testCaseSheetRows == -1){
					log.error(ReportUtils.formatError("要执行的测试用例Sheet不存在，请检查：[ " + testCaseID + " ]"));//如果返回 -1，则结束当前循环，继续下次。
					result = false;
					
					//将测试结果设置为Failure：2 - Failure;
					wholeTestResult.setStatus(2);
					continue;
				}
				   
				// 测试步骤，第一行为标题，第二行才是真正的测试步骤；
				int testStep = 1;

				// 执行测试用例的步骤开始：
				for (; testStep < testCaseSheetRows; testStep++) {
					result = true; // 每个新步骤开始前，重置为true;
					
					//String stepCellData = ExcelUtils.getCellData(testStep, ExcelConstants.Col_StepNum, testCaseID);
					//if(StringUtils.isNotBlank(stepCellData)){
						//Excel中的" step_description" 列：
						stepDescription = ExcelUtils.getCellData(testStep, ExcelConstants.Col_Step_Description, testCaseID);
						
						//Excel中的" keywords" 列：
						keywords = ExcelUtils.getCellData(testStep, ExcelConstants.Col_Keywords, testCaseID);
						
						//" locator_type "列，存放定位类型，id,name,xpath...：
						locatorType = ExcelUtils.getCellData(testStep, ExcelConstants.Col_Locator_Type, testCaseID);
						
						// " test_data/locator/actual_reault " 列, 存放定位信息、数据、断言前的实际结果值，不设置 locator 返回 Null
						locator = ExcelUtils.getCellData(testStep, ExcelConstants.Col_Locator, testCaseID);
						
						//" varible/expected_result " 列，存放临时变量、断言时的期望结果值，如等所需要的值
						testData = ExcelUtils.getCellData(testStep, ExcelConstants.Col_InputData, testCaseID);
					//}
					

					/**
					 *  开始执行测试步骤中的关键字：
					 */
					 try {
						 executeActions(testStep);
						
					} catch (Throwable e) {//如果异常类型为“StopRunningException”时，则跳出当前用例的执行，去执行下一条用例；
						if (e instanceof StopRunningException || e.getMessage().equals(BaseKeywords_backup.StoppingRunning)) {
							break; 
						}
					}finally{
						if (result) {
							// 给TestStep打结果：
							ExcelUtils.setCellData(excelFile, ExcelConstants.Result_PASS, testStep, ExcelConstants.Col_TestStepResult, testCaseID);
						} else {
							ExcelUtils.setCellData(excelFile, ExcelConstants.Result_FAIL, testStep, ExcelConstants.Col_TestStepResult, testCaseID);
						}
						//重置重跑信息：
						KeywordDrivenUtil.resetTestStatus();
					}
				}
				// 给Tes和CaseList中的每条TestCase打结果：
				if (result) {
					ExcelUtils.setCellData(excelFile, ExcelConstants.Result_PASS, testcase, ExcelConstants.Col_TestResult, ExcelConstants.TestCasesList_Sheet);
				} else {
					ExcelUtils.setCellData(excelFile, ExcelConstants.Result_FAIL, testcase, ExcelConstants.Col_TestResult, ExcelConstants.TestCasesList_Sheet);
					
					/**
					 * 描述：下面的判断针对的是：在生成的报告中，如果有任意一个用例是失败的，则最终显示的结果是 Failed.
					 * 
					 * 如果当前进入当前分支，则说明当前用例执行失败，有任何失败的用例，则将整个WorkBook的测试结果置为“ Failed ”：
					 * Testng在跑第一条Case的时候，ITestResult的 status = STARTED,状态码 = 16，所以要判断状态为STARTED,和状态“isSuccess()"为true的时候，
					 * 如果有失败用例就将用例结果置为 Failed，如果已经有用例将其置为 Failed了，则无需再执行该步骤，提高性能：
					 */
					if(wholeTestResult.getStatus() == 16 || wholeTestResult.isSuccess()){//如果已有失败的用例将结果置为了 false 此处就不再进行设置，只要有一个是失败的，最终报告就会显示 Failed：
						wholeTestResult.setStatus(2); //2: Failure;
					}
				}
				keywordsObj.endTestCase(null, null, null);
				/**
				 * 给执行完的Case打上最后执行时间：last_run 单元格中；
				 * 日期格式：2018/08/07-18:09:44
				 */
				String dateLastRun = FrameworkUtil.dateFormater();
				//将最后执行测试的日期记录到相应的单元格中：
				ExcelUtils.setCellData(excelFile, dateLastRun, testcase, ExcelConstants.Col_Test_Last_Run, ExcelConstants.TestCasesList_Sheet);
			}
		}
	}
	/**
	 * @Description：执行具体关键字的方法，当该关键字执行失败后，会根据配置的重跑次数重跑该关键字，
	 * @author：James Guo
	 * @return: void 
	 */
	private void executeActions(int testStep) throws Exception {
		String currentMethodName = "";// 当前正在调用的方法名
		Throwable throwable = null;
		boolean foundKeyword = false;

		for (int i = 0; i < methods.length; i++) {
			// 判断当前遍历的ActionKeywrods类中的方法是否与读取的Excel中的方法相同；
			if (StringUtils.isNotBlank(keywords.trim())) {//先做关键字的非空判断，否则容易出现空指针异常；
				if(methods[i].getName().trim().equals(keywords.trim())){
					foundKeyword = true;
					// 将定位信息拼接成“locatorType=locator”的形式，之前定位信息全写在了locator列，形式为xpath=xxxx,代码中是以这种形式解析的。
					// 现在将定位类型单独成列，为不修改原来的解析代码，在调用方法执行前，先将定位信息拼接成原来的形式,
					// 如果关键字不需要定位信息，如，wait... , 则在写Case时，将 locator_type 列置空：
					if (null != locatorType) {
						locator = locatorType + "=" + locator;
					}
					currentMethodName = methods[i].getName().trim();// 取出当前执行的方法名称，后边要用；
					try {
						methods[i].setAccessible(true);
						// 反向调用关键字方法，调用时需要传入stepDescription，locator, testData
						methods[i].invoke(keywordsObj, stepDescription, locator, testData);

					} catch (InvocationTargetException e) {// 通过反射调用方法时，会抛出这个异常：InvocationTargetException
						throwable = e.getTargetException();// 得到调用目标方法实际抛出的异常类型；

						throwable.printStackTrace();

					} catch (Throwable t) {
						t.printStackTrace();

						throwable = t;
					} finally {

						if (null != throwable) {// 有异常抛出，判断当前是否满足重跑条件：如果是“准备测试数据”和“删除测试数据”的关键字，则不执行重跑动作：
							boolean isReRuntimesLeft = KeywordDrivenUtil.isReRunTimesLeft(currentRunCount, this.reRuntCounOnFailure, keywords);
							boolean isValidCae = !currentMethodName.equals("prepareTestData") && !currentMethodName.equals("delTestData");
						
							if(isReRuntimesLeft && isValidCae){
								
								currentRunCount++;// 使当前重跑次数+1；

								i--;// 重跑失败的步骤；
								continue;
							} 
							//超过重跑次数，失败
							log.error(ReportUtils.formatError("该步骤重跑 [ " + currentRunCount + " ] 次后失败..."));

							result = false;
							throw new StopRunningException(throwable);
						}
					}
				}
				
			}else{//关键字为Null，跳过：
				log.info(ReportUtils
						.formatError("很抱歉，未找到匹配的关键字 [ <b><i>" + keywords + "</i></b> ], 该步骤被跳过， 请检查该关键字是否已定义或书写是否有误..."));
				ExcelUtils.setCellData(excelFile, ExcelConstants.Result_SKIP, testStep, ExcelConstants.Col_TestStepResult, testCaseID);
			}
		}
		// 循环完成，但未找到关键字, 将当前步骤SKIPPED：
		if (! foundKeyword) {
			keywordsObj.stepInfo("该步骤未能执行，原因：未找到匹配的关键字 [ <b>" + keywords + "</b> ]");// 未找到关键字时，此处需要单独写步骤，否则报告中无步骤信息；

			log.info(ReportUtils
					.formatError("很抱歉，未找到匹配的关键字 [ <b><i>" + keywords + "</i></b> ], 该步骤被跳过， 请检查该关键字是否已定义或书写是否有误..."));
			ExcelUtils.setCellData(excelFile, ExcelConstants.Result_SKIP, testStep, ExcelConstants.Col_TestStepResult, testCaseID);
			
			keywordsObj.increaceStepNum();//跳过关键字要自增步骤；
		}
	}
}
