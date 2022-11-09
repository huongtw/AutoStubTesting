package com.dse.report.element;

import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcasescript.object.TestSubprogramNode;
import com.dse.util.Utils;

import java.util.List;

public class TestCaseTable extends Table {

    private final List<ITestCase> testCases;

    public TestCaseTable(List<ITestCase> testCases) {
        super(false);

        this.testCases = testCases;

        generateHeader();
        generateBody();
    }

    private void generateHeader() {
        rows.add(new HeaderRow("Unit(s)", "Subprogram(s)", "Test Case(s)"));
    }

    private void generateBody() {
        for (ITestCase testCase : testCases) {
            String testCaseName = testCase.getName();

            String functionName = "";
            String unitName = "";

            if (testCase instanceof TestCase) {
                ICommonFunctionNode function = ((TestCase) testCase).getFunctionNode();
                functionName = function.toString();
                unitName = Utils.getRelativePath(Utils.getSourcecodeFile(function));
            } else if (testCase instanceof CompoundTestCase){
                functionName = TestSubprogramNode.COMPOUND_SIGNAL;
                unitName = TestSubprogramNode.COMPOUND_SIGNAL;
            }

            Row row = new Row();
            row.getCells().add(new Cell<Text>(unitName, COLOR.LIGHT));
            row.getCells().add(new Cell<Text>(functionName, COLOR.LIGHT));
            row.getCells().add(new Cell<Text>(testCaseName, COLOR.LIGHT));

            rows.add(row);
        }
    }

    public List<ITestCase> getTestCases() {
        return testCases;
    }
}
