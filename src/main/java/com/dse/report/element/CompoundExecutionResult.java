package com.dse.report.element;

import com.dse.util.TestPathUtils;
import com.dse.testcase_execution.result_trace.AssertionResult;
import com.dse.testcase_execution.result_trace.gtest.Execution;
import com.dse.testcase_manager.*;
import com.dse.util.Utils;

import java.io.File;
import java.util.List;

public class CompoundExecutionResult extends AbstractExecutionResult {

    public CompoundExecutionResult(ITestCase testCase) {
        super(testCase);
    }

    @Override
    protected void generate(ITestCase tc) {
        CompoundTestCase testCase = (CompoundTestCase) tc;

        Line sectionTitle = new Line(testCase.getName() + " Execution Result", COLOR.DARK);
        title.add(sectionTitle);

        String testPath = testCase.getTestPathFile();

        if (testPath == null || !new File(testPath).exists()) {
            body.add(new Line("Test case haven't executed yet", COLOR.LIGHT));

        } else {
            body.add(new Section.BlankLine());

            testCase.setExecutionResult(new AssertionResult());

            for (TestCaseSlot slot : testCase.getSlots()) {
                String elementName = slot.getTestcaseName();
                TestCase element = TestCaseManager.getBasicTestCaseByName(elementName);

                if (element != null) {
                    BasicExecutionResult elementResults = new BasicExecutionResult(element);
                    testCase.getExecutionResult().increaseTotal();
                    if (element.getExecutionResult().isAllPass()) {
                        testCase.getExecutionResult().increasePass();
                    }

                    body.add(elementResults);
                }
            }

            Table overall = generateCompoundOverallResultTable(testCase);
            body.add(0, overall);
        }

        body.add(new BlankLine());
    }

    private Table generateCompoundOverallResultTable(CompoundTestCase testCase) {
        File exeResult = new File(testCase.getTestPathFile());
        Text resultText = new Text(Execution.RUNTIME_ERROR, TEXT_STYLE.BOLD, "red");

        if (exeResult.exists()) {
            List<TestCaseSlot> slots = testCase.getSlots();
            TestCaseSlot lastSlot = slots.get(slots.size() - 1);
            final String END_TAG = TestPathUtils.END_TAG + lastSlot.getTestcaseName().toUpperCase();

            String content = Utils.readFileContent(exeResult);
            if (content.contains(END_TAG)) {
                AssertionResult result = testCase.getExecutionResult();
                String status = Execution.PASSED + String.format(" (%d/%d)", result.getPass(), result.getTotal());
                resultText = new Text(status, TEXT_STYLE.BOLD, result.isAllPass() ? "green" : "red");
            }
        }

        Table overall = new Table();
        overall.getRows().add(new Table.Row(new Table.Cell<Text>("Result:"), new Table.Cell<>(resultText)));
//        if (execution != null) {
        double executedTime = testCase.getExecutedTime();
        overall.getRows().add(new Table.Row("Executed Time:", String.format("%.3fs", executedTime)));
//        }

        return overall;
    }

}
