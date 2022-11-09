package com.dse.report.element;

import com.dse.testcase_execution.result_trace.gtest.Execution;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.util.DateTimeUtils;
import com.dse.util.Utils;

import java.time.LocalDateTime;

public class TestCaseConfiguration extends Section {
    private static final String NO_EXECUTION_RESULTS = "No Execution Results Exist";

    private String testCaseName, subprogramName, unitName;

    private String executedDate, executedTime;

    private String creationDate, creationTime;

    public TestCaseConfiguration(ITestCase testCase) {
        super(testCase.getName() + "-config");
        init(testCase);
        generate();
    }

    private void init(ITestCase testCase) {
        testCaseName = testCase.getName();
        creationDate = testCase.getCreationDate();
        creationTime = testCase.getCreationTime();

        if (testCase instanceof TestCase) {
            ICommonFunctionNode subprogram = ((TestCase) testCase).getRootDataNode().getFunctionNode();
            subprogramName = subprogram.toString();
            unitName = Utils.getRelativePath(Utils.getSourcecodeFile(subprogram));
        } else {
            subprogramName = "&lt;&lt;COMPOUND&gt;&gt;";
            unitName = "&lt;&lt;COMPOUND&gt;&gt;";
        }

        LocalDateTime executionDateTime = testCase.getExecutionDateTime();
        if (executionDateTime == null) {
            executedDate = NO_EXECUTION_RESULTS;
            executedTime = NO_EXECUTION_RESULTS;
        } else {
            executedDate = DateTimeUtils.getDate(executionDateTime);
            executedTime = DateTimeUtils.getTime(executionDateTime);
        }
    }

    public void generate() {
        generateTitle();
        generateBody();
    }

    private void generateTitle() {
        Line sectionTitle = new Line("Test Case Configuration", COLOR.MEDIUM);

        title.add(sectionTitle);
    }

    private void generateBody() {
        Table table = new Table();

        table.getRows().add(new Table.Row("Unit Under Test:", unitName));
        table.getRows().add(new Table.Row("Subprogram:", subprogramName));
        table.getRows().add(new Table.Row("Test Case Name:", testCaseName));
        table.getRows().add(new Table.BlankRow(2));

        table.getRows().add(new Table.Row("Date of Creation:", creationDate));
        table.getRows().add(new Table.Row("Time of Creation:", creationTime));
        table.getRows().add(new Table.BlankRow(2));

        table.getRows().add(new Table.Row("Date of Execution:", executedDate));
        table.getRows().add(new Table.Row("Time of Execution:", executedTime));

        body.add(table);
        body.add(new BlankLine());
    }
}
