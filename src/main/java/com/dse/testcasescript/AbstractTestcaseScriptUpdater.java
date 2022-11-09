package com.dse.testcasescript;

import com.dse.search.Search2;
import com.dse.testcasescript.object.ITestcaseNode;
import com.dse.testcasescript.object.TestNameNode;
import com.dse.testcasescript.object.TestNewNode;
import com.dse.testcasescript.object.TestNormalSubprogramNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.SubprogramNode;
import com.dse.logger.AkaLogger;

import java.util.List;

public abstract class AbstractTestcaseScriptUpdater {
    final static AkaLogger logger = AkaLogger.get(AbstractTestcaseScriptUpdater.class);
    /**
     * The root of test script
     */
    private ITestcaseNode rootTestScript;

    /**
     * The root of data tree
     */
    private IDataNode rootDataTree;

    private String nameOfTestcase;

    public abstract void updateOnTestcaseScript() throws Exception;

    /**
     * @param rootDataTree   the root of data tree of a function
     * @param rootTestScript the root of tree of test script
     * @return
     */
    protected MatchingPair findMatchingPairBetweenDataTreeAndTestscriptTree(IDataNode rootDataTree, ITestcaseNode rootTestScript) {
        if (rootDataTree != null && rootTestScript != null) {
            // find all function nodes in the data tree
            List<SubprogramNode> targetNodes = Search2.searchNodes(getRootDataTree(), SubprogramNode.class);

            if (targetNodes.size() == 1) {
                IDataNode n = targetNodes.get(0);
                if (n != null) {

                    // find the corresponding node in the test script tree
                    List<ITestcaseNode> candidateNodes = TestcaseSearch.searchNode(getRootTestScript(), new TestNormalSubprogramNode());
                    for (ITestcaseNode candidateNode : candidateNodes)

                        if (candidateNode instanceof TestNormalSubprogramNode) {
                            String fullName = ((SubprogramNode) n).getFunctionNode().getAbsolutePath();
                            String fullName2 = ((TestNormalSubprogramNode) candidateNode).getName();

                            if (fullName.equals(fullName2)) {
                                logger.debug("Found a matching.");
                                MatchingPair p = new MatchingPair();
                                p.setTestcaseNode(candidateNode);
                                p.setDataNode(n);
                                return p;
                            }
                        }
                }
            } else {
                logger.debug("The data tree must have one function.");
            }
        }
        return null;
    }

    protected ITestcaseNode findNodeInTestscriptTreeByName(String nameOfTestcase, ITestcaseNode testNormalSubprogramNode) {
        if (testNormalSubprogramNode instanceof TestNormalSubprogramNode) {
            for (ITestcaseNode child : testNormalSubprogramNode.getChildren())
                if (child instanceof TestNewNode) {
                    for (ITestcaseNode child2 : child.getChildren())
                        if (child2 instanceof TestNameNode && ((TestNameNode) child2).getName().equals(nameOfTestcase))
                            return child;
                }
        }
        return null;
    }

    /**
     * Detect whether we need to update a existed node or add a new node
     */
    protected boolean isExistedBefore() {
        MatchingPair pair = findMatchingPairBetweenDataTreeAndTestscriptTree(getRootDataTree(), getRootTestScript());
        if (pair != null) {
            logger.debug("Found a matching: " + pair.getTestcaseNode() + " <-> " + pair.getDataNode());
            ITestcaseNode testNewNode = findNodeInTestscriptTreeByName(getNameOfTestcase(), pair.getTestcaseNode());
            return testNewNode != null;

        } else
            return false;
    }

    public IDataNode getRootDataTree() {
        return rootDataTree;
    }

    public void setRootDataTree(IDataNode rootDataTree) {
        this.rootDataTree = rootDataTree;
    }

    public ITestcaseNode getRootTestScript() {
        return rootTestScript;
    }

    public void setRootTestScript(ITestcaseNode rootTestScript) {
        this.rootTestScript = rootTestScript;
    }

    public String getNameOfTestcase() {
        return nameOfTestcase;
    }

    public void setNameOfTestcase(String nameOfTestcase) {
        this.nameOfTestcase = nameOfTestcase;
    }

    class MatchingPair {
        IDataNode dataNode = null;
        ITestcaseNode testcaseNode = null;

        public IDataNode getDataNode() {
            return dataNode;
        }

        public void setDataNode(IDataNode dataNode) {
            this.dataNode = dataNode;
        }

        public ITestcaseNode getTestcaseNode() {
            return testcaseNode;
        }

        public void setTestcaseNode(ITestcaseNode testcaseNode) {
            this.testcaseNode = testcaseNode;
        }
    }
}
