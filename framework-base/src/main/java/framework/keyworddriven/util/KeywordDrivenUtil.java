package framework.keyworddriven.util;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import framework.base.LoggerManager;
import framework.base.utils.ReportUtils;
import framework.keyworddriven.domain.TestCaseListBean;
import framework.keyworddriven.domain.TestCaseStepBean;
import framework.keyworddriven.executionengine.ExecutionEngine;
import framework.util.FrameworkUtil;
import framework.webdriver.TestContext;

public class KeywordDrivenUtil {
	private static Logger log = LoggerManager.getLogger(KeywordDrivenUtil.class.getSimpleName());
/**
 * 设置读入的Excel测试文件：
 * @author James Guo
 * @param excelFile
 * @throws Exception
 */
	public static String setExcelFile(String excelFile) throws Exception{

		if(StringUtils.isBlank(excelFile)){
			log.info(ReportUtils.formatError("当前测试无效 --- 传入的文件路径不合法：为 '' 或 null. 请检查..."));
			throw new RuntimeException("传入的文件路径非法：" + excelFile);
		}
		
		//文件为相对路径，补全地址在配置文件中，如果是绝对路径，则直接设置文件：
		if(! Pattern.matches("^[a-zA-Z]{1}:{1}.{1,}$", excelFile)) {
			
			URL url = KeywordDrivenUtil.class.getClassLoader().getResource("testcases/" + excelFile);

			if (null != url) {
				excelFile = URLDecoder.decode(url.getPath(), "utf-8");
			} else {
				throw new RuntimeException("传入的文件路径不合法，找不到指定的文件: <i><font color='blue'>" +  excelFile  + "</font></i> 请检查....");
			}
		} 
		//设置Excel File，得到WookBook对象：
		ExcelUtils.setExcelFile(excelFile);
		
		return excelFile;
	}
	
	/**
	 * 封装Excel中的TestCaseList表格数据；
	 * 封装完成的结构：{login=TestCaseListBean@xxx，XXX=TestCasListBean@xxx}
	 * 	封装依据：
	 * 		1、遍历“TestCaseLists”的Sheet中的所有的Case项；
	 * 		2、取出“RunMode=‘Yes’”的Case，封装成TestCaseListBean对象；
	 * 		3、将2中封装的对象，再封装到以key=testCaseID,value=TestCaseListBean的Map中返回；
	 * 		4、最终返回的Map中，只包含“RunMode=Yes”的Case,其它的不封装；
	 * @author James Guo
	 * @return Map<String, TestCaseListBean>
	 * @throws Exception 
	 */
	public static Map<String, TestCaseListBean> geTestCaseListBean() throws Exception{
		Map<String, TestCaseListBean> map = new LinkedHashMap<>();
		
		TestCaseListBean bean = null;
		
		// 在TestCases的Sheet中, 得到共有多少条Case，；
		int testCaseListInTestCasesSheet = ExcelUtils.getRowCount(ExcelConstants.TestCasesList_Sheet);
		
		//为TestContext设置测试用例总数，用于在报告中显示；
		TestContext.testCaseCountFromExcel = testCaseListInTestCasesSheet -1;
		
		for(int i=1; i<testCaseListInTestCasesSheet; i++){//TestCaseLists中，首先不计；
			String testCaseID = ExcelUtils.getCellData(i, ExcelConstants.Col_TestCaseID, ExcelConstants.TestCasesList_Sheet);
			String runMode = ExcelUtils.getCellData(i, ExcelConstants.Col_RunMode, ExcelConstants.TestCasesList_Sheet);
			
			if(runMode.equalsIgnoreCase("yes")){//当RunMode=Yes时才封装进Map，否则不封装；
				bean = new TestCaseListBean();
				bean.setRunMode(runMode);
				bean.setTestCaseID(testCaseID);
				bean.setTestCaseNumInSheet(i);//设置该Case所有表格的实际行数，打结果时用；
				
				map.put(testCaseID, bean);
			}
		}
		return map;
	}
	/**
	 * 封装Excel中的测试用例Sheet数据为TestCaseStepsBean表格数据:
	 * 	封装依据：
	 * 		1、封装TestCaseLists中标记为RunMode=‘Yes’的Case；
	 * 		2、将具体的包含测试用例的Sheet中的每个步骤，封装成一个TestCaseStepBean；
	 * 		3、将sheet中的封装的多个步骤的TestCaseStepBean，再封装进List，形成一个包含当前Sheet中所有测试步骤的TestCaseStepBean的List；
	 * 		4、最后，将TestCaseStepBean的列表再封装进以key=test_case_id(sheetName), value=TestcaseBean List的Map中；
	 * @author James Guo
	 * @return Map<String, List<TestCaseStepBean>>
	 * @throws Exception 
	 */
	public static Map<String, List<TestCaseStepBean>> getTestCaseStepsBean() throws Exception {
		// 1、先调用geTestCaseListBean()，得到要封装的TestCase(只包含RunMode=Yes的Case):
		Map<String, TestCaseListBean> testCaseListMap = geTestCaseListBean();

		// 用来存放最终的Sheet中步骤对象列表的Map：
		Map<String, List<TestCaseStepBean>> testStepsMap = new LinkedHashMap<>();

		List<TestCaseStepBean> list = null;
		TestCaseStepBean bean = null;

		// 2、遍历1中取出的Case对象，再依次取出其“testCaseId”，依据该testCaseId，去找具体的Sheet,testCaseId即SheetName：
		Iterator<String> it = testCaseListMap.keySet().iterator();

		while (it.hasNext()) {
			String testCaseId = it.next();
			list = new LinkedList<>();//存放同一个Sheet中各个步骤的List；
			// 获取该Sheet中的步骤数：
			int stepCounts = ExcelUtils.getRowCount(testCaseId);
			for (int j = 1; j < stepCounts; j++) {// j=0:标题行；遍历当前Case的每个Step，封装成ExcelTestStepBean,放入List
				//记录该步骤前边的“步骤号”：
				String colStepNum = ExcelUtils.getCellData(j, ExcelConstants.Col_StepNum, testCaseId);
				
				String stepDescription = ExcelUtils.getCellData(j, ExcelConstants.Col_Step_Description, testCaseId);

				// Excel中的" keywords" 列：
				String keywords = ExcelUtils.getCellData(j, ExcelConstants.Col_Keywords, testCaseId);

				// " locator_type "列，存放定位类型，id,name,xpath...：
				String locatorType = ExcelUtils.getCellData(j, ExcelConstants.Col_Locator_Type, testCaseId);

				// " test_data/locator/actual_reault " 列,
				// 存放定位信息、数据、断言前的实际结果值，不设置 locator 返回 Null
				String locator = ExcelUtils.getCellData(j, ExcelConstants.Col_Locator, testCaseId);

				// " varible/expected_result " 列，存放临时变量、断言时的期望结果值，如等所需要的值
				String actualResult = ExcelUtils.getCellData(j, ExcelConstants.Col_InputData, testCaseId);

				bean = new TestCaseStepBean();

				bean.setTestCaseID(testCaseId);
				bean.setColStepNum(colStepNum);
				bean.setStepDescription(stepDescription);
				bean.setKeywords(keywords);
				bean.setLocatorType(locatorType);
				bean.setLocator(locator);
				bean.setActualResult(actualResult);// test_data列；

				list.add(bean);// List中存放每个步骤Bean实例
			}
			testStepsMap.put(testCaseId, list);// 以key=testcaseid, value=List<TestCaseStepBean>
		}
		return testStepsMap;
	}
	
