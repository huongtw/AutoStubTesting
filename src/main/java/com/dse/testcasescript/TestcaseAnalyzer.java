package com.dse.testcasescript;

import com.dse.config.WorkspaceConfig;
import com.dse.testcase_manager.ITestCase;
import com.dse.testcasescript.object.*;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;
import com.dse.util.Utils;

import java.io.File;
import java.util.List;
import java.util.Stack;

/**
 * Analyze a test case script to construct a test case tree
 */
public class TestcaseAnalyzer implements ITestcaseCommandList {
    final static AkaLogger logger = AkaLogger.get(TestcaseAnalyzer.class);

    public static void main(String[] args) {
        // create tree from script
        TestcaseAnalyzer analyzer = new TestcaseAnalyzer();
        ITestcaseNode root = analyzer.analyze(new File("/home/lamnt/IdeaProjects/akautauto/datatest/duc-anh/aka-working-space/v23.tst"));

        // display tree
        ToStringForTestcaseTree converter = new ToStringForTestcaseTree();
        String output = converter.convert(root);
        logger.debug("Tree of test case script:\n" + output);

        // export the test case tree to file
        String export = root.exportToFile();
        logger.debug("Export = \n" + export);
    }

    public ITestcaseNode removeNonExistTestcase(ITestcaseNode root) {
        List<ITestcaseNode> sutNodes = TestcaseSearch.searchNode(root, new TestNormalSubprogramNode());
        for (ITestcaseNode sutNode : sutNodes) {
            for (int i = sutNode.getChildren().size() - 1; i >= 0; i--) {
                ITestcaseNode child = sutNode.getChildren().get(i);
                if (child instanceof TestNewNode) {
                    String name = ((TestNewNode) child).getName();
                    String path = "";
                    if (name.startsWith(ITestCase.COMPOUND_SIGNAL)) {
                        path = new WorkspaceConfig().fromJson().getCompoundTestcaseDirectory() + File.separator + name + ".json";
                    } else
                        path = new WorkspaceConfig().fromJson().getTestcaseDirectory() + File.separator + name + ".json";
                    if (!new File(path).exists()) {
                        logger.debug("Delete the non-exist test case " + name);
                        sutNode.getChildren().remove(i);
                    }
                }
            }
        }

        return root;
    }

