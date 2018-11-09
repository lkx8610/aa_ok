package framework.keyworddriven.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import framework.base.LoggerManager;

public class ExcelUtils {
	private static XSSFSheet excelWSheet;
	private static XSSFWorkbook excelWBook;
	private static Cell cell;
	private static XSSFRow row;

	private static Logger log = LoggerManager.getLogger(ExcelUtils.class.getSimpleName());

	public static void setExcelFile(String excelFileName) throws Exception {
		/*
		 * String filePath = TestContext.getProperty("dataDriven_file_path" +
		 * "/" + excelFileName); InputStream in =
		 * ExcelUtils.class.getClassLoader().getResourceAsStream(filePath);
		 */
		try {
			InputStream excelFile = new FileInputStream(excelFileName);
			log.debug("excel file path: " + excelFileName);

			excelWBook = new XSSFWorkbook(excelFile);

			log.info("<font color='green'><b>成功加载测试文件：[ " + excelFileName + " ]</b></font>");
		} catch (Exception e) {
			log.error("setExcelFile() Exception : " + e.getMessage());
			// ExecutionEngine.result = false;
			throw new RuntimeException("加载Excel测试文件 " + excelFileName + " 失败...[ " + e.getMessage() + " ]");
		}
	}

	public static String getCellData(int rowNum, int colNum, String sheetName) throws Exception {
		try {
			excelWSheet = excelWBook.getSheet(sheetName);
			cell = excelWSheet.getRow(rowNum).getCell(colNum);

			// String cellData =
			// cell.getStringCellValue();//此句中，如果Cell的数据是数字，则在代码中用的时候报错
			String cellData = getCellText(cell);// 获取Cell中的文本值

			if (null == cellData || "".equals(cellData)) {
				return null; // 在通过反射执行ActionKeywords中的方法时，需要这个列上的值做为参数，如果这个列上无值，返回null,
							//反射调用的时候：xxx.invoke(xxx, null);
			}
			return cellData;
		} catch (Exception e) {
			log.error("读取Excel表格数据失败，sheet: " + sheetName + " - row: " + rowNum + " - column: " + colNum + " error "
					+ e.getMessage());
			return null;
		}
	}

	public static int getRowCount(String sheetName) {
		int number = 0;
		try {
			excelWSheet = excelWBook.getSheet(sheetName);
			if (null == excelWSheet) {// 兼容老张的表格：如果没有设置“TestCasesList”这个Sheet，则返回
										// -1，在调用端判断，如果返回-1表示没设置该Sheet，则直接走以后的逻辑；
				return -1;
			}
			number = excelWSheet.getLastRowNum() + 1;

		} catch (Exception e) {
			log.error("getRowCount() Exception: " + e.getMessage());
		}
		return number;
	}

	public static int getRowContains(String testCaseId, int colNum, String sheetName) throws Exception {
		int rowNum = 0;
		try {
			// excelWSheet = ExcelWBook.getSheet(SheetName);
			int rowCount = ExcelUtils.getRowCount(sheetName);
			for (; rowNum < rowCount; rowNum++) {
				if (ExcelUtils.getCellData(rowNum, colNum, sheetName).equalsIgnoreCase(testCaseId)) {
					break;
				}
			}
		} catch (Exception e) {
			log.error("getRowContains Exception: " + e.getCause());
		}
		return rowNum;
	}

	public static int getTestStepsCount(String sheetName, String testCaseID, int testCaseStart) throws Exception {
		try {
			for (int i = testCaseStart; i <= ExcelUtils.getRowCount(sheetName); i++) {
				if (!testCaseID.equals(ExcelUtils.getCellData(i, ExcelConstants.Col_TestCaseID, sheetName))) {
					int number = i;
					return number;
				}
			}
			excelWSheet = excelWBook.getSheet(sheetName);
			int number = excelWSheet.getLastRowNum() + 1;
			return number;
		} catch (Exception e) {
			log.error("getTestStepsCount() error... " + e.getMessage());
			return 0;
		}
	}

	/**
	 * 为Excel表格设置测试结果
	 */
	@SuppressWarnings("all")
	public static void setCellData(String excelFilePath, String Result, int RowNum, int ColNum, String SheetName)
			throws Exception {
		try {
			excelWSheet = excelWBook.getSheet(SheetName);
			row = excelWSheet.getRow(RowNum);
			cell = row.getCell(ColNum, row.RETURN_BLANK_AS_NULL);
			if (cell == null) {
				cell = row.createCell(ColNum);
				cell.setCellValue(Result);
			} else {
				cell.setCellValue(Result);
			}

			FileOutputStream fileOut = new FileOutputStream((new File(excelFilePath)).getAbsolutePath());
			excelWBook.write(fileOut);
			fileOut.flush();
			fileOut.close();
			// excelWBook = new XSSFWorkbook(new
			// FileInputStream(excelFilePath));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取当前的WorkBook中的sheet数量：
	 * @return
	 */
	public static int getSheetsCount(){
		return excelWBook.getNumberOfSheets();
	}
	/**
	 * 获取指定sheetIndex的sheet name:
	 * @param sheetIndex
	 * @return
	 */
	public static String getSheetName(int sheetIndex){
		return excelWBook.getSheetName(sheetIndex);
	}
	@SuppressWarnings("all")
	// private String getCellText(Row row, int column) {
	private static String getCellText(Cell cell) {
		String cellText = "";

		// Cell cell = row.getCell(column, Row.CREATE_NULL_AS_BLANK);
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			cellText = cell.getStringCellValue();//.trim();
			break;
		case Cell.CELL_TYPE_BLANK:
			cellText = "";
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			cellText = String.valueOf(cell.getBooleanCellValue());
			break;
		// 对数字类型的单元格处理
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				cellText = String.valueOf(cell.getDateCellValue());
			} else {
				cell.setCellType(Cell.CELL_TYPE_STRING);
				String value = cell.getStringCellValue();
				if (value.indexOf(".") != -1) {
					cellText = String.valueOf(new Double(value)).trim();
				} else {
					cellText = value.trim();
				}
			}
			break;
		case Cell.CELL_TYPE_ERROR:
			cellText = "";
			break;
		case Cell.CELL_TYPE_FORMULA:
			cell.setCellType(Cell.CELL_TYPE_STRING);
			cellText = cell.getStringCellValue();
			if (null != cellText) {
				cellText = cellText.replaceAll("#N/A", "").trim();
			}
			break;
		default:
			cellText = "";
			break;
		}
		return cellText;
	}
}
