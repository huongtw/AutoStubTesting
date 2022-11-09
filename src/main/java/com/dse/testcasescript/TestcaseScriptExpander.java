package com.dse.testcasescript;

import com.dse.parser.ProjectParser;
import com.dse.parser.object.FunctionNode;
import com.dse.search.Search;
import com.dse.search.condition.FunctionNodeCondition;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.testcasescript.object.TestNameNode;
import com.dse.testcasescript.object.TestNewNode;
import com.dse.testcasescript.object.TestValueNode;
import com.dse.testdata.gen.module.SimpleTreeDisplayer;
import com.dse.testdata.gen.module.subtree.InitialArgTreeGen;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.OneDimensionDataNode;
import com.dse.testdata.object.RootDataNode;
import com.dse.util.Utils;
import com.dse.logger.AkaLogger;

import java.io.File;

public class TestcaseScriptExpander extends AbstractTestcaseScriptUpdater {
    final static AkaLogger logger = AkaLogger.get(TestcaseScriptExpander.class);

    public static void main(String[] args) throws Exception {
        /*
          Step 1: Analyze a test script
         */
        String testscriptPath = "datatest/duc-anh/TestcaseScriptExpander/testscript.tst";
        TestcaseAnalyzer testscriptAnalyzer = new TestcaseAnalyzer();
        ITestcaseNode rootTestscript = testscriptAnalyzer.analyze(new File(Utils.normalizePath(testscriptPath)));

        /*
         Step 2: Create a data tree corresponding to the test script
         */
        // Parse project
        String projectPath = "datatest/duc-anh/TestcaseScriptExpander";
        ProjectParser parser = new ProjectParser(new File(Utils.normalizePath(projectPath)));
        parser.setCpptoHeaderDependencyGeneration_enabled(true);
        parser.setExpandTreeuptoMethodLevel_enabled(true);
        parser.setParentReconstructor_enabled(true);
        parser.setFuncCallDependencyGeneration_enabled(true);
        parser.setGlobalVarDependencyGeneration_enabled(true);
        parser.setParentReconstructor_enabled(true);
        parser.setExtendedDependencyGeneration_enabled(true);

        // Get a function
        String name = "f3(int[])";
        FunctionNode function = (FunctionNode) Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), name).get(0);
        logger.debug("function " + function.getAST().getRawSignature());

        // create initial data tree
        RootDataNode rootDataTree = new RootDataNode();
        rootDataTree.setFunctionNode(function);
        InitialArgTreeGen dataTreeGen = new InitialArgTreeGen();
        dataTreeGen.generateCompleteTree(rootDataTree, null);

        // display the data tree
        logger.debug("Initial tree:\n" + new SimpleTreeDisplayer().toString(rootDataTree));

        /*
          Step 3: Update the test script based on the data tree
         */
        IDataNode n = rootDataTree.getChildren().get(0).getChildren().get(0);
        if (n instanceof OneDimensionDataNode){
            ((OneDimensionDataNode) n).setSize(10);
        }
        logger.debug(rootDataTree.getRoot().generateInputToSavedInFile());

        AbstractTestcaseScriptUpdater updater = new TestcaseScriptExpander();
        updater.setRootDataTree(rootDataTree);
        updater.setRootTestScript(rootTestscript);
        updater.setNameOfTestcase("xxx.001");
        updater.updateOnTestcaseScript();
        logger.debug("New test script:\n" + rootTestscript.exportToFile());
    }

    public void updateOnTestcaseScript() throws Exception {
        MatchingPair pair = findMatchingPairBetweenDataTreeAndTestscriptTree(getRootDataTree(), getRootTestScript());
        if (pair != null) {
            logger.debug("Found a matching: " + pair.getTestcaseNode() + " <-> " + pair.getDataNode());

            /*
              Detect whether we need to update a existed node or add a new node
             */
            ITestcaseNode testNewNode = findNodeInTestscriptTreeByName(getNameOfTestcase(), pair.getTestcaseNode());
            boolean isExisted = testNewNode != null;

            if (isExisted){
                testNewNode.getParent().getChildren().remove(testNewNode);
                logger.debug("The test case has been existed. Deleted!");
            }

            logger.debug("Adding new node to the test script tree");
            // add node "TEST.NEW"
            testNewNode = new TestNewNode();
            testNewNode.setParent(pair.getTestcaseNode());
            pair.getTestcaseNode().addChild(testNewNode);

            // add node "TEST.NAME"
            TestNameNode nameNode = new TestNameNode();
            nameNode.setName(getNameOfTestcase());
            nameNode.setParent(testNewNode);
            testNewNode.addChild(nameNode);

            //
            String values = pair.getDataNode().getRoot().generateInputToSavedInFile();
            for (String value : values.split("\n"))
                if (value.contains("=")) {
                    String identifier = value.split("=")[0];
                    String valueOfIdentifier = value.split("=")[1];
                    TestValueNode valueNode = new TestValueNode();
                    valueNode.setIdentifier(identifier);
                    valueNode.setValue(valueOfIdentifier);

                    valueNode.setParent(testNewNode);
                    testNewNode.addChild(valueNode);
                }
        } else {
            logger.error("Can not find matching pair");
        }
    }
}
