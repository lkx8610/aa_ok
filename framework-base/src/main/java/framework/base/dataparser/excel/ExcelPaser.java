package framework.base.dataparser.excel;
/** 
*@description 整个Excel工作薄的读取解析类，可能包含多个Sheet。 
*@author James Guo
**/

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import framework.base.LoggerManager;

public class ExcelPaser {
	Logger logger = LoggerManager.getLogger(this.getClass().getSimpleName());
	private Workbook workbook = null;	
	//定义一个存放各个Sheet的集合,其中Key是每个Sheet的名字，Value是每个Sheet中的具体数据：
	private HashMap<String, ExcelSheetData> sheetDataMap = new HashMap<>();
	private int sheetCount = -1;//定义整个WorkBook的Sheet个数的变量：
	
	public ExcelPaser() {}
	
	public ExcelPaser(String filePath) {
		
		try {
			if (null == filePath.trim() || "".equals(filePath.trim())) {
				logger.error("cannot find the Excel file: '" + filePath + "'. pls double check...");
				throw new FileNotFoundException("cannot find the Excel file: '" + filePath + "'. pls double check...");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(filePath);
		try {
			logger.info("parsing Excel file: '" + filePath);
			workbook = WorkbookFactory.create(in);
			logger.debug("paresed Excel: '" + filePath + "' finished...");
			
			sheetCount = workbook.getNumberOfSheets();//获取整个工作表中的Sheet数
			logger.debug("there're '" + sheetCount + "' sheets in the wookbook");
			
			logger.debug("setting sheet data for every sheet...");
			setSheetDataMap();
			logger.debug("setting sheet data finished...");
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	private void setSheetDataMap() {
		for(int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
			String sheetName = workbook.getSheetName(sheetIndex);//获取每个Sheet的Name。
			logger.debug("current sheet is: '" + sheetName);
			
			//调用SheetData中记取单个Sheet的方法,最后得到一个List<String[])，包含各行数据的List：
			ExcelSheetData sheetData = new ExcelSheetData(workbook.getSheetAt(sheetIndex));
			//将读取到的Sheet的值放入Map中：
			sheetDataMap.put(sheetName, sheetData);
		}
	}

	public String getSheetName(int sheetIndex) {//获取每个Sheet的Name，可能在别处需要调用这个方法，所以单独声明：
		// TODO Auto-generated method stub
		return workbook.getSheetName(sheetIndex);
	}

	public ExcelSheetData getSheetDataByName(String sheetName) {
		//判断所传入的名字是否在sheetDataMap中存在：
		Set<String> sheetNames = sheetDataMap.keySet();
		for(String name : sheetNames) {
			if(sheetName.equals(name)) {
				return sheetDataMap.get(sheetName);
			}
		}
		return null;
	}
	
	public int getSheetIndex(String sheetName) {
		return workbook.getSheetIndex(sheetName);
	}
	
	public String getText(String sheetName, int rowNum, int cellNum) {
		return getSheetDataByName(sheetName).getText(rowNum, cellNum);
	}
	
	public Map<String, ExcelSheetData> getExcelDataMap(){
		return sheetDataMap;
	}
}



















