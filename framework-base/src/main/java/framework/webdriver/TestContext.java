package framework.webdriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.ITestResult;
import org.testng.Reporter;

import framework.base.BaseFramework;
import framework.base.LoggerManager;
import framework.webdriver.TestContext;

/**
 * TestContext是一个让测试框架与测试用例都可访问的容器；
 * 每个测试所处在一个单独的测试上下文（TestContext）中，放在ThreadLocal对象中保存，保证为每个线程独有；
 * 每一个测试线程被一个独立的TestContext维护； 包含： 当前测试线程的TestContext实例的获取； Webdriver实例的获取；
 * 配置文件的读取； 测试报告参数的生成 ...
 * 
 */
public class TestContext {

	private static Logger logger = LoggerManager.getLogger(Thread.currentThread().getName() + ".TestContext");

	// 所有线程可共享的资源
	private static Properties properties;
	private static DesiredCapabilities capabilities;
	private static int filePrefix = 0;
	private static boolean isInitialized = false;
	private static boolean verifyServerStatus = false;//判断当前是否已经进行过一次测试前服务器状态的判断了,如果已判断过一次，以后测试就不再每次去校验服务器，节省时间,设置默认false；
	//定义一个全局的变量，用来记录总共跑了多少条测试用例：
	private int totalCaseCount;
	// 线程独有的资源
	private ITestResult currentTestResult;//记录
	private WebDriver webDriver;
	private int testNumber;
	private String testReportFile;
	private String testLogFile;
	
	//为“TotalCaseCount”设置 getter & setter”，在关键字类的ExecutionEngine类中为其设置值；
	public int getTotalCaseCount() {
		return totalCaseCount;
	}
	public void setTotalCaseCount(int totalCaseCount) {
		this.totalCaseCount = totalCaseCount;
	}

	//为了在报告中显示当前正在跑的Excel中共有多少测试用例，在这里加入一个字段：
	public static int testCaseCountFromExcel;
	
	ITestResult testResult; //记录当前测试的测试结果，在关键字中，如果有测试步骤失败，就将其置为Failed，最终报告中的结果也是Failed；

	// 创建ThreadLocal对象，并覆写initalValue（），使其初始值不为null;
	private static ThreadLocal<TestContext> testContext = new ThreadLocal<TestContext>() {
		@Override
		protected TestContext initialValue() {
			return new TestContext();
		}
	};

	/**
	 * 判断用户是否配置了开始测试前的服务器状态校验，如果配置了，则在测试前先校验服务器的启动状态：
	 * @author James Guo
	 * @return
	 */
	public static boolean getIsVerifyServerStateFirst(){
		return (null != properties.getProperty("isVerifyServerStateFirst"));
	}
	
	public static void setVerifyServerStatus(boolean verifyStatus){//设置已校验服务器的开关，如果已校验过一次，此处设置为true; 
		verifyServerStatus = verifyStatus;
	}
	public static boolean getVerifyServerStatus(){//返回当前是否已经校验过Server的状态；
		return verifyServerStatus;
	}
	
	//记录当前WookBook中测试用例的数量：
	public static int getTestCaseCountFromExcel() {
		return testCaseCountFromExcel;
	}
	public static void setTestCaseCountFromExcel(int testCaseCountFromExcel) {
		TestContext.testCaseCountFromExcel = testCaseCountFromExcel;
	}
	/**
	 * 返回当前线程的测试上下文对象（TestContext）：
	 */
	public static TestContext get() {
		return testContext.get();
	}

	public String getTestLogFile() {
		return testLogFile;
	}

	public void setTestLogFile(String testLogFile) {
		this.testLogFile = testLogFile;
	}

	public String getTestReportFile() {
		return testReportFile;
	}

	public void setTestReportFile(String testReportFile) {
		this.testReportFile = testReportFile;
	}

	public int getTestNumber() {
		return testNumber;
	}

	public void setTestNumber(int testNumber) {
		this.testNumber = testNumber;
	}

	public static boolean isInitialized() {
		return isInitialized;
	}

	static void setInitialized() {
		isInitialized = true;
	}

	public void setWebDriver(WebDriver swebDriver) {

		webDriver = swebDriver;
	}

	// 获取Webdriver
	public static WebDriver getWebDriver() {
		return get().webDriver;// 返回当前测试线程的TextContext的Webdriver对象
	}

	public static String getBrowserType() {
		return getProperty("browser.type");
	}

	// 获取登录地址
	public static String getServerUrl() {
		String serverAddress = getProperty("server.base.address");
		
		if(null != getProperty("server.login.address")){//如果有后置地址，则加上；
			serverAddress += "/" + getProperty("server.login.address");
		}
		
		return serverAddress;
	}

	public static String getUsername() {
		return getProperty("login.user");
	}

	public static String getPassword() {
		return getProperty("login.password");
	}

	public static void setProperties(Properties properties) {
		TestContext.properties = properties;
	}

	public static String getProperty(String key) {
		// Allow System property to override
		String value = System.getProperty(key);
		if (value == null) {
			value = System.getenv(key);
		}
		if (value == null) {
			value = properties.getProperty(key);
			if (value == null) {
				logger.debug("No value found for property " + key);
			}
		} else {
			logger.debug("Property value " + key + " overridden by System property : " + value);
		}
		return value;
	}

