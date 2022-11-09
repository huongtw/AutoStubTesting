package com.dse.guifx_v3.objects;

import com.dse.coverage.CoverageManager;
import com.dse.environment.object.EnviroCoverageTypeNode;
import com.dse.guifx_v3.helps.CacheHelper;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.Factory;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.CheckBoxTreeItem;

import java.util.ArrayList;
import java.util.List;

public class TestCasesTreeItem extends CheckBoxTreeItem<ITestcaseNode> {
    final static AkaLogger logger = AkaLogger.get(TestCasesTreeItem.class);

    private String coverage;

    private TestCasesTreeItem() {}
    public TestCasesTreeItem(ITestcaseNode testcaseNode) {
        setValue(testcaseNode);
        setGraphic(Factory.getIcon(testcaseNode));

        // get coverage progress of testCase
        // todo: statement and branch
        if (testcaseNode instanceof TestNewNode) {
            List<ITestcaseNode> names = TestcaseSearch.searchNode(testcaseNode, new TestNameNode());
            if (names.size() == 1) {
                TestCase testCase = TestCaseManager.getBasicTestCaseByNameWithoutData(((TestNameNode) names.get(0)).getName());
                if (testCase != null) {
                    List<TestCase> testCases = new ArrayList<>();
                    testCases.add(testCase);

                    String typeOfCoverage = Environment.getInstance().getTypeofCoverage();
                    switch (typeOfCoverage) {
                        case EnviroCoverageTypeNode.STATEMENT:
                        case EnviroCoverageTypeNode.BRANCH:
                        case EnviroCoverageTypeNode.BASIS_PATH:
                        case EnviroCoverageTypeNode.MCDC: {
                            coverage = CoverageManager.getProgress(testCase, typeOfCoverage) + "";
                            break;
                        }

                        case EnviroCoverageTypeNode.STATEMENT_AND_BRANCH: {
                            float stmCoverage = CoverageManager.getProgress(testCase, EnviroCoverageTypeNode.STATEMENT);
                            float branchCoverage = CoverageManager.getProgress(testCase, EnviroCoverageTypeNode.BRANCH);
                            coverage = Utils.round(stmCoverage * 100, 4) + "% / " + Utils.round(branchCoverage * 100, 4) + "%";
                            break;
                        }

                        case EnviroCoverageTypeNode.STATEMENT_AND_MCDC: {
                            float stmCoverage = CoverageManager.getProgress(testCase, EnviroCoverageTypeNode.STATEMENT);
                            float mcdcCoverage = CoverageManager.getProgress(testCase, EnviroCoverageTypeNode.MCDC);
                            coverage = Utils.round(stmCoverage * 100, 4) + "% / " + Utils.round(mcdcCoverage * 100, 4) + "%";
                            break;
                        }
                    }

                }
            }
        } else if (testcaseNode instanceof TestNormalSubprogramNode) {
            // put to a map for updating when auto generate testcases
            try {
                ICommonFunctionNode functionNode = UIController.searchFunctionNodeByPath(((TestNormalSubprogramNode) testcaseNode).getName());
                if (!CacheHelper.getFunctionToTreeItemMap().containsKey(functionNode)) {
                    CacheHelper.getFunctionToTreeItemMap().put(functionNode, this);
                    CacheHelper.getTreeItemToListTestCasesMap().put(this, new ArrayList<>());
                }
            } catch (FunctionNodeNotFoundException fe) {
                logger.debug("function node not found: " + fe.getFunctionPath());
            }
        }

        if (hasChild()) {
            this.getChildren().add(new TestCasesTreeItem());// treat as a parent node
        }
        expandedProperty().addListener((observable, oldValue, newValue) -> {
            BooleanProperty bp = (BooleanProperty) observable;
            TestCasesTreeItem node = (TestCasesTreeItem) bp.getBean();
            if (hasChild()) {
                if (isExpanded() && node.getChildren().size() >= 1) {
                    node.loadChildren(false);
                }
            }
        });
    }

