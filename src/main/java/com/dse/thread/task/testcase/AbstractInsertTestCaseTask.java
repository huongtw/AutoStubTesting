package com.dse.thread.task.testcase;

import auto_testcase_generation.pairwise.Testcase;
import com.dse.environment.Environment;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcase_manager.ITestItem;
import com.dse.testcasescript.object.TestNewNode;
import com.dse.testcasescript.object.TestSubprogramNode;
import com.dse.thread.AbstractAkaTask;

public abstract class AbstractInsertTestCaseTask<T extends ITestItem> extends AbstractAkaTask<T> {

    private final TestSubprogramNode navigatorNode;

    public AbstractInsertTestCaseTask(TestSubprogramNode node) {
        navigatorNode = node;
    }


    @Override
    protected T call() throws Exception {
        T testCase = generateTestCase(navigatorNode);

        // update test script
        TestNewNode testNewNode = testCase.getTestNewNode();
        navigatorNode.getChildren().add(testNewNode);
        testNewNode.setParent(navigatorNode);

        Environment.getInstance().saveTestcasesScriptToFile();

        return testCase;
    }

    protected abstract T generateTestCase(TestSubprogramNode navigatorNode) throws Exception;
}
