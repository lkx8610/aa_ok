package classic.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.google.common.base.Function;

import framework.base.BaseFramework;
import framework.base.LoggerManager;
import framework.base.utils.BaseFrameworkUtil;
import framework.base.utils.PerformanceTimer;
import framework.base.utils.ReportUtils;
import framework.webdriver.TestContext;
import framework.webdriver.WebDriverUtils;
/**
 * 页面和组件的基础父类
 * 
 * @author James Guo
 *
 */
public abstract class AbstractBasePage extends BaseFramework {

	protected Logger logger = LoggerManager.getLogger((AbstractBasePage.class.getSimpleName()));
	
	protected String widgetLocator;
	
	private WebElement widgetEle = null;

	/**
	 * 实现PageFactory，完成Webdriver创建和页面元素的初始化
	 */
	public AbstractBasePage() {
		PageFactory.initElements(getWebDriver(), this);
	}

	public AbstractBasePage(String widgetLocator) throws Exception {
		this();
		this.widgetLocator = widgetLocator;
		
		widgetEle = getElement(widgetLocator);//初始化对象时，完成组件的定位。
	}

	//获取已定位的组件：
	public WebElement getWidget(){
		return this.widgetEle;
	}
	
	//获取Webdriver：
	protected WebDriver getWebDriver() {
		return TestContext.getWebDriver();
	}

	protected WebElement findElement(By by) {
		return findElement(null, by);
	}
	//根据传入的：xpath=xxx, id=xxx定位元素的方法: 定位单个元素
	protected WebElement getElement(String locator) throws Exception{
		PerformanceTimer timer = new PerformanceTimer(TestContext.getDomTimeout());
		
		List<WebElement> list = this.getElements(locator);
		
		
		if(null != list && list.size() > 0 ){
			logger.debug("找到元素 ' " + list.get(0).toString() + " -> ' 耗时：" + timer.getElapsedTime() + "(ms)");
			return list.get(0);
		}
		return null;
	}
	
	//定位多个元素：需要传入的Locaotr格式： xpath=xxx, id=xxx定位元素的方法
	protected List<WebElement> getElements(String locator) throws Exception{
		
		List<WebElement> eleList = null;
		
		if(BaseFrameworkUtil.isNotNullOrBlank(locator)){
			int index = locator.indexOf("=");//xpath=//div[]的格式；
			if(index != -1){
				String key = locator.substring(0, index).trim();
				String value = locator.substring(index + 1).trim();
				//try{
					switch(key){
						case "id":
							logger.debug(ReportUtils.formatData("根据元素ID查找元素：[ " + locator + " ]"));
							
							eleList = waitUntilElements(By.id(value), TestContext.getDomTimeout());
							break;
						case "name":
							logger.debug(ReportUtils.formatData("根据元素Name查找元素：[ " + locator + " ]"));
							
							eleList = waitUntilElements(By.name(value), TestContext.getDomTimeout());
							break;
						case "xpath":
							logger.debug(ReportUtils.formatData("根据元素Xpath查找元素：[ " + locator + " ]"));
							
							eleList = waitUntilElements(By.xpath(value), TestContext.getDomTimeout());
							break;
						case "className":
							logger.debug(ReportUtils.formatData("根据元素ClassName查找元素：[ " + locator + " ]"));
							
							eleList = waitUntilElements(By.className(value), TestContext.getDomTimeout());
							break;
						case "css":
							logger.debug(ReportUtils.formatData("根据元素CssSelector查找元素：[ " + locator + " ]"));
							
							eleList = waitUntilElements(By.cssSelector(value), TestContext.getDomTimeout());
							break;
						case "tagName":
							logger.debug(ReportUtils.formatData("根据元素TagName查找元素：[ " + locator + " ]"));
							
							eleList = waitUntilElements(By.tagName(value), TestContext.getDomTimeout());
							break;
						case "linkText":
							logger.debug(ReportUtils.formatData("根据元素链接文字LinkText查找元素：[ " + locator + " ]"));
							
							eleList = waitUntilElements(By.linkText(value), TestContext.getDomTimeout());
							break;
						case "partialLinkText":
							logger.debug(ReportUtils.formatData("根据元素部分链接文字PartialLinkText查找元素：[ " + locator + " ]"));
							
							eleList = waitUntilElements(By.partialLinkText(value), TestContext.getDomTimeout());
							break;
					}
					//return eleList;
				/*}catch(Exception e){
					
				}*/
			}
		}else{
			logger.error("locator cannot be blank, and format should be: 'xpath=xxx / id=xxx / class=xxx / tagName=xxx / link=xxx'");
				throw new RuntimeException("locator's format error, should be: 'xpath=xxx / id=xxx / class=xxx / tagName=xxx / link=xxx'");
		}
		
		return eleList;
	}
	
