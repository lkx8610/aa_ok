package framework.webdriver;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.apache.log4j.PatternLayout;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;

import framework.base.HTMLLayout;
import framework.base.LoggerManager;
import framework.base.TestMetrics;
import framework.base.Validate;
import framework.base.Verify;
import framework.base.TestMetrics.TestMetric;
import framework.base.anotation.TestDoc;
import framework.base.utils.ReportUtils;
import framework.base.utils.TestUtils;
import framework.webdriver.LogLevel;
import framework.webdriver.TestContext;
import framework.webdriver.TestNGListener;
/**自定义的TestNG的监听器类：TestNGListener
 */
public class TestNGListener implements ITestListener, IInvokedMethodListener{
	private static final Logger logger = LoggerManager.getLogger(TestNGListener.class.getName());
	private BufferedWriter testIndex;
	private BufferedWriter testIndexCSV;
	private int testNumber = 0;
	private int reportNumber = 0;
	private TestMetrics testMetrics = new TestMetrics();
	private String testCaseRun;
	ITestResult testResult = null;
	
	public TestNGListener() throws Exception {//测试启动时，TestNG会调用该实现类的无参构造创建对象，在该构造被调用时完成以下工作：
		loadProperties();//加载框架的配置文件
		TestContext.setCapabilities(new DesiredCapabilities());
		TestContext.setInitialized();
		//生成报告的"index.html"页面:
		setupTestIndexReport();
		// 为当前执行的用例生成唯一的ID：
		testCaseRun = new SimpleDateFormat("MMddkkmmss").format(new Date());
	}

