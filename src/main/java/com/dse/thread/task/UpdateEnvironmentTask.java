package com.dse.thread.task;

import com.dse.environment.Environment;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.EnviroSBFNode;
import com.dse.environment.object.EnviroUUTNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.helps.UIController;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.ParseToLambdaFunctionNode;
import com.dse.search.LambdaFunctionNodeCondition;
import com.dse.search.Search;
import com.dse.search.condition.AbstractFunctionNodeCondition;
import com.dse.search.condition.MacroFunctionNodeCondition;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.testcase_manager.CompoundTestCase;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateEnvironmentTask extends BuildNewEnvironmentTask {

    private final static AkaLogger logger = AkaLogger.get(BuildNewEnvironmentTask.class);

    @Override
    protected void updateRemaining() {
        // TODO: needToRemoveAllPPFunctionNodes
//        for (IFunctionNode functionNode : needToRemoveAllPPFunctionNodes) {
//            ProbePointManager.getInstance().moveAllProbePointsToInvalid(functionNode);
//        }

        super.updateRemaining();

//        UIController.loadProbePoints();
//
//        UIController.insertIncludesForProbePoints();
    }

    @Override
    protected BuildEnvironmentResult call() {
        BuildEnvironmentResult result = super.call();
        updateTestCaseNavigator();
        return result;
    }

    private void updateTestCaseNavigator() {
//        Environment.getInstance().refreshTestCaseScriptRootNode();
        TestcaseRootNode root = Environment.getInstance().getTestcaseScriptRootNode();

        // <<SBF>> and <<UUT>>
        IEnvironmentNode envRoot = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> uutNodes = EnvironmentSearch.searchNode(envRoot, new EnviroUUTNode());
        List<IEnvironmentNode> allSourceCodeNodes = new ArrayList<>(uutNodes);
        List<IEnvironmentNode> sbfNodes = EnvironmentSearch.searchNode(envRoot, new EnviroSBFNode());
        allSourceCodeNodes.addAll(sbfNodes);

        List<ITestcaseNode> unitNodes = TestcaseSearch.searchNode(root, new TestUnitNode());
        for (ITestcaseNode node : unitNodes) {
            TestUnitNode unitNode = (TestUnitNode) node;
            IEnvironmentNode uutNode = null;
            for (IEnvironmentNode n : allSourceCodeNodes) {
                if (n instanceof EnviroUUTNode &&  ((EnviroUUTNode) n).getName().equals(unitNode.getName())) {
                    uutNode = n;
                    break;
                } else if (n instanceof EnviroSBFNode && ((EnviroSBFNode) n).getName().equals(unitNode.getName())) {
                    uutNode = n;
                    break;
                }
            }
            ISourcecodeFileNode fileNode = UIController.searchSourceCodeFileNodeByPath(unitNode.getName());
            if (fileNode != null && uutNode != null) {
//                ParseToLambdaFunctionNode newParser = new ParseToLambdaFunctionNode();
//                newParser.parse(fileNode);
                allSourceCodeNodes.remove(uutNode);

                List<INode> children = Search.searchNodes(fileNode, new AbstractFunctionNodeCondition());
                children.addAll(Search.searchNodes(fileNode, new MacroFunctionNodeCondition()));
                children.addAll(Search.searchNodes(fileNode, new LambdaFunctionNodeCondition()));
                children = children.stream().distinct().collect(Collectors.toList());

                List<ITestcaseNode> subprogramNodes = TestcaseSearch.searchNode(unitNode, new TestNormalSubprogramNode());
                for (ITestcaseNode node1 : subprogramNodes) {
                    TestNormalSubprogramNode normalSubNode = (TestNormalSubprogramNode) node1;
                    try {
                        ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(normalSubNode.getName());
                        children.remove(functionNode);
//                        ICommonFunctionNode prevFunctionNode = UIController.searchFunctionNodeByPathInBackupEnvironment(normalSubNode.getName());
//                        List<TestCase> testCases = TestCaseManager.getTestCasesByFunction(prevFunctionNode);
//
//                        for (TestCase testCase : testCases) {
//                            node.addChild(testCase.getTestNewNode());
//                            testCase.getTestNewNode().setParent(node);
//                        }
                    } catch (FunctionNodeNotFoundException fe) {
                        unitNode.getChildren().remove(normalSubNode);
                    }
                }

                for (INode function : children) {
                    if (function instanceof ICommonFunctionNode) {
                        TestNormalSubprogramNode newSubprogram = new TestNormalSubprogramNode();
                        newSubprogram.setName(function.getAbsolutePath());
                        unitNode.addChild(newSubprogram);
                    }
                }
            } else {
                root.getChildren().remove(unitNode);
            }
        }

        for (IEnvironmentNode scNode : allSourceCodeNodes) {
            TestUnitNode unitNode = new TestUnitNode();

            if (scNode instanceof EnviroUUTNode) {
                unitNode.setName(((EnviroUUTNode) scNode).getName());
            } else if (scNode instanceof EnviroSBFNode) {
                unitNode.setName(((EnviroSBFNode) scNode).getName());
            }

            root.addChild(unitNode);

            ISourcecodeFileNode matchedSourcecodeNode = UIController.searchSourceCodeFileNodeByPath(unitNode.getName());

            // add subprograms
            if (matchedSourcecodeNode != null) {
                ParseToLambdaFunctionNode newParser = new ParseToLambdaFunctionNode();
                newParser.parse(matchedSourcecodeNode);

                List<INode> children = Search.searchNodes(matchedSourcecodeNode, new AbstractFunctionNodeCondition());
                children.addAll(Search.searchNodes(matchedSourcecodeNode, new MacroFunctionNodeCondition()));
                children.addAll(Search.searchNodes(matchedSourcecodeNode, new LambdaFunctionNodeCondition()));
                children = children.stream().distinct().collect(Collectors.toList());

                for (INode function : children) {
                    if (function instanceof ICommonFunctionNode) {
                        TestNormalSubprogramNode newSubprogram = new TestNormalSubprogramNode();
                        newSubprogram.setName(function.getAbsolutePath());
                        unitNode.addChild(newSubprogram);
                    }
                }
            }
        }
//        List<ITestcaseNode> nodes = TestcaseSearch.searchNode(root, new TestNormalSubprogramNode());
//
//        try {
//            for (ITestcaseNode node : nodes) {
//                TestNormalSubprogramNode normalSubNode = (TestNormalSubprogramNode) node;
//                ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPathInBackupEnvironment(normalSubNode.getName());
//                List<TestCase> testCases = TestCaseManager.getTestCasesByFunction(functionNode);
//
//                for (TestCase testCase : testCases) {
//                    node.addChild(testCase.getTestNewNode());
//                    testCase.getTestNewNode().setParent(node);
//                }
//            }
//        } catch (FunctionNodeNotFoundException fe) {
//            logger.debug(fe.getMessage());
//        }

        List<ITestcaseNode> nodes = TestcaseSearch.searchNode(root, new TestCompoundSubprogramNode());
        List<CompoundTestCase> compoundTestCases = new ArrayList<>();
        List<String> compTCNames = new ArrayList<>(TestCaseManager.getNameToCompoundTestCaseMap().keySet());
        for (String name : compTCNames) {
            compoundTestCases.add(TestCaseManager.getCompoundTestCaseByName(name));
        }

        for (CompoundTestCase testCase : compoundTestCases) {
            nodes.get(0).addChild(testCase.getTestNewNode());
            testCase.getTestNewNode().setParent(nodes.get(0));
        }

        Environment.getInstance().saveTestcasesScriptToFile();
    }
}