	protected WebElement findElement(WebElement container, By by) {
		PerformanceTimer timer = new PerformanceTimer();
		try {
			WebElement element = container != null ? container.findElement(by) : getWebDriver().findElement(by);  
			logger.debug("findElement " + by.toString() + " " + timer.getElapsedTimeString());
			return element;			
		} catch (NoSuchElementException ex) {
			throw new NoSuchElementException("No Element in the DOM matching " + by.toString(), ex);
		}
	}

	protected List<WebElement> findElements(By by) {
		return findElements(null, by);
	}

	protected List<WebElement> findElements(WebElement container, By by) {
		PerformanceTimer timer = new PerformanceTimer();
		List<WebElement> elements = container != null ? container.findElements(by) : getWebDriver().findElements(by);
		logger.debug("findElements " + by.toString() + " " + timer.getElapsedTimeString());
		return elements;
	}


	protected WebElement waitUntilElement(final By by) {
		return waitUntilElement(by, TestContext.getDomTimeout());
	}

	protected WebElement waitUntilElement(final WebElement container, final By by) {
		return waitUntilElement(container, by, TestContext.getDomTimeout());
	}

	protected WebElement waitUntilElement(final By by, int timeoutMilliseconds) {
		return waitUntilElement(null, by, timeoutMilliseconds);
	}
	
	/*protected List<WebElement> waitUntilElements(final By by, int timeoutMilliseconds) throws Exception{
		return this.waitUntilElements(null, by, timeoutMilliseconds);
	}*/

	/**
	 * Wait for an element to appear in the DOM relative to a parent container, returns the WebElement if found.
	 */
	protected WebElement waitUntilElement(final WebElement container, final By by, int timeoutMilliseconds) {
		PerformanceTimer perfTimer = new PerformanceTimer(timeoutMilliseconds);
		boolean found = false;
		WebElement webElement = null;
		logger.debug("waitUntilElement: " + by.toString());
		while (! found && ! perfTimer.hasExpired()) {
			try {
				if (container == null) {
					webElement = getWebDriver().findElement(by);
				} else {
					webElement = container.findElement(by);
				}
				found = (webElement != null);
			} catch (NoSuchElementException ex) {
				PerformanceTimer.wait(300);
			}
		}
		if (perfTimer.hasExpired()) {//等待元素超时失败；
			logger.error("timeout waiting for element: " + by.toString());
			throw new NoSuchElementException("Timeout waiting for " + by.toString());
		}
		logger.debug("waitUntilElement " + by.toString() + " " + perfTimer.getElapsedTimeString());
		return webElement;
		
		//return this.waitUntilElements(container, by, timeoutMilliseconds).get(0);
	}
	/**
	 * 查找的结果是多个元素组成的List
	 * @author James Guo
	 * @param container
	 * @param by
	 * @param timeoutMilliseconds
	 * @return
	 */
	protected List<WebElement> waitUntilElements(WebElement container, By by, int timeoutMilliseconds) throws Exception{
		PerformanceTimer perfTimer = new PerformanceTimer(timeoutMilliseconds);
		boolean found = false;
		List<WebElement> webElements = null;
		logger.info("waitUntilElements " + by.toString());
		
		//当未超时，且!found=true;时一直循环：while(true && !timer.hasExpired()){......}
		while (! found && ! perfTimer.hasExpired()) {//found=false; while(!found) => while(true)死循环
			try {
				if (container == null) {
					webElements = getWebDriver().findElements(by);
				} else {
					webElements = container.findElements(by);
				}
				found = (webElements != null);
			} catch (NoSuchElementException ex) {
				PerformanceTimer.wait(300);
				logger.debug(ReportUtils.formatData("尚未找到元素，等待 300ms 后重新尝试查找..."));
			}
		}
		if (perfTimer.hasExpired()) {//等待元素超时失败；
			logger.error("timeout waiting for element: " + by.toString());
			throw new NoSuchElementException("Timeout waiting for " + by.toString());
		}
		logger.debug("waitUntilElement " + by.toString() + " " + perfTimer.getElapsedTimeString());
		return webElements;
	}
	
	protected List<WebElement> waitUntilElements(By by, int timeoutMilliseconds) throws Exception{
		waitForPageLoad();
		waitForAjaxComplete();
		
		PerformanceTimer perfTimer = new PerformanceTimer(timeoutMilliseconds);
		boolean found = false;
		List<WebElement> webElements = null;
		logger.debug("waitUntilElements " + by.toString());
		
		//当未超时，且!found=true;时一直循环：while(true && !timer.hasExpired()){......}
		while (! found && ! perfTimer.hasExpired()) {//found=false; while(!found) => while(true)死循环
			try {
				webElements = getWebDriver().findElements(by);
				found = (webElements != null);
			} catch (Exception ex) {
				PerformanceTimer.wait(300);
				logger.debug(ReportUtils.formatData("尚未找到元素，等待 300ms 后重新尝试查找..."));
			}
		}
		if (perfTimer.hasExpired()) {//等待元素超时失败；
			logger.error("timeout waiting for element: " + by.toString());
			throw new NoSuchElementException("Timeout waiting for " + by.toString());
		}
		
		logger.debug("found element: " + by.toString() + " , time elapsed: " + perfTimer.getElapsedTimeString() + " ms.");
		return webElements;
	}

    /**
     * Wait for an element to be *removed* from the DOM
     */
    protected void waitUntilElementNotPresent(By by) {
        PerformanceTimer perfTimer = new PerformanceTimer(TestContext.getAjaxTimeout());
        logger.debug("waitUntilElementGone " + by.toString());
        try {        	
        	getWebDriver().findElement(by);
        	// Found it, now wait for it to be removed
        	boolean found = true;
        	do {
				try {
					getWebDriver().findElement(by);
					PerformanceTimer.wait(100);
				} catch (NoSuchElementException nsee) {
					found = false;
				}
			} while (found && !perfTimer.hasExpired());

        } catch (NoSuchElementException nsee) {
        	// 
        }

        if (perfTimer.hasExpired()) {
            throw new TimeoutException("Timed out waiting for " + by.toString() + " to disappeared.");
        }

        logger.debug("waitUntilElementGone " + by.toString() + " " + perfTimer.getElapsedTimeString());
    }

    /**
     * Wait for an element in the DOM, then wait for it to become invisible.
     * Used for the 'Processing' mask in the Whiteboards.
     */
    protected void waitUntilElementNotDisplayed(By by) {
        PerformanceTimer perfTimer = new PerformanceTimer(TestContext.getAjaxTimeout());
        logger.debug("waitUntilElementNotDisplayed " + by.toString());
        boolean displayed = false;
       	try {
       		WebElement element = waitUntilElement(by, 500);
       		displayed = element.isDisplayed();
       	} catch (NoSuchElementException ex) {
       		// 
       	} catch (StaleElementReferenceException ex) {
       		//
       	}
        	
    	// Found it, now wait for it to be removed
    	while (displayed && ! perfTimer.hasExpired()) {
			try {
				WebElement element = getWebDriver().findElement(by);
				displayed = element.isDisplayed();
				if (displayed) {
					PerformanceTimer.wait(50);
				}
			} catch (NoSuchElementException ex) {
				displayed = false;
			} catch (StaleElementReferenceException ex) {
				displayed = false;
			}
    	}

        if (perfTimer.hasExpired()) {
            throw new TimeoutException("Timed out waiting for " + by.toString() + " to go away");
        }

        logger.debug("waitUntilElementNotDisplayed " + by.toString() + " " + perfTimer.getElapsedTimeString());
    }

	/**
	 * Used to determine if a web element is enabled
	 */
	protected boolean isElementEnabled(WebElement webElement, String disabledClassAttribute) {
		String classAttribute = webElement.getAttribute("class");
		return webElement.isEnabled() && ! classAttribute.contains(disabledClassAttribute);
	}
	
	/**
	 * Wait until an element is displayed.   Used when the element is in the DOM but not yet visible.
	 *
	 * @param webElement	- The WebElement that is going to be displayed
	 * @return
	 */
	protected WebElement waitUntilElementDisplayed(final WebElement webElement) {
		return waitUntilElementDisplayed(webElement, TestContext.getDomTimeout());
	}
	protected WebElement waitUntilElementDisplayed(final WebElement webElement, int timeoutMilliseconds) {
		PerformanceTimer perfTimer = new PerformanceTimer(timeoutMilliseconds);
		logger.debug("waitUntilElementDisplayed");
		while (! webElement.isDisplayed() && ! perfTimer.hasExpired()) {
			PerformanceTimer.wait(100);
		}
		if (perfTimer.hasExpired()) {
			throw new IllegalStateException("Element is still not displayed");
		}
		logger.debug("waitUntilElementDisplayed " + perfTimer.getElapsedTimeString());
		return webElement;
	}

	//判断页面是否加载完成,如，登录系统后或页面跳转后，判断页面是否加载完成再继续其它的操作：
	  protected Function<WebDriver, Boolean> isPageLoaded() {
	        return new Function<WebDriver, Boolean>() {
	            @Override
	            public Boolean apply(WebDriver driver) {
	                return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
	            }
	        };
	    }

	  //等待页面加载完毕
	  public void waitForPageLoad() {
	        WebDriverWait wait = new WebDriverWait(getWebDriver(), TestContext.getDomTimeout());
	        wait.until(isPageLoaded());
	    }
	    
	/**
	 * Determine if an ajax call is executing and wait for it to complete.
	 */
	protected void waitForAjaxComplete() {
		logger.debug("waitForAjaxComplete");
		PerformanceTimer timer = new PerformanceTimer(TestContext.getAjaxTimeout());

		while (isAjaxExecuting() && ! timer.hasExpired()) {
			PerformanceTimer.wait(100);
		}
		if (timer.hasExpired()) {
			throw new TimeoutException("Timed out waiting for ajax call to complete, exceeded " + TestContext.getAjaxTimeout() + " ms");			
		}
		logger.debug("waitForAjaxComplete elapsed time " + timer.getElapsedTimeString());
	}

	protected boolean isAjaxExecuting() {
		try {
			Long ajaxInProgressIndicator = (Long) WebDriverUtils.executeJavaScript("return ajaxInProgressIndicator;");
			logger.debug("ajaxInProgressIndicator = "
					+ (ajaxInProgressIndicator == null ? " null" : ajaxInProgressIndicator));
			return ajaxInProgressIndicator != null && ajaxInProgressIndicator > 0;
		} catch (WebDriverException e) {
			return false;
		}
	}

	/**
	 * report是对 log.info(msg)的简单封装；
	 */
	protected void report(String message) {
		logger.info(message);
	}

	protected void mouseMove(WebElement element) {
		int xOffset = element.getSize().getWidth() / 2;
		int yOffset = element.getSize().getHeight() / 2;
		(new Actions(TestContext.getWebDriver())).moveToElement(element, xOffset, yOffset).perform();
	}
	
	protected void moveToAndClick(WebElement element) {
		report(ReportUtils.formatAction(String.format("select element %s at (%d, %d)", element.getText(), element.getLocation().x, element.getLocation().y)));
		(new Actions(TestContext.getWebDriver())).moveToElement(element).click().build().perform();
		waitForAjaxComplete();
	}
	/**
	 * Move the mouse to an element and click at the specified offset.
	 */
	protected void moveToAndClick(WebElement element, int xOffset, int yOffset) {
		report(ReportUtils.formatAction(String.format("select element %s at a specic offset (%d, %d)", element.getText() ,xOffset,yOffset)));
		(new Actions(TestContext.getWebDriver())).moveToElement(element, xOffset, yOffset).click().perform();
		
		waitForAjaxComplete();
	}

	
	/**
	 * drag and drop
	 */
	protected void actionDragAndDrop(WebElement source, WebElement target){
		report(ReportUtils.formatAction(String.format("drag the element from %s to %s",source.getLocation().toString(),target.getLocation().toString())));
		(new Actions(TestContext.getWebDriver())).dragAndDrop(source, target).perform();		
	}	
	
	/***
	 * Drag a element for source to point(source.x+xOffset, source.y+yOffset)
	 * @param source
	 * @param xOffset
	 * @param yOffset
	 */
	protected void actionDragAndDrop(WebElement source, int xOffset, int yOffset){
		report(ReportUtils.formatAction(String.format("drag the element from %s, offset to (%d, %d)", source.getLocation().toString(),xOffset,yOffset)));
		(new Actions(TestContext.getWebDriver())).dragAndDropBy(source, xOffset, yOffset).perform();
		waitForAjaxComplete();
	}
	
	/**
	 * Drag a element for source to the point(target.X+xOffsetTarget, target.Y+yOffsetTarget)
	 * @param source
	 * @param target
	 * @param xOffsetTarget
	 * @param yOffsetTarget
	 */
	protected void actionDragAndDrop(WebElement source, WebElement target, int xOffsetTarget, int yOffsetTarget){		
		int xOffsetSource = target.getLocation().getX() + xOffsetTarget - source.getLocation().getX() + 10;
		int yOffsetSource = target.getLocation().getY() + yOffsetTarget - source.getLocation().getY() ;
		actionDragAndDrop(source, xOffsetSource, yOffsetSource);
	}
	
	/**
	 * Keys down the Control key, clicks the element, then keys up on the Control key.
	 */
	public void clickWithControlKeyPressed(WebElement element) {
		report(ReportUtils.formatAction("Press 'Control' and click on element."));
		Actions actions = new Actions(TestContext.getWebDriver());
		actions.keyDown(Keys.CONTROL).perform();
		element.click();
		actions.keyUp(Keys.CONTROL).perform();
		waitForAjaxComplete();
	}
	
	public void setComboBoxValueWithControlKeyPressed(WebElement element, String value) {
		report(ReportUtils.formatAction("Press 'Control' and select '"+value+"' in combo box."));
		Actions actions = new Actions(TestContext.getWebDriver());
		actions.keyDown(Keys.CONTROL).perform();
		setComboBoxValue(element, value);
		actions.keyUp(Keys.CONTROL).perform();
		waitForAjaxComplete();
	}
	
	//当页面中的元素显示出来了，但被遮挡在页面下边，此方法为拉动页面滚动条直到元素被看到，然后再元素做所需的操作：
	public void scrollIntoView(String locator){
		String xpathExpression = locator.substring(locator.lastIndexOf("=") + 1);
		report(ReportUtils.formatAction(String.format("Scroll element %s Into Views ", xpathExpression)));
		//执行JS方式：
		JavascriptExecutor jsExecutor = (JavascriptExecutor) TestContext.getWebDriver();		
		jsExecutor.executeScript("arguments[0].scrollIntoView(true);", getWebDriver().findElement(By.xpath(xpathExpression)));
		report(ReportUtils.formatAction("scroll element into view succeded."));
		//移动到元素，再执行Actions的方式：
		/*WebDriver driver = TestContext.getWebDriver();
		Actions action = (Actions)driver;
		action.moveToElement(element).build().perform();*/
	}
	
	protected void setTextValue(String formId, String fieldName, String value) {
		report(ReportUtils.formatAction("Set Text Field ", formId + " " + fieldName, "'" + value + "'"));
		WebElement form = findElement(By.id(formId));
		setTextValue(form.findElement(By.name(fieldName)), value);
	}
	
	protected String getTextValue(String formId, String fieldName) {
		WebElement form = findElement(By.id(formId));
		return form.findElement(By.name(fieldName)).getAttribute("value");
	}
	
	/**
	 * Enters text using the sendKeys method.  
	 * (Does NOT clear the existing text or use any blur key, such as Enter or Tab.)
	 * 
	 * @param WebElement element
	 * @param String value  Text to Enter
	 */
	protected void enterText(WebElement element, String text) {
		element.sendKeys(text);
	}

	/**
	 * Text fields (Classic screens) identified with a DOM Id 
	 */
	protected void setTextValue(String domId, String value) {
		report(ReportUtils.formatAction("Set Text Field ", domId, "'" + value + "'"));
		setTextValue(findElement(By.id(domId)), value);
	}
	
	protected void setCheckBox(String elementId, boolean checked) {
		report(ReportUtils.formatAction("Set Checkbox ", elementId, checked ? " Checked" : "Unchecked"));
		setCheckbox(waitUntilElementDisplayed(findElement(By.id(elementId))), checked);
	}
	
	protected void setCheckBox(String formId, String fieldName, boolean checked) {
		report(ReportUtils.formatAction("Set Checkbox ", formId + " " + fieldName, checked ? " Checked" : "Unchecked"));
		WebElement form = waitUntilElement(By.id(formId));
		setCheckbox(form.findElement(By.name(fieldName)), checked);
	}

	protected boolean isFieldsetChecked(String fieldSetId) {
		return isChecked(fieldSetId, fieldSetId.replace("-", "."));
	}
	protected void setFieldsetCheckbox(String fieldSetId, boolean checked) {
		setCheckBox(fieldSetId, fieldSetId.replace("-", "."), checked);
	}

	protected boolean isChecked(String elementId) {
		return isChecked(findElement(By.id(elementId)));
	}
	protected boolean isChecked(String formId, String fieldName) {
		WebElement form = findElement(By.id(formId));
		return isChecked(form.findElement(By.name(fieldName)));
	}

	protected boolean isElementEnabled(String formId, String fieldName) {
		WebElement form = findElement(By.id(formId));
		return form.findElement(By.name(fieldName)).isEnabled();
	}

	protected String getInnerText(WebElement element) {
		return element.getText();
	}

	/**
	 *获取Select的值
	 */
	public static List<String> getComboBoxValues(By by) {
		Select select = new Select(TestContext.getWebDriver().findElement(by));
		List<String> options = new ArrayList<String>(select.getOptions().size());
		for (WebElement option : select.getOptions()) {
			options.add(option.getText());
		}
		return options;
	}

	public static String getComboSelection(By by) {
		Select select = new Select(TestContext.getWebDriver().findElement(by));
		return select.getFirstSelectedOption().getText();
	}
	public static void setComboBoxValue(By by, String value) {
		setComboBoxValue(TestContext.getWebDriver().findElement(by), value);
	}
	public static void setComboBoxValue(WebElement element, String value) {
		Select comboBox = new Select(element);
		comboBox.selectByVisibleText(value);
//		if (! comboBox.getFirstSelectedOption().getText().equals(value)) {
//			throw new NoSuchElementException(value + " was not found in Combobox " + element.getAttribute("id"));
//		}
	}
	
	public static void setComboBoxValueCaseInsensitive(WebElement element, String value) {
		Select comboBox = new Select(element);
		List<WebElement> options = comboBox.getOptions();
		for (WebElement option : options) {
			if (value.equalsIgnoreCase(option.getText())) {
				option.click();
			}
		}

	}
	
	// 出现这个异常 stale element reference，
	// 是因为之前获取的元素，因为JS等对页面的刷新，导致元素找不到，下面的方法是尝试多次获取：
	public void retryAndInput(By by, String contents) {
		int retryTimes = 0;
		while (retryTimes <= 3) {
			try {
				WebElement e = getWebDriver().findElement(by);
				if (null != e) {
					e.sendKeys(contents);
				}
			} catch (Exception e) {
				logger.info("cannot locate element, retry again...'" + retryTimes + "' times");
				logger.debug("sleep 100ms");
				waitFor(100);
				retryTimes++;

				// throw exception....
			}
		}
	}

	public WebElement retryIfPageRefreshed(By by, int retryTimes) {
		int retry = 0;
		while (retry <= retryTimes) {
			try {
				WebElement e = getWebDriver().findElement(by);
				if (null != e) {
					logger.debug("found element: '" + e + "'");
					return e;
				}
			} catch (Exception e) {
				waitFor(100);
				
				logger.info("cannot locate element '" + by.toString() + "'. sleep: 100ms. and retry" + " '" + (retry + 1) + " times...");
				// throw exception....
			}
			
			retry++;
		}
		return null;
	}
	
	//为字符串加上双引号：" + value + "
	public static String wrapDoubleQuotes(String value) {
		return "\"" + value + "\"";
	}
	
	//为字符串加上单引号：' + value + '
	public static String wrapSingleQuotes(String value) {
		return "\'" + value + "\'";
	}
	
	/**
	 * 获取页面中错误提示：如不填写必填项时，会在输入框的右侧或下侧出现错误提示：
	 */
	public WebElement getErrorTipEle(String locator){
		WebElement ele = waitUntilElement(By.xpath(locator));
		
		return null == ele ? null : ele;
	}
	public String getErrorTipText(String locator){
		
		return null == getErrorTipEle(locator) ? "" : getErrorTipEle(locator).getText();
	}
	
	/**
	 * 检查页面上的链接
	 */
	public void checkLinks(){
		String locator = "//a[@href]";//TODO
		
	}
	/**
	 * download功能
	 */
	public void download(){
		
	}
	
	/**
	 * 在任意页面返回“产品中心”页面：
	 */
	public void back2ApplicationCenterPage(String locator){
		waitUntilElement(By.xpath(locator)).click();
	}
}