	private void setupTestIndexReport() throws IOException {
		//在报告头部生成测试执行的时间：
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String runDate = df.format(new Date().getTime());
		
		testIndex = new BufferedWriter(new FileWriter(TestContext.getOutputDirectory() + "/index.html"));
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">" + Layout.LINE_SEP);
		sbuf.append("<html>");
		sbuf.append("<head>");
		sbuf.append("<style type=\"text/css\">");
		sbuf.append("body, table {font-family: arial,sans-serif; font-size: small;}");
		sbuf.append("th {background: #336699; color: #FFFFFF; text-align: left;}" + Layout.LINE_SEP);
		sbuf.append(".failed { color: red; font-weight: bold; }\n");
		sbuf.append(".passed { color: green; font-weight: bold; }\n");
		sbuf.append("</style>" + Layout.LINE_SEP);
		sbuf.append("<script src=\"js/sorttable.js\"></script>");
		sbuf.append("</head>");
		sbuf.append("<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">" + Layout.LINE_SEP);
		sbuf.append("<br>" + Layout.LINE_SEP);
		sbuf.append("<a href=index.csv>CSV Report</a>&nbsp;&nbsp;");
		sbuf.append("<a href=\"#metrics\">Test Metrics</a>");
		sbuf.append("&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp; <font color='grenn'> <b> 测试执行时间：" + runDate + "</b></font>");//设置测试执行时间
		sbuf.append("<br/><br/>" + Layout.LINE_SEP);
		sbuf.append("<table class='sortable' cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">" + Layout.LINE_SEP);
		sbuf.append("<tr>");
		sbuf.append("<th style='width:12px;'/>");//结果状态小图标
		sbuf.append("<th style='text-align:right;'>Test #</th>");
		sbuf.append("<th style='text-align:center;'>Test Group</th>");
		sbuf.append("<th style='text-align:center;'>Package</th>");
		sbuf.append("<th style='text-align:center;'>Test Class</th>");
		sbuf.append("<th style='width: 180px; text-align:center;'>Test Method</th>");
		sbuf.append("<th style='width: 180px; text-align:center;'>Total TestCases</th>");//记录关键字测试用例中，一个WorkBook中包含的用例总数；
		sbuf.append("<th style='text-align:center;'>Report</th>");
		sbuf.append("<th style='text-align:center;'>Log</th>");
		sbuf.append("<th style='text-align:center;'>Status</th>");
		sbuf.append("<th style='text-align:center;'>Verify Count</th>");
		sbuf.append("<th style='text-align:center;'>Time</th>");
		sbuf.append("</tr>");
		testIndex.write(sbuf.toString());
		testIndexCSV = new BufferedWriter(new FileWriter(TestContext.getOutputDirectory() + "/index.csv"));
		testIndexCSV.write("Package,Class,Method (Test Name),Test Case ID,Story ID,Bug ID,Objective,Date Documented,Result,Verifies,Time\n");
	}
	
	/**
	 * 加载主配置文件“framework.properties"和其它用户自定义文件；
	 */
	private void loadProperties() throws IOException {
		//如果有系统变量“profile”，则加载完框架基础配置后，再加载这个profile的配置文件来覆盖主配置文件的值，以完成客户自定义的操作
		String profile =  System.getProperty("profile");
		
		Properties baseProperties = new Properties(); 
		//先加载主配置文件
		baseProperties.load(getClass().getClassLoader().getResourceAsStream("conf/framework.properties"));
		if (StringUtils.isBlank(profile)) {//如果未配置profile则加载local配置文件
			
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("conf/local.properties");
			if (inputStream != null) {//如果有local.properties就加载
				logger.debug("Loading local.properties");
				baseProperties.load(inputStream);
				logger.debug("Loaded local.properties successfully.");
			}
		} else {//如果有“profile”对应的配置文件
			logger.debug("loadProfile(): profile=" + profile); 
			String propertiesFileName = "";//profile的Key对应的文件名；
			int index = profile.indexOf(".properties");
			if(-1 != index){
				propertiesFileName = profile;
			}else{
				propertiesFileName = profile + ".properties";
			}  
			logger.debug("Loading " + propertiesFileName);
			baseProperties.load(getClass().getClassLoader().getResourceAsStream("conf/" + propertiesFileName));
			logger.debug("Loaded " + propertiesFileName + " successfully.");
		}
		for (Object prop : baseProperties.keySet()) {
			logger.debug(prop.toString() + "=" + baseProperties.getProperty(prop.toString()));
		}
		TestContext.setProperties(baseProperties);//配置文件加载完毕，将其设置到TestContext的properties类属性中
	}
	
	@Override
	public synchronized void onTestStart(ITestResult testResult) {
		TestContext.get().setTestNumber(testNumber++);
		TestContext.get().setCurrentTestResult(testResult);
		logger.debug("Executing : " + getTestMethod(testResult));

		removeAppender("TestAppender");
		removeAppender("TestLog");

		setupTestAppender(testResult);
		
		ConstructorOrMethod constructorOrMethod = testResult.getMethod().getConstructorOrMethod();
		Method method = constructorOrMethod.getMethod();

		logTestDocumentation(method.getAnnotation(TestDoc.class));
	}

	/**
	 * 将有 TestDoc 的注解内容写到 Report.
	 */
	private void logTestDocumentation(TestDoc testDoc) {
		if (testDoc == null) {
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		//将@TestDoc中的内容写到一个表格中，如需修改样式，在此处修改：
		sb.append("<table cellspacing=0 cellpadding=4 border=1 bordercolor=#289C23 width=100%>");
		sb.append("<tr><th width='200px' style='white-space: nowrap' >测试用例书写日期 : </th><td>" + testDoc.dateDocumented()).append("</td></tr>");
		sb.append("<tr><th width='200px' style='white-space: nowrap'>本用例测试目标: </th><td>" + testDoc.testObjective()).append("</td></tr>");
		
		logOptionalAttribute(sb, "测试用例 ID: ", testDoc.testCaseID(), false);
		logOptionalAttribute(sb, "Bug ID:", testDoc.bugJiraID(), true);
		logOptionalAttribute(sb, "Story ID: ", testDoc.storyJiraID(), true);
		sb.append("</table>");
		
		//logger.debug(sb.toString());设置TestDoc在何种日志级别是输出
		logger.info(sb.toString());
	}
	
	/**
	 * Log optional TestDoc attributes only if they are present.
	 */
	private void logOptionalAttribute(StringBuilder sb, String label, String[] data, boolean jiraLink) {
		if (data != null && data.length > 0) {
			sb.append("<tr><th width='100px' style='white-space: nowrap'>").append(label).append("</th><td>");
			for (String s : data) {
				sb.append(jiraLink ? "<a href=http://" + s + ">" + s + "</a>" : s).append(" ");
			}
			sb.append("</td></tr>");			
		}
	}

	@Override
	public void onTestFailure(ITestResult testResult) {
		//测试失败时自动截图，可以调用TestContext中的截图API，完成截图操作，并记录截图的href到日志中：
		//如果浏览器在本地，则调用本地截图，如果在远程则调用远程截图
		try {
			TestContext.get().captureScreen("失败截图  ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 整个测试结束，记录Package Metrics/Class Metrics/Test Metrics / 在HTML头部生成“总用例数：xx” 的报告；
	 */
	@Override
	public synchronized void onFinish(ITestContext testContext) {
		try {
			testIndex.write("</table><br/><br/>");//测试完成，先将最顶层的Table标签闭合：
			
			/**
			 * 记录个Metrics的小表格：
			 */
			/**生成的Package Metrircs, 用处不大，暂时注释掉。*/
			/*testIndex.write("<h3><a name=metrics>Package Metrics</a></h3>");
			testIndex.write("<table class='sortable' cellspacing=0 cellpadding=4 border=1 bordercolor=#224466 width=30%>" + Layout.LINE_SEP);
			testIndex.write("<tr>");
			testIndex.write("<tr><th>Package</th><th style='text-align: center;'># Tests</th><th style='text-align: center; white-space: nowrap;'>Total Time</th><th style='text-align: center;'>Avg. Time</th></tr>");
			
			Map<String, TestMetric> packageMetrics = testMetrics.getPackageMetrics();
			for (String packageName : packageMetrics.keySet()) {
				TestMetric testMetric = packageMetrics.get(packageName);
				testIndex.write("<tr><td>" + packageName + "</td>");
				testIndex.write("<td style='text-align: center;'>" + testMetric.totalTests + "</td>");
				testIndex.write("<td style='text-align: center;'>" + String.format("%8.2f min", testMetric.totalTime / (1000*60)) + "</td>");//将totalTime的毫秒换算成分，注意运算的顺序totalTime / (1000*60)；
				testIndex.write("<td style='text-align: center;'>" + String.format("%8.2f min", testMetric.averageTime / (1000*60)) + "</td>");
				testIndex.write("</tr>");	
			}
			testIndex.write("</table><br/><br/>");*/

			// Class Metrics
			testIndex.write("<h3>Test Class Metrics</h3>");
			testIndex.write("<table class='sortable' cellspacing=0 cellpadding=4 border=1 bordercolor=#224466 width=50%>" + Layout.LINE_SEP);
			testIndex.write("<tr>");
			testIndex.write("<tr><th>Class</th><th style='text-align: center;'># Tests</th><th style='text-align: center; white-space: nowrap;'>Total Time</th><th style='text-align: center;'>Avg. Time</th></tr>");
			
			Map<String, TestMetric> classMetrics = testMetrics.getClassMetrics();
			for (String className : classMetrics.keySet()) {
				TestMetric testMetric = classMetrics.get(className);
				testIndex.write("<tr><td>" + className + "</td><td style='text-align: center;'>" + testMetric.totalTests + "</td>");
				testIndex.write("<td style='text-align: center;'>" + String.format("%8.2f min", testMetric.totalTime / (1000*60)) + "</td>");
				testIndex.write("<td style='text-align: center;'>" + String.format("%8.2f min", testMetric.averageTime / (1000*60)) + "</td>");
				testIndex.write("</tr>");
			}
			testIndex.write("</table>");

			// Group Metrics
			testIndex.write("<h3>Test Group Metrics</h3>");
			testIndex.write("<table class='sortable' cellspacing=0 cellpadding=4 border=1 bordercolor=#224466 width=30%>" + Layout.LINE_SEP);
			testIndex.write("<tr>");
			testIndex.write("<tr><th>Group</th><th style='text-align: center;'># Tests</th><th style='text-align: center; white-space: nowrap;'>Total Time</th><th style='text-align: center;'>Avg. Time</th></tr>");
			
			Map<String, TestMetric> groupMetrics = testMetrics.getGroupMetrics();
			for (String groupName : groupMetrics.keySet()) {
				TestMetric testMetric = groupMetrics.get(groupName);
				testIndex.write("<tr><td>" + groupName + "</td><td style='text-align: center;'>" + testMetric.totalTests + "</td>");
				testIndex.write("<td style='text-align: center;'>" + String.format("%8.2f min", testMetric.totalTime / (1000*60)) + "</td>");
				testIndex.write("<td style='text-align: center;'>" + String.format("%8.2f min", testMetric.averageTime / (1000*60)) + "</td>");
				testIndex.write("</tr>");
			}
			testIndex.write("</table>");
			
			testIndex.write("</body></html>");
			testIndex.flush();
			testIndex.close();
			
			testIndexCSV.close();
		} catch (IOException e) {
			// 
		}
		getTotalCaseCount();
		openTestReport();
	}
	
	/**
	 * @Description： 获取最终一共跑了多少条Case： TODO
	 * @author：James 
	 * @date： 2018年8月27日 
	 * @return: int
	 */
	public int getTotalCaseCount(){
		//int count = TestContext.get().getTotalCaseCount();
		return TestContext.get().getTotalCaseCount();
	}

	/**
	 * 测试完成后，是否在默认浏览器中打开测试报告
	 */
	private void openTestReport() {
		if (TestContext.getPropertyBoolean("openTestReportOnEnd")) {//是否自动打开测试报告的开头，配置在配置文件中，此处先不做处理，需要时扩展
			try {
				Desktop.getDesktop().open(new File(TestContext.getOutputDirectory() + "/index.html"));
			} catch (IOException ex) {
				logger.error(ex, ex);
			}
		}
	}
	
	
	@Override
	public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
		NDC.pop();
		NDC.push(String.format("%3d. ", testNumber) + getTestMethod(testResult));
		if (testResult.getMethod().isTest()) {
			/**
			 * 2018-7-31注释掉setCookie部分；
			 */
			//setCookie("TestCase", getTestMethod(testResult));
			//setCookie("TestCaseRun", testCaseRun);
		}
	}

	@SuppressWarnings("unused")
	private void setCookie(String name, String value) {
		if (TestContext.getWebDriver() != null) {
			try { 
				TestContext.getWebDriver().manage().deleteCookieNamed(name);
				TestContext.getWebDriver().manage().addCookie(new Cookie(name, value));
			} catch (Exception ex) {
				logger.warn(ex.toString());
			}
		}
	}
	//设置Log4j的Appernder：日志LayOut、日志路径。。。
	private void setupTestAppender(ITestResult testResult) {
		HTMLLayout layout = new HTMLLayout();
		String testMethod = getTestMethod(testResult); 

		Object[] parameterList = testResult.getParameters();
		StringBuilder parameters = new StringBuilder();
		if (parameterList != null) {
			String delimiter = "(";
			for (Object parm : parameterList) {
				parameters.append(delimiter);
				parameters.append(parm == null ? "null" : parm.toString());
				delimiter = ", ";
			}
			if (delimiter.equals(", ")) {
				parameters.append(")");
			}
		}
		
		layout.setTestName(testMethod.replace("-", " ") + " " + parameters.toString());
		try {
			Logger rootLogger;
			if (isMultiThreadedRun()) {
				rootLogger = Logger.getLogger(Thread.currentThread().getName());
				rootLogger.removeAllAppenders();
			} else {
				rootLogger = Logger.getRootLogger();
			}
			//主报告中的详细报告的级别：
			TestContext.get().setTestReportFile(testNumber + "-" + testMethod + ".html");
			FileAppender appender = new FileAppender(layout, TestContext.getOutputDirectory() + "/" + TestContext.get().getTestReportFile(), false);
			appender.setName("TestAppender");
			//appender.setThreshold(Level.INFO);//自定义报告中的日志输出级别，可配置在外部配置文件中；
			appender.setThreshold(LogLevel.levelMap.get(TestContext.getProperty("detailedReportLevel").toLowerCase()));//自定义报告中的日志输出级别，可配置在外部配置文件中；
			rootLogger.addAppender(appender);
			
			Layout patternLayout = new PatternLayout("%d{ISO8601} %-5p %-30x %-30c : %m %n");
			TestContext.get().setTestLogFile(testNumber + "-" + testMethod + ".log");
			appender = new FileAppender(patternLayout, TestContext.getOutputDirectory() + "/" + TestContext.get().getTestLogFile(), false);
			appender.setName("TestLog");
			//appender.setThreshold(Level.DEBUG);
			appender.setThreshold(LogLevel.levelMap.get(TestContext.getProperty("logReportFile").toLowerCase()));
			rootLogger.addAppender(appender);
			
		} catch (IOException e) {
			logger.warn("Could not create Logger ", e);
		}
	}
	/**
	 * 在所有TestNG annotated methods 被调用后执行, 包括 BeforeClass 、 BeforeMethod
	 */
	@Override
	@SuppressWarnings("all")
	public synchronized void afterInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
		if (testResult.getMethod().isTest()) {
			
			testResult.setEndMillis(System.currentTimeMillis());
			
			WebDriver webDriver = TestContext.getWebDriver();
			//System.out.println(webDriver);//webDriver = ChromeDriver: chrome on XP (null)
			if (null != webDriver) {
				/*webDriver.manage().deleteCookieNamed("TestCase");
				webDriver.manage().deleteCookieNamed("TestCaseRun");*/
			}
			/* 暂时注释掉关于 UncauhtException部分： date: 6-28
			String uncaughtException = WebDriverUtils.getUncaughtException();
			if (uncaughtException != null) {
				testResult.setStatus(ITestResult.FAILURE);
				testResult.setThrowable(new AssertionError("Unhandled Javascript exception : " + uncaughtException));
			}*/

			/*ConstructorOrMethod constructorOrMethod = testResult.getMethod().getConstructorOrMethod();
			Method method = constructorOrMethod.getMethod();*/
			
			logReportData(testResult);
			logger.info(String.format("%-10s: ", getStatus(testResult)) + getTestMethod(testResult));
			
			Reporter.setCurrentTestResult(testResult);
			testMetrics.recordMetrics(testResult);
			
			File screenshotFile = null;
			if ( ! testResult.isSuccess()) {
				screenshotFile = TestContext.get().captureScreen("失败截图");
				TestContext.get().saveDOM();
			}
			removeAppender("TestAppender");
			removeAppender("TestLog");

		} else {
			if (! testResult.isSuccess()) {
				logger.error(testResult.getThrowable(), testResult.getThrowable());
			}
		}
	}

	/**
	 * 将测试日志记录到 HTML report和 CSV文件中.
	 */
	private void logReportData(ITestResult testResult) {
		long totalTime = testResult.getEndMillis() - testResult.getStartMillis();
		int verifyCount = Verify.getAndResetVerifyCount();
		int validationFailureCount = Validate.getAndResetValidationFailureCount();
		
		try {
			final String testPackage = testResult.getInstance().getClass().getPackage().getName();
			final String testClass = testResult.getInstance().getClass().getSimpleName();
			final String testMethodName = testResult.getMethod().getMethodName();

			String thrownMessage = "";
			if (testResult.isSuccess()) {
				// Test passed all verifies, see if there were any validation (soft) failures
				if (validationFailureCount > 0) {
					testResult.setStatus(ITestResult.FAILURE);
					thrownMessage = "<br/>There were " + validationFailureCount + " validation failures";
					testResult.setThrowable(new AssertionError(thrownMessage));
					logger.error(ReportUtils.formatError(thrownMessage));
				}
			}
			
			if (testResult.getThrowable() != null) {//如果有异常时，将异常信息全部输出，将\n转换成HTMl的<br />
				//logger.info(ReportUtils.formatError("<pre>" + TestUtils.getTruncatedStackTrace(testResult.getThrowable())).replaceAll("\n", "<br/>") + "</pre>");
				logger.info(ReportUtils.formatError("<pre>" + TestUtils.getFailureMessage(testResult.getThrowable())).replaceAll("\n", "<br/>") + "</pre>");
				//logger.debug(ReportUtils.formatError(StringEscapeUtils.escapeHtml(ExceptionUtils.getgetFullStackTrace(testResult.getThrowable()))));
				thrownMessage = "<pre>\n" + TestUtils.getFailureMessage(testResult.getThrowable()).replaceAll("<br/>", "\n") + "</pre>";
			}
			/**
			 * 测试报告首页行及行上的各列：
			 */
			testIndex.write("<tr>");
			//第一列，用来显示成功与失败的小图标素材(success.png, failed.png)：
			String statusIconBasePath = TestNGListener.class.getClassLoader().getResource("images").getPath();
			testIndex.write(String.format("<td><img src='" + statusIconBasePath + "/" + "%s.png' /></td>", testResult.isSuccess()  ? "success" : "failed"));
			//testIndex.write(String.format("<td><img src='" + "../images/" + "%s.png' /></td>", testResult.isSuccess()  ? "success" : "failed"));//状态图标；
			//第二列：编号：1、2、3...
			testIndex.write("<td style='text-align: right;'>");
			testIndex.write(String.format(" %d.</td>", ++reportNumber));//Testcase编号；
			//第三列：测试用例所属组：
			testIndex.write(String.format("<td style='text-align:center;'>%s</td>", getTestGroups(testResult)));//所属组；
			//第四列：测试用例所属包：
			testIndex.write(String.format("<td style='text-align:center;'>%s</td>", testPackage));//所属包；
			//第五列：Test Class的状态：如果一个Class中有方法失败则以红色显示。Passed/Failed:
			testIndex.write(String.format("<td class='%s'>%s</td>", (testResult.isSuccess() ? "passed" : "failed"), testClass));
			
			Object[] parameterList = testResult.getParameters();//显示当前测试方法的参数，如无参数，则不显示；
			StringBuilder parameters = new StringBuilder();
			if (parameterList != null) {
				String delimiter = "(";
				for (Object parm : parameterList) {
					parameters.append(delimiter);
					parameters.append(parm == null ? "null" : parm.toString());
					delimiter = ", ";
				}
				if (delimiter.equals(", ")) {
					parameters.append(")");
				}
			}
			
			//TestMethod列中的值，不让异常信息在index报告中显示，可将“thrownMessage”参数去掉。 
			//第六列：测试方法：
			testIndex.write(String.format("<td class='%s'>%s%s%s</td>", (testResult.isSuccess() ? "passed" : "failed"), testMethodName, parameters.toString(), ""/*, thrownMessage*/));
			//第七列：记录当前运行的Excel文件中共有多少条测试用例：
			testIndex.write(String.format("<td style='text-align:center; font-color:blue;'> %s </td>", TestContext.testCaseCountFromExcel + ""));
			//第八列：详细报告链接：
			testIndex.write(String.format("<td style='text-align:center;'><a href='%s' target='_blank'>Detailed Report</a></td>", TestContext.get().getTestReportFile()));
			//第九列：文本日志链接：
			testIndex.write(String.format("<td style='text-align:center;'><a href='%s' target='_blank'>Text Log</a></td>", TestContext.get().getTestLogFile()));
			//第十列：测试状态列：Passed/Failed
			testIndex.write("<td style='text-align: center;' class=" + (testResult.isSuccess()  ? "passed" : "failed") + ">");
			testIndex.write(getStatus(testResult) + "</td>");
			//第十一列：断言列：
			testIndex.write(String.format("<td style='text-align: center;'>%d</td>", verifyCount));
			//testIndex.write("</td>");
			//第十二列：测试时间：
			double elapsedTime = totalTime;
			elapsedTime = elapsedTime / 1000;//秒为单位；
			testIndex.write(String.format("<td style='text-align: center;'>%8.2f s</td>", elapsedTime));

			testIndex.write("</tr>");
			testIndex.flush();

			ConstructorOrMethod constructorOrMethod = testResult.getMethod().getConstructorOrMethod();
			Method testMethod = constructorOrMethod.getMethod();
			
			String testObjective = "";
			String testCaseId = "";
			String storyIds = "";
			String bugIds = "";
			String dateDocumented = "";
			TestDoc testDoc = testMethod.getAnnotation(TestDoc.class);
			if (testDoc != null) {
				
				testObjective = testDoc.testObjective();
				testCaseId = StringUtils.join(testDoc.testCaseID(), ",");
				storyIds = StringUtils.join(testDoc.storyJiraID(), ",");
				bugIds = StringUtils.join(testDoc.bugJiraID(), ",");
				dateDocumented = testDoc.dateDocumented();
			}
			
			testIndexCSV.write(testPackage + "," + wrapQuotes(testClass) + "," + wrapQuotes(String.format("%s %s", testMethodName, parameters.toString())) + ","
					+ wrapQuotes(testCaseId) + "," + wrapQuotes(storyIds) + "," + wrapQuotes(bugIds) + "," + wrapQuotes(testObjective) + ","
					+ wrapQuotes(dateDocumented) + "," + wrapQuotes(getStatus(testResult)) + "," + verifyCount + "," + String.format("%8.2f", elapsedTime) + "\n");

			testIndexCSV.flush();
			
		} catch (IOException ex) {
			logger.warn(ex, ex);
		}		
	}
	//定义测试状态，输出到测试报告中
	private String getStatus(ITestResult testResult) {
		String status = "";
		switch (testResult.getStatus()) {
			case ITestResult.SUCCESS :
				status = "PASSED";
				break;
			case ITestResult.FAILURE :
				status = "FAILED";
				break;
			case ITestResult.SKIP :
				status = "SKIPPED";
				break;
			default :
				status = "Status " + testResult.getStatus();
				break;
		}
		return status;
	}
	
	private void removeAppender(String appenderName) {
		Appender appender = Logger.getRootLogger().getAppender(appenderName);
		if (appender != null) {
			appender.close();
			Logger.getRootLogger().removeAppender(appender);

			if (isMultiThreadedRun()) {
				Logger threadLogger = Logger.getLogger(Thread.currentThread().getName());
				threadLogger.removeAppender(appender);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private String getHostName() {
		String hostName = "";
		try {
			InetAddress addr = InetAddress.getLocalHost();
			hostName = addr.getHostName();
		} catch (UnknownHostException e) {
			logger.error(e, e);
		}
		return hostName;
	}
	
	private String wrapQuotes(String value) {
		return "\"" + value + "\"";
	}
	@SuppressWarnings("unused")
	private String getProperty(String key) {
		return TestContext.getProperty(key);
	}
	
	@Override
	public void onStart(ITestContext arg0) { 
		
		// Nothing to implement yet
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
		// Nothing to implement yet
	}

	@Override
	public void onTestSkipped(ITestResult testResult) {
		
		logger.info(testResult.getName() + " Skipped");
	}

	@Override
	public void onTestSuccess(ITestResult testResult) {
		//
	}

	private String getTestMethod(ITestResult testResult) {
		return testResult.getInstance().getClass().getSimpleName() + "-" + testResult.getMethod().getMethodName();
	}

	private String getTestGroups(ITestResult testResult) {
		StringBuilder testGroups = new StringBuilder();
		String delimiter = ",";
		for (Annotation annotation : testResult.getMethod().getConstructorOrMethod().getMethod().getAnnotations()) {
			if (annotation instanceof Test) {
				Test test = (Test) annotation;
				for (String group : test.groups()) {
					testGroups.append(group).append(delimiter);
					//delimiter=",";
				}
				break;
			}
		}
		return testGroups.toString();
	}
	
	@SuppressWarnings("unused")
	private String getTestcaseName(ITestResult testResult) {
		return testResult.getInstance().getClass().getName() + "." + testResult.getMethod().getMethodName();

	}
	
	/**
	 * Determines if the test are running multi-threaded so Logging can be configured correctly.
	 */
	private boolean isMultiThreadedRun() {
		return StringUtils.isNotBlank(TestContext.getProperty("grid.Server"));/* || StringUtils.isNotEmpty(TestContext.getProperty("backendregression"));*/
	}
}
