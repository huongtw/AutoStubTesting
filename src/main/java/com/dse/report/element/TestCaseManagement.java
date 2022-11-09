package com.dse.report.element;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.search.Search;
import com.dse.search.SearchCondition;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.MacroFunctionNodeCondition;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.logger.AkaLogger;
import com.dse.util.SpecialCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TestCaseManagement extends Section {

    private static final AkaLogger logger = AkaLogger.get(TestCaseManagement.class);

    private final boolean haveCompound;
    private final List<INode> units;

    public TestCaseManagement(boolean compound, List<INode> units) {
        super("tcs-manage");

        this.haveCompound = compound;
        this.units = units;

        generate();
    }

    private void generate() {
        title.add(new Line("Test Case Management", COLOR.DARK));

        Table table = new Table(false);
        table.getRows().add(new Table.HeaderRow("Unit", "Subprogram", "Test Cases", "Execution Date and Time", "Pass/Fail"));

        if (haveCompound)
            table.getRows().addAll(generateCompoundTable());

        for (INode unit : units) {
            if (units.indexOf(unit) == 0) {
                if (haveCompound)
                    table.getRows().add(new Table.BlankRow(5));
            } else
                table.getRows().add(new Table.BlankRow(5));

            List<Table.Row> row = generateBasicTable(unit);
            table.getRows().addAll(row);
        }

        body.add(table);
    }

    private List<Table.Row> generateBasicTable(INode unit) {
        List<Table.Row> rows = new ArrayList<>();

        AssertionResult summaryResult = new AssertionResult();
        int subprogramLength = 0;
        int testCaseLength = 0;

        List<SearchCondition> conditions = new ArrayList<>();
        conditions.add(new AbstractFunctionNodeCondition());
        conditions.add(new MacroFunctionNodeCondition());
        List<ICommonFunctionNode> subprograms = Search.searchNodes(unit, conditions);

        AtomicInteger counter = new AtomicInteger();
        subprogramLength += subprograms.size();

        for (ICommonFunctionNode subprogram : subprograms) {
            logger.debug(String.format("[%d/%d] Calculate assertion in Unit %s / Subprogram %s", counter.incrementAndGet(), subprograms.size(), unit.getName(), subprogram.getName()));

            if (TestCaseManager.getFunctionToTestCasesMap().get(subprogram) == null)
                continue;

            List<String> testCaseNames = new ArrayList<>(TestCaseManager.getFunctionToTestCasesMap().get(subprogram));

            testCaseNames = testCaseNames.stream().distinct().collect(Collectors.toList());

            boolean firstSubprogram = subprograms.indexOf(subprogram) == 0;

            if (testCaseNames.isEmpty()) {
                String unitCol = SpecialCharacter.EMPTY;
                if (firstSubprogram)
                    unitCol = unit.getName();

                String subprogramCol = subprogram.getName();

                rows.add(new Table.Row(unitCol, subprogramCol,
                        SpecialCharacter.EMPTY, SpecialCharacter.EMPTY, SpecialCharacter.EMPTY
                ));
            }

            for (String name : testCaseNames) {
                TestCase testCase = TestCaseManager.getBasicTestCaseByNameWithoutData(name);

                if (testCase != null) {
                    boolean firstTestCase = testCaseNames.indexOf(name) == 0;
                    firstSubprogram = firstSubprogram && testCaseNames.indexOf(name) == 0;

                    Table.Row row = generateRow(testCase, unit, subprogram, firstSubprogram, firstTestCase);
                    rows.add(row);

                    AssertionResult result = testCase.getExecutionResult();
                    if (result != null) {
                        if (result.isAllPass())
                            summaryResult.increasePass();
                        summaryResult.increaseTotal();
                    }

                    testCaseLength++;
                }
            }
        }

        if (subprogramLength != 0) {
            Table.Row endRow = generateEndRow(
                    String.valueOf(subprogramLength), String.valueOf(testCaseLength), summaryResult);
            rows.add(endRow);
        }

        return rows;
    }

    private List<Table.Row> generateCompoundTable() {
        List<Table.Row> rows = new ArrayList<>();

        AssertionResult summaryResult = new AssertionResult();

        Set<String> testCaseNames = TestCaseManager.getNameToCompoundTestCaseMap().keySet();

        AtomicInteger counter = new AtomicInteger();

        for (String testCaseName : testCaseNames) {
            logger.debug(String.format("[%d/%d] Calculate assertion in %s", counter.incrementAndGet(), testCaseNames.size(), testCaseName));
            CompoundTestCase testCase = TestCaseManager.getCompoundTestCaseByName(testCaseName);

            if (testCase != null) {
                Table.Row row = generateRow(testCase, null, null, false, false);
                rows.add(row);

                AssertionResult result = testCase.getExecutionResult();
                if (result != null) {
                    if (result.isAllPass())
                        summaryResult.increasePass();

                    summaryResult.increaseTotal();
                }
            }
        }

        if (!rows.isEmpty()) {
            Table.Cell<Text> firstCell = rows.get(0).getCells().get(0);
            firstCell.setContent(new Text("<<COMPOUND>>"));
        } else {
            rows.add(new Table.Row("<<COMPOUND>>",
                    SpecialCharacter.EMPTY,
                    SpecialCharacter.EMPTY,
                    SpecialCharacter.EMPTY,
                    SpecialCharacter.EMPTY
            ));
        }

        Table.Row endRow = generateEndRow(SpecialCharacter.EMPTY, String.valueOf(testCaseNames.size()), summaryResult);
        rows.add(endRow);

        return rows;
    }

    private Table.Row generateEndRow(String subprograms, String testCases, AssertionResult summaryResult) {
        Table.Row endRow = new Table.Row(new Text("TOTAL", TEXT_STYLE.BOLD),
                new Text(subprograms, TEXT_STYLE.BOLD),
                new Text(testCases, TEXT_STYLE.BOLD),
                new Text(SpecialCharacter.EMPTY)
        );

        Table.Cell<Text> statusCell = generateStatusCell(summaryResult);
        statusCell.getContent().setStyle(TEXT_STYLE.BOLD);
        endRow.getCells().add(statusCell);

        return endRow;
    }

    private Table.Row generateRow(ITestCase testCase, INode unit, INode subprogram,
                                  boolean firstSubprogram, boolean firstTestCase) {
        String unitCol = SpecialCharacter.EMPTY;
        if (firstSubprogram)
            unitCol = unit.getName();

        String subprogramCol = SpecialCharacter.EMPTY;
        if (firstTestCase)
            subprogramCol = subprogram.getName();

        String testCaseCol = testCase.getName();

        String execDateTimeCol = SpecialCharacter.EMPTY;
        if (testCase.getExecutionDateTime() != null)
            execDateTimeCol = testCase.getExecutionDate() + " " + testCase.getExecutionTime();

        Table.Row row = new Table.Row(unitCol, subprogramCol, testCaseCol, execDateTimeCol);

        AssertionResult result = testCase.getExecutionResult();
        row.getCells().add(generateStatusCell(result));

        return row;
    }

    private Table.Cell<Text> generateStatusCell(AssertionResult result) {
        String statusCol = SpecialCharacter.EMPTY;

        String bgColor;
        if (result == null || result.getTotal() == 0)
            bgColor = COLOR.LIGHT;
        else {
            statusCol = String.format("PASS %d/%d", result.getPass(), result.getTotal());

            if (result.isAllPass())
                bgColor = COLOR.GREEN;
            else if (result.getPass() == 0)
                bgColor = COLOR.RED;
            else
                bgColor = COLOR.YELLOW;
        }

        return new Table.Cell<>(statusCol, bgColor);
    }
}
