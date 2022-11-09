package com.dse.guifx_v3.objects;

import com.dse.guifx_v3.controllers.TestCaseTreeTableController;
import com.dse.guifx_v3.controllers.TestCasesNavigatorController;
import com.dse.guifx_v3.helps.DataNodeIterationDialog;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.parser.object.NumberOfCallNode;
import com.dse.report.converter.Converter;
import com.dse.testcase_manager.IDataTestItem;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testdata.Iterator;
import com.dse.testdata.gen.module.TreeExpander;
import com.dse.testdata.gen.module.subtree.InitialStubTreeGen;
import com.dse.testdata.object.*;
import com.dse.testdata.object.GlobalRootDataNode;
import com.dse.testdata.object.stl.ListBaseDataNode;
import com.dse.logger.AkaLogger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.List;

public class ParameterColumnCellFactory implements Callback<TreeTableColumn<DataNode, Boolean>, TreeTableCell<DataNode, Boolean>> {
    private final static AkaLogger logger = AkaLogger.get(ParameterColumnCellFactory.class);
    private final IDataTestItem testCase;

    public ParameterColumnCellFactory(IDataTestItem testCase) {
        super();
        this.testCase = testCase;
    }

    @Override
    public TreeTableCell<DataNode, Boolean> call(TreeTableColumn<DataNode, Boolean> param) {
        ParameterColumnCell cell = new ParameterColumnCell(testCase);

        cell.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
            cell.addTooltip();
        });

        return cell;
    }

    /**
     * Represents a single row/column in the test case tab
     */
    public static class ParameterColumnCell extends CheckBoxTreeTableCell<DataNode, Boolean> {
        private final IDataTestItem testCase;

        private ObservableValue<Boolean> booleanProperty;

        private BooleanProperty indeterminateProperty;

        public ParameterColumnCell(IDataTestItem testCase) {
            this.testCase = testCase;
        }

        private ContextMenu setupContextMenu(DataNode dataNode) {
            ContextMenu contextMenu = new ContextMenu();

            if (dataNode instanceof GlobalRootDataNode) {
                GlobalRootDataNode globalRoot = (GlobalRootDataNode) dataNode;
                final boolean isShowRelated = globalRoot.isShowRelated();

                MenuItem menuItem = new MenuItem();
                if (isShowRelated) {
                    menuItem.setText("Show all variables");
                } else {
                    menuItem.setText("Show related variables");
                }

                menuItem.setOnAction(event -> changeViewMode(globalRoot, !isShowRelated));
                contextMenu.getItems().add(menuItem);
            }

            if (dataNode instanceof ArrayDataNode || dataNode instanceof PointerDataNode
                    || dataNode instanceof ListBaseDataNode) {

                MenuItem miExpandFirstItem = new MenuItem("Expand first children");
                StringBuilder input = new StringBuilder();
                int dim = 1;
                if (dataNode instanceof MultipleDimensionDataNode)
                    dim = ((MultipleDimensionDataNode) dataNode).getDimensions();
                else if (dataNode instanceof PointerDataNode)
                    dim = ((PointerDataNode) dataNode).getLevel();
                for (int i = 0; i < dim; i++)
                    input.append("[0]");
                miExpandFirstItem.setOnAction(event -> {
                    try {
                        expandArrayItems(input.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                contextMenu.getItems().add(miExpandFirstItem);

                MenuItem miExpandSelectedItem = new MenuItem("Expand specific children");
                miExpandSelectedItem.setOnAction(event -> UIController.showArrayExpanderDialog(this));
                contextMenu.getItems().add(miExpandSelectedItem);

            }

            if (dataNode instanceof MacroSubprogramDataNode) {
                MenuItem miDeclareType = new MenuItem("Define macro type");
                miDeclareType.setOnAction(event ->
                        UIController.showMacroTypeDefinitionDialog((MacroSubprogramDataNode) dataNode,this));
                contextMenu.getItems().add(miDeclareType);
            }

            if (dataNode instanceof TemplateSubprogramDataNode) {
                MenuItem miDeclareType = new MenuItem("Define template type");
                miDeclareType.setOnAction(event ->
                        UIController.showTemplateTypeDefinitionDialog((TemplateSubprogramDataNode) dataNode,this));
                contextMenu.getItems().add(miDeclareType);
            }

//            if (dataNode instanceof ValueDataNode && ((ValueDataNode) dataNode).isStubArgument()) {
//                MenuItem miIterator = new MenuItem("Add iterator");
//                miIterator.setOnAction(event -> {
//                    DataNodeIterationDialog dialog = new DataNodeIterationDialog((ValueDataNode) dataNode) {
//
//                        @Override
//                        public void onExpand(Iterator iterator) {
//                            expandIteration(iterator);
//                        }
//                    };
//
//                    Platform.runLater(dialog::showAndWait);
//                });
//
//                contextMenu.getItems().add(miIterator);
//            }

            return contextMenu;
        }

        private void expandIteration(Iterator iterator) {
            if (iterator.getRepeat() == Iterator.FILL_ALL && iterator.getStartIdx() == 0)
                return;

            DataNode iteratorNode = iterator.getDataNode();

            TestDataTreeItem child = new TestDataTreeItem(iteratorNode);
            if (iteratorNode.getName().equals("RETURN"))
                child.setColumnType(TestDataTreeItem.ColumnType.INPUT);
            else
                child.setColumnType(TestDataTreeItem.ColumnType.EXPECTED);
            TreeItem<DataNode> current = getTreeTableRow().getTreeItem();
            TreeItem<DataNode> parent = current.getParent();

            int index = parent.getChildren().indexOf(current);
            parent.getChildren().remove(index);
            parent.getChildren().add(index, child);

            TestCaseTreeTableController.loadChildren(testCase, child);

            getTreeTableView().refresh();

            TestCaseManager.exportTestCaseToFile(testCase);
        }

        private String getDisplayName(DataNode dataNode) {
            String displayName = dataNode.getDisplayNameInParameterTree();

            TreeItem<DataNode> treeItem = getTreeTableRow().getTreeItem();
            TreeItem<DataNode> parentTreeItem = treeItem.getParent();
            DataNode parentNode = parentTreeItem.getValue();

            if (parentNode.getDisplayNameInParameterTree().equals(displayName)) {
                if (parentNode instanceof ValueDataNode) {
                    Iterator firstIterator = ((ValueDataNode) parentNode).getIterators().get(0);
                    if (dataNode == firstIterator.getDataNode()) {
                        displayName = firstIterator.getDisplayName();
                    }
                }
            }

            return displayName;
        }

        public void addTooltip() {
            // Get datanode object
            TreeTableRow<DataNode> row = getTreeTableRow();

            if (row.getItem() instanceof ValueDataNode) {
                ValueDataNode node = (ValueDataNode) row.getItem();
                if (node.isStubArgument()) {
                    // check if multiple value
                    if (node.getIterators().size() > 1) {
                        StringBuilder content = new StringBuilder("Value = {");
                        for (Iterator i : node.getIterators()) {
                            content.append(Converter.getNodeValue(i.getDataNode()));
                            if (i.getRepeat() != 1) {
                                content.append("(")
                                        .append(i.getRepeat())
                                        .append(")");
                            }

                            if (node.getIterators().indexOf(i) != node.getIterators().size() - 1)
                                content.append(", ");
                        }
                        content.append("}");
                        setTooltip(new Tooltip(content.toString()));
                    }
                }
            }
        }

        private void updateDisplay(DataNode dataNode) {
            if (dataNode != null) {
                String displayName = "";
                if (dataNode instanceof IterationSubprogramNode) {
                    displayName = getDisplayName(dataNode) + "_call" + ((IterationSubprogramNode) dataNode).getIndex();
                } else {
                    displayName = getDisplayName(dataNode);
                }
                Label label = new Label(displayName);
                ContextMenu contextMenu = setupContextMenu(dataNode);

                if (!contextMenu.getItems().isEmpty())
                    setContextMenu(contextMenu);

                // Các node show checkbox
                if (dataNode instanceof SubprogramNode  && !(dataNode instanceof IterationSubprogramNode) && ((SubprogramNode) dataNode).isStubable()) {
                    CheckBox checkBox = new CheckBox();
                    checkBox.setAlignment(Pos.TOP_LEFT);
                    checkBox.addEventFilter(MouseEvent.MOUSE_PRESSED,
                            event -> commitEdit(dataNode.getChildren().isEmpty()));
                    bindingCheckbox(dataNode, checkBox);

                    label.setGraphic(checkBox);
                    setGraphic(label);
                    label.requestFocus();

                    setText(null);
                }

                else if (dataNode instanceof ValueDataNode
                        && ((ValueDataNode) dataNode).isStubArgument()
                        && ((ValueDataNode) dataNode).getIterators().size() > 1) {
                    VBox vBox = new VBox();
                    vBox.setSpacing(-5);

                    ImageView up = new ImageView("/icons/debug/up.png");
                    up.setScaleX(0.7f);
                    up.setScaleY(0.7f);
                    Hint.tooltipNode(up, "View next iteration");
                    up.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            List<Iterator> iterators = ((ValueDataNode) dataNode).getIterators();
                            int index = getIterationIndex((ValueDataNode) dataNode);

                            if (index < iterators.size() - 1) {
                                expandIteration(iterators.get(index + 1));
                            }
                        }
                    });

                    ImageView down = new ImageView("icons/debug/down.png");
                    down.setScaleX(0.7f);
                    down.setScaleY(0.7f);
                    Hint.tooltipNode(down, "View previous iteration");
                    down.setOnMouseClicked(new EventHandler<MouseEvent>() {
                        @Override
                        public void handle(MouseEvent event) {
                            List<Iterator> iterators = ((ValueDataNode) dataNode).getIterators();
                            int index = getIterationIndex((ValueDataNode) dataNode);

                            if (index > 0) {
                                expandIteration(iterators.get(index - 1));
                            }
                        }
                    });

                    int index = getIterationIndex((ValueDataNode) dataNode);
                    List<Iterator> iterators = ((ValueDataNode) dataNode).getIterators();

                    if (index == 0) {
                        down.setVisible(false);
                        down.setDisable(true);
                    } else if (index == iterators.size() - 1) {
                        up.setVisible(false);
                        up.setDisable(true);
                    } else {
                        up.setVisible(true);
                        up.setDisable(false);
                        down.setVisible(true);
                        down.setDisable(false);
                    }

                    vBox.getChildren().addAll(up, down);

                    label.setGraphic(vBox);
                    setGraphic(label);
                    label.requestFocus();

                    setText(null);
                }

                // Các node show text
                else {
                    setGraphic(label);
                    label.requestFocus();
                }

            } else {
                logger.debug("There is no matching between a cell and a data node");
            }
        }

        private int getIterationIndex(ValueDataNode dataNode) {
            List<Iterator> iterators = dataNode.getIterators();
            int size = iterators.size();

            int index = 0;
            for (int i = 0; i < size; i++) {
                if (iterators.get(i).getDataNode() == dataNode) {
                    index = i;
                }
            }

            return index;
        }

        private void bindingCheckbox(DataNode node, CheckBox checkBox) {
            // uninstall bindings
            if (booleanProperty != null) {
                checkBox.selectedProperty().unbindBidirectional((BooleanProperty)booleanProperty);
            }
            if (indeterminateProperty != null) {
                checkBox.indeterminateProperty().unbindBidirectional(indeterminateProperty);
            }

            // install new bindings.
            // this can only handle TreeItems of type CheckBoxTreeItem
            TreeItem<DataNode> treeItem = getTreeTableRow().getTreeItem();

            if (treeItem instanceof CheckBoxTreeItem) {
                CheckBoxTreeItem<DataNode> cbti = (CheckBoxTreeItem<DataNode>) treeItem;
                booleanProperty = new SimpleBooleanProperty(!node.getChildren().isEmpty());
                checkBox.selectedProperty().bindBidirectional((BooleanProperty)booleanProperty);

                indeterminateProperty = cbti.indeterminateProperty();
                checkBox.indeterminateProperty().bindBidirectional(indeterminateProperty);
            }
        }

        @Override
        public void startEdit() {
            logger.debug("Start editing on the cell at line " + this.getIndex());
            super.startEdit();

            setText(null);

//            TreeItem<DataNode>
            DataNode dataNode = getTreeTableRow().getTreeItem().getValue();
            updateDisplay(dataNode);
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();
            getTreeTableView().refresh();
            logger.debug("Canceled the edit on the cell");
        }

        @Override
        public void commitEdit(Boolean newValue) {
            super.commitEdit(newValue);

            TestCasesNavigatorController.getInstance().refreshNavigatorTree();

            TreeTableRow<DataNode> row = getTreeTableRow();
            DataNode dataNode = row.getItem();

            if (dataNode == null) {
                logger.debug("There is matching between a cell and its data");
            }

            TreeItem<DataNode> treeItem = row.getTreeItem();

            if (dataNode instanceof SubprogramNode && ((SubprogramNode) dataNode).isStubable()) {
                SubprogramNode subprogram = (SubprogramNode) dataNode;

                subprogram.getChildren().clear();

                if (newValue) {
                    NumberOfCallNode numberOfCall = new NumberOfCallNode("Number of calls");
                    subprogram.addChild(numberOfCall);
                    numberOfCall.setParent(subprogram);
                }

                TestCaseTreeTableController.loadChildren(testCase, treeItem);

                getTreeTableView().refresh();
                logger.debug("Refreshed the current test case tab");

                // save data tree to the test script
                TestCaseManager.exportTestCaseToFile(testCase);
            }
        }

        @Override
        public void updateItem(Boolean item, boolean empty) {
            super.updateItem(item, empty);

            if (getTreeTableRow().getTreeItem() != null) {
                DataNode dataNode = getTreeTableRow().getTreeItem().getValue();

                setEditable(true);

                updateDisplay(dataNode);

            } else {
                setText(null);
                setGraphic(null);
            }
        }

        public void refresh() {
            TreeTableRow<DataNode> row = getTreeTableRow();
            TreeItem<DataNode> treeItem = row.getTreeItem();

            TestCaseTreeTableController.loadChildren(testCase, treeItem);

            getTreeTableView().refresh();
            logger.debug("Refreshed the current test case tab");

            // save data tree to the test script
            TestCaseManager.exportTestCaseToFile(testCase);
        }

        private void changeViewMode(GlobalRootDataNode root, boolean mode) {
            root.setShowRelated(mode);

            TreeItem<DataNode> treeItem = getTreeTableRow().getTreeItem();
            TestCaseTreeTableController.loadChildren(testCase, treeItem);

            getTreeTableView().refresh();
            logger.debug("Refreshed the current test case tab");
        }

        public void expandArrayItems(String input) throws Exception {
            TreeItem<DataNode> treeItem = getTreeTableRow().getTreeItem();
            DataNode node = treeItem.getValue();

            List<String> expanded = new TreeExpander()
                    .expandArrayItemByIndex((ValueDataNode) node, input);

            TestCaseTreeTableController.loadChildren(testCase, treeItem, expanded);

            getTreeTableView().refresh();
            logger.debug("Refreshed the current test case tab");

            // save data tree to the test script
            TestCaseManager.exportTestCaseToFile(testCase);
        }
    }
}