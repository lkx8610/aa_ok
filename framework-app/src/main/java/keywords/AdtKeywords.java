package keywords;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;

import framework.base.BaseFramework;
import framework.base.LoggerManager;
import framework.base.exception.StopRunningException;
import framework.base.utils.PerformanceTimer;
import framework.base.utils.ReportUtils;
import framework.keyworddriven.BaseKeywords;
import framework.util.FrameworkUtil;
import framework.webdriver.TestContext;

/**
 * 该类是ADT产品的独有关键字类，继承于BaseActionKeywords
 * 
 * @author James Guo
 */
public class AdtKeywords extends BaseKeywords {
	Logger log = LoggerManager.getLogger(this.getClass().getSimpleName());
	// 接口Excel文件的位置；
	String apiExcelFilePath = "";

	/**
	 * ADT 登录系统关键字
	 * @author James Guo
	 * @throws Exception 
	 */
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
		completeWait(100);
		getElement("xpath=" + BasicElementLocators.APP_LINK_XPATH).click();
		
		boolean isNavigated = isPageNavigated("xpath=" + BasicElementLocators.APPLICATION_CENTER_XPTH);

		//waitForPageNavigated("等待页面跳转...", "xpath=" + BasicElementLocators.APPLICATION_CENTER_XPTH, null);
		if(isNavigated){
			log.info("登录成功, 耗时：" + timer.getElapsedTime() / 1000 + " s");
		}else{
			log.info("登录失败，请检查, 耗时：" + timer.getElapsedTime() / 1000 + " s");
		}
		
	}

}
