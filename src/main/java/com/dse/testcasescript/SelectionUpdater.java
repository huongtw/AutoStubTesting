package com.dse.testcasescript;

import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.testcasescript.object.*;

import java.util.List;

/**
 * Save the selection in test case navigator
 */
public class SelectionUpdater {
    public static void check(ITestcaseNode checkedNode) {
        List<ITestcaseNode> allChildren = TestcaseSearch.searchNode(checkedNode, new AbstractTestcaseNode());
        for (ITestcaseNode child : allChildren)
            child.setSelectedInTestcaseNavigator(true);

        while (checkedNode != null) {
            checkedNode.setSelectedInTestcaseNavigator(true);
            checkedNode = checkedNode.getParent();
        }
    }

    public static void uncheck(ITestcaseNode checkedNode) {
        List<ITestcaseNode> allChildren = TestcaseSearch.searchNode(checkedNode, new AbstractTestcaseNode());
        for (ITestcaseNode child : allChildren)
            child.setSelectedInTestcaseNavigator(false);

        while (checkedNode != null) {
            boolean checkedAtLeastOne = false;
            for (ITestcaseNode child : checkedNode.getChildren())
                if (child.isSelectedInTestcaseNavigator()) {
                    checkedAtLeastOne = true;
                    break;
                }

            if (checkedAtLeastOne)
                checkedNode.setSelectedInTestcaseNavigator(true);
            else
                checkedNode.setSelectedInTestcaseNavigator(false);

            checkedNode = checkedNode.getParent();
        }
    }

    public static void reset(ITestcaseNode resetNode) {
        List<ITestcaseNode> allChildren = TestcaseSearch.searchNode(resetNode, new AbstractTestcaseNode());
        for (ITestcaseNode child : allChildren)
            child.setSelectedInTestcaseNavigator(false);
        resetNode.setSelectedInTestcaseNavigator(false);

        TestCasesNavigatorController.getInstance().getTestCasesNavigator().getSelectionModel().clearSelection();
    }

    public static boolean selectAtLeastOneTestcase(ITestcaseNode node) {
        List<ITestcaseNode> allChildren = TestcaseSearch.searchNode(node, new TestNameNode());
        if (node instanceof TestNameNode)
            allChildren.add(node);
        for (ITestcaseNode child : allChildren)
            if (child instanceof TestNameNode)
                if (child.isSelectedInTestcaseNavigator())
                    return true;
        return false;
    }

    public static boolean selectJustOneTestcase(ITestcaseNode node) {
        List<ITestcaseNode> allChildren = TestcaseSearch.searchNode(node, new TestNameNode());
        if (node instanceof TestNameNode)
            allChildren.add(node);
        int count = 0;
        for (ITestcaseNode child : allChildren)
            if (child instanceof TestNameNode)
                if (child.isSelectedInTestcaseNavigator()) {
                    count++;
                    if (count >= 2)
                        return false;
                }
        return count == 1;
    }

    public static boolean selectAtLeastOneSubprogram(ITestcaseNode node) {
        List<ITestcaseNode> allChildren = TestcaseSearch.searchNode(node, new TestNormalSubprogramNode());
        if (node instanceof TestNormalSubprogramNode)
            allChildren.add(node);
        for (ITestcaseNode child : allChildren)
            if (child instanceof TestNormalSubprogramNode)
                if (child.isSelectedInTestcaseNavigator())
                    return true;
        return false;
    }

    public static boolean selectAtLeastOneUnit(ITestcaseNode node) {
        List<ITestcaseNode> allChildren = TestcaseSearch.searchNode(node, new TestUnitNode());
        if (node instanceof TestUnitNode)
            allChildren.add(node);
        for (ITestcaseNode child : allChildren)
            if (child instanceof TestUnitNode)
                if (child.isSelectedInTestcaseNavigator())
                    return true;
        return false;
    }

    public static List<ITestcaseNode> getAllSelectedSourcecodeNodes(ITestcaseNode root) {
        List<ITestcaseNode> nodes = TestcaseSearch.searchNode(root, new TestUnitNode());
        if (root instanceof TestUnitNode)
            nodes.add(root);
        for (int i = nodes.size() - 1; i >= 0; i--)
            if (!nodes.get(i).isSelectedInTestcaseNavigator())
                nodes.remove(i);
        return nodes;
    }
    public static List<ITestcaseNode> getAllSelectedTestcases(ITestcaseNode root) {
        List<ITestcaseNode> nodes = TestcaseSearch.searchNode(root, new TestNameNode());
        if (root instanceof TestNameNode)
            nodes.add(root);
        for (int i = nodes.size() - 1; i >= 0; i--) {
            ITestcaseNode cur = nodes.get(i);
            if (cur instanceof TestNameNode)
                if (!cur.isSelectedInTestcaseNavigator())
                    nodes.remove(i);
                else if (cur.getParent() != null && ((TestNewNode) cur.getParent()).isPrototypeTestcase())
                    nodes.remove(i);
        }
        return nodes;
    }

    public static List<ITestcaseNode> getAllSelectedFunctions(ITestcaseNode root) {
        List<ITestcaseNode> nodes = TestcaseSearch.searchNode(root, new TestNormalSubprogramNode());
        if (root instanceof TestNormalSubprogramNode)
            nodes.add(root);
        for (int i = nodes.size() - 1; i >= 0; i--)
            if (nodes.get(i) instanceof TestNormalSubprogramNode)
                if (!nodes.get(i).isSelectedInTestcaseNavigator())
                    nodes.remove(i);
        return nodes;
    }
}
