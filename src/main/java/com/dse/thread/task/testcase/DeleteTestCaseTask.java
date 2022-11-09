package com.dse.thread.task.testcase;

import com.dse.environment.Environment;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.testcasescript.object.TestCompoundSubprogramNode;
import com.dse.testcasescript.object.TestNewNode;
import com.dse.testcasescript.object.TestNormalSubprogramNode;
import com.dse.thread.AbstractAkaTask;

public class DeleteTestCaseTask extends AbstractAkaTask<DeleteTestCaseTask.TestCaseType> {

    private final TestNewNode node;

//    private final TestCasesTreeItem treeItem;

    public DeleteTestCaseTask(TestNewNode node) {
        this.node = node;
//        this.treeItem = treeItem;
    }

    @Override
    protected TestCaseType call() throws Exception {
        ITestcaseNode parent = node.getParent();

        // remove the testcase
        parent.getChildren().remove(node);

        // save the testcases scripts to file .tst
        Environment.getInstance().saveTestcasesScriptToFile();

        String name = node.getName();

        if (parent instanceof TestNormalSubprogramNode) {
            if (node.isPrototypeTestcase()) {
                // remove from disk
                TestCaseManager.removeBasicTestCase(name);

                return TestCaseType.PROTOTYPE;

            } else {
                // remove from disk
                TestCaseManager.removeBasicTestCase(name);

                return TestCaseType.BASIC;
            }

        } else if (parent instanceof TestCompoundSubprogramNode) {
            // remove from disk
            TestCaseManager.removeCompoundTestCase(name);
            return TestCaseType.COMPOUND;
        }

        return TestCaseType.UNKNOWN;
    }

    public enum TestCaseType {
        BASIC,
        COMPOUND,
        PROTOTYPE,
        UNKNOWN
    }
}
