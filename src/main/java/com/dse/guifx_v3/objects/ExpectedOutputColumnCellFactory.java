package com.dse.guifx_v3.objects;

import com.dse.guifx_v3.controllers.TestCaseTreeTableController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.logger.AkaLogger;
import com.dse.search.Search2;
import com.dse.testcase_manager.IDataTestItem;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testdata.Iterator;
import com.dse.testdata.comparable.AssertMethod;
import com.dse.testdata.object.*;
import javafx.css.PseudoClass;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class ExpectedOutputColumnCellFactory implements Callback<TreeTableColumn<DataNode, String>, TreeTableCell<DataNode, String>> {
    private final static AkaLogger logger = AkaLogger.get(ExpectedOutputColumnCellFactory.class);
    private static final PseudoClass pseudoClass = PseudoClass.getPseudoClass("expected-output-column-parameter");
    private final IDataTestItem testCase;

    public ExpectedOutputColumnCellFactory(IDataTestItem testCase) {
        super();
        this.testCase = testCase;
    }

    @Override
    public TreeTableCell<DataNode, String> call(TreeTableColumn<DataNode, String> param) {
        ExpectedCell cell = new ExpectedCell(testCase);
        cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            TreeItem<DataNode> treeItem = cell.getTreeTableRow().getTreeItem();
            if (treeItem instanceof TestDataParameterTreeItem) {
                if (!treeItem.getValue().getName().equals("RETURN")) {
                    if (((TestDataParameterTreeItem) treeItem).getSelectedColumn() != TestDataTreeItem.ColumnType.EXPECTED) {
                        ((TestDataParameterTreeItem) treeItem).setSelectedColumn(TestDataTreeItem.ColumnType.EXPECTED);
                        cell.getTreeTableView().refresh();
                        // lazy load
                        if (treeItem.getChildren().isEmpty()) {
                            TestCaseTreeTableController.loadChildren(testCase, treeItem);
                        }
                    }
                }
            } else if (treeItem instanceof TestDataGlobalVariableTreeItem) {
                if (((TestDataGlobalVariableTreeItem) treeItem).getSelectedColumn() != TestDataTreeItem.ColumnType.EXPECTED) {
                    ((TestDataGlobalVariableTreeItem) treeItem).setSelectedColumn(TestDataTreeItem.ColumnType.EXPECTED);
                    cell.getTreeTableView().refresh();
                    // lazy load
                    if (treeItem.getChildren().isEmpty()) {
                        TestCaseTreeTableController.loadChildren(testCase, treeItem);
                    }
                }
            }

            if (treeItem != null && treeItem.getValue() != null) {
                if (treeItem instanceof TestDataTreeItem) {
                    if (!((TestDataTreeItem) treeItem).getColumnType()
                            .equals(TestDataTreeItem.ColumnType.INPUT)) { // if not input column
                        ContextMenu contextMenu = cell.setupContextMenu(treeItem.getValue());
                        if (!contextMenu.getItems().isEmpty()) {
                            cell.setContextMenu(contextMenu);
                        }
                    }
                }
            }
        });

