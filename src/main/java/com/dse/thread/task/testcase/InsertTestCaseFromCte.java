package com.dse.thread.task.testcase;

import com.dse.environment.Environment;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.testcase_manager.ITestItem;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.TestNewNode;
import com.dse.testcasescript.object.TestSubprogramNode;
import com.dse.thread.AbstractAkaTask;
import javafx.application.Platform;

public class InsertTestCaseFromCte extends AbstractAkaTask<TestCase> {

    private final TestSubprogramNode navigatorNode;
    private TestCase testCase;

    public InsertTestCaseFromCte(TestSubprogramNode navigatorNode, TestCase testCase) {
        this.navigatorNode = navigatorNode;
        this.testCase = testCase;
    }


    @Override
    protected TestCase call() throws Exception {
        Platform.runLater(() -> {
            TestCaseManager.addTestCaseFromCte(testCase);
            TestNewNode testNewNode = testCase.getTestNewNode();
            navigatorNode.getChildren().add(testNewNode);
            testNewNode.setParent(navigatorNode);

            Environment.getInstance().saveTestcasesScriptToFile();

        });
        return testCase;
    }
}
