package com.dse.report.element;

import com.dse.testcase_execution.result_trace.gtest.Execution;
import com.dse.testcase_manager.ITestCase;

public abstract class AbstractTestCaseSection extends Section {

    public AbstractTestCaseSection(ITestCase testCase) {
        super(testCase.getName());

        generate(testCase);
    }

    protected abstract void generate(ITestCase testCase);
}
