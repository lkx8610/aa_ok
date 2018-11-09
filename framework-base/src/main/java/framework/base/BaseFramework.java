package framework.base;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import framework.webdriver.TestContext;

/** 
*@description 
	此类是作为后续有关自动化框架中业务功能类的基类的。
	对基本的和通用 的事务进行了处理
	类中对于参数处理的方法，结合数据管理方案，可以实现不同数据的获取及使用场景。
*@author James Guo
**/
public abstract class BaseFramework {
	private static final Logger log = LoggerManager.getLogger(BaseFramework.class);
	private final HashMap<String, String> parameters = new HashMap<String, String>();
	
	public BaseFramework() {
	}
	
	public void parseParams(String... params) {
		for(String param : params){
			try {
				String name = param.substring(0, param.indexOf("=")).trim();
				String value = param.substring(param.lastIndexOf("=")).trim();
				if(!parameters.containsKey(name)) {
					parameters.put(name, value);
				}else {
					log.warn("重复的参数：" + name);
				}
			}catch(Exception e) {
				log.error("无法解析参数：" + param + "请确认格式是否正确：【name=value】" + e.getMessage());
				e.getMessage();
			}
		}
	}
	public String getParam(String name) {
		if(null != name && !"".equals(name)) {
			if(!parameters.containsKey(name)) {
				log.debug("Requested param: 【" + name + "】doesn't exsits, returing null...");
				return null;
			}else {
				return parameters.get(name);
			}
		}
		log.error("param input error: " + name);
		return null;
	}
	public void setParam(String name, String value) {
		parameters.put(name, value);
	}
	
	protected boolean isParamDefined(String paramName) {
		if(null != paramName && !"".equals(paramName) && parameters.containsKey(paramName)) {
			return true;
		}
		return false;
	}
	protected abstract void prepare();//框架子类需要重写的方法，用于完成一些初始化等的操作；
	
	public <T extends BaseFramework> T navigateTo(T t) {
		t.prepare();
		return t;
	} 
	public static void waitFor(long milles) {//简单等待
		try{
			Thread.sleep(milles);
		}catch(Exception e) {
			log.error("\n timeout with exception: " + e.getMessage());
		}
	}
	/**
	 * 用Robot截图功能, 截图保存成功后，返回该图片在本地的物理地址，方便在测试报告中以链接的形式打开：
	 * @author James Guo
	 * @param destFilePath
	 * @param screenshotFileName
	 * @return
	 * @throws Exception
	 */
	public static File takeScreenshotByRobot(String destFilePath, String screenshotFileName) throws Exception {//测试失败时的截图操作：
		Rectangle screenRect = null;
		String fileExtension = "png";
		
		BufferedImage bufferedImage = null;		
		// 保存截图的文件夹
		File destDir = new File(destFilePath);
		if(! destDir.exists()) {//先创建存放截图的目标文件夹
			destDir.mkdirs();
		}
		//存放截图文件：
		File file = new File(destFilePath, screenshotFileName/* + "." + fileExtension*/);
		
		try {
			screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());// 设置图片范围,这里是载全屏；
			bufferedImage = new Robot().createScreenCapture(screenRect);
			ImageIO.write(bufferedImage, fileExtension, file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}
	
	/**
	 * 判断是否截图成功：
	 * @author James Guo
	 * @param destFilePath
	 * @param screenshotFileName
	 * @return
	 * @throws Exception
	 */
	public static boolean isScreenshotCaptured(String destFilePath, String screenshotFileName) throws Exception {
		return (null != takeScreenshotByRobot(destFilePath, screenshotFileName));
		
	}
	
	//---------------- 操作元素的一般方法封装----------------------//
	/**
	 * 检查和关闭弹出窗口 
	 */
	public static String dismissAlert() {
		String alertText = null;
		WebDriver webDriver = TestContext.getWebDriver();
		if (webDriver != null) {
			try {
				Alert alert = webDriver.switchTo().alert();
				alertText = alert.getText();
				alert.accept();
				Thread.sleep(200);
			} catch (Exception ex) {
				// 如果走到这里， 可以不做任何处理，表示当前没有任何的弹出窗口需要处理
			}
			webDriver.switchTo().defaultContent();
		}
		return alertText;
	}

	/**
	 * 对输入框操作方法的封装
	 */
	public static void setTextValue(By by, String value) {
		setTextValue(TestContext.getWebDriver().findElement(by), value);
	}

	public static void setTextValue(WebElement element, String value) {
		setTextValueWithBlurKey(element, value, Keys.TAB);
	}

	/**
	 * 比如在搜索框中输入关键字后，按Enter键发起查询
	 */
	public static void setTextValueWithEnterKey(WebElement element, String value) {// 输入内容后按“Enter”生效的方法，比如前端页面上的搜索框，在输入完内容后点Enter才能生效；
		setTextValueWithBlurKey(element, value, Keys.ENTER);
	}

	/**
	 * 用于需要对输入的内容进行验证的时候，输入内容后再点tab键，有验证时就会出现验证的panel，对这种验证效果很好,如AJAX验证用户名、密码等；
	 * 等价于先对输入框clear再sendKeys最后Tab；
	 */
	public static void setTextValueWithBlurKey(WebElement element, String value, Keys blurKey) {
		element.sendKeys(Keys.chord(Keys.CONTROL, "a") + Keys.DELETE + value + blurKey);
	}

	//清空文本
	public static void clearText(WebElement element) {
		element.clear();
	}
	//先清空文本，再输入指定内容
	public static void clearAndInput(WebElement element, String contents) throws Exception{
			element.clear();//若元素为空，抛异常
			waitFor(600);
			element.sendKeys(contents);
			log.info("元素信息：" + element.toString() + "; 输入的内容: " + contents);
	}
	//获取属性值
	public static String getAttributeValue(By by) {
		return TestContext.getWebDriver().findElement(by).getAttribute("value");
	}
	public static String getAttributeValue(WebElement element) {
		return element.getAttribute("value");
	}
	//获取innerHTML
	public static String getValue(WebElement element){
		return element.getText();
	}
	/**
	 * 操作 checkboxes
	 */
	public static void setCheckbox(WebElement element, boolean checked) {
		if (checked) {
			if (!element.isSelected()) {
				element.click();
			}
		} else {
			if (element.isSelected()) {
				element.click();
			}
		}
	}

	public static boolean isChecked(WebElement element) {
		return element.isSelected();
	}
}