	/**
	 * 获取TestCaseStepBean的步骤值：
	 * @author James Guo
	 * @throws Exception
	 */
	public static Map<String, String> getTestCaseStepInfo(TestCaseStepBean stepBean){
		Map<String, String> stepsInfoMap = new LinkedHashMap<>();
		
		String sheetName = stepBean.getTestCaseID();
		stepsInfoMap.put("sheetName", sheetName);
		String stepDescription = stepBean.getStepDescription();
		stepsInfoMap.put("stepDescription", stepDescription);
		String keyword = stepBean.getKeywords();
		stepsInfoMap.put("keyword", keyword);
		String locatorType = stepBean.getLocatorType();
		stepsInfoMap.put("locatorType", locatorType);
		String locator = stepBean.getLocator();
		stepsInfoMap.put("locator", locator);
		String expectedResult = stepBean.getExpecteResult();
		stepsInfoMap.put("expectedResult", expectedResult);
		String actualResult = stepBean.getActualResult();
		stepsInfoMap.put("actualResult", actualResult);
		String comments = stepBean.getComments();
		stepsInfoMap.put("comments", comments);
		
		return stepsInfoMap;
	}
	/**
	 * 为Testcase中的步骤打结果：
	 */
	public static void setStepResult(String excelFile, boolean result, int stepNum, String sheetName){
		
		 try {
			 if(result){//执行完成，未抛异常，当前步骤结果置为“Passed”：
				 ExcelUtils.setCellData(excelFile,ExcelConstants.Result_PASS, stepNum,ExcelConstants.Col_TestStepResult, sheetName);
			 }else{
				 ExcelUtils.setCellData(excelFile,ExcelConstants.Result_FAIL, stepNum,ExcelConstants.Col_TestStepResult, sheetName);
			 }
			
		} catch (Exception e) {
			log.warn(ReportUtils.formatWarn("[" + sheetName + "]-[" + stepNum + "] 未成功标记测试结果，请检查..."));
			e.printStackTrace();
		}
		
	}
	/**
	 * 为TestCaseLists中的Testcase打结果：
	 * @author：James Guo
	 * @param excelFile
	 * @param testCaseId
	 * @param result
	 */
	public static void setTestCaseResult(String excelFile, String testCaseId, boolean result) {
		// 得到该条用例在TestCaseLists的Sheet中的实际行数：
		int testCaseRowInTestCaseList = getTestCaseRowInTestCaseListSheet(excelFile, testCaseId);
		// 给Tes和CaseList中的每条TestCase打结果：
		try {
			if (result) {
				ExcelUtils.setCellData(excelFile, ExcelConstants.Result_PASS, testCaseRowInTestCaseList, ExcelConstants.Col_TestResult, ExcelConstants.TestCasesList_Sheet);
			} else {
				ExcelUtils.setCellData(excelFile, ExcelConstants.Result_FAIL, testCaseRowInTestCaseList, ExcelConstants.Col_TestResult, ExcelConstants.TestCasesList_Sheet);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.debug(ReportUtils.formatWarn("未能成功标记测试结果：" + testCaseId));
		}
	}
	/**
	 * 防止出现在Excel的步骤中，选择了LocatorType，但未填写Locator信息时出现错误，此处处理方式是，跳过该步，给出提示信息：
	 * @author James Guo
	 * @param excelFile
	 * @param bean
	 * @param stepNum
	 * @return
	 */
	public static boolean checkLocatorFormat(String excelFile, TestCaseStepBean bean, int stepNum){
		if (StringUtils.isNotBlank(bean.getLocatorType()) && StringUtils.isBlank(bean.getLocator())) {
			// 选择了LocatorType,但locator未填写
			log.warn(ReportUtils.formatWarn("[" + bean.getStepDescription() + "]选择了locatorType，但locator为空，请检查是否影响测试运行结果... "));
			skipTestStep(excelFile, bean.getStepDescription(), stepNum, bean.getTestCaseID(), 1);// 跳过该步；
			
			return false;
		}
		return true;
	}
	/**
	 * 将指定的Step标记为“SKIPPED”：
	 * 	flag=1: 标记了locatorType, 但未填写locator
	 * 	flag=2: 未匹配到关键字而跳过的步骤
	 * 	flag=3: 步骤前边没有步骤Num，不计入步骤数而跳过，一般是我们的描述信息
	 */
	public static void skipTestStep(String excelFile, String stepDescription, int stepNum, String sheetName, int flag) {
		try {
			switch (flag) {
			case 1://标记了locatorType, 但未填写locator
				log.warn(ReportUtils.formatWarn("步骤：[ " + stepDescription + " ] 选择了locatorType，但locator为空，该步骤被跳过，请检查... "));
				logSkipResult(excelFile, stepDescription, stepNum, sheetName);
				break;
			case 2://未匹配到关键字而跳过的步骤
				log.warn(ReportUtils.formatWarn("步骤：[ " + stepDescription + " ] 未找到关键字或未设置关键字，该步骤被跳过，请检查... "));
				logSkipResult(excelFile, stepDescription, stepNum, sheetName);
				break;
			case 3://步骤前边没有步骤Num，不计入步骤数而跳过，一般是我们的描述信息
				log.warn(ReportUtils.formatWarn("步骤：[ " + stepDescription + " ] 未标识步骤Num，可能是功能描述信息，该步骤被跳过，请检查... "));
				logSkipResult(excelFile, stepDescription, stepNum, sheetName);
				break;
			}
		} catch (Exception e) {
			log.warn(ReportUtils.formatWarn("[" + sheetName + "]-[" + stepNum + "] 未成功标记测试结果，请检查..."));
			e.printStackTrace();
		}
	}
	private static void logSkipResult(String excelFile, String stepDescription, int stepNum, String sheetName) throws Exception{
		ExcelUtils.setCellData(excelFile, ExcelConstants.Result_SKIP, stepNum, ExcelConstants.Col_TestStepResult, sheetName);
	}
	/**
	 * 获取当前类及当前类的直接父类中的所有方法：
	 * @author James Guo
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("all")
	public static Method[] getAllMethods(Class<?> clazz) {
		Method[] currentClass = clazz.getDeclaredMethods();// 获取当前类中的所有方法；
		Method[] superClass = {};// 定义父类中的方法数组；
		Method[] totalMethods = {}; // 当前类的方法 + 父类方法的总和；

		Class<?> cl = clazz.getSuperclass();// 获取当前类的父类，针对当前项目而言，就一个父类；

		/**
		 * 如果有父类，且父类中有方法，则取出父类中的所有方法 + 当前类的所有方法，放入总和数组变量，返回；
		 */
		if (null != cl) {
			superClass = cl.getDeclaredMethods();
			/**
			 * 如果父类中有方法，则加入总和数组中：
			 */
			if (superClass.length > 0) {
				totalMethods = new Method[currentClass.length + superClass.length];// totalMethods[]为当前类与父类中的方法总和；

				for (int j = 0; j < superClass.length; j++) {
					totalMethods[j + currentClass.length] = superClass[j];
				}
				/**
				 * 遍历当前类的方法数组，放入总和的数组后返回：
				 */
				for (int i = 0; i < currentClass.length; i++) {
					totalMethods[i] = currentClass[i];
				}
			}
			return totalMethods;// 如果有父类，且父类中有方法，遍历结束后，totalMethods[]中放置的是子类+父类的方法总和，返回这个总和；
		}
		/**
		 * 如果无父类，或父类中无方法定义，则只返回当前的类中的方法：
		 */
		return currentClass;
	}
	/**
	 * 判断失败步骤重跑的次数是否达到最大，如果还有重跑次数，就重跑该关键字的步骤的方法：
	 * @author James Guo
	 * @param testStep
	 */
	@SuppressWarnings("all")
	public static boolean isReRunTimesLeft(int currentRunCount, int totalRerunConut, String keyword) {
		if(totalRerunConut >= currentRunCount){
			log.info(ReportUtils.formatAction("当前步骤执行失败，将执行关键字[ " + keyword + " ]重跑此步骤. 当前重跑第 [ " + (currentRunCount) + " ] 次, 可重跑[ " + totalRerunConut + " ]次."));
			return true;
		}
		return false;
		
		//将重跑标志置为“true”：
		//ExecutionEngine.isRerun = true;
	}
	/**
	 * @Description：将测试状态标志重置：
	 * 				包括：isRerun, currentRunCount, result; 
	 * @author James Guo
	 * @return: void 
	 */
	@SuppressWarnings("all")
	public static void resetTestStatus(){
		//重置 result:
		ExecutionEngine.result = true;
		//重置 isRerun 标志：
		ExecutionEngine.isRerun = false;
		//重置 currentRunCount:
		ExecutionEngine.currentRunCount = 0;
	}
	/**
	 * @Description：在每条新用例或每一个测试步骤执行前，先将结果置为true，防止前边的失败导致后续结果的判定
	 * @author：James Guo	
	 * @return: void
	 */
	public static void resetTestResult(boolean testResult){
		if(!testResult){
			ExecutionEngine.result = true;
		}
	}
	
	private static int getTestCaseRowInTestCaseListSheet(String excelFile, String testCaseId){
		Map<String, TestCaseListBean> testCaseListMap;
		int testCaseInSheet = 0;
		try {
			testCaseListMap = geTestCaseListBean();
			//得到该条用例在TestCaseLists的Sheet中的实际行数：
			testCaseInSheet = testCaseListMap.get(testCaseId).getTestCaseNumInSheet();
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return testCaseInSheet;
	}
	 /**
	  * 为TestCaseLists中的每条Case打上执行日期：
	  * @author：James Guo
	  * @param excelFile
	  * @param testCaseId
	  */
	public static void setTestLastRun(String excelFile, String testCaseId){
		//得到该条用例在TestCaseLists的Sheet中的实际行数：
		int testCaseRowInTestCaseList = getTestCaseRowInTestCaseListSheet(excelFile, testCaseId);
		
		String dateLastRun = FrameworkUtil.dateFormater();
		//将最后执行测试的日期记录到相应的单元格中：
		try {
			ExcelUtils.setCellData(excelFile, dateLastRun, testCaseRowInTestCaseList, ExcelConstants.Col_Test_Last_Run, ExcelConstants.TestCasesList_Sheet);
		} catch (Exception e) {
			log.warn(ReportUtils.formatWarn(testCaseId + " 未能正确设置 TestLastRun, 请检查..."));
			e.printStackTrace();
		}
	}
}
