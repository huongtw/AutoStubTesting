package com.dse.guifx_v3.objects;

import com.dse.guifx_v3.controllers.TestCaseTreeTableController;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.*;
import com.dse.parser.object.INode;
import com.dse.testcase_manager.IDataTestItem;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testdata.Iterator;
import com.dse.testdata.object.*;
import javafx.css.PseudoClass;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

public class InputColumnCellFactory implements Callback<TreeTableColumn<DataNode, String>, TreeTableCell<DataNode, String>> {
    private final static AkaLogger logger = AkaLogger.get(InputColumnCellFactory.class);
    private final IDataTestItem testCase;
    private static final PseudoClass pseudoClass = PseudoClass.getPseudoClass("input-column-parameter");

    public InputColumnCellFactory(IDataTestItem testCase) {
        super();
        this.testCase = testCase;
    }

    @Override
    public TreeTableCell<DataNode, String> call(TreeTableColumn<DataNode, String> param) {
        InputColumnCell cell = new InputColumnCell(testCase);

        cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            TreeItem<DataNode> treeItem = cell.getTreeTableRow().getTreeItem();
            if (treeItem instanceof TestDataParameterTreeItem) {
                if (!treeItem.getValue().getName().equals("RETURN")) {
                    if (((TestDataParameterTreeItem) treeItem).getSelectedColumn() != TestDataTreeItem.ColumnType.INPUT) {
                        ((TestDataParameterTreeItem) treeItem).setSelectedColumn(TestDataTreeItem.ColumnType.INPUT);
                        cell.getTreeTableView().refresh();
                    }
                }
            } else if (treeItem instanceof TestDataGlobalVariableTreeItem) {
                if (((TestDataGlobalVariableTreeItem) treeItem).getSelectedColumn() != TestDataTreeItem.ColumnType.INPUT) {
                    ((TestDataGlobalVariableTreeItem) treeItem).setSelectedColumn(TestDataTreeItem.ColumnType.INPUT);
                    cell.getTreeTableView().refresh();
                }
            }

            if (treeItem instanceof TestDataTreeItem && treeItem.getValue() != null) {
                if (!((TestDataTreeItem) treeItem).getColumnType()
                        .equals(TestDataTreeItem.ColumnType.EXPECTED)) { // if not expected output column
                    ContextMenu contextMenu = cell.setupContextMenu(treeItem.getValue());
                    if (!contextMenu.getItems().isEmpty()
//                            && !treeItem.getValue().getName().equals("RETURN")
                    ) {
                        cell.setContextMenu(contextMenu);
                    }
                }
            }
        });

