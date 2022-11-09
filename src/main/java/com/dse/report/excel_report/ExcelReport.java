package com.dse.report.excel_report;

import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.testcase_execution.result_trace.IResultTrace;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.object.*;
//import com.sun.istack.internal.Nullable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sun.security.provider.SHA;

import java.io.*;
import java.util.*;

public class ExcelReport {
    private LoadingPopupController loadPopup;

    private static final String TEST_REPORT_TEMPLATE = "testReport/Template_Software_Unit_Test_Specification_and_Test_Report.xls";
    private String TEST_REPORT_DIRECTORY_OUTPUT = "";
    private Workbook workbook = null;

    private String PROJECT_NAME = "";
    private final int PROJECT_NAME_ROW = 16;
    private final int PROJECT_NAME_CELL = 6;

    private final String COVER_SHEET = "Cover";
    private final String REFERENCE_DOCUMENT_SHEET = "Reference Document";
    private final String TEST_ENVIRONMENT_SHEET = "Test Environment";
    private final String TEST_CASE_SHEET = "Test case";
    private final String RECORD_OF_CHANGES_SHEET = "Record of Changes";

    private final int DESCRIPTION_ROW = 13;
    private int UNIT_NAME_ROW_FIRST = 12;
    private final int UNIT_NAME_COLUMN = 2;

    private final List<Integer> NUMBER_OF_TESTCASE_COLUMN_DATA_MERGED = Arrays.asList(1, 2, 3, 20, 21, 22, 23, 24, 25, 26, 27);
    private final int START_COLUMN_NEED_MERGED = 1;
    private final int END_COLUMN_NEED_MERGED = 15;

    private final int LAST_LEFT_COLUMN = 26;
    private final int INPUT_NAME_COLUMN = 16;
    private final int INPUT_VALUE_COLUMN = 17;
    private final int EXPECTED_OUTPUT_COLUMN = 18;
    private final int ACTUAL_VALUE_COLUMN = 19;

    private final int TEST_RESULT_COLUMN = 20;

    private Integer LAST_ROW_OF_TESTCASE_SHEET = 13;

    //list of merged cells in conlumn Unit Name
    private List<CellRangeAddress> mergedCellColumnUnitNameList = new ArrayList<>();

    private Map<String, TestCase> testCaseMap = new HashMap<>();
//    private List<TestCaseDataExcel> testCaseDataExcelList = new ArrayList<>();
    private Map<Integer, TestCaseDataExcel> testCaseDataExcelMap = new HashMap<>();

    public static void main(String[] args) {
//        writeData();
//        readData();
//        setTestReportDirectory(System.getProperty("user.dir"));
//        initReport();
//        TEST_REPORT_DIRECTORY = System.getProperty("user.dir") + "/Report.xlsm";
//        initReport();
    }

    public Map<Integer, TestCaseDataExcel> getTestCaseDataExcelMap() {
        return testCaseDataExcelMap;
    }

    public void setTestCaseDataExcelMap(Map<Integer, TestCaseDataExcel> testCaseDataExcelMap) {
        this.testCaseDataExcelMap = testCaseDataExcelMap;
    }

    public void setTEST_REPORT_DIRECTORY_OUTPUT(/*@Nullable*/ String TEST_REPORT_DIRECTORY_OUTPUT) {
        this.TEST_REPORT_DIRECTORY_OUTPUT = TEST_REPORT_DIRECTORY_OUTPUT;
    }

    public void setTestReportDirectory(String directoryPath) {
        setTestReportDirectory(directoryPath + "/Test_case_Report.xlsm");
    }

