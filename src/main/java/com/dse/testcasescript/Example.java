package com.dse.testcasescript;

import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.testcasescript.object.TestUnitNode;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.util.List;

public class Example {
    private final static AkaLogger logger = AkaLogger.get(Example.class);

    public static void main(String[] args) {
        // create tree from script
        TestcaseAnalyzer analyzer = new TestcaseAnalyzer();
        ITestcaseNode root = analyzer.analyze(new File("datatest/duc-anh/testcase_sample/script09"));

        // display tree
        ToStringForTestcaseTree converter = new ToStringForTestcaseTree();
        String output = converter.convert(root);
        logger.debug("Tree of test case script:\n" + output);

        // from tree, export to file
        String export = root.exportToFile();
        logger.debug("Export = \n" + export);

        // Search in test case tree
        List<ITestcaseNode> searchNodes = TestcaseSearch.searchNode(root, new TestUnitNode());
        logger.debug("search nodes = " + searchNodes);
    }
}