//        cell.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
//            cell.addBoundaryTooltip(AbstractTableCell.CellType.EXPECTED);
//        });

        return cell;
    }

    /**
     * Represents a single row/column in the test case tab
     */
    private class ExpectedCell extends AbstractTableCell {
        public ExpectedCell(IDataTestItem testCase) {
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

                // if the value is parameter of Stub Function, and not is a return node
                if (!(columnType.equals(TestDataTreeItem.ColumnType.EXPECTED) && testDataTreeItem.getValue().getName().equals("RETURN"))) {
                    if (columnType != TestDataTreeItem.ColumnType.INPUT) {
                        showText(CellType.EXPECTED);
                    }
                }
            }

//            if (isEditing()) {
            escapePressed = false;
            final TreeTableView<DataNode> treeTableView = getTreeTableView();
            tablePos = treeTableView.getEditingCell();
//            }
        }

        @Override
        protected void doCommitEdit(String newValue) {
            TreeTableRow<DataNode> row = getTreeTableRow();
            DataNode dataNode = row.getItem();

            if (dataNode instanceof ValueDataNode) {
                try {
                    ValueDataNode expectedNode, actualNode = null;

                    if (row.getTreeItem() instanceof TestDataParameterTreeItem) {
                        TestDataParameterTreeItem parameterTreeItem = (TestDataParameterTreeItem) row.getTreeItem();

                        if (dataNode.getName().equals("RETURN")) {
                            expectedNode = (ValueDataNode) dataNode;
                        } else {
                            expectedNode = parameterTreeItem.getExpectedOutputDataNode();
                        }

                        actualNode = parameterTreeItem.getInputDataNode();

                    } else if (row.getTreeItem() instanceof TestDataGlobalVariableTreeItem) {
                        TestDataGlobalVariableTreeItem globalVarTreeItem = (TestDataGlobalVariableTreeItem) row.getTreeItem();

                        expectedNode = globalVarTreeItem.getExpectedOutputDataNode();
                        actualNode = globalVarTreeItem.getInputDataNode();

                    } else {
                        expectedNode = (ValueDataNode) dataNode;
                        actualNode = Search2.getActualValue(expectedNode);
                    }

                    // clear value
                    if (newValue == null || newValue.isEmpty()) {
                        clearValue(expectedNode);
                    }
                    // commit edit new value
                    else {
                        onRetrieveValue(expectedNode, newValue);
                    }

                    // reload các con của tree item
                    if (!(expectedNode instanceof NormalDataNode || expectedNode instanceof EnumDataNode)) {
                        TreeItem<DataNode> treeItem = row.getTreeItem();
                        TestCaseTreeTableController.loadChildren(testCase, treeItem);
                    } else {
                        expectedNode.getIterators().forEach(i -> i.getDataNode().setAssertMethod(AssertMethod.ASSERT_EQUAL));
//                        expectedNode.setAssertMethod(AssertMethod.ASSERT_EQUAL);
                        if (actualNode != null)
                            actualNode.getIterators().forEach(i -> i.getDataNode().setAssertMethod(AssertMethod.ASSERT_EQUAL));
//                            actualNode.setAssertMethod(AssertMethod.ASSERT_EQUAL);
                    }

                } catch (Exception ex) {
                    logger.error("Error " + ex.getMessage() + " when entering data for " + dataNode.getClass());
                    UIController.showErrorDialog("Error " + ex.getMessage() + " when entering data for " + dataNode.getName(), "Test data entering", "Invalid value");
                }

                getTreeTableView().refresh();
                logger.debug("Refreshed the current test case tab");

                // save data tree to the test script
                TestCaseManager.exportTestCaseToFile(testCase);

            } else {
                logger.debug("There is matching between a cell and its data");
            }
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            pseudoClassStateChanged(pseudoClass, false);

            TreeItem<DataNode> treeItem = getTreeTableRow().getTreeItem();
            if (treeItem != null && treeItem.getValue() != null) {
                if (treeItem instanceof TestDataTreeItem
                        && ((TestDataTreeItem) treeItem).getColumnType().equals(TestDataTreeItem.ColumnType.EXPECTED)
                        && treeItem.getValue().getName().equals("RETURN")) {
                    setEditable(false);
                    setText(null);
                    setGraphic(null);
                    return;
                }

                if (treeItem instanceof TestDataTreeItem
                        && ((TestDataTreeItem) treeItem).getColumnType() == TestDataTreeItem.ColumnType.INPUT) {
                    setEditable(false);
                    setText(null);
                    setGraphic(null);
                    return;
                }

                if (treeItem instanceof TestDataParameterTreeItem || treeItem instanceof TestDataGlobalVariableTreeItem) {
                    pseudoClassStateChanged(pseudoClass, true);
                }

                DataNode dataNode = treeItem.getValue();

                // if the dataNode is a parameter, show value of Expected Output
                if (treeItem instanceof TestDataParameterTreeItem) {
                    if (dataNode.getName().equals("RETURN")) {
                        inputCellHandler.update(this, dataNode);
                    } else {
                        ValueDataNode expectedOutput = ((TestDataParameterTreeItem) treeItem).getExpectedOutputDataNode();
                        inputCellHandler.update(this, expectedOutput);
                    }
                } else if (treeItem instanceof TestDataGlobalVariableTreeItem) {
                    ValueDataNode expectedOutput = ((TestDataGlobalVariableTreeItem) treeItem).getExpectedOutputDataNode();
                    inputCellHandler.update(this, expectedOutput);
                } else if (treeItem.getValue() instanceof TemplateSubprogramDataNode) {
                    // show nothing
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
