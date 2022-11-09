package com.dse.report.element;

import com.dse.testcase_execution.result_trace.gtest.Execution;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;

public class BasicTestCaseSection extends AbstractTestCaseSection {

    public BasicTestCaseSection(ITestCase testCase) {
        super(testCase);
    }

    @Override
    protected void generate(ITestCase testCase) {
        Line sectionTitle = new Line("Test Case Section: " + testCase.getName(), COLOR.DARK);
        title.add(sectionTitle);

        body.add(new TestCaseConfiguration(testCase));
        body.add(new TestCaseData((TestCase) testCase));
    }
}
