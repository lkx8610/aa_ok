package framework.webdriver;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import classic.pages.AbstractBasePage;
import framework.base.LoggerManager;
import framework.base.utils.ReportUtils;
import framework.webdriver.IFrameHelper;
/**
 * @description 可用于切换单层或多层的Frame / Iframe
 * @author James Guo
 **/
public class IFrameHelper extends AbstractBasePage{

	private static final Logger logger = LoggerManager.getLogger(IFrameHelper.class);

	private WebDriver driver;
	private String frameId;
	private List<String> framePath = new ArrayList<>();

	public IFrameHelper(RemoteWebDriver driver, String frameId) {
		this.driver = driver;
		this.frameId = frameId;
	}

	public IFrameHelper(RemoteWebDriver driver, List<String> framePath) {
		this.driver = driver;
		this.framePath = framePath;
		this.frameId = null;
	}

	public void switchFrame() throws Exception {
		if (framePath.size() > 0) {// 多层Iframe时的切换；
			logger.info("多层frame = " + framePath.toString());
			for (int i = 0; i < framePath.size(); i++) {
				logger.info(ReportUtils.formatAction("will switch to :[" + framePath.get(i).toString() + "]"));
				
				driver.switchTo().frame(framePath.get(i));

				waitForPageLoad();
				logger.info("wait for page loaded to switch to the target Frame.");
			}
		}
		if (null != frameId) {//切换单层的Iframe
			logger.info(ReportUtils.formatAction("单层的IFrame：[" + frameId + "]"));
			driver.switchTo().frame(frameId);
		}
	}
	public void back2DefaultContent() {//切换到根Frame
		driver.switchTo().defaultContent();
	}

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		
	}
}
