package com.dse.report.element;

import com.dse.testcase_execution.result_trace.gtest.Execution;
import com.dse.testcase_manager.ITestCase;

public abstract class AbstractExecutionResult extends Section {

    protected static final String C_FAILURE_TAG = "<CUNIT_RUN_TEST_FAILURE>";

    public AbstractExecutionResult(ITestCase testCase) {
        super(testCase.getName() + "-result");
        generate(testCase);
    }

    protected abstract void generate(ITestCase testCase);
}
