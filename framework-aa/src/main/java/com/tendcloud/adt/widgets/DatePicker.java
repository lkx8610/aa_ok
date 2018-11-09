package com.tendcloud.adt.widgets;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import framework.base.Verify;
import framework.base.utils.ReportUtils;

/**
 * @description TODO
 * @author James Guo
 **/
public class DatePicker {
	public static class CalendarEntry {
		int month, day, year;

		public CalendarEntry(int month, int day, int year) {
			this.month = month;
			this.day = day;
			this.year = year;
		}

		public int getMonth() {
			return month;
		}

		public int getDay() {
			return day;
		}

		public int getYear() {
			return year;
		}
	}

	/*public String getSelectedMonth() {
		return getElement().findElement(By.xpath(ClassicDOMIds.DatePicker_SelectedMonth_Xpath)).getText();
	}

	public int getSelectedYear() {
		return Integer
				.parseInt(getElement().findElement(By.xpath(ClassicDOMIds.DatePicker_SelectedYear_Xpath)).getText());
	}

	*//**
	 * 
	 * @param month
	 *            Numeric value of Month. No leading zeroes (e.g., 8)
	 * @param day
	 *            Date of the month. No leading zeros (e.g., 1)
	 * @param year
	 *            Four digit year (e.g., 2018)
	 *//*
	public void setDate(CalendarEntry date) {
		setMonth(date.getMonth());
		setYear(date.getYear());
		setDay(date.getDay());
	}

	private void setCurrentDate(CalendarEntry date, boolean today) {
		setMonth(date.getMonth());
		setYear(date.getYear());
		setDay(date.getDay(), today);
	}

	public void setCurrentDate() {
		Calendar calendar = Calendar.getInstance();
		CalendarEntry calendarEntry = new CalendarEntry(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE),
				calendar.get(Calendar.YEAR));
		setCurrentDate(calendarEntry, true);
	}

	*//**
	 * 
	 * @param month
	 *            Full month name (e.g., December)
	 *//*
	public void setMonth(int month) {
		String selectedMonth = getSelectedMonth();
		String strMonth = getStringValueOfMonth(month);
		if (strMonth.equalsIgnoreCase(selectedMonth)) {
			return;
		}

		int currentMonth = getNumericValueOfMonth(selectedMonth);
		int destination = month;
		int clicksNeeded = destination - currentMonth;
		if (clicksNeeded < 0) {
			for (int i = clicksNeeded; i < 0; i++) {
				clickDecreaseMonth();
			}
		} else {
			for (int i = clicksNeeded; i > 0; i--) {
				clickIncreaseMonth();
			}
		}
	}

	*//**
	 * 
	 * @param year
	 *            Four digit year (ex: 2013)
	 *//*
	public void setYear(int year) {
		int selectedYear = getSelectedYear();
		if (selectedYear == year) {
			return;
		}

		int clicksNeeded = year - selectedYear;

		if (clicksNeeded < 0) {
			for (int i = clicksNeeded; i < 0; i++) {
				clickDecreaseYear();
			}
		} else {
			for (int i = clicksNeeded; i > 0; i--) {
				clickIncreaseYear();
			}
		}
	}

	public void setDay(int day) {
		setDay(day, false);
	}

	public void setDay(int day, boolean today) {
		report(ReportUtils.formatAction("Click day", String.format("'%s'", day)));
		String xpath = null;
		if (today) {
			xpath = String.format(ClassicDOMIds.DatePicker_CurrentDay_Xpath_Format, day);
		} else {
			xpath = String.format(ClassicDOMIds.DatePicker_Day_Xpath_Format, day);
		}
		getElement().findElement(By.xpath(xpath)).click();
		waitForAjaxComplete();
	}

	private int getNumericValueOfMonth(String month) {
		int numericValue = 0;
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] months = dfs.getMonths();
		for (int i = 0; i < months.length; i++) {
			if (months[i].equals(month)) {
				return i + 1;
			}
		}
		return numericValue;
	}

	private String getStringValueOfMonth(int month) {
		DateFormatSymbols dfs = new DateFormatSymbols();
		String[] months = dfs.getMonths();
		return months[month - 1];
	}

	private void clickDecreaseMonth() {
		report(ReportUtils.formatAction("Click decrease month button."));
		getElement().findElement(By.id(ClassicDOMIds.DatePicker_DecreaseMonth_ID)).click();
		waitForAjaxComplete();
	}

	private void clickIncreaseMonth() {
		report(ReportUtils.formatAction("Click increase month button."));
		getElement().findElement(By.id(ClassicDOMIds.DatePicker_IncreaseMonth_ID)).click();
		waitForAjaxComplete();
	}

	private void clickDecreaseYear() {
		report(ReportUtils.formatAction("Click decrease year link."));
		getElement().findElement(By.xpath(ClassicDOMIds.DatePicker_DecreaseYear_Xpath)).click();
		waitForAjaxComplete();
	}

	private void clickIncreaseYear() {
		report(ReportUtils.formatAction("Click increase year link."));
		getElement().findElement(By.xpath(ClassicDOMIds.DatePicker_IncreaseYear_Xpath)).click();
		waitForAjaxComplete();
	}

	@Override
	protected WebElement getElement() {
		List<WebElement> tables = findElements(By.xpath(ClassicDOMIds.DatePicker_Xpath));
		for (WebElement table : tables) {
			if (table.isDisplayed()) {
				return table;
			}
		}
		Verify.verifyFail("Date Picker is not displayed.");
		return null;
	}*/
}
