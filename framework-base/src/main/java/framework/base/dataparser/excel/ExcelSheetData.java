package framework.base.dataparser.excel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @description 封装Excel Sheet中数据的工具类
 * @author James Guo
 **/
public class ExcelSheetData {
	private Sheet sheet;
	// 存放整个Sheet中的所有行数据：
	private List<String[]> dataList = new ArrayList<>();

	public ExcelSheetData(Sheet sheet) {
		this.sheet = sheet;
		setSheetDataSet();
	}

	// 解析Sheet，取出所有行，填充到dataList中：
	private void setSheetDataSet() {
		// 记录Excel数据的总列数：
		int columnNum = 0;
		if (null != sheet.getRow(1)) {// 测试数据的第一行是标题，跳过；
			// 得到总列数：
			columnNum = sheet.getRow(1).getLastCellNum() - sheet.getRow(1).getFirstCellNum();
		}
		// 遍历各行，取出数据：
		if (columnNum > 0) {
			for (Row row : sheet) {
				String[] singleRow = new String[columnNum];
				int n = 0;
				for (int i = 0; i < columnNum; i++) {
					singleRow[n] = getCellText(row, i);
					n++;

				}
				if ("".equals(singleRow[0])) {
					continue;
				}
				dataList.add(singleRow);// 遍历完成，dataList中是存放的是当前Sheet中的所有数据；
			}
		}
	}

	@SuppressWarnings("all")
	private String getCellText(Row row, int column) {
		String cellText = "";

		Cell cell = row.getCell(column, Row.CREATE_NULL_AS_BLANK);
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			cellText = cell.getStringCellValue().trim();
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

	public List<String[]> getSheetDataSet() {
		return dataList;
	}

	public int getRowCount() {
		return sheet.getLastRowNum();
	}

	public int getCellCount() {
		Row row = sheet.getRow(0);
		int cellNum = row.getLastCellNum();
		return cellNum > 0 ? cellNum : 0;
	}

	public String[] getRowData(int rowNum) {
		return rowNum > this.getRowCount() && this.dataList.size() > 0 ? null : dataList.get(rowNum);
	}

	// 取指定单元格的值，可能有多个符合的值在一列上：
	public String[] getCallData(int cellIndex) {
		String[] cellData = null;
		if (cellIndex > this.getCellCount()) {
			return null;
		} else {
			if (this.dataList.size() > 0) {
				cellData = new String[dataList.size()];
				int i = 0;
				for (String[] rowData : dataList) {
					if (null != rowData) {
						cellData[i] = rowData[cellIndex];
						i++;
					}
				}
			}
		}
		return cellData;
	}

	public String getText(int rowNum, int cellNum) {
		return this.getRowData(rowNum)[cellNum];
	}

	public static void main(String[] args) throws IOException {
		Workbook wb = new XSSFWorkbook();
		FileOutputStream fileOut = new FileOutputStream("c:/aa.xlsx");
		wb.write(fileOut);
		fileOut.close();
	}
}