//        cell.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
//            cell.addBoundaryTooltip(AbstractTableCell.CellType.INPUT);
//        });

        return cell;
    }

    /**
     * Represents a single row/column in the test case tab
     */
    private class InputColumnCell extends AbstractTableCell {
        public InputColumnCell(IDataTestItem testCase) {
            super(testCase);
        }

        @Override
        public void startEdit() {
            logger.debug("Start editing on the cell at line " + this.getIndex());
            super.startEdit();

            saveValueWhenUsersPressEnter();
            if (getTreeTableRow().getTreeItem() instanceof TestDataTreeItem) {
                TestDataTreeItem testDataTreeItem = (TestDataTreeItem) getTreeTableRow().getTreeItem();
                TestDataTreeItem.ColumnType columnType = testDataTreeItem.getColumnType();
                if (columnType != TestDataTreeItem.ColumnType.EXPECTED) {
                    showText(CellType.INPUT);
                } else if (testDataTreeItem.getValue() != null && testDataTreeItem.getValue().getName().equals("RETURN")) {
                    // if testDataTreeItem is RETURN parameter of Stub function
                    showText(CellType.INPUT);
                }
            } else if (getTreeTableRow().getTreeItem() != null && getTreeTableRow().getTreeItem().getValue() instanceof TemplateSubprogramDataNode) {
                showText(CellType.INPUT);
            }

//            if (isEditing()) {
            escapePressed = false;
            final TreeTableView<DataNode> treeTableView = getTreeTableView();
            tablePos = treeTableView.getEditingCell();
//            }
        }

        @Override
        protected void doCommitEdit(String newValue) {
            //update status of testcase
            if (testCase instanceof TestCase)
                ((TestCase) testCase).setStatus(TestCase.STATUS_NA);

            TestCasesNavigatorController.getInstance().refreshNavigatorTree();

            TreeTableRow<DataNode> row = getTreeTableRow();
            DataNode dataNode = row.getItem();

            if (dataNode == null) {
                logger.debug("There is matching between a cell and its data");
            }

            TreeItem<DataNode> treeItem = row.getTreeItem();

            try {
                ValueDataNode valueDataNode = (ValueDataNode) dataNode;

                // clear value
                if (newValue == null || newValue.isEmpty()) {
                    clearValue(valueDataNode);
                }
                // commit value
                else {
                    onRetrieveValue(valueDataNode, newValue);
                }

                // reload các con của tree item
                if (!(dataNode instanceof NormalCharacterDataNode || dataNode instanceof NormalNumberDataNode
                        || dataNode instanceof EnumDataNode))
                    TestCaseTreeTableController.loadChildren(testCase, treeItem);

                // Subprogram under test is method of current instance
                // then expand with corresponding template arguments
                if (isCommitInstanceOfSut(dataNode)) {
                    TestCaseTreeTableController.loadChildren(testCase, getSutTreeItem(treeItem));
                }

            } catch (Exception ex) {
                assert dataNode != null;
                logger.debug("Error " + ex.getMessage() + " when entering data for " + dataNode.getClass());
                ex.printStackTrace();
                UIController.showErrorDialog("Error " + ex.getMessage() + " when entering data for " + dataNode.getName(), "Test data entering", "Invalid value");
            }

            getTreeTableView().refresh();
            logger.debug("Refreshed the current test case tab");

            // save data tree to the test script
            TestCaseManager.exportTestCaseToFile(testCase);
        }

        private boolean isCommitInstanceOfSut(DataNode dataNode) {
            ICommonFunctionNode sut = testCase.getFunctionNode();

            INode realParent = sut.getParent();

            if (sut instanceof IFunctionNode && ((IFunctionNode) sut).getRealParent() != null)
                realParent = ((IFunctionNode) sut).getRealParent();

            if (realParent instanceof ClassNode && sut.isTemplate()) {
                if (dataNode instanceof TemplateClassDataNode && !dataNode.getChildren().isEmpty()
                        && ((TemplateClassDataNode) dataNode).getCorrespondingVar() instanceof InstanceVariableNode) {
                    INode classNode = ((TemplateClassDataNode) dataNode).getCorrespondingType();
                    return classNode.equals(realParent.getParent());
                }
            }

            return false;
        }

        private TreeItem<DataNode> getSutTreeItem(TreeItem<DataNode> current) {
            // STEP 1: get root
            TreeItem<DataNode> root = current;
            while (root.getParent() != null)
                root = root.getParent();

            // STEP 2: get unit under test
            TreeItem<DataNode> uut = null;
            for (TreeItem<DataNode> child : root.getChildren()) {
                DataNode node = child.getValue();
                if (node instanceof UnitUnderTestNode) {
                    uut = child;
                    break;
                }
            }

            // STEP 3: get subprogram under test
            INode sut = testCase.getFunctionNode();
            if (uut != null) {
                for (TreeItem<DataNode> child : uut.getChildren()) {
                    DataNode node = child.getValue();
                    if (node instanceof SubprogramNode && ((SubprogramNode) node).getFunctionNode().equals(sut)) {
                        return child;
                    }
                }
            }

            return null;
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            pseudoClassStateChanged(pseudoClass, false);

            TreeItem<DataNode> treeItem = getTreeTableRow().getTreeItem();
            if (treeItem != null && treeItem.getValue() != null) {
                // if the tree item is child of a Parameter tree item, disable expected output column
                if (treeItem instanceof TestDataTreeItem
                        && ((TestDataTreeItem) treeItem).getColumnType() == TestDataTreeItem.ColumnType.EXPECTED
                        && !treeItem.getValue().getName().equals("RETURN")) {
                    setEditable(false);
                    setText(null);
                    setGraphic(null);
                    return;
                }

                if (treeItem instanceof TestDataParameterTreeItem) {
                    if (!treeItem.getValue().getName().equals("RETURN")) {
                        pseudoClassStateChanged(pseudoClass, true);
                    }
                } else if (treeItem instanceof TestDataGlobalVariableTreeItem) {
                    pseudoClassStateChanged(pseudoClass, true);
                }

                DataNode dataNode = treeItem.getValue();

                // disable input when the variable is return variable
                if (dataNode instanceof ValueDataNode)
                    if (((ValueDataNode) dataNode).isExpected()) {
//                        disable();
                        return;
                    }

                if (treeItem instanceof TestDataParameterTreeItem) {
                    ValueDataNode inputDataNode = ((TestDataParameterTreeItem) treeItem).getInputDataNode();
                    inputCellHandler.update(this, inputDataNode);
                } else if (treeItem instanceof TestDataGlobalVariableTreeItem) {
                    ValueDataNode inputDataNode = ((TestDataGlobalVariableTreeItem) treeItem).getInputDataNode();
                    inputCellHandler.update(this, inputDataNode);
                } else {
                    inputCellHandler.update(this, dataNode);
                }

            } else {
                setText(null);
                setGraphic(null);
            }
        }

    }
}