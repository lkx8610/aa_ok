package framework.keyworddriven.executionengine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.testng.ITestResult;

import framework.base.LoggerManager;
import framework.base.exception.NonStopRunningException;
import framework.base.exception.StopRunningException;
import framework.base.utils.ReportUtils;
import framework.keyworddriven.BaseKeywords;
import framework.keyworddriven.domain.TestCaseStepBean;
import framework.keyworddriven.util.KeywordDrivenUtil;
import framework.webdriver.TestContext;

/**
 * 该类负责解析Excel中的测试用例，利用反射方式调用关键字类中定义的关键字；
 * @author James Guo
 */
public class ExecutionEngine {
	private Logger log = LoggerManager.getLogger(ExecutionEngine.class.getSimpleName());
	public static boolean result;// 默认=true;
	private Object obj = null;//关键字反射调用对象；
	private BaseKeywords keywordsObj = new BaseKeywords();
	private int reRuntCountOnFailure = TestContext.getPropertyInt("reRunCountOnFailure");// 读配置文件中的重跑次数；
	public static int currentRunCount = 1;
	public static boolean isRerun = false;
	public String excelFile = "";// TestCase的Excel文件路径；

	ITestResult wholeTestResult = TestContext.get().getCurrentTestResult(); 
	/**
	 * 有参构造，传入Excel文件信息，完成测试文件初始化；
	 * @author James Guo
	 * @param excelFile String 
	 * @param clazz Class<? extends BaseKeywords>
	 */
	public ExecutionEngine(String excelFile, Class<? extends BaseKeywords> clazz) {
		try {
			//1、设置测试文件；
			this.excelFile = KeywordDrivenUtil.setExcelFile(excelFile);
			
			//2、解析测试文件
			this.executeTestCase(clazz);

		} catch (Exception e) {// 如果读文件错误，最终结果也是Failed
			log.info(ReportUtils.formatError(e.getMessage()));
			wholeTestResult.setStatus(2); // 2: Failure;
		}
	}
	/**
	 * 执行指定的Sheet - 获取所有的TestCaseStepBean并遍历，取出所需信息，调用关键字执行方法、为测试用例打结果
	 * @author James Guo
	 * @param clazz
	 * @throws Exception
	 */
	private void executeTestCase(Class<? extends BaseKeywords> clazz) throws Exception {
		
		obj = clazz.newInstance();
		
		// 调用工具类方法，获取Excel的TestCaseStepsMap(封装了测试用例Sheet中的数据),该Map中只封装了RunMode=yes的Case，所以不用再判断Runmode：
		Map<String, List<TestCaseStepBean>> stepsBeanMap = KeywordDrivenUtil.getTestCaseStepsBean();
		
		//遍历stepsBeanMap取出各个key,即各个SheetName：
		Iterator<String> it = stepsBeanMap.keySet().iterator();
		
		while(it.hasNext()){
			//每个sheet开始执行前，先将状态回置，包括：result, isRerun, currentRunCount:
			KeywordDrivenUtil.resetTestStatus();
			
			//key=sheetName:
			String sheetName = it.next();
			log.info("<font color='#76E11F'><h3> >>>开始执行测试用例 - [ " + sheetName + " ]<<< </h3></font> </br>");
			//当前key(sheetName)对应的value(步骤实体）列表：
			List<TestCaseStepBean> stepBeanList = stepsBeanMap.get(sheetName);
			
			for(int i=0; i<stepBeanList.size(); i++){
				//每个步骤开始前，先将状态回置，包括：result, isRerun, currentRunCount:
				KeywordDrivenUtil.resetTestStatus();
				
				//取出具体的StepBean对象；
				TestCaseStepBean bean = stepBeanList.get(i);
				
				//如果当前的步骤号为空（可能是描述信息）时，跳过该步骤,继续下一步：
				if(StringUtils.isBlank(bean.getColStepNum())){
					KeywordDrivenUtil.skipTestStep(this.excelFile, bean.getStepDescription(), i + 1, bean.getTestCaseID(), 3);
					
					continue;
				}
				
				try {
					//调用关键字方法，开始执行测试：
					executeKeywords(bean, clazz, i);
					
				} catch (Exception e) {
					/**
					 * 描述：下面的判断针对的是：在生成的报告中，如果有任意一个用例是失败的，则最终显示的结果是 Failed.
					 * Testng在跑第一条Case的时候，ITestResult的 status = STARTED,状态码 = 16，
					 * 所以要判断状态为STARTED,和状态“isSuccess()"为true的时候， 如果有失败用例就将用例结果置为
					 * Failed，如果已经有用例将其置为 Failed了，则无需再执行该步骤，提高性能：
					 */
					if (wholeTestResult.getStatus() == 16 || wholeTestResult.isSuccess()) {
						wholeTestResult.setStatus(2); // 2: Failure;
					}
					//如果抛出的异常类型是 StopRunningException，则中止后续的步骤执行，去执行下一条Case：
					if(e instanceof StopRunningException){
						break;
					}
					
				}finally{
					//测试步骤结果：
					KeywordDrivenUtil.setStepResult(this.excelFile, result, (i + 1), bean.getTestCaseID());
				}
			}
			//给执行完的Case打上最后执行时间：last_run 单元格中； 日期格式：2018/08/07-18:09:44
			KeywordDrivenUtil.setTestLastRun(this.excelFile, sheetName);
			//为整条测试用例在TestCaseLists中打结果：
			KeywordDrivenUtil.setTestCaseResult(this.excelFile, sheetName, result);
			//执行“当前测试结束”这一步：
			keywordsObj.endTestCase(null, null, null);
		}
			
	}
	/**
	 * 执行关键字方法及关键字执行失败时重跑该关键字的方法
	 * @author James Guo
	 * @param bean TestCaseStepBean
	 * @param stepNum
	 * @param clazz
	 * @throws Exception
	 */
	private void executeKeywords(TestCaseStepBean bean, Class<? extends BaseKeywords> clazz, int stepNum) throws Exception {
		Throwable throwable = null;
		boolean isKeywordFound = false;

		// 获取当前类和父类中的所有方法；
		Method[] methods = KeywordDrivenUtil.getAllMethods(clazz);
		
		//获取封装当前步骤Bean信息的Map：
		//Map<String, String> stepInfoMap = KeywordDrivenUtil.getTestCaseStepInfo(bean);
		
		//取出当前步骤Bean的所需信息：
		String sheetName = bean.getTestCaseID();
		String stepDescription = bean.getStepDescription();
		String keyword = bean.getKeywords();
		String locatorType = bean.getLocatorType();
		String locator = bean.getLocator();
		String actualResult = bean.getActualResult();
		String realStepNum = bean.getColStepNum();//
		
		//遍历所有关键字（BaseKeyWords + ADTKeywords):
		for (int i = 0; i < methods.length; i++) {
			// 判断当前遍历到的关键字类中的方法是否与读取的Excel中的方法相同；
			if (methods[i].getName().trim().equals(keyword)) {
				isKeywordFound = true;
				
				//判断是否是LocatorType选择了，但未填写Locator：是=false,否=true, 满足条件时，跳过该步骤:
				boolean checkLocatorType = KeywordDrivenUtil.checkLocatorFormat(this.excelFile, bean, stepNum);				
				if (! checkLocatorType) {
						continue;
					}
				//如果LocatorType和Locator都存在，则拼接locator为:xpath=xxx/id=xxx/...的形式；
				if(StringUtils.isNotBlank(locatorType) && StringUtils.isNoneBlank(locator)){
					
					//不满足xpath=xxx时再拼接，防止出现重跑时再次拼接locatorType(形成xpath=xpath=xxxx的形式)
					if(! Pattern.matches("^[a-zA-Z]{2,5}={1}.{1,}$", locator)){
						locator = locatorType + "=" + locator;
					}
				}
					
				try {
					methods[i].setAccessible(true);
					log.debug("执行关键字：" + methods[i].getName());
					methods[i].invoke(obj, stepDescription, locator, actualResult, realStepNum);
					
					break;

				} catch (InvocationTargetException e) {// 通过反射调用方法时，会抛出这个异常：InvocationTargetException
					throwable = e.getTargetException();// 得到调用目标方法实际抛出的异常类型；
					throwable.printStackTrace();
				} catch (Throwable t) {
					t.printStackTrace();
					throwable = t;
				} finally {
					if (null != throwable) {
						// 有异常抛出，判断当前是否满足重跑条件：如果是“准备测试数据”和“删除测试数据”的关键字，则不执行重跑动作：
						boolean isReRuntimesLeft = KeywordDrivenUtil.isReRunTimesLeft(currentRunCount, reRuntCountOnFailure, keyword);
						boolean isValidCase = !methods[i].getName().equals("prepareTestData") && !methods[i].getName().equals("delTestData");
						
						//失败重跑,判断是否满足重跑条件 - reRuntCountOnFailure >= currentRunCount：
						if(isReRuntimesLeft && isValidCase){
							currentRunCount++;
							i--;
							isRerun = true;
							continue;
						}
						//非准备数据和删除数据的用例在重跑后仍失败时，写日志，并置result=false, 
						if(isValidCase){
							log.error(ReportUtils.formatError("步骤：[" + stepDescription + "] 重跑 " + currentRunCount + " 次后失败..."));
						}
						result = false;

						//如果重跑后失败，再将实际的异常抛出：
						if (throwable instanceof StopRunningException) {
							throw new StopRunningException(throwable.getMessage());
						}
						if (throwable instanceof NonStopRunningException) {
							throw new NonStopRunningException(throwable.getMessage());
						}
					}
				}
			}
		}
		// 循环完成，但未找到关键字, 将当前步骤SKIPPED：
		if (!isKeywordFound || StringUtils.isBlank(keyword)) {
			KeywordDrivenUtil.skipTestStep(this.excelFile, stepDescription, stepNum, sheetName, 2);
		}
	}
}
