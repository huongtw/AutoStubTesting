package com.dse.thread.task.testcase;

import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.TestSubprogramNode;

public class InsertTestCaseTask extends AbstractInsertTestCaseTask<TestCase> {

    public InsertTestCaseTask(TestSubprogramNode node) {
        super(node);
    }



    @Override
    protected TestCase generateTestCase(TestSubprogramNode navigatorNode) throws FunctionNodeNotFoundException {
        ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(navigatorNode.getName());
        return TestCaseManager.createTestCase(functionNode);
    }
}
