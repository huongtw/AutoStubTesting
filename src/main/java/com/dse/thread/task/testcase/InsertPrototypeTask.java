package com.dse.thread.task.testcase;

import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcase_manager.TestPrototype;
import com.dse.testcasescript.object.TestNormalSubprogramNode;
import com.dse.testcasescript.object.TestSubprogramNode;

public class InsertPrototypeTask extends AbstractInsertTestCaseTask<TestPrototype> {

    public InsertPrototypeTask(TestSubprogramNode node) {
        super(node);
    }

    @Override
    protected TestPrototype generateTestCase(TestSubprogramNode navigatorNode) throws Exception {
        ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(navigatorNode.getName());
        String prefix = TestPrototype.PROTOTYPE_SIGNAL + functionNode.getSimpleName();
        String prototypeName = TestCaseManager.generateContinuousNameOfTestcase(prefix);
        return TestCaseManager.createPrototype(functionNode, prototypeName);
    }
}