	public static void setProperty(String key, String value) {
		System.setProperty(key, value);
	}

	// 读取framework.properties中的int类型的值：waitAjaxTimeout、waitDomTimeout
	public static int getPropertyInt(String name) {
		String value = getProperty(name);
		return Integer.valueOf(value);
	}

	public static int getDomTimeout() {// 获取配置文件中的等待Dom元素出现的最大时间：
		return getPropertyInt("waitDomTimeout");
	}

	public static int getAjaxTimeout() {
		return getPropertyInt("waitAjaxTimeout");
	}

	// 读取framework.properties中的boolean类型的值：openTestReportOnEnd、closeBowserOnEnd
	public static boolean getPropertyBoolean(String name) {
		return "true".equals(getProperty(name));
	}

	public static void setCapabilities(DesiredCapabilities capabilities) {
		TestContext.capabilities = capabilities;
	}

	public static DesiredCapabilities getCapabilities() {
		return capabilities;
	}

	/**
	 * 设置和获取当前测试线程的TestResult：
	 * @author James Guo
	 * @return
	 */
	public ITestResult getCurrentTestResult() {
		return currentTestResult;
	}
	public void setCurrentTestResult(ITestResult currentTestResult) {
		this.currentTestResult = currentTestResult;
	}

	// 在TestNGListener中检测到失败用例时调用的截图方法，并保留在日志中
	public File captureScreen(String description) {
		// File file = null;
		File file = null;// 配置文件中的 screenshot.dir ;

		try {
			String fileName = (filePrefix++) + "-" + currentTestResult.getInstance().getClass().getSimpleName() + "-"
					+ currentTestResult.getMethod().getMethodName() + ".png";

			// 判断Webdriver是在本地跑的脚本还是远程，如果是本地，直接调用本地的截图方法，如果是远程则需要Webdriver的远程实例去截图：
			if (null != getProperty("grid.server") || !"".equals(getProperty("grid.server"))) {// 远程截图
				WebDriver driver = getWebDriver();
				if (null != driver && (driver instanceof TakesScreenshot)) {
					driver = new Augmenter().augment(driver);

					file = new File(getScreensotDirectory() + "/" + fileName);//目标截图的存放位置；
					
					File tmpFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
					
					logger.debug("Capturing screenshot '" + description + "' to " + file.getAbsolutePath());
					try {
						FileUtils.copyFile(tmpFile, new File(file.getPath() + "/" + fileName));// 拷贝截图到目标目录
						// captured = true;
					} catch (IOException ex) {
						logger.warn("Unable to save screenshot file ", ex);
					}
				} else {
					logger.warn("This WebDriver instance does not implement the TakeScreenshot interface");
				}
			}
			// 脚本在本地跑，只能截取本地的截图：
			else {
				file = BaseFramework.takeScreenshotByRobot(getScreensotDirectory().getPath(), "/" + fileName);
				// System.out.println(file.getAbsolutePath());	
			}
			
			Reporter.setCurrentTestResult(currentTestResult);
			if (null != file) {
				String href = "<a href=\"" + getScreensotDirectory().getAbsolutePath() + "/" + fileName	+ "\" >" +
						      "<font color='#A5A52E' size='5'><b><i>" + description + "</i></b></font> </a>";
				logger.info(href);
			}
		} catch (Exception ex) {
			logger.warn(ex, ex);
		}
		return file;
	}

	public void saveDOM() {
		if (webDriver != null) {
			String dom = webDriver.getPageSource();
			String fileName = currentTestResult.getInstance().getClass().getSimpleName() + "-"
					+ currentTestResult.getMethod().getMethodName() + "-DOM.txt";
			logger.debug("Capturing DOM state to " + fileName);
			File file = new File(getOutputDirectory(), fileName);
			try {
				FileOutputStream io = new FileOutputStream(file);
				io.write(dom.getBytes());
				io.close();
				Reporter.setCurrentTestResult(currentTestResult);
				String href = "<a href=\"" + fileName + "\">DOM Capture</a>";
				logger.info(href);
			} catch (IOException e) {
				logger.error(e, e);
			}
		}
	}

	public static File getOutputDirectory() {// 获取配置文件中配置的报告存放路径：
		// return ensureExists("build/reports/automation");
		// return ensureExists(ConfigReader.getInstance().getReport_dir());
		return ensureExists(TestContext.getProperty("report.dir"));
	}

	public static File getLogDirectory() {// 设置Log日志的存放目录，在测试报告中的logs目录下：
		// return ensureExists(ConfigReader.getInstance().getLog_path() +
		// "/logs");
		return ensureExists(getOutputDirectory().getAbsolutePath() + "/logs");
	}

	// 定义放置截图的位置
	public static File getScreensotDirectory() {
		return ensureExists(TestContext.getProperty("screenshot.dir"));
	}

	// 确保文件存在的方法
	private static File ensureExists(String directory) {
		File dir = new File(directory);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	protected static File getExpectedDataDirectory() {// 暂不处理这个；
		String directory = "expectData";
		File dir = new File(directory);
		if (!dir.exists()) {
			logger.error("Couldn't find the expectedData directory.");
		}
		return dir;
	}
}