    public boolean hasChild() {
        ITestcaseNode node = this.getValue();
        if (node instanceof TestNewNode) {
            return false;
        } else if (node instanceof TestNormalSubprogramNode) {
            return hasChild((TestNormalSubprogramNode) node);
        } else if (node instanceof TestInitSubprogramNode) {
            return hasChild((TestInitSubprogramNode) node);
        } else if (node instanceof TestCompoundSubprogramNode) {
            return hasChild((TestCompoundSubprogramNode) node);
        } else if (node instanceof TestUnitNode) {
            return hasChild((TestUnitNode) node);
        } else return node instanceof TestcaseRootNode;
    }

    private boolean hasChild(TestCompoundSubprogramNode node) {
        List<ITestcaseNode> searchNodes = TestcaseSearch.searchNode(node, new TestNewNode());
        // TODO: update this condition later
        return searchNodes.size() > 0;
    }

    private boolean hasChild(TestInitSubprogramNode node) {
        List<ITestcaseNode> searchNodes = TestcaseSearch.searchNode(node, new TestNewNode());
        // TODO: update this condition later
        return searchNodes.size() > 0;
    }

    private boolean hasChild(TestNormalSubprogramNode node) {
        List<ITestcaseNode> searchNodes = TestcaseSearch.searchNode(node, new TestNewNode());
        return searchNodes.size() > 0;
    }
    private boolean hasChild(TestUnitNode node) {
        List<ITestcaseNode> searchNodes = TestcaseSearch.searchNode(node, new TestNormalSubprogramNode());
        return searchNodes.size() > 0;
    }

    /**
     *
     * @param isRecursive true if we want to load children of a tree item node recursively
     */
    public void loadChildren(boolean isRecursive) {
        if(hasChild()) {
            this.getChildren().clear();
            ITestcaseNode node = this.getValue();
            List<ITestcaseNode> children = new ArrayList<>();
            if (node instanceof TestcaseRootNode) {
                List<ITestcaseNode> compounds = TestcaseSearch.searchNode(node, new TestCompoundSubprogramNode());
                List<ITestcaseNode> inits = TestcaseSearch.searchNode(node, new TestInitSubprogramNode());
                List<ITestcaseNode> units = TestcaseSearch.searchNode(node, new TestUnitNode());
                children.addAll(compounds);
                children.addAll(inits);
                children.addAll(units);

            } else if (node instanceof TestUnitNode) {
                List<ITestcaseNode> subprograms = TestcaseSearch.searchNode(node, new TestNormalSubprogramNode());

                // Filter function list
                filter(subprograms);

                children.addAll(subprograms);

            } else if (node instanceof TestNormalSubprogramNode) {
                List<ITestcaseNode> testCases = TestcaseSearch.searchNode(node, new TestNewNode());
                children.addAll(testCases);
            } else if (node instanceof TestCompoundSubprogramNode) {
                List<ITestcaseNode> testCases = TestcaseSearch.searchNode(node, new TestNewNode());
                children.addAll(testCases);
            }

            for (ITestcaseNode child : children)
                if (child != null){
                    try {
                        TestCasesTreeItem childItem = new TestCasesTreeItem(child);

                        if (this.getChildren() != null) {
                            this.getChildren().add(childItem);

                            if (isRecursive)
                                childItem.loadChildren(isRecursive);
                        }
                    }catch (Exception e){
                        logger.error("Cant load children", e);
                    }
                }

        }
    }

    private List<ITestcaseNode> filter(List<ITestcaseNode> subprograms) {
        return UIController.filterSubprograms(subprograms);
    }

    /**
     * add TestNewNode to a TestNormalSubprogramNode (used for auto generating testcase)
     * @return void
     */
    public void addTestNewNode(TestCasesTreeItem testNormalSubprogram, TestCase testCase) {
        if (testNormalSubprogram.getValue() instanceof TestNormalSubprogramNode) {
            List<TestCase> testCases = CacheHelper.getTreeItemToListTestCasesMap().get(testNormalSubprogram);

            if (! testCases.contains(testCase)) {
                TestNewNode testNewNode = testCase.getTestNewNode();

                testNewNode.setParent(testNormalSubprogram.getValue());
                testNormalSubprogram.getValue().getChildren().add(testNewNode);

                TestCasesTreeItem newTreeItem = new TestCasesTreeItem(testNewNode);
                testNormalSubprogram.getChildren().add(newTreeItem);

                testCases.add(testCase);
            }
        }
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }
}