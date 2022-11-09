package com.dse.regression;

import com.dse.environment.Environment;
import com.dse.exception.FunctionNodeNotFoundException;
import com.dse.guifx_v3.helps.Factory;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.AbstractFunctionNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.StructOrClassNode;
import com.dse.testcasescript.TestcaseSearch;
import com.dse.testcasescript.object.*;
import javafx.scene.control.CheckBoxTreeItem;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;

import java.util.ArrayList;
import java.util.List;

public class TestCasesTreeItemForIncrementalBuild extends CheckBoxTreeItem<ITestcaseNode> {

    public TestCasesTreeItemForIncrementalBuild(ITestcaseNode testcaseNode) {
        setValue(testcaseNode);
        setGraphic(Factory.getIcon(testcaseNode));
        setExpanded(true);

        loadChildren();
    }

    private boolean hasChild() {
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

    public void loadChildren() {
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
                subprograms = filter(subprograms);

                children.addAll(subprograms);

            } else if (node instanceof TestNormalSubprogramNode) {
                List<ITestcaseNode> testCases = TestcaseSearch.searchNode(node, new TestNewNode());
                children.addAll(testCases);
            } else if (node instanceof TestCompoundSubprogramNode) {
                List<ITestcaseNode> testCases = TestcaseSearch.searchNode(node, new TestNewNode());
                children.addAll(testCases);
            }

            for (ITestcaseNode child : children) {
                if (shouldBeDisplay(child)) {
                    this.getChildren().add(new TestCasesTreeItemForIncrementalBuild(child));
                }
            }
        }
    }

    /**
     * If unit node or testSubprogram node have no test cases inside then hide them
     * @param node
     * @return
     */
    private boolean shouldBeDisplay(ITestcaseNode node) {
        if (node instanceof TestcaseRootNode || node instanceof TestNewNode) return true;
        if (node instanceof TestSubprogramNode && node.getChildren().size() == 0) return false;
        for (ITestcaseNode child : node.getChildren()) {
            if (shouldBeDisplay(child)) return true;
        }

        return false;
    }

    private List<ITestcaseNode> filter(List<ITestcaseNode> subprograms) {
        subprograms.removeIf(subprogram -> {
            ICommonFunctionNode function;

            try {
                function = UIController
                        .searchFunctionNodeByPathInBackupEnvironment(((TestNormalSubprogramNode) subprogram).getName());

                if (!Environment.getInstance().isOnWhiteBoxMode()) {
                    if (function.getVisibility() != ICPPASTVisibilityLabel.v_public)
                        return true;

                    if (function instanceof AbstractFunctionNode) {
                        INode realParent = ((AbstractFunctionNode) function).getRealParent();
                        if (realParent == null) realParent = function.getParent();

                        if (realParent instanceof StructOrClassNode) {
                            if (((StructOrClassNode) realParent).getVisibility() != ICPPASTVisibilityLabel.v_public)
                                return true;
                        }
                    }
                }

                return function.getParent() instanceof ICommonFunctionNode;

            } catch (FunctionNodeNotFoundException e) {
                UIController.showErrorDialog("Do not found function " + ((TestNormalSubprogramNode) subprogram).getName(), "Function Not Found", "FunctionNodeNotFoundException");
                return true;
            }
        });

        return subprograms;
    }
}