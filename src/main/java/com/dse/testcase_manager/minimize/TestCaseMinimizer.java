package com.dse.testcase_manager.minimize;

import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.testcasescript.object.TestNewNode;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class TestCaseMinimizer implements ITestCaseMinimizer {

    public static final AkaLogger logger = AkaLogger.get(TestCaseMinimizer.class);

    public void clean(List<TestCase> testCases, Scope scope) {
        final int originSize = testCases.size();
        int optimizeSize = originSize;

        logger.debug("Grouping test case by subprogram");
        Map<ICommonFunctionNode, List<TestCase>> group = groupTestCaseByFunction(testCases);

        for (Map.Entry<ICommonFunctionNode, List<TestCase>> entry : group.entrySet()) {
            try {
                List<TestCase> list = entry.getValue();
                if (list.size() > 1) {
                    long before = System.nanoTime();

                    List<TestCase> optimizes = minimize(list, scope);

                    // keep at least 1 test case
                    if (optimizes.isEmpty())
                        optimizes.add(testCases.get(0));

                    List<TestCase> unnecessary = list.stream()
                            .filter(tc -> !optimizes.contains(tc))
                            .collect(Collectors.toList());

                    long after = System.nanoTime();
                    long executedTime = after - before;
                    logger.debug("Executed Time: " + executedTime);

                    optimizeSize -= unnecessary.size();                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             
                    deleteTestCase(unnecessary);
                }                                                                                                                                                                                                                                                                                                                                                                                                                                       
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        logger.debug("Optimize test cases done: " + originSize + " -> " + optimizeSize);
    }

    private void deleteTestCase(List<TestCase> unnecessary) {
        TreeItem<ITestcaseNode> tiRoot = TestCasesNavigatorController
                .getInstance().getTestCasesNavigator().getRoot();

        ITestcaseNode tcRoot = tiRoot.getValue();

        for (TestCase testCase : unnecessary) {
            String name = testCase.getName();

            TestNewNode node = TestcaseSearch.getFirstTestNewNodeByName(tcRoot, name);

            if (node != null) {
                TestCasesTreeItem treeItem = TestcaseSearch
                        .searchTestCaseTreeItem((TestCasesTreeItem) tiRoot, node);

                TestCasesNavigatorController.getInstance().deleteTestCase(node, treeItem);
            }

            TestCaseManager.removeBasicTestCase(name);
        }
    }

    // Grouping test case by subprogram under test
    protected static Map<ICommonFunctionNode, List<TestCase>> groupTestCaseByFunction(List<TestCase> testCases) {
        Map<ICommonFunctionNode, List<TestCase>> map = new HashMap<>();

        for (TestCase testCase : testCases) {
            if (testCase != null) {
                ICommonFunctionNode sut = testCase.getFunctionNode();

                List<TestCase> list = map.get(sut);
                if (list == null)
                    list = new ArrayList<>();

                if (!list.contains(testCase))
                    list.add(testCase);

                map.put(sut, list);
            }
        }

        return map;
    }
}
