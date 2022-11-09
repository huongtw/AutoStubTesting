package com.dse.guifx_v3.controllers;

import com.dse.guifx_v3.objects.*;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.FunctionPointerTypeNode;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.IFunctionPointerTypeNode;
import com.dse.parser.object.IVariableNode;
import com.dse.testcase_manager.IDataTestItem;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.object.*;
import com.dse.util.NodeType;
import com.dse.util.TemplateUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TestCaseTreeTableController implements Initializable {
    private final static AkaLogger logger = AkaLogger.get(TestCaseTreeTableController.class);

    private TreeItem<DataNode> root;
    private IDataTestItem testCase;

    @FXML
    private TreeTableColumn<DataNode, String> typeCol;
    @FXML
    private TreeTableColumn<DataNode, Boolean> parameterCol;
    @FXML
    private TreeTableColumn<DataNode, String> inputCol;
    @FXML
    private TreeTableColumn<DataNode, String> outputCol;
    @FXML
    private TreeTableColumn<DataNode, String> assertCol;

    @FXML
    private TreeTableView<DataNode> treeTableView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        root = new TreeItem<>(new RootDataNode());

        treeTableView.setRoot(root);
        treeTableView.setShowRoot(false);
        treeTableView.setEditable(true);
        treeTableView.getSelectionModel().setCellSelectionEnabled(true);

//        treeTableView.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
//            final KeyCombination keyComb = new KeyCodeCombination(KeyCode.ESCAPE,
//                    KeyCombination.CONTROL_ANY);
//            public void handle(KeyEvent ke) {
//                if (keyComb.match(ke)) {
//                    System.out.println("Key Pressed: " + keyComb);
//                    ke.consume(); // <-- stops passing the event to next node
//                }
//            }
//        });

        final PseudoClass pseudoClassInput = PseudoClass.getPseudoClass("input-column-is-selected");
        final PseudoClass pseudoClassExpected = PseudoClass.getPseudoClass("expected-output-column-is-selected");

        treeTableView.setRowFactory(param -> {
            final TreeTableRow<DataNode> row = new TreeTableRow<>();
            row.treeItemProperty().addListener((o, oldValue, newValue) -> {
                if (newValue != null) {
                    if (row.getTreeItem() instanceof TestDataTreeItem) {
                        if (((TestDataTreeItem) row.getTreeItem()).getColumnType() == TestDataTreeItem.ColumnType.INPUT) {
                            row.pseudoClassStateChanged(pseudoClassInput, true);
                        } else if (((TestDataTreeItem) row.getTreeItem()).getColumnType() == TestDataTreeItem.ColumnType.EXPECTED) {
                            row.pseudoClassStateChanged(pseudoClassExpected, true);
                        }
                    }

                    if (row.getTreeItem() != null) {
                        row.getTreeItem().expandedProperty().addListener(event -> {
                            row.getTreeTableView().refresh();
                        });
                    }

                }
            });

            return row;
        });

        ChangeListener<Number> scrollListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                treeTableView.refresh();
            }
        };

        treeTableView.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent scrollEvent) {
                treeTableView.refresh();
            }
        });

        treeTableView.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                ScrollBar scrollbar = getVerticalScrollbar();
                if (scrollbar != null) {
                    scrollbar.valueProperty().removeListener(scrollListener);
                    scrollbar.valueProperty().addListener(scrollListener);
                }
            }
        });

        typeCol.setCellValueFactory(param -> {
            String type = null;

            DataNode node = param.getValue().getValue();

            if (node instanceof ValueDataNode) {
                type = ((ValueDataNode) node).getRealType();

                if (node instanceof FunctionPointerDataNode) {
                    FunctionPointerDataNode fpDataNode = (FunctionPointerDataNode) node;
                    IFunctionPointerTypeNode typeNode = fpDataNode.getCorrespondingType();
                    if (typeNode != null)
                        type = typeNode.getReturnType();
                }

                type = type.replaceAll("\\s+", " ");
            }

            if (type != null)
                type = TemplateUtils.deleteTemplateParameters(type);

            return new SimpleStringProperty(type);
        });
    }

    private ScrollBar getVerticalScrollbar() {
        ScrollBar result = null;
        for(Node n : treeTableView.lookupAll(".scroll-bar")) {
            if(n instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) n;
                if(bar.getOrientation().equals(Orientation.VERTICAL)) {
                    result = bar;
                }
            }
        }
        return result;
    }

    private void loadContent(IDataTestItem testCase) {
        if (testCase != null) {
            RootDataNode rootDataNode = testCase.getRootDataNode();
            root = new TestDataTreeItem(rootDataNode);
            root.getChildren().clear();
            loadChildren(testCase, root);
            treeTableView.setRoot(root);
        }
    }

    public void loadTestCase(IDataTestItem testCase) {
        if (testCase != null) {
            this.testCase = testCase;
            inputCol.setCellFactory(new InputColumnCellFactory(testCase));
            if (testCase.isPrototypeTestcase())
                outputCol.setVisible(false);
            else {
                outputCol.setVisible(true);
                outputCol.setCellFactory(new ExpectedOutputColumnCellFactory(testCase));
            }
            parameterCol.setCellFactory(new ParameterColumnCellFactory(testCase));
            if (testCase instanceof TestCase)
                assertCol.setCellFactory(new AssertColumnCellFactory((TestCase) testCase));
            else
                treeTableView.getColumns().remove(assertCol);
            loadContent(testCase);
        }
    }

    public static void loadChildren(IDataTestItem testCase, TreeItem<DataNode> treeItem,
                                    List<String> children) {
        if (treeItem == null)
            return;

        treeItem.getChildren().clear();

        TestDataTreeItem.ColumnType defaultType = guestColumnType(treeItem);

        DataNode node = treeItem.getValue();

        if (!isVisible(node))
            return;

        for (IDataNode child : node.getChildren()) {
            if (children.contains(child.getName())) {
                loadChild(testCase, treeItem, (DataNode) child, defaultType);
            }
        }
    }

    public static void loadChildren(IDataTestItem testCase, TreeItem<DataNode> treeItem) {
        if (treeItem == null)
            return;

        treeItem.getChildren().clear();

        TestDataTreeItem.ColumnType defaultType = guestColumnType(treeItem);

        DataNode node = treeItem.getValue();

        if (!isVisible(node))
            return;

        for (IDataNode child : node.getChildren()) {
            loadChild(testCase, treeItem, (DataNode) child, defaultType);
        }
    }

