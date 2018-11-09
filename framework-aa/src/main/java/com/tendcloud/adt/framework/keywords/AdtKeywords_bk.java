/*package com.tendcloud.adt.framework.keywords;

import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.tendcloud.adt.framework.constants.BasicElementLocators;
import com.tendcloud.adt.pages.application.AppPlatformsType;
import com.tendcloud.adt.pages.application.ApplicationCenterPage;
import com.tendcloud.adt.pages.application.ApplicationPage;
import com.tendcloud.adt.widgets.DataTable;

import api.framework.domain.ExcelRequestBean;
import api.framework.domain.TDResponse;
import api.framework.util.HttpClientTool;
import framework.base.BaseFramework;
import framework.base.LoggerManager;
import framework.base.exception.StopRunningException;
import framework.base.utils.PerformanceTimer;
import framework.base.utils.ReportUtils;
import framework.keyworddriven.BaseKeywords;
import framework.keyworddriven.executionengine.ExecutionEngine;
import framework.util.FrameworkUtil;
import framework.webdriver.TestContext;

*//**
 * 该类是ADT产品的独有关键字类，继承于BaseActionKeywords
 * 
 * @author James Guo
 *//*
public class AdtKeywords_bk extends BaseKeywords {
	Logger log = LoggerManager.getLogger(this.getClass().getSimpleName());
	// 接口Excel文件的位置；
	String apiExcelFilePath = "";
	ExecutionEngine engine = null;

	// 存放“appName”的List，每添加一条记录时，将appName记录下来，然后将appName=appKey放入tempValueMap,在删除操作时，再根据appName取出appKey:
	List<String> appNameList = new ArrayList<>();

	// 利用Key可重复的MultiMap来存储appKey，因为在存储的时候会有重复的情况出现，导致在存储时后者覆盖前者，在删除时只能删除后者：
	ListMultimap<String, String> appValueMap = ArrayListMultimap.create();

	private void readApiExcelAndExecuteAction(String apiExcelFile) throws Exception {

		if (StringUtils.isBlank(apiExcelFile)) {
			log.info(ReportUtils.formatError("传入的文件路径为null. 请检查..."));
			throw new RuntimeException("传入的文件路径非法：" + apiExcelFile);
		}
		if (Pattern.matches("^[a-zA-Z]{1}:{1}.{1,}$", apiExcelFile)) {
			apiExcelFilePath = apiExcelFile;
		} else {
			URL url = AdtKeywords_bk.class.getClassLoader().getResource(apiExcelFile);

			if (null != url) {
				apiExcelFilePath = URLDecoder.decode(url.getPath(), "utf-8");
			} else {
				log.error(ReportUtils.formatError("未找到测试数据文件： " + apiExcelFile));
				throw new RuntimeException(
						"传入的文件路径不合法，找不到指定的文件: <i><font color='blue'>" + apiExcelFile + "</font></i> 请检查....");
			}
		}
		log.info(ReportUtils.formatAction("准备执行 [ " + apiExcelFilePath + " ]文件，以准备测试所需数据..."));
		// 执行指定的API文件，准备测试数据；读取文件，且执行相应的doGet,doPost...
		engine = new ExecutionEngine(apiExcelFilePath);
	}

	*//**
	 * 准备测试数据的方法（调用API方式）：如，在测试编辑应用前，首先创建应用进行测试数据的准备，保证数据的存在，测试完成，再将测试数据删除；
	 * apiExcelFile: 调用API完成数据准备的Excel文件的位置；
	 * 
	 * @author James Guo
	 *//*
	public void prepareTestData(String stepDescription, String apiExcelFile, String testData, String stepNum) throws Exception {
		stepInfo(stepDescription, stepNum);

		boolean success = true;// 如有听响应码不为200时，将此值置为false, 最后抛出StopRunningException；
		int successAddCount = 0;//记录成功添加的数据数量

		try {
			this.readApiExcelAndExecuteAction(apiExcelFile);// 调用方法，读取文件，且执行相应的doGet,doPost...

			
			}

			// 最后获取所有请求的Bean，并遍历，取出各自的状态码，如果有不为200的，就说明测试数据没有完全添加成功，需要停止运行：
			List<ExcelRequestBean> beanList = engine.getBeanList();
			for (ExcelRequestBean bean : beanList) {
				if (!bean.getExpectedStatusCode().equals("200")) {
					log.error(ReportUtils.formatError("测试数据添加失败, 请检查数据格式：" + bean.getFuncDescription() + " | 响应状态码：" + bean.getExpectedStatusCode()));
					success = false;
				}
			}
			if (success) {
				log.info(ReportUtils.formatData("测试数据准备完成, 成功添加  [ " + successAddCount + " ] 条数据"));
			}

		} catch (Exception e) {
			log.error(ReportUtils.formatError("未知错误，准备测试数据操作失败：" + e.getMessage()));
		} finally {
			if (!success) {
				throw new StopRunningException("准备测试数据失败...");
			}
		}
	}

	*//**
	 * TODO:目录的临时解决方案，将删除接口配置在properties文件中，参数，如appkey,在添加数据时放入List中，为删除方法提供参数：
	 * 删除测试数据的方式（调用API的方式）：当前测试结束后，将准备的数据删除： apiExcelFile:
	 * 调用API完成数据准备的Excel文件的位置；
	 * 
	 * @param apiPropFile:
	 *            存放接口地址的配置文件路径
	 * @throws Exception
	 *//*
	public void delTestData(String stepDescription, String apiPropFile, String testData, String stepNum) throws Exception {
		stepInfo(stepDescription, stepNum);
		log.info(ReportUtils.formatAction("即将删除准备的测试数据..."));

		String path = this.getClass().getClassLoader().getResource(apiPropFile).getPath();
		// 读取配置文件中的接口信息：
		Properties prop = new Properties();
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(apiPropFile);
		prop.load(in);

		log.info("loaded properties file: " + path);

		String apiUrl = prop.getProperty("url");
		log.info("读取的接口地址：" + apiUrl);
		// Map<String, String> headerMap =
		// JsonUtil.parseRequestJson2Map("Content-Type=application/json;charset=UTF-8");
		// 从“this.appValueMap”中取出Appkey：
		int deletedTestData = 0;// 记录删除成功的记录数
		int totalTestDataCount = 0;
		boolean removed = false;

		for (String appName : appNameList) {
			String params = null;
			TDResponse response = null;

			List<String> appKeyList = this.appValueMap.get(appName);// 根据appName的Key取出appKey，是一个List
			totalTestDataCount += appKeyList.size();

			for (String appKey : appKeyList) {
				params = "appkey=" + appKey;// 根据创建的appName取出appKey
				response = HttpClientTool.doGet(apiUrl, null, params);

				if (response.getCode() != 200) {
					log.error(ReportUtils.formatError("删除测试数据失败...[响应状态码: " + response.getCode() + " appName: "
							+ appName + " appKey: " + appKey + "]，请手动删除..."));
					removed = false;
				} else {
					log.info(ReportUtils.formatData("成功删除一条数据，[appName: " + appName + " appKey: " + appKey + " ]"));
					deletedTestData++;
					removed = true;
				}

			}
			if (removed) {// 测试数据删除后，要从临时Map中移除该数据：
				log.info("即将从容器中移除已删除的测试数据：" + appName);
				super.tempValueMap.remove(appName);
				this.appValueMap.removeAll(appName);
			}
		}
		if (deletedTestData == 0) {
			log.error(ReportUtils.formatError(
					"由于某种原因，删除测试数据不成功，请检查，并手动删除. 共有测试数据：" + totalTestDataCount + " 共删除测试数据：" + deletedTestData));
		} else if (deletedTestData < totalTestDataCount) {
			log.error(ReportUtils
					.formatError("由于某种原因，部分数据删除不成功，共有测试数据： " + totalTestDataCount + "共删除测试数据： " + deletedTestData));
		} else {
			log.info(ReportUtils.formatData("测试数据删除完成, 共删除 [ " + deletedTestData + " ]条测试数据."));
		}
	}

	*//** --------------应用CURD相关关键字开始------------- *//*
	*//**
	 * 创建小程序：
	 * 
	 * @author James Guo
	 * @param params:
	 *            创建小程序时所需的参数，传入的参数格式：小程序名,小程序ID,小程序密钥 ；
	 * @param testData：
	 *            null
	 * @throws Exception
	 *//*
	public void createSmallApp(String stepDescription, String params, String testData, String stepNum) throws Throwable {// params：传递来的以逗号分隔的参数列表，分别是“名称、ID、Key”：
		stepInfo(stepDescription, stepNum);
		// 处理参数：
		String[] param = {};
		try {
			if (StringUtils.isNotBlank(params)) {

				if (params.contains(",")) {
					param = params.split(",");
				} else {
					log.info(ReportUtils.formatError("参数列表格式错误，多参数要以\" , \" 分隔. 参数顺序：小程序名称 - 小程序ID - 小程序密钥"));
					throw new StopRunningException(BaseKeywords.StoppingRunning);
				}
			}
			ApplicationPage ap = new ApplicationPage();
			ap.createSmallApp(param[0], param[1], param[2]);// param[0]=name,//
															// param[1]=id ,//
															// param[2]=key
		} catch (Exception e) {
			takeScreenshot();
			throw new StopRunningException(BaseKeywords.StoppingRunning, e);
		} 
	}

	*//**
	 * 
	 * @author James Guo
	 * @param stepDescription:
	 *            步骤信息
	 * @param params：包含[应用名称、应用平台、应用状态、下载链接]
	 * @param testData:
	 *            null
	 * @throws StopRunningException
	 *//*
	public void createApp(String stepDescription, String params, String testData) throws StopRunningException {
		String[] appParams = params.split(",");

		String appName = appParams[0];
		String platform = appParams[1];
		String appStatus = appParams[2];
		String downloadLink = FrameworkUtil.genRandomDownloadLink();
		// 应用平台类型的Map：
		Map<String, AppPlatformsType> appPlatformMap = new HashMap<>();
		appPlatformMap.put("iso", AppPlatformsType.IOS);
		appPlatformMap.put("android", AppPlatformsType.ANDROID);
		appPlatformMap.put("h5", AppPlatformsType.HTML5);
		appPlatformMap.put("smallApp", AppPlatformsType.SMALLAPP);

		if (appPlatformMap.containsKey(platform.toLowerCase())) {
			// platform =
		}
		ApplicationPage ap = new ApplicationPage();

		// ap.createNewApp(appName, platform, appStatus, downloadLink);
	}

	*//**
	 * 根据名称删除单个应用
	 * 
	 * @author James Guo
	 * @param appName：要删除的应用的名称
	 * @param testData：null
	 * @throws StopRunningException
	 *//*
	public void delSingleApp(String stepDescription, String appName, String testData, String stepNum) throws StopRunningException {
		stepInfo(stepDescription, stepNum);
		try {
			ApplicationPage ap = new ApplicationPage();
			ap.delApplication(appName);
		} catch (Exception e) {
			report(ReportUtils.formatError("删除应用[" + appName + "] 时失败..."));
			takeScreenshot();
			throw new StopRunningException(BaseKeywords.StoppingRunning, e);
		} 
	}

	*//**
	 * 批量删除：传入要删除的应用的基名（如传入应用名称的前缀 'ADT-AUTO-TEST'），如果不传参，则意为要全部删除：
	 * 
	 * @author James Guo
	 * @param locator：传入的应用名称的基名（如应用的前缀）
	 * @param testData：null
	 * @throws StopRunningException
	 *//*
	public void batchDel(String stepDescription, String locator, String testData, String stepNum) throws StopRunningException {// locator
		stepInfo(stepDescription, stepNum);
		ApplicationPage ap = new ApplicationPage();

		try {
			ap.batchDel(locator);
		} catch (Exception e) {
			report(ReportUtils.formatError("删除应用时失败.. " + "[" + e.getMessage() + "]"));
			takeScreenshot();
			throw new StopRunningException(BaseKeywords.StoppingRunning, e);
		} 
	}

	*//**
	 * 查找“应用”,
	 * 如果要查找指定具体名字的应用，名字是放在“locator"列中，如果要查找上边步骤创建的应用，则用"testData"列中的变量值从Map中取值；
	 * 
	 * @author James Guo
	 * @param locator：传入一个实际的应用名称，或传入上边步骤查找到的应用时定义的变量名
	 *            ；
	 * @param testData：定义一个变量，用于存储查找到的应用
	 * @throws StopRunningException
	 *//*
	public void searchApp(String stepDescription, String locator, String testData, String stepNum) throws StopRunningException {
		stepInfo(stepDescription, stepNum);
		WebElement e = null;
		String appName = locator;
		try {
			ApplicationCenterPage ap = new ApplicationCenterPage();// 调用封装好的页面方法；
			if (StringUtils.isNotBlank(locator)) {// 如果“locator”列是有值，就按locator列中的值去查找,String
													// appName = locator;
				e = ap.searchSingleApplication(locator);
			} else if (StringUtils.isNotBlank(testData)) {
				appName = (String) tempValueMap.get(testData);// 如果是查找上边步骤创建的应用
																// ，则用变量名从map中取；
				e = ap.searchSingleApplication(appName);// 调用封装好的页面方法；
			}
			// TODO
			tempValueMap.put(locator, e);
		} catch (Exception t) {
			log.info(ReportUtils.formatError("查询应用失败，请检查，要查询的应用：' "
					+ (StringUtils.isNotEmpty(appName) ? appName : locator) + "' " + t.getMessage()));
			takeScreenshot();
			throw new StopRunningException(StoppingRunning, t);// 查询失败时，不再进行后续操作；
		}
	}

	*//**
	 * 获取应用列表：TODO
	 * 
	 * @author James Guo
	 * @param locator
	 * @param testData
	 * @return
	 *//*
	public List<WebElement> getAppList(String stepDescription, String locator, String testData, String stepNum) {
		stepInfo(stepDescription, stepNum);
		List<WebElement> list = null;
		try {
			DataTable dt = new DataTable(locator);
			list = dt.getTableElements();
			report(ReportUtils.formatError("获取列表信息：[" + locator + "]" + " 共有：" + list.size() + " 条记录。"));
		} catch (Exception e) {
			report(ReportUtils.formatError("获取数据列表失败...[" + locator + "]"));
		}
		return list;
	}
	*//** --------------应用CURD相关关键字结束------------- *//*

	*//**
	 * 生成随机的下载链接：
	 * 
	 * @author James Guo
	 * @param locator：null
	 * @param testData:
	 *            定义变量，用于存储生成的下载链接
	 *//*
	public void createDownloadLink(String stepDescription, String locator, String testData, String stepNum) {
		stepInfo(stepDescription, stepNum);
		String downloadLink = null;
		try {
			downloadLink = FrameworkUtil.genRandomDownloadLink();
			log.info("生成随机的下载链接为：" + wrapSingleQuotes(downloadLink));

			tempValueMap.put(testData, downloadLink);// 存放的时候，Key为Excel中的test_data列的值；
		} catch (Exception e) {
			log.info(ReportUtils.formatError("随机下载链接创建不成功，请检查..." + " -> " + e.getMessage()));
		} 
	}
	*//**
	 * ADT 登录系统关键字
	 * @author James Guo
	 * @throws Exception 
	 *//*
	public void login(String stepDescription, String locator, String testData, String stepNum) throws Exception {
		stepInfo(stepDescription, stepNum);
		log.info("正在登录系统...");
		PerformanceTimer timer = new PerformanceTimer(TestContext.getDomTimeout());

		getWebDriver().navigate().to(TestContext.getServerUrl());
		waitForPageLoad();

		WebElement username = getElement("xpath=" + BasicElementLocators.USERNAME_INPUT_XPATH);
		WebElement password = getElement("xpath=" + BasicElementLocators.PASSWORD_INPUT_XPATH);
		
		BaseFramework.clearAndInput(username, TestContext.getUsername());
		completeWait(100);
		BaseFramework.clearAndInput(password, TestContext.getPassword());
		completeWait(100);
		//login:
		getElement("xpath=" + BasicElementLocators.LOGIN_BUTTON_XPATH).click();
		
		boolean isNavigated = isPageNavigated("xpath=" + BasicElementLocators.APPLICATION_CENTER_XPTH);

		//waitForPageNavigated("等待页面跳转...", "xpath=" + BasicElementLocators.APPLICATION_CENTER_XPTH, null);
		if(isNavigated){
			log.info("登录成功, 耗时：" + timer.getElapsedTime() / 1000 + " s");
		}else{
			log.info("登录失败，请检查, 耗时：" + timer.getElapsedTime() / 1000 + " s");
		}
		
	}

	*//**
	 * 无论在哪个页面，都返回“产品中心”页面，在一条新用例测试前，先回到“产品中心”页面,无需传值
	 * @author James Guo
	 * @param locator: null
	 * @param testData: null
	 *//*
	public void back2ApplicationCenterPage(String stepDescription, String locator, String testData, String stepNum) throws Exception {
		stepInfo(stepDescription, stepNum);
		try {
			ApplicationPage ap = new ApplicationPage();
			ap.back2ApplicationCenterPage();
			isPageNavigated("xpath=" + BasicElementLocators.APPLICATION_CENTER_XPTH);

		} catch (Exception e) {
			log.error(ReportUtils.formatError("返回 '产品中心' 页面失败... 原因： " + e.getMessage()));
			takeScreenshot();
			throw new StopRunningException(StoppingRunning, e);
		} 
	}
}
*/