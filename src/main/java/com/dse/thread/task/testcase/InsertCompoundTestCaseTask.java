package com.dse.thread.task.testcase;

import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.TestSubprogramNode;

public class InsertCompoundTestCaseTask extends AbstractInsertTestCaseTask<CompoundTestCase> {

    public InsertCompoundTestCaseTask(TestSubprogramNode node) {
        super(node);
    }

    @Override
    protected CompoundTestCase generateTestCase(TestSubprogramNode navigatorNode) {
        return TestCaseManager.createCompoundTestCase();
    }
}
