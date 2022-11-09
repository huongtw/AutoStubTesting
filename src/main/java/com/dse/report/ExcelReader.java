package com.dse.report;

import com.dse.report.element.IElement;
import com.dse.report.element.Table;
import com.dse.report.element.Text;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelReader {

    public static Workbook readExcel(String excelFilePath) throws IOException {
        // Get file
        InputStream inputStream= ExcelReader.class.getResourceAsStream(excelFilePath);

        if (inputStream == null)
            throw new FileNotFoundException(excelFilePath + " not found");

        // Get workbook
        Workbook workbook = getWorkbook(inputStream, excelFilePath);

        return workbook;
    }

    // Get Workbook
    private static Workbook getWorkbook(InputStream inputStream, String excelFilePath) throws IOException {
        Workbook workbook = null;
        if (excelFilePath.endsWith("xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else if (excelFilePath.endsWith("xls")) {
            workbook = new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }

        return workbook;
    }

    public static Table.Cell<Text> toReportCell(Cell cell) {
        String content = getCellValue(cell);
        boolean isBold = false;
        if (cell instanceof HSSFCell) {
            HSSFCellStyle cellStyle = (HSSFCellStyle) cell.getCellStyle();
            HSSFFont font = cellStyle.getFont(cell.getSheet().getWorkbook());
            isBold = font.getBold();
        } else if (cell instanceof XSSFCell) {
            XSSFCellStyle cellStyle = ((XSSFCell) cell).getCellStyle();
            isBold = cellStyle.getFont().getBold();
        }
        Text text = new Text(content, isBold ? IElement.TEXT_STYLE.BOLD : IElement.TEXT_STYLE.NORMAL);
        if (cell.getHyperlink() != null) {
            String href = cell.getHyperlink().getAddress();
            text.setHref(href);
        }
        return new Table.Cell<>(text, isBold ? IElement.COLOR.MEDIUM : IElement.COLOR.LIGHT);
    }

    // Get cell value
    public static String getCellValue(Cell cell) {
        CellType cellType = cell.getCellTypeEnum();
        String cellValue = null;
        switch (cellType) {
            case BOOLEAN:
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA:
                Workbook workbook = cell.getSheet().getWorkbook();
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                cellValue = String.valueOf(evaluator.evaluate(cell).getNumberValue());
                break;
            case NUMERIC:
                cellValue = String.valueOf(cell.getNumericCellValue());
                break;
            case STRING:
                cellValue = cell.getStringCellValue();
                break;
            case _NONE:
            case BLANK:
            case ERROR:
                break;
            default:
                break;
        }

        return cellValue;
    }

    public static class Position {

        private final int row;
        private final int col;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getCol() {
            return col;
        }

        public int getRow() {
            return row;
        }
    }
}
