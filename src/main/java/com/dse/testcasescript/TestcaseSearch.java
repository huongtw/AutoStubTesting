package com.dse.testcasescript;

import com.dse.guifx_v3.objects.TestCasesTreeItem;
import com.dse.testcasescript.object.AbstractTestcaseNode;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.testcasescript.object.TestNewNode;
import com.dse.testcasescript.object.TestValueNode;
import javafx.scene.control.TreeItem;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestcaseSearch {
    final static AkaLogger logger = AkaLogger.get(TestcaseSearch.class);

    public static void main(String[] args) {
        // create tree from script
        TestcaseAnalyzer analyzer = new TestcaseAnalyzer();
        ITestcaseNode root = analyzer.analyze(new File("datatest/duc-anh/testcase_sample/script07"));

        // display tree
        ToStringForTestcaseTree converter = new ToStringForTestcaseTree();
        String output = converter.convert(root);
        logger.debug(output);

        // search nodes in test case tree
        logger.debug(TestcaseSearch.searchNode(root, new TestValueNode()));
    }

    public static List<ITestcaseNode> searchNode(ITestcaseNode searchRoot, AbstractTestcaseNode condition) {
        List<ITestcaseNode> output = new ArrayList<>();

        if (searchRoot==null || searchRoot.getChildren() == null) {
            return output;
        }
        for (ITestcaseNode child : searchRoot.getChildren()) {
            if (condition.getClass().isInstance(child)) // check whether child is an sub-class of condition
                output.add(child);
            output.addAll(searchNode(child, condition));
        }
        return output;
    }

    public synchronized static TestNewNode getFirstTestNewNodeByName(ITestcaseNode root, String name) {
        if (root instanceof TestNewNode && ((TestNewNode) root).getName().equals(name))
            return (TestNewNode) root;

        for (ITestcaseNode child : root.getChildren()) {
            TestNewNode res = getFirstTestNewNodeByName(child, name);
            if (res != null)
                return res;
        }

        return null;
    }

    public synchronized static TestCasesTreeItem searchTestCaseTreeItem(TestCasesTreeItem root, ITestcaseNode corresponding) {
        if (root.getValue() != null && root.getValue().equals(corresponding))
            return root;

        for (TreeItem<ITestcaseNode> child : root.getChildren()) {
            if (child instanceof TestCasesTreeItem) {
                TestCasesTreeItem res = searchTestCaseTreeItem((TestCasesTreeItem) child, corresponding);
                if (res != null)
                    return res;
            }
        }

        return null;
    }
}
