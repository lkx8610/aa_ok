package framework.keyworddriven.util;

import static framework.base.Verify.verifyContains;
import static framework.base.Verify.verifyEquals;
import static framework.base.Verify.verifyNotContains;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.testng.Assert;

import classic.pages.AbstractBasePage;
import framework.base.LoggerManager;
import framework.base.utils.PerformanceTimer;
import framework.base.utils.ReportUtils;
import framework.keyworddriven.BaseKeywords;
import framework.webdriver.TestContext;

public class BaseKeywordsUtil{
	private static Logger log = LoggerManager.getLogger(BaseKeywordsUtil.class.getSimpleName());
	
	
}
