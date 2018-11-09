package com.tendcloud.adt.widgets;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.tendcloud.adt.pages.common.AbstractBasePage;

import framework.webdriver.TestContext;

/**
 * 对 Select 对象的封装
 * @author James Guo
 *
 */
public class SelectObj extends AbstractBasePage{
	

	public List<String> getSelectValues(By by) {
		Select select = new Select(getWebDriver().findElement(by));
		
		List<String> options = new ArrayList<String>(select.getOptions().size());
		for (WebElement option : select.getOptions()) {
			options.add(option.getText());
		}
		return options;
	}

	public String getSelectedValue(By by) {//获取选中的选项值；
		Select select = new Select(TestContext.getWebDriver().findElement(by));
		return select.getFirstSelectedOption().getText();
	}

	public void setSelectValue(By by, String value) {
		setSelectValue(TestContext.getWebDriver().findElement(by), value);
	}

	public void setSelectValue(WebElement element, String value) {
		Select select = new Select(element);
		select.selectByVisibleText(value);
		// if (! Select.getFirstSelectedOption().getText().equals(value)) {
		// throw new NoSuchElementException(value + " was not found in Select
		// " + element.getAttribute("id"));
		// }
	}

	public void setSelectValueCaseInsensitive(WebElement element, String value) {
		Select select = new Select(element);
		List<WebElement> options = select.getOptions();
		for (WebElement option : options) {
			if (value.equalsIgnoreCase(option.getText())) {
				option.click();
			}
		}

	}
}