    public ITestcaseNode analyze(File testcaseFile) {
        if (!testcaseFile.exists())
            return null;
        String currentContent = Utils.readFileContent(testcaseFile);
        String[] lines = currentContent.split("\n");
        int startLine = 0;
        int numOfLines = lines.length;
        int currentLineIndex = startLine;

        Stack<ITestcaseNode> parents = new Stack<>();
        TestcaseRootNode topRootNode = new TestcaseRootNode();
        topRootNode.setAbsolutePath(testcaseFile.getAbsolutePath());
        parents.add(topRootNode);
        ITestcaseNode currentRoot = topRootNode;

        while (currentLineIndex < numOfLines) {
            String currentLineCommand = lines[currentLineIndex].trim();
            //logger.debug("Analyze " + currentLineCommand);

            if (currentLineCommand.length() == 0) {
                // there is no content in this line
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(TEST_UNIT)) {
                ITestcaseNode newNode = setUnit(topRootNode, currentLineCommand);

                // the current root is the top-root tree
                parents.removeAll(parents);
                parents.push(topRootNode);
                parents.push(newNode);
                currentRoot = newNode;

                currentLineIndex++;

            } else if (currentLineCommand.equals(TEST_NEW) ||
                    currentLineCommand.equals(TEST_ADD)
                    || currentLineCommand.equals(TEST_REPLACE)) {
                ITestcaseNode newNode = analyzeTestcaseBlock(currentRoot, currentLineCommand);
                // append new root to the stack
                parents.push(newNode);
                currentRoot = newNode;

                currentLineIndex++;

            } else if (currentLineCommand.equals(TEST_END)) {
                // remove the current root
                parents.pop();
                currentRoot = parents.peek();

                currentLineIndex++;

            } else if (currentLineCommand.startsWith(TEST_NAME)) {
                setNameOfTestcase(currentRoot, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(TEST_SUBPROGRAM)) {
                ITestcaseNode newNode = setSubprogram(new TemporaryNode(), currentLineCommand);

                if (newNode instanceof TestInitSubprogramNode || newNode instanceof TestCompoundSubprogramNode) {
                    // if the current node is init test case or compound test case, its root must be the top root
                    newNode.setParent(topRootNode);
                    topRootNode.addChild(newNode);

                } else if (newNode instanceof TestNormalSubprogramNode) {
                    // the parent of normal subprogram node must be a unit node
                    // get the closest unit node
                    while (!(parents.peek() instanceof TestUnitNode))
                        parents.pop();
                    currentRoot = parents.peek();
                    newNode.setParent(currentRoot);
                    currentRoot.addChild(newNode);
                }

                // update the new root
                parents.push(newNode);
                currentRoot = newNode;

                currentLineIndex++;

            } else if (currentLineCommand.startsWith(TEST_SLOT)) {
                createTestSlot(currentRoot, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(TEST_EXPECTED)) {
                createTestExpectedNode(currentRoot, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(TEST_NOTES)) {
                List<String> block = currentRoot.getBlockOfTag(TEST_END_NOTES, currentLineIndex, lines);
                createTestNotes(currentRoot, block);
                currentLineIndex += block.size();

            } else if (currentLineCommand.startsWith(TEST_REQUIREMENT_KEY)) {
                createRequirementKey(currentRoot, currentLineCommand);
                currentLineIndex += 1;

            } else if (currentLineCommand.startsWith(TEST_EXPECTED_USER_CODE)) {
                List<String> block = currentRoot.getBlockOfTag(TEST_END_EXPECTED_USER_CODE, currentLineIndex, lines);
                createTestExpectedUserCode(currentRoot, block);
                currentLineIndex += block.size();

            } else if (currentLineCommand.startsWith(TEST_VALUE_USER_CODE)) {
                List<String> block = currentRoot.getBlockOfTag(TEST_END_VALUE_USER_CODE, currentLineIndex, lines);
                createTestValueUserCode(currentRoot, block);
                currentLineIndex += block.size();

            } else if (currentLineCommand.startsWith(TEST_VALUE)) {
                createTestValueNode(currentRoot, currentLineCommand);
                currentLineIndex++;

            } else if (currentLineCommand.startsWith(COMMENT)) {
                setComment(currentRoot, currentLineCommand);
                currentLineIndex++;

            } else {
                logger.error("Do not support " + currentLineCommand);
                currentLineIndex++;
            }
        }

        //mergeTestcaseNodes(topRootNode);
        removeNonExistTestcase(topRootNode);
        return topRootNode;
    }

    /**
     * Test cases of a subfunction may be defined in separate location
     *
     * @param topRootNode
     */
    private void mergeTestcaseNodes(ITestcaseNode topRootNode) {
        List<ITestcaseNode> unitNodes = TestcaseSearch.searchNode(topRootNode, new TestUnitNode());

        for (int i = unitNodes.size() - 1; i >= 0; i--) {
            ITestcaseNode unitNode = unitNodes.get(i);
            List<ITestcaseNode> normalSubprogramNodes = TestcaseSearch.searchNode(unitNode, new TestNormalSubprogramNode());

            for (ITestcaseNode normalSubprogramNode : normalSubprogramNodes)
                if (normalSubprogramNode instanceof TestNormalSubprogramNode) {
                    TestNormalSubprogramNode normalSubprogramNodeCast = (TestNormalSubprogramNode) normalSubprogramNode;
                    ITestcaseNode parent = normalSubprogramNodeCast.getParent();

                    if (parent instanceof TestUnitNode)
                        for (int j = 0; j < i; j++) {
                            ITestcaseNode previousUnitNode = unitNodes.get(j);
                            if (previousUnitNode instanceof TestUnitNode) {
                                TestUnitNode previousUnitNodeCast = (TestUnitNode) previousUnitNode;
                                if (previousUnitNodeCast.getName().equals(((TestUnitNode) parent).getName())) {
                                    ITestcaseNode identicalSubprogramNode = previousUnitNodeCast.findNormalSubprogramNode(normalSubprogramNode);

                                    if (identicalSubprogramNode != null) {
                                        for (ITestcaseNode child : normalSubprogramNode.getChildren()) {
                                            child.setParent(identicalSubprogramNode);
                                            identicalSubprogramNode.addChild(child);
                                        }
                                        unitNode.getChildren().remove(normalSubprogramNode);
                                    }
                                }
                            }
                        }
                }
        }


    }

    /**
     * @param parent
     * @param lineCommand Example: "TEST.EXPECTED: math.sine.return:0.0..1.0"
     */
    private void createTestExpectedNode(ITestcaseNode parent, String lineCommand) {
        TestExpectedNode newNode = new TestExpectedNode();
        String[] tokens = lineCommand.substring(lineCommand.indexOf(DELIMITER_BETWEEN_COMMAND_AND_VALUE) + 1)
                .split(TestExpectedNode.DELIMITER_BETWEEN_KEY_AND_VALUE);

        String[] identifiers = tokens[0].split(TestExpectedNode.DELIMITER_BETWEEN_ATTRIBUTES);
        newNode.setUnit(identifiers[TestExpectedNode.UNIT_INDEX_IN_IDENTIFIER].trim());
        newNode.setParameter(identifiers[TestExpectedNode.PARAMETER_INDEX_IN_IDENTIFIER].trim());
        newNode.setSubprogram(identifiers[TestExpectedNode.SUBPROGRAM_INDEX_IN_IDENTIFIER].trim());

        String value = tokens[1];
        newNode.setExpectedValue(value);

        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    /**
     * @param parent
     * @param lineCommand Example: "TEST.VALUE:manager.(cl)Manager::PlaceOrder.Order.Beverage:Wine", where
     *                    identifier = "manager.(cl)Manager::PlaceOrder.Order.Beverage",
     *                    value = "Wine"
     */
    private void createTestValueNode(ITestcaseNode parent, String lineCommand) {
        TestValueNode newNode = new TestValueNode();
        String tokens = lineCommand.substring(lineCommand.indexOf(DELIMITER_BETWEEN_COMMAND_AND_VALUE) + 1);
        newNode.setIdentifier(tokens.substring(0, tokens.lastIndexOf(TestValueNode.DELIMITER_BETWEEN_KEY_AND_VALUE)).trim());
        newNode.setValue(tokens.substring(tokens.lastIndexOf(TestValueNode.DELIMITER_BETWEEN_COMMAND_AND_VALUE) + 1).trim());
        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    /**
     * @param parent
     * @param block  Example:
     *               "TEST.VALUE_USER_CODE:file_io.WriteLine.fp
     *               <<file_io.WriteLine.*fp>> = ( <<file_io.CreateFile.return>> );
     *               TEST.END_VALUE_USER_CODE:"
     */
    private void createTestValueUserCode(ITestcaseNode parent, List<String> block) {
        TestValueUserCodeNode newNode = new TestValueUserCodeNode();
        newNode.analyzeBlock(block);
        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    /**
     * @param parent
     * @param block  Example:
     *               "TEST.EXPECTED_USER_CODE:file_io.CreateFile.return
     *               {{ <<file_io.CreateFile.return>> != NULL }}
     *               TEST.END_EXPECTED_USER_CODE:"
     */
    private void createTestExpectedUserCode(ITestcaseNode parent, List<String> block) {
        TestExpectedUserCodeNode newNode = new TestExpectedUserCodeNode();
        newNode.analyzeBlock(block);
        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    /**
     * @param parent
     * @param block  Example:
     *               "TEST.NOTES:
     *               This testcase illustrates the implementation of a function that is done after a
     *               using directive. This function is in normal scope of the application and cannot
     *               be accessed through the plane_makers namespace.
     *               TEST.END_NOTES:"
     */
    private void createTestNotes(ITestcaseNode parent, List<String> block) {
        TestNotesNode newNode = new TestNotesNode();
        newNode.setParent(parent);
        parent.addChild(newNode);
        newNode.analyzeBlock(block);
    }

    /**
     * @param parent
     * @param lineCommand Example: TEST.SLOT: "2", "file_io", "WriteLine", "1", "WRITELINE.001"
     */
    private void createTestSlot(ITestcaseNode parent, String lineCommand) {
        TestSlotNode newNode = new TestSlotNode();
        String[] attributeTokens = lineCommand.substring(lineCommand.indexOf(DELIMITER_BETWEEN_COMMAND_AND_VALUE) + 1)
                .split(TestSlotNode.DELIMITER_BETWEEN_ATTRIBUTES);

        if (attributeTokens.length == 5 || attributeTokens.length == 6) {
            newNode.setSlotNum(Integer.parseInt(attributeTokens[TestSlotNode.SLOT_INDEX]
                    .replace("\"", "") /*the number of slot is put in "..."*/
                    .trim()));
            newNode.setUnit(attributeTokens[TestSlotNode.UNIT_INDEX].trim());
            newNode.setSubprogramName(attributeTokens[TestSlotNode.SUBPROGRAM_NAME_INDEX].trim());
            newNode.setNumberOfIterations(Integer.parseInt(attributeTokens[TestSlotNode.NUMBER_OF_ITERATIONS_INDEX].replace("\"", "").trim()));
            newNode.setTestcaseName(attributeTokens[TestSlotNode.TESTCASE_NAME_INDEX].trim());

            if (attributeTokens.length == 6)
                newNode.setDelay(Integer.parseInt(attributeTokens[TestSlotNode.DELAY_INDEX]
                        .replace("\"", "")/*the number of slot is put in "..."*/
                        .trim()));
            else
                // the delay is set to 0 by default
                newNode.setDelay(0);

            newNode.setParent(parent);
            parent.addChild(newNode);
        } else {
            logger.error("Error on handling " + lineCommand);
        }
    }

    private ITestcaseNode analyzeTestcaseBlock(ITestcaseNode parent, String lineCommand) {
        ITestcaseNode newNode = null;
        switch (lineCommand) {
            case TestActionNode.ADD_INFORMATION_TO_TESTCASE_SIGNAL: {
                newNode = new TestAddNode();
                break;
            }
            case TestActionNode.REPLACE_TEST_CASE_SIGNAL: {
                newNode = new TestReplaceNode();
                break;
            }
            case TestActionNode.NEW_TESTCASE_SIGNAL: {
                newNode = new TestNewNode();
                break;
            }
            default: {
                logger.error("Do not support " + lineCommand);
            }
        }
        if (newNode != null) {
            newNode.setParent(parent);
            parent.addChild(newNode);
        }
        return newNode;
    }

    private void setComment(ITestcaseNode parent, String lineCommand) {
        TestCommentNode newNode = new TestCommentNode();
        newNode.setComment(lineCommand);
        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    private void setNameOfTestcase(ITestcaseNode parent, String lineCommand) {
        TestNameNode newNode = new TestNameNode();

        String name = lineCommand.substring(lineCommand.indexOf(DELIMITER_BETWEEN_COMMAND_AND_VALUE) + 1).trim();
        newNode.setName(name);

        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    private TestSubprogramNode setSubprogram(ITestcaseNode parent, String lineCommand) {
        TestSubprogramNode newNode;

        String name = lineCommand.substring(lineCommand.indexOf(DELIMITER_BETWEEN_COMMAND_AND_VALUE) + 1).trim();
        switch (name) {
            case TestSubprogramNode.COMPOUND_SIGNAL: {
                newNode = new TestCompoundSubprogramNode();
                break;
            }
            case TestSubprogramNode.INIT_SIGNAL: {
                newNode = new TestInitSubprogramNode();
                break;
            }
            default: {
                newNode = new TestNormalSubprogramNode();
            }
        }

        name = PathUtils.toAbsolute(name);
        newNode.setName(name);

        newNode.setParent(parent);
        parent.addChild(newNode);
        return newNode;
    }

    private void createRequirementKey(ITestcaseNode parent, String lineCommand) {
        TestRequirementKeyNode newNode = new TestRequirementKeyNode();
        String requirementKey = lineCommand.substring(lineCommand.indexOf(DELIMITER_BETWEEN_COMMAND_AND_VALUE) + 1).trim();
        newNode.setRequirement(requirementKey);
        newNode.setParent(parent);
        parent.addChild(newNode);
    }

    private TestUnitNode setUnit(ITestcaseNode parent, String lineCommand) {
        TestUnitNode newNode = new TestUnitNode();

        String name = lineCommand.substring(lineCommand.indexOf(DELIMITER_BETWEEN_COMMAND_AND_VALUE) + 1).trim();
        name = PathUtils.toAbsolute(name);
        newNode.setName(name);

        newNode.setParent(parent);
        parent.addChild(newNode);
        return newNode;
    }
}
