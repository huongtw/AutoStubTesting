package com.dse.guifx_v3.objects;

import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.helps.Factory;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.NumberOfCallNode;
import com.dse.search.Search2;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testdata.comparable.AssertMethod;
import com.dse.testdata.object.*;
import com.dse.user_code.controllers.ParameterUserCodeDialogController;
import com.dse.logger.AkaLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.io.File;

public class AssertColumnCellFactory implements Callback<TreeTableColumn<DataNode, String>, TreeTableCell<DataNode, String>> {

    private final static AkaLogger logger = AkaLogger.get(AssertColumnCellFactory.class);
    private final TestCase testCase;

    public AssertColumnCellFactory(TestCase testCase) {
        super();
        this.testCase = testCase;
    }

    @Override
    public TreeTableCell<DataNode, String> call(TreeTableColumn<DataNode, String> param) {
        AssertColumnCell cell = new AssertColumnCell(testCase);
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
        });


        return cell;

    }

    /**
     * Represents a single row/column in the test case tab
     */
    private static class AssertColumnCell extends TreeTableCell<DataNode, String> {

        private final TestCase testCase;

        public AssertColumnCell(TestCase testCase) {
            this.testCase = testCase;
        }


        @Override
        public void startEdit() {
            if (getTreeTableRow().getItem() instanceof NumberOfCallNode)
                return;

            logger.debug("Start editing on the cell at line " + this.getIndex());
            super.startEdit();

            showComboBox();
        }

        protected void showComboBox() {
            DataNode dataNode = getTreeTableRow().getTreeItem().getValue();

            setText(null);
            setGraphic(null);

            if (isVisible(dataNode)) {
                ValueDataNode valueNode = (ValueDataNode) dataNode;
                logger.debug("The type of data node corresponding to the cell: " + valueNode.getClass());
                logger.debug("Support assert method:");

                ComboBox<String> comboBox = new ComboBox<>();
                ObservableList<String> options = FXCollections.observableArrayList();

                ValueDataNode expectedNode = Search2.getExpectedValue(valueNode);
                if (expectedNode == null)
                    expectedNode = valueNode;
                String prevMethod = expectedNode.getAssertMethod();
                if (prevMethod == null)
                    comboBox.setValue("<<Select Method>>");
                else
                    comboBox.setValue(prevMethod);

                String[] supportedMethod = expectedNode.getAllSupportedAssertMethod();
                options.addAll(supportedMethod);

                comboBox.setItems(options);
                // Chỉnh sửa cho combobox vừa với ô của tree table.
                comboBox.setMaxWidth(getTableColumn().getMaxWidth());
                // Khi chọn giá trị trong combobox thì commit giá trị đó.
                comboBox.valueProperty().addListener((ov, oldValue, newValue) -> commitEdit(newValue));

                setGraphic(comboBox);

            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            getTreeTableView().refresh();
            logger.debug("Canceled the edit on the cell");
        }

        @Override
        public void commitEdit(String newValue) {
            super.commitEdit(newValue);

            if (newValue.isEmpty())
                newValue = null;

            //update status of testcase
            testCase.setStatus(TestCase.STATUS_NA);
            TestCasesNavigatorController.getInstance().refreshNavigatorTree();

            TreeTableRow<DataNode> row = getTreeTableRow();
            DataNode dataNode = row.getItem();

            if (dataNode == null) {
                logger.debug("There is matching between a cell and its data");
                return;
            }

            try {
                if (dataNode instanceof ValueDataNode) {
                    ValueDataNode valueNode = (ValueDataNode) dataNode;
                    ValueDataNode expectedNode = Search2.getExpectedValue(valueNode);
                    if (expectedNode != null) {
                        expectedNode.setAssertMethod(newValue);
                    }
                    valueNode.setAssertMethod(newValue);

                    if (AssertMethod.USER_CODE.equals(newValue)) {
                        ParameterUserCodeDialogController controller = ParameterUserCodeDialogController
                                .getAssertInstance(valueNode);
                        if (controller != null && controller.getStage() != null) {
                            controller.setTestCase(testCase);
                            controller.showAndWaitStage(UIController.getPrimaryStage());
                        }
                    }
                }

            } catch (Exception ex) {
                logger.debug("Error " + ex.getMessage() + " when entering data for " + dataNode.getClass());
                ex.printStackTrace();
            }

            getTreeTableView().refresh();
            logger.debug("Refreshed the current test case tab");

            // save data tree to the test script
            TestCaseManager.exportBasicTestCaseToFile(testCase);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            TreeItem<DataNode> treeItem = getTreeTableRow().getTreeItem();

            if (treeItem != null && isVisible(treeItem.getValue())) {
                ValueDataNode valueNode = (ValueDataNode) treeItem.getValue();

                if (valueNode instanceof NumberOfCallNode) {
                    NumberOfCallNode numberOfCallNode = (NumberOfCallNode) valueNode;
                    if (numberOfCallNode.haveValue()) {
                        setText(AssertMethod.ASSERT_EQUAL);
                        setGraphic(null);
                    } else {
                        setText(null);
                        setGraphic(null);
                    }
                } else {
                    String assertMethod = valueNode.getAssertMethod();
                    ValueDataNode expectedNode = Search2.getExpectedValue(valueNode);
                    if (expectedNode != null) {
                        String expectedAssertMethod = expectedNode.getAssertMethod();
                        if (assertMethod == null) {
                            assertMethod = expectedAssertMethod;
                        } else if (!assertMethod.equals(expectedAssertMethod))
                            assertMethod = expectedAssertMethod;

                        expectedNode.setAssertMethod(assertMethod);
                        valueNode.setAssertMethod(assertMethod);

//                    if (AssertMethod.USER_CODE.equals(assertMethod)) {
//                        AssertUserCode userCode = valueNode.getAssertUserCode();
//                        expectedNode.setAssertUserCode(userCode);
//                    }
                    }

                    if (assertMethod != null) {
                        setText(assertMethod);

                        if (assertMethod.equals(AssertMethod.USER_CODE)) {
                            Button button = new Button();
                            ImageView imageView = new ImageView(
                                    new Image(Factory.class.getResourceAsStream("/icons/file/newEnvironment.png")));
                            button.setGraphic(imageView);
                            imageView.setOnMousePressed(new EventHandler<MouseEvent>() {
                                @Override
                                public void handle(MouseEvent event) {
                                    System.out.println("CLICK");
                                }
                            });
                            button.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    commitEdit(AssertMethod.USER_CODE);
                                }
                            });
                            setGraphic(button);

                        } else
                            setGraphic(null);

                    } else {
                        setText("<<Select Method>>");
                        setGraphic(null);
                    }

                }

            } else {
                setText(null);
                setGraphic(null);
            }
        }

        private boolean isVisible(DataNode dataNode) {
            if (dataNode == null)
                return false;

            if (!(dataNode instanceof ValueDataNode))
                return false;

            ValueDataNode valueNode = (ValueDataNode) dataNode;

            if (valueNode instanceof SubprogramNode)
                return false;

//            if (valueNode instanceof StructDataNode
//                    || valueNode instanceof UnionDataNode
//                    || valueNode instanceof ClassDataNode)
//                return true;

            if (valueNode.isVoidPointerValue())
                return false;

            boolean isReturnItem = valueNode.getPathFromRoot().contains(RETURN_TAG);

            return !isReturnItem || valueNode.isExpected();
        }
    }

    private static final String RETURN_TAG = File.separator + "RETURN";
}