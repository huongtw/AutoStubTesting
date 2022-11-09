package com.dse.thread.task.testcase_selection;

import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.ProjectNode;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.testcasescript.object.TestNewNode;
import com.dse.testcasescript.object.TestUnitNode;
import com.dse.thread.AbstractAkaTask;
import com.dse.logger.AkaLogger;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ClassifyTestCaseTask extends AbstractAkaTask<ClassifyTestCaseTask.TestCaseClassification> {

    private static final AkaLogger logger = AkaLogger.get(ClassifyTestCaseTask.class);

    private TestUnitNode unitNode;
    private TestCasesTreeItem treeItem;

    public ClassifyTestCaseTask(TestUnitNode unitNode, TestCasesTreeItem treeItem) {
        this.unitNode = unitNode;
        this.treeItem = treeItem;
    }


    @Override
    protected TestCaseClassification call() throws Exception {
        TestCaseClassification classification = new TestCaseClassification();

        ProjectNode root = Environment.getInstance().getProjectNode();
        ISourcecodeFileNode sourcecodeFileNode =
                UIController.searchSourceCodeFileNodeByPath(unitNode, root);

        if (sourcecodeFileNode != null)
            logger.debug("The selected src file = " + sourcecodeFileNode.getAbsolutePath());
        else
            logger.debug(unitNode.getName() + " not found");

        /**
         * STEP: Get unsuccessful test cases
         */
        for (TreeItem<ITestcaseNode> subprogramTreeItem : treeItem.getChildren()) {
            for (Object testcaseItem : subprogramTreeItem.getChildren()) {
                // only consider the test cases checked on test case navigator
                if (testcaseItem instanceof CheckBoxTreeItem &&
                        ((CheckBoxTreeItem) testcaseItem).getValue() != null &&
                        ((ITestcaseNode) ((CheckBoxTreeItem) testcaseItem).getValue()).isSelectedInTestcaseNavigator()) {
                    TestNewNode testNewNode = (TestNewNode) ((CheckBoxTreeItem) testcaseItem).getValue();
                    if (!testNewNode.isPrototypeTestcase()) {
                        String name = testNewNode.getName();
                        TestCase testCase = TestCaseManager.getBasicTestCaseByName(name);
                        if (testCase != null) {
                            logger.debug("Loading test case " + testCase.getName());
                            if (testCase.getStatus().equals(TestCase.STATUS_SUCCESS)
                                    || testCase.getStatus().equals(TestCase.STATUS_RUNTIME_ERR)) {
                                classification.getSuccesses().add(testCase);
                            } else {
                                // add to display on popup
                                classification.getFailures().add(testCase);
                            }
                        }
                    }
                }
            }
        }
        return classification;
    }

    @Override
    public TestCaseClassification get(){
        try {
            return super.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new TestCaseClassification();
        }
    }

    public static class TestCaseClassification {

        private final List<TestCase> successes;
        private final List<TestCase> failures;

        public TestCaseClassification() {
            successes = new ArrayList<>();
            failures = new ArrayList<>();
        }

        public List<TestCase> getFailures() {
            return failures;
        }

        public List<TestCase> getSuccesses() {
            return successes;
        }
    }
}
