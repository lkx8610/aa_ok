package com.tendcloud.adt.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.tendcloud.adt.pages.common.AbstractBasePage;

import framework.base.LoggerManager;
import framework.webdriver.TestContext;
/**
 * 表格中的行、列都是从 1 开始的；表格定位：//table
 * 第一行是标题行：定位：//table/tbody/tr[1]
 * 第一行的标题列是 th：//table/tbody/tr/th
 * 
 * 真实数据行是第二行开始：//table/tbody/tr[2],若要返回全部数据行，可以用先返回整个Table.rows-1；遍历数据行时从第二行真实数据开始；
 * 真实数据列是: td : //table/tbody/tr/td
 * 返回指定行的所有的列： //table/tbody/tr[3]/td 返回的是第三行的所有的列；
 * @author James Guo
 */
public class DataTable extends AbstractBasePage {
	private Logger log = LoggerManager.getLogger(DataTable.class.getName());
	
	//Table元素的定位串：//xxx/table
	String tableLocator;

	//构造方法，初始化Webdriver与Table元素定位串：
	public DataTable(String tableLocator){
		super();
		this.tableLocator = tableLocator;
	}

	//获取Table中的所有数据列表：
	public  List<WebElement> getTableElements() throws Exception{//tr:产品中心的Table数据：//table/tbody/tr
		List<WebElement> eleList = waitUntilElements(By.xpath(tableLocator + "//tr"), TestContext.getDomTimeout());//包含标题行
		
		List<WebElement> dataList = new ArrayList<>();//仅存放数据行，不包含标题行；
		if(eleList.size() -1 <= 0 ){//返回的数据包括了标题行，故 -1；
			//return Collections.emptyList();//如果找列表元素为空，则返回一个空的列表：
			return null;
		}

		for(int i=1; i<eleList.size(); i++){//遍历原始列表，将原始包含标题行的列表数据放到只包含数据行的列表中
			dataList.add(eleList.get(i));
		}
		return dataList;
	}
	
	//获取总行数：
	public int getRowCout() throws Exception{
		return getTableElements().size();//返回的数据包括了标题行，故 -1；
	}
	
	//获取一行中的总列数：
	public int getColumnCount(){
		
		List<WebElement> tableCols = getWebDriver().findElements(By.xpath(tableLocator + "/tbody/tr[2]/td"));//TODO 逻辑不对
		
		int colunmCount = tableCols.size();
		return colunmCount;
	}
	//表格内查找,找到返回行数，找不到，返回 -1：
	private int searchWithinGivenColumn(int maxRow, int MaxColumn, String content){
		int totalRowCount = maxRow;//指定查找范围；
		int totalColumnCont = MaxColumn;
		int returnNo = -1;//如没找到，返回-1；
		String cellText = "";
		
		//从第一行第一列按照行序列指定的最大行列数内进行查找
		labelBreak:
		for(int rowId = 2; rowId <= totalRowCount; rowId++){
			for(int columnId = 1; columnId <= totalColumnCont; columnId++){
				try{
					cellText = getCellText(rowId, columnId);
					log.debug("[" + rowId + "]" + "[" + columnId + "] = " + cellText);
				}catch(Exception e){
					log.error("failed to find row with contntent:" + content);
					log.debug("error happened:" + e.getMessage());
				}
				if(cellText.hashCode() == content.hashCode()){
					returnNo = rowId;
					log.debug("found rowId[" + rowId + "] columnId[" + columnId + "] cellText ==" + cellText);
					
					break labelBreak;//查找到了，退出双重循环；
				}
			}
		}
		return returnNo; //返回行数；
	}
	
	public int findFirstRowWithExactContent(String content) throws Exception{
		int totalRowCount = getRowCout();//获取该表格总行数
		logger.debug("the total rows: " + wrapSingleQuotes(totalRowCount + ""));
		
		int totalColumnCount = getColumnCount();//获取表格的总列数；
		logger.debug("the total columns: " + wrapSingleQuotes(totalColumnCount + ""));
		
		return searchWithinGivenColumn(totalRowCount, totalColumnCount, content);//执行查找并返回对应的行数，若没找到，返回-1；
		
	}
	
	public String getCellText(int row, int column) throws Exception{
		String cellText = "";
		WebElement element = getItemInCell(row, column, "/a");//查找具体列中的
		if(null != element){
			cellText = element.getText();//获取元素文本
		}
		return cellText;
	}
	
	protected WebElement getItemInCell(int rowId, int colId, String itemString) throws Exception{
		String locator = ("xpath=//tbody/tr[" + rowId + "]/td[" + colId + "]" + itemString).trim();
		WebElement element = getElement(locator);
		return element;
	}
	
	//如果表格中超链接，"应用名称", "应用操作" 此方法提供这个动作；
	public void clickLinkInCell(int rowId, int colId) throws Exception{
		WebElement element = getItemInCell(rowId, colId, "/a");
		if(null != element){
			element.click();
		}
	}
	//获取某一列的数据：
	public WebElement getCellData(String name){
		
		return null;
	}
	
}