//    public static void loadChildren(TreeItem<DataNode> treeItem) {
//        if (treeItem == null)
//            return;
//
//        treeItem.getChildren().clear();
//
//        DataNode node = treeItem.getValue();
//
//        if (!isVisible(node))
//            return;
//
//        for (IDataNode child : node.getChildren()) {
//            DataNode n = (DataNode) child;
//            TreeItem<DataNode> item = new TreeItem<>(n);
//
//            if (child instanceof SubprogramNode && !(child instanceof ConstructorDataNode))
//                item = new CheckBoxTreeItem<>(n);
//
//            treeItem.getChildren().add(item);
//
//            if (n.getChildren() != null) {
//                loadChildren(item);
//            }
//        }
//    }

    private static TestDataTreeItem.ColumnType guestColumnType(TreeItem<DataNode> treeItem) {
        TestDataTreeItem.ColumnType columnType = TestDataTreeItem.ColumnType.NONE;

        if (treeItem instanceof TestDataTreeItem) {
            columnType = ((TestDataTreeItem) treeItem).getColumnType();

            if (treeItem instanceof TestDataParameterTreeItem) {
                // if the tree item is of RETURN variable then its children is column type expected
                if (treeItem.getValue().getName().equals("RETURN")) {
                    columnType = TestDataTreeItem.ColumnType.EXPECTED;
                } else {
                    columnType = ((TestDataParameterTreeItem) treeItem).getSelectedColumn();
                }
            } else if (treeItem instanceof TestDataGlobalVariableTreeItem) {
                columnType = ((TestDataGlobalVariableTreeItem) treeItem).getSelectedColumn();
            }
        }

        return columnType;
    }

    private static void loadChild(IDataTestItem testCase, TreeItem<DataNode> treeItem,
                                  DataNode child, TestDataTreeItem.ColumnType defaultType) {
        if (!isVisible(child))
            return;

        if (testCase.isPrototypeTestcase() &&
                child.getParent() != null && child.getParent().getParent() != null &&
                child.getParent().getParent() instanceof SubprogramNode) {
            return;
        }

        TreeItem<DataNode> item = new TestDataTreeItem(child);

        TestDataTreeItem dataItem = (TestDataTreeItem) item;
        if (isStubFunction(treeItem.getValue())) {
            if (child.getName().equals("RETURN")) {
                dataItem.setColumnType(TestDataTreeItem.ColumnType.INPUT);
            } else
                dataItem.setColumnType(TestDataTreeItem.ColumnType.EXPECTED);
        } else
            dataItem.setColumnType(defaultType);

        if (child instanceof SubprogramNode && !(child instanceof ConstructorDataNode))
            item = new CheckBoxTreeItem<>(child);
        else if (child instanceof ValueDataNode) {
            ValueDataNode valueNode = (ValueDataNode) child;
            if (isParameter(testCase, valueNode))
                item = new TestDataParameterTreeItem(valueNode);
            else if (isGlobalVariable(valueNode))
                item = new TestDataGlobalVariableTreeItem(valueNode);
        }

        treeItem.getChildren().add(item);

        if (child.getChildren() != null) {
            loadChildren(testCase, item);
        }
    }

    public static boolean isParameter(IDataTestItem testCase, ValueDataNode dataNode) {
        IDataNode parent = dataNode.getParent();
        if (parent instanceof SubprogramNode) {
            // get grandparent to distinguish with parameter of a constructor of a global variable
            if (parent.getParent() instanceof UnitUnderTestNode) {
                ICommonFunctionNode sut = testCase.getFunctionNode();
                return sut == ((SubprogramNode) parent).getFunctionNode();
            }
        } else if (parent instanceof RootDataNode) {
            RootDataNode root = (RootDataNode) dataNode.getParent();
            NodeType level = root.getLevel();
            return level == NodeType.STATIC;
        }

        return false;
    }

    private static boolean isStubFunction(DataNode dataNode) {
        return dataNode instanceof SubprogramNode
                && ((SubprogramNode) dataNode).isStubable()
                && dataNode.getChildren().size() > 0;
    }

    private static boolean isGlobalVariable(ValueDataNode dataNode) {
        if (dataNode.getParent() instanceof RootDataNode) {
            RootDataNode root = (RootDataNode) dataNode.getParent();
            NodeType level = root.getLevel();
            return level == NodeType.GLOBAL;// || level == NodeType.STATIC;
        }

        return false;
    }

    public TreeTableView<DataNode> getTreeTableView() {
        return treeTableView;
    }

    private static boolean isVisible(DataNode node) {
        if (node == null)
            return false;

        if (node instanceof RootDataNode) {
            return !node.getChildren().isEmpty();
        }

        if (node instanceof ValueDataNode) {
            IDataNode parent = node.getParent();
            if (parent instanceof GlobalRootDataNode) {
                GlobalRootDataNode globalRoot = (GlobalRootDataNode) parent;
                if (globalRoot.isShowRelated()) {
                    IVariableNode v = ((ValueDataNode) node).getCorrespondingVar();
                    return globalRoot.isRelatedVariable(v);
                }
            }
            else if (parent instanceof UnionDataNode) {
                String field = ((UnionDataNode) parent).getSelectedField();
                return field == null || field.equals(node.getName());
            }
        }

        return true;
    }
}
