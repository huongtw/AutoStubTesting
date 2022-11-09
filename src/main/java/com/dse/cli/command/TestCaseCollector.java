package com.dse.cli.command;

import com.dse.environment.Environment;
import com.dse.parser.object.IFunctionNode;
import com.dse.parser.object.INode;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.object.TestSubprogramNode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TestCaseCollector {
    public static String[] get(String unit, String subprogram, String[] names) throws Exception {
        List<String> testCases = new ArrayList<>();

        // select specific test cases
        if (names != null && names.length > 0)
            testCases = Arrays.asList(names);

        else if (unit != null) {
            // compound test case
            if (unit.equals(TestSubprogramNode.COMPOUND_SIGNAL) || subprogram.equals(TestSubprogramNode.COMPOUND_SIGNAL))
                testCases.addAll(TestCaseManager.getNameToCompoundTestCaseMap().keySet());
            // basic test case
            else
                testCases.addAll(getBasicTestCase(unit, subprogram));

        }

        return testCases.toArray(new String[0]);
    }

    private static List<String> getBasicTestCase(String unit, String subprogram) throws Exception {
        List<String> testCases = new ArrayList<>();

        // find unit physical node
        INode root = Environment.getInstance().getProjectNode();
        String unitRelativePath = File.separator + unit;
        List<INode> unitNodes = Search.searchNodes(root, new SourcecodeFileNodeCondition(), unitRelativePath);

        if (unitNodes.isEmpty())
            throw new Exception("unit " + unit + " not found");

        for (INode unitNode : unitNodes) {
            // select specific subprogram under test
            if (subprogram != null) {
                testCases.addAll(
                        getBasicTestCases(unitNode, subprogram));
            }
            // select all subprogram under selected unit
            else
                testCases.addAll(getBasicTestCases(unitNode));
        }

        return testCases;
    }

    private static Set<String> getBasicTestCases(INode unitNode, String subprogram) throws Exception {
        IFunctionNode subprogramNode = (IFunctionNode) Search
                .searchNodes(unitNode, new AbstractFunctionNodeCondition())
                .stream()
                .filter(f -> ((IFunctionNode) f).getSimpleName().equals(subprogram))
                .findFirst()
                .orElse(null);

        if (subprogramNode == null)
            throw new Exception("subprogram " + subprogram + " not found");

        return TestCaseManager.getFunctionToTestCasesMap().get(subprogramNode);
    }

    private static List<String> getBasicTestCases(INode unitNode) {
        List<String> testCases = new ArrayList<>();

        List<IFunctionNode> subprogramNodes = Search
                .searchNodes(unitNode, new AbstractFunctionNodeCondition())
                .stream()
                .map(s -> (IFunctionNode) s)
                .collect(Collectors.toList());

        for (IFunctionNode subprogramNode : subprogramNodes)
            testCases.addAll(TestCaseManager
                    .getFunctionToTestCasesMap().get(subprogramNode));

        return testCases;
    }
}