    public void setTestCaseMap(Map<String, TestCase> testCaseMap) {
        this.testCaseMap = testCaseMap;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    public void setPROJECT_NAME(String PROJECT_NAME) {
        this.PROJECT_NAME = PROJECT_NAME;
    }

    public void setUNIT_NAME_ROW_FIRST(int UNIT_NAME_ROW_FIRST) {
        this.UNIT_NAME_ROW_FIRST = UNIT_NAME_ROW_FIRST;
    }

    public void setLAST_ROW_OF_TESTCASE_SHEET(Integer LAST_ROW_OF_TESTCASE_SHEET) {
        this.LAST_ROW_OF_TESTCASE_SHEET = LAST_ROW_OF_TESTCASE_SHEET;
    }

    public LoadingPopupController getLoadPopup() {
        return loadPopup;
    }

    public void setLoadPopup(LoadingPopupController loadPopup) {
        this.loadPopup = loadPopup;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public List<CellRangeAddress> getMergedCellColumnUnitNameList() {
        return mergedCellColumnUnitNameList;
    }

    public String getTEST_REPORT_DIRECTORY_OUTPUT() {
        return TEST_REPORT_DIRECTORY_OUTPUT;
    }

    public Map<String, TestCase> getTestCaseMap() {
        return testCaseMap;
    }

    /**
     * clone from Template to create a new test report in excel.
     */
    private void cloneTestReportFromTemplate() {
        try {
            InputStream excelFile = getFileFromResourceAsStream(TEST_REPORT_TEMPLATE);
            Workbook wb = new XSSFWorkbook(excelFile);
            workbook = wb;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setProjectName(String name) {
        PROJECT_NAME = name;
    }

    public void initReport() {
        try {
            cloneTestReportFromTemplate();
            FileOutputStream outputStream = new FileOutputStream(TEST_REPORT_DIRECTORY_OUTPUT);
            setProjectNameInReport(PROJECT_NAME);
            refactorTemplate();
//            workbook.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showPopup() {
        if (loadPopup == null) {
            loadPopup = LoadingPopupController.newInstance("Generating Test Report in Excel");
        }
        loadPopup.setText("Generating...");
        loadPopup.initOwnerStage(UIController.getPrimaryStage());
        loadPopup.show();
    }

    /**
     * export all test cases of the project under test into the test report Excel.
     *
     * @param testCaseMap Map of test cases
     */
    public void exportTestcases(Map<String, TestCase> testCaseMap) {
//        showPopup();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initReport();
                    System.out.println("sheet is resizing...");
                    Sheet sheet = workbook.getSheet(TEST_CASE_SHEET);
                    sheet.autoSizeColumn(INPUT_NAME_COLUMN, true);
                    sheet.autoSizeColumn(INPUT_VALUE_COLUMN, true);
                    sheet.autoSizeColumn(UNIT_NAME_COLUMN, true);
                    System.out.println("sheet is resized!");
                    // TODO: 4/20/22 : Parse Data
                    for (TestCase testCase : testCaseMap.values()) {
                        exportOneTestcase(testCase);
                    }
                } catch (Exception e) {
                e.printStackTrace();
                loadPopup.close();
                UIController.showErrorDialog(e.getMessage(), "Test Cases Report", "Generate Failed.");
            }
                try {
                    System.out.println("Writing into " + TEST_REPORT_DIRECTORY_OUTPUT + "...");
                    workbook.write(new FileOutputStream(TEST_REPORT_DIRECTORY_OUTPUT));
                    System.out.println("Written.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Finish Generation!");
                if (loadPopup.getStage().isShowing()) {
                    loadPopup.close();
                    UIController.showSuccessDialog("Finish.", "Test Cases Report", "Generate Successfully.");
                }
                System.out.println("Finish Export Excel report");
            }
        });
        thread.start();
    }

    public void exportOneTestcase(TestCase testCase) {
        //                    TestCase testCase = TestCaseManager.getBasicTestCaseByName(node.getName());
        TestCaseDataExcel testCaseDataExcel = new TestCaseDataExcel(testCase, generateID());// TODO: 3/16/22
        loadPopup.setText("Generating test case: " + testCase.getName());
        System.out.println("-----START iterate test case: " + testCase.getName());
        //                    List<IResultTrace> list = ResultTrace.load(testCase);
        //refactor

        generateTestCaseData(testCaseDataExcel);//todo:

        //end refactor


        //                    addUnitName(testCase);
        System.out.println("-----END iterate test case: " + testCase.getName());
    }

    /**
     * get the directory path of the test report.
     *
     * @return path
     */
    public void getTestReportDirectoryInput() {
        String path = UIController.chooseDirectoryOfNewFile(".xlsx");
        if (path == null) {
            setTEST_REPORT_DIRECTORY_OUTPUT(null);
            return;
        }
        setTEST_REPORT_DIRECTORY_OUTPUT(path);
    }

    /**
     * set Name of the Project under test.
     *
     * @param projectName project name
     */
    private void setProjectNameInReport(String projectName) {
        Sheet coverSheet = workbook.getSheet(COVER_SHEET);
        Row row = coverSheet.getRow(PROJECT_NAME_ROW);
        Cell cell = row.getCell(PROJECT_NAME_CELL);
        cell.setCellValue(projectName);
    }

    public void addElements(Row firstRow, TestCaseDataExcel testCaseDataExcel) {
        //add unit name
        addValueToCell(firstRow.getCell(UNIT_NAME_COLUMN), testCaseDataExcel.getUnitName());
        //add test result
        addValueToCell(firstRow.getCell(TEST_RESULT_COLUMN), String.valueOf(testCaseDataExcel.getResult()));
    }
    public void addValueToCell(Cell cell, String value) {
        cell.setCellValue(value);
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
//        style.setFillPattern(FillPatternType.DIAMONDS);
        cell.getRow().setRowStyle(style);
        cell.setCellStyle(style);
    }

    public void addInpExpActData(Row firstRow, TestCaseDataExcel testCaseDataExcel) {
        List<InputExpectedActualValue> params = testCaseDataExcel.getParameterList();
        int maxNumberRows = params.size();
        int count = LAST_ROW_OF_TESTCASE_SHEET;

        int rowNum = firstRow.getRowNum();
        Sheet sheet = workbook.getSheet(TEST_CASE_SHEET);

        for (InputExpectedActualValue parameter : params) {
            Row row = sheet.getRow(rowNum);

            String name = parameter.getNameInExcel();
            String input = parameter.getInputToString();
            String expected = parameter.getExpectedOutputToString();
            String actual = parameter.getActualOutputToString();

            row.getCell(INPUT_NAME_COLUMN).setCellValue(name);
            row.getCell(INPUT_VALUE_COLUMN).setCellValue(input);
            row.getCell(EXPECTED_OUTPUT_COLUMN).setCellValue(expected);
            row.getCell(ACTUAL_VALUE_COLUMN).setCellValue(actual);

            rowNum++;
        }
    }

    /**
     * get an available cell under for an Unit Name of Test case.
     *
     * @return the final cell
     */
    private Cell getAvailableUnitNameCell() {
        // TODO: 4/6/22 Bug, this bug delete the cell in previous test case 
        Sheet sheet = workbook.getSheet(TEST_CASE_SHEET);
        Row row = sheet.getRow(UNIT_NAME_ROW_FIRST);
        Cell cell = row.getCell(INPUT_NAME_COLUMN);
        if (cell == null) {
            cell = row.createCell(INPUT_NAME_COLUMN);
            System.out.println("CELL NULL");
            return cell;
        }
//        int i = UNIT_NAME_ROW;
        while (!cell.getStringCellValue().equals("")) {
//            i++;
            row = getRowBelow(sheet, row);
            cell = row.getCell(INPUT_NAME_COLUMN);
        }
        UNIT_NAME_ROW_FIRST = cell.getRowIndex();
        return cell;
    }

    /**
     * iterating the data node in console for debugging
     *
     * @param dataNode root data node
     */
    public void iterateTestData(IDataNode dataNode) {
        System.out.println(dataNode.getName());
        if (dataNode instanceof NormalNumberDataNode) {
            System.out.println("------Value: " + ((NormalNumberDataNode) dataNode).getValue());

        }
        List<IDataNode> children = dataNode.getChildren();
        if (children == null) {
            return;
        }
        for (IDataNode node : children) {
            iterateTestData(node);
        }
    }

    /**
     * insert a row below and merge it into current row.
     *
     * @param sheet sheet
     * @param row   current row
     */
    private void addRowBelow(Sheet sheet, Row row) {
//        insertRowBelow(sheet, row);
        mergeRowToBelow(sheet, row);
    }

    private Row insertRowBelow(Sheet sheet, Row row) {
        int i = row.getRowNum() + 1;
        Row r = sheet.getRow(i);
        if (r == null) {
            r = sheet.createRow(i);
            return r;
        }
        while (r.getCell(START_COLUMN_NEED_MERGED) != null && r.getCell(START_COLUMN_NEED_MERGED).equals(row.getCell(START_COLUMN_NEED_MERGED))) {
            i++;
            r = sheet.getRow(i);
            if (r == null) {
                r = sheet.createRow(i);
                return r;
            }
        }
        return r;
    }

    /**
     * get an available row below.
     *
     * @param sheet "Test Case" sheet
     * @param row   row
     * @return row below
     */
    private Row getRowBelow(Sheet sheet, Row row) {
        int nextRowNum = row.getRowNum() + 1;
        Row nextRow = (sheet.getRow(nextRowNum) == null) ? sheet.createRow(nextRowNum) : sheet.getRow(nextRowNum);

        Cell cell = nextRow.createCell(UNIT_NAME_COLUMN);
//        if (cell == null) {
//            cell = nextRow.createCell(UNIT_NAME_COLUMN);
//        }
        while (isMerged(cell, sheet)) {
            nextRowNum++;
            nextRow = sheet.getRow(nextRowNum);
            if (nextRow == null) {
                nextRow = sheet.createRow(nextRowNum);
            }

            cell = nextRow.getCell(UNIT_NAME_COLUMN);
            if (cell == null) {
                cell = nextRow.createCell(UNIT_NAME_COLUMN);
            }
        }

        for (int i = 1; i < LAST_LEFT_COLUMN; i++) {
            Cell c = nextRow.getCell(i);
            if (c == null) {
                c = nextRow.createCell(i);
            }
            setCellBorder(c);
        }
        return cell.getRow();
    }

    /**
     * merge the row below into this row with the column in the list NUMBER_OF_TESTCASE_COLUMN_DATA_MERGED
     *
     * @param sheet sheet "Test Case" often
     * @param row   main row.
     */
    private void mergeRowToBelow(Sheet sheet, Row row) {
        Row rowBelow = getRowBelow(sheet, row);
        UNIT_NAME_ROW_FIRST = row.getRowNum();

//        int startColumn = START_COLUMN_NEED_MERGED;
//        int endColumn = END_COLUMN_NEED_MERGED;
//        for (int i = startColumn; i <= endColumn; i++) {
//            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), rowBelow.getRowNum(), i, i));
//        }
        for (int i : NUMBER_OF_TESTCASE_COLUMN_DATA_MERGED) {
            int rowNum = row.getRowNum();
            int rowBelowNum = rowBelow.getRowNum();
            CellRangeAddress range = new CellRangeAddress(rowNum, rowBelowNum, i, i);
            if (mergedCellColumnUnitNameList == null) {
                mergedCellColumnUnitNameList = new ArrayList<>();
            }
            if (i == UNIT_NAME_COLUMN) {
                boolean isMerged = false;
                for (int j = 0; j < mergedCellColumnUnitNameList.size(); j++) {
                    CellRangeAddress rangeAddress = mergedCellColumnUnitNameList.get(j);
                    int newRangeNum = range.getFirstRow();
                    if (rangeAddress.getFirstRow() == newRangeNum) {
                        mergedCellColumnUnitNameList.set(j, range);
                        isMerged = true;
                        break;
                    }
                }
                if (!isMerged) {
                    mergedCellColumnUnitNameList.add(range);
                }
            }
            sheet.addMergedRegion(range);
        }
    }

    /**
     * get the list of the merged range in this sheet.
     *
     * @param sheet sheet
     * @return list
     */
    private List<CellRangeAddress> getMergedRegion(Sheet sheet) {
        List<CellRangeAddress> list = new ArrayList<>();
        int length = sheet.getNumMergedRegions();
        for (int i = 0; i < length; i++) {
            CellRangeAddress range = sheet.getMergedRegion(i);
            list.add(range);
        }
        //sort this list in order to optimize the searching and checking collision loop.
//        list.sort(new Comparator<CellRangeAddress>() {
//            @Override
//            public int compare(CellRangeAddress o1, CellRangeAddress o2) {
//                int firstCol1 = o1.getFirstColumn();
//                int firstCol2 = o2.getFirstColumn();
//                if (firstCol1 > firstCol2) return 1;
//                else if (firstCol1 < firstCol2) return -1;
//                else return 0;
//            }
//        });
        return list;
    }

    private boolean isMerged(Cell cell, Sheet sheet) {
        if (getMergedCellColumnUnitNameList() == null) {
            return false;
        }
        for (CellRangeAddress range : getMergedCellColumnUnitNameList()) {
            int firstRowRange = range.getFirstRow();
            int lastRowRange = range.getLastRow();
            int firstColRange = range.getFirstColumn();
            int lastColRange = range.getLastColumn();

            int colCell = cell.getColumnIndex();
            int rowCell = cell.getRowIndex();

            if (firstRowRange <= rowCell && rowCell <= lastRowRange
                    && firstColRange <= colCell && colCell <= lastColRange) {
                return true;
            }
        }
        return false;
    }

    private void setCellBorder(Cell cell) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        cell.setCellStyle(style);
    }

    /**
     * add the input data of one test case into the Input column in "Test Case" sheet.
     *
     * @param firstRow        first row of test case
     * @param sheet           "Test Case" sheet
     * @param currentDataNode root data node
     * @param levelTabSpace   level count to tab space
     */
    public void addTestcaseInputValues(/*@Nullable*/ final Row firstRow, Sheet sheet, IDataNode currentDataNode
            , List<IResultTrace> testcaseTraces, int levelTabSpace) {
        int count = LAST_ROW_OF_TESTCASE_SHEET;

        Row row = sheet.getRow(count);
        Cell cellName = row.getCell(INPUT_NAME_COLUMN);
        Cell cellValue = row.getCell(INPUT_VALUE_COLUMN);
        Cell cellExpectedOutput = row.getCell(EXPECTED_OUTPUT_COLUMN);
        Cell cellActualVal = row.getCell(ACTUAL_VALUE_COLUMN);

        String name = "";
        String actualName = "";
        String value = "";
        String expectedValue = "";
        String actualValue = "";

        List<IDataNode> children = currentDataNode.getChildren();
        DataNode root = (DataNode) currentDataNode;
//        for (int i = 0; i < levelTabSpace; i++) {
//            name += "    ";
//        }
        if (root instanceof IValueDataNode) {
            ValueDataNode valueDataNode = (ValueDataNode) root;
            actualName = valueDataNode.getVituralName();
            if (root instanceof NormalDataNode) {
                NormalDataNode node = (NormalDataNode) root;
                name += node.getName();
                value += node.getValue();
//                System.out.println("Name: " + node.getRawType() + "    " + node.getName() + "\tValue: " + node.getValue());
            } else if (root instanceof PointerDataNode || root instanceof ArrayDataNode) {
//                PointerDataNode node = (PointerDataNode) root;
                name += root.getName();
                value += root.getChildren().size();
                root.getVituralName();
//                System.out.println("Name: " + node.getRawType() + "    " + node.getName() + "\tSize: " + node.getChildren().size());
            } else if (root instanceof NullPointerDataNode) {
                NullPointerDataNode node = (NullPointerDataNode) root;
                name += node.getName();
//                System.out.println("Name: " + node.getRawType() + "    " + node.getName() + "\tValue: Null");
            } else if (root instanceof StructureDataNode) {
                StructureDataNode node = (StructureDataNode) root;
                name += node.getName();
//                value += node.getRealType();
//                System.out.println("Name: " + node.getRawType() + "    " + node.getName() + "\tValue: " + node.getRealType());
            } else if (root instanceof UnresolvedDataNode) {
                if (root instanceof FunctionPointerDataNode) {
                    FunctionPointerDataNode node = (FunctionPointerDataNode) root;
                    name += node.getName();
                    value += node.getRealType();
                } else {
                    VoidPointerDataNode node = (VoidPointerDataNode) root;
                    name += node.getName();
                    value += node.getReferenceType();
                }
//                System.out.println();
            } else {
                try {
                    name += root.getName();
//                    System.out.println("Other: " + "\tName: " + root.getName() + "\tValue: ");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            LAST_ROW_OF_TESTCASE_SHEET--;
            levelTabSpace--;
            name += root.getName();
        }

        //set data for expected output and actual output
        if (testcaseTraces != null) {
            for (IResultTrace trace : testcaseTraces) {
                if (actualName.equals(trace.getActualName())) {
                    expectedValue = trace.getExpected();
                    actualValue = trace.getActual();
                }
            }
        }

        System.out.println(name + "    " + value);

        // set data input column
        cellName.setCellValue(name);
        cellValue.setCellValue(value);

        // set data expected output column
        cellExpectedOutput.setCellValue(expectedValue);
        cellActualVal.setCellValue(actualValue);
        for (IDataNode node : children) {
            LAST_ROW_OF_TESTCASE_SHEET++;
            if (count != LAST_ROW_OF_TESTCASE_SHEET) {
                addRowBelow(sheet, firstRow);
            }
            addTestcaseInputValues(firstRow, sheet, node, testcaseTraces, levelTabSpace + 1);
        }
    }

    public void addInpExpAct(/*@Nullable*/ final Row firstRow, Sheet sheet
            , InputExpectedActualValue inputExpectedActualValue) {
        int count = LAST_ROW_OF_TESTCASE_SHEET;

        Row row = sheet.getRow(count);
        Cell cellName = row.getCell(INPUT_NAME_COLUMN);
        Cell cellInput = row.getCell(INPUT_VALUE_COLUMN);
        Cell cellExpectedOutput = row.getCell(EXPECTED_OUTPUT_COLUMN);
        Cell cellActual = row.getCell(ACTUAL_VALUE_COLUMN);

        cellName.setCellValue(inputExpectedActualValue.getName());
        cellInput.setCellValue(inputExpectedActualValue.getInputToString());
        cellExpectedOutput.setCellValue(inputExpectedActualValue.getExpectedOutputToString());
        cellActual.setCellValue(inputExpectedActualValue.getActualOutputToString());
    }

    public Integer generateID() {
        int index = 0;
        Map<Integer, TestCaseDataExcel> testCaseDataExcelMap = getTestCaseDataExcelMap();
        while (testCaseDataExcelMap.get(index) != null) {
            index++;
        }
        return index;
    }

    public void generateTestCaseData(TestCaseDataExcel testCaseDataExcel) {
//        Cell cell = getAvailableUnitNameCell();
//        Row firstRow = cell.getRow();
        int startIndex = getAvailableUnitNameCell().getRow().getRowNum();
        int endIndex = startIndex + testCaseDataExcel.getParameterList().size();

        initialRows(startIndex, testCaseDataExcel.getParameterList().size(), workbook.getSheet(TEST_CASE_SHEET));
//        printTest(startIndex, endIndex);
        mergeUnitNameCell(startIndex, endIndex);
//        printTest(startIndex, endIndex);
        Sheet sheet = workbook.getSheet(TEST_CASE_SHEET);
        Row firstRow = sheet.getRow(startIndex);
        Cell cell = firstRow.getCell(UNIT_NAME_COLUMN);
        addElements(firstRow, testCaseDataExcel);
//        addUnitName(cell, testCaseDataExcel);
        addInpExpActData(firstRow, testCaseDataExcel);
    }

    public void printTest(int start, int end) {
        Sheet sheet = workbook.getSheet(TEST_CASE_SHEET);
        for (;start <= end; start++) {
            Cell cell = sheet.getRow(start).getCell(INPUT_NAME_COLUMN);
            String val = (cell != null) ? cell.getStringCellValue() : "null";
            System.out.println("Line " + start + " :" + val);
        }
    }

    public void mergeUnitNameCell(int start, int end) {
        if (start == end) return;
        Sheet sheet = workbook.getSheet(TEST_CASE_SHEET);
        for (Integer i : NUMBER_OF_TESTCASE_COLUMN_DATA_MERGED) {
            CellRangeAddress range = new CellRangeAddress(start, end, i, i);
            sheet.addMergedRegion(range);
            mergedCellColumnUnitNameList.add(range);
        }
//        CellRangeAddress unitNameRange = new CellRangeAddress(start, end, UNIT_NAME_COLUMN, UNIT_NAME_COLUMN);
//        CellRangeAddress testResultRange = new CellRangeAddress(start, end, TEST_RESULT_COLUMN, TEST_RESULT_COLUMN);
    }

    public void initialRows(int from, int numberOfRows, Sheet sheet) {
        for (int i = from; i <= from + numberOfRows; i++) {
            Row row = (sheet.getRow(i) != null) ? sheet.getRow(i) : sheet.createRow(i);
            initialCells(0, 26, row);
        }
    }
    public void initialCells(int from, int numberOfCells, Row row) {
        for (int i = 0; i < from + numberOfCells; i++) {
            Cell cell = row.getCell(i);
//            System.out.println(row.getRowNum() + " " + i);
            if (cell == null) row.createCell(i);
            cell = row.getCell(i);
//            cell.setCellValue("*");
            setCellBorder(cell);
        }
    }

    private static InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = ExcelReport.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found " + fileName);
        } else {
            return inputStream;
        }
    }

    private void refactorTemplate() {
        Sheet sheet = workbook.getSheet(TEST_CASE_SHEET);
        sheet.removeRow(sheet.getRow(DESCRIPTION_ROW));
        sheet.removeRowBreak(DESCRIPTION_ROW);
    }

}
