package com.dse.guifx_v3.helps;

import com.dse.guifx_v3.controllers.TestCasesExecutionTabController;
import com.dse.guifx_v3.objects.TestCaseExecutionDataNode;
import com.dse.parser.object.FunctionNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionNode;
import com.dse.testcase_manager.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TCExecutionDetailLogger {
    private static Map<TestCase, TestCaseExecutionDataNode> testCaseTestCaseExecutionDataNodeMap = new HashMap<>();
    private static Map<ICommonFunctionNode, List<TestCase>> functionNodeListMap = new HashMap<>();
    private static Map<ICommonFunctionNode, TestCasesExecutionTabController> testCasesExecTabControllerMap = new HashMap<>();

    public static void addTestCasesExecutionTabController(ICommonFunctionNode functionNode, TestCasesExecutionTabController controller) {
        if (!testCasesExecTabControllerMap.containsKey(functionNode)) {
            testCasesExecTabControllerMap.put(functionNode, controller);
        }
    }

    public static TestCasesExecutionTabController getTCExecTabControllerByFunction(ICommonFunctionNode functionNode) {
        return testCasesExecTabControllerMap.get(functionNode);
    }

    public static void clearExecutions(ICommonFunctionNode functionNode) {
        List<TestCase> testCases = functionNodeListMap.get(functionNode);
        if (testCases != null) {
            for (TestCase testCase : testCases) {
                testCaseTestCaseExecutionDataNodeMap.remove(testCase);
            }
        }
        functionNodeListMap.remove(functionNode);
        testCasesExecTabControllerMap.remove(functionNode);
    }

    public static void initFunctionExecutions(ICommonFunctionNode functionNode) {
        if (!functionNodeListMap.containsKey(functionNode)) {
            functionNodeListMap.put(functionNode, new ArrayList<>());
            // generate and add testcase execution tab controller to testCasesExecTabControllerMap
            Factory.generateTestCasesExecutionTab(functionNode);
        }
    }

    public static void addTestCase(ICommonFunctionNode functionNode, TestCase testCase) {
        if (!testCaseTestCaseExecutionDataNodeMap.containsKey(testCase)) {
//            initFunctionExecutions(functionNode);
            if (functionNodeListMap.containsKey(functionNode)) {
                functionNodeListMap.get(functionNode).add(testCase);
                TestCaseExecutionDataNode dataNode = new TestCaseExecutionDataNode();
                dataNode.setName(testCase.getName());
                testCaseTestCaseExecutionDataNodeMap.put(testCase, dataNode);
            }
        }
    }

    public static TestCaseExecutionDataNode getExecutionDataNodeByTestCase(TestCase testCase) {
        return testCaseTestCaseExecutionDataNodeMap.get(testCase);
    }

    public static void logDetailOfTestCase(TestCase testCase, String message) {
        TestCaseExecutionDataNode dataNode = getExecutionDataNodeByTestCase(testCase);
        if (dataNode != null && message != null)
        dataNode.getDetail().add(message);
    }
}
