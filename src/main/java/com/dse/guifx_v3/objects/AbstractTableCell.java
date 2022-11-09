package com.dse.guifx_v3.objects;

import com.dse.boundary.PrimitiveBound;
import com.dse.environment.Environment;
import com.dse.guifx_v3.controllers.TestCaseTreeTableController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.*;
import com.dse.testcase_manager.IDataTestItem;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.testcase_manager.TestPrototype;
import com.dse.testdata.InputCellHandler;
import com.dse.testdata.Iterator;
import com.dse.testdata.object.*;
import com.dse.testdata.object.stl.ListBaseDataNode;
import com.dse.testdata.object.stl.STLArrayDataNode;
import com.dse.testdata.object.stl.SmartPointerDataNode;
import com.dse.user_code.controllers.ParameterUserCodeDialogController;
import com.dse.util.SpecialCharacter;
import com.dse.util.TemplateUtils;
import com.dse.util.VariableTypeUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractTableCell extends TreeTableCell<DataNode, String> {
    private final static AkaLogger logger = AkaLogger.get(AbstractTableCell.class);

    private final IDataTestItem testCase;
    protected TextField textField = null;
    protected InputCellHandler inputCellHandler;

    protected boolean escapePressed = false;
    protected TreeTablePosition<DataNode, ?> tablePos = null;

    public enum CellType {
        INPUT,
        EXPECTED,
    }

    /**
     * Add boundary tooltip to current cell
     */
    private void addBoundaryTooltip() {
        // Get datanode object
        TreeTableRow<DataNode> row = getTreeTableRow();

        if (row.getItem() instanceof NormalDataNode) {
            ValueDataNode dataNode = (ValueDataNode) row.getItem();

            String type = dataNode.getRealType();
            type = VariableTypeUtils.removeRedundantKeyword(type);

            // Get the boundaries
            PrimitiveBound bound = Environment.getBoundOfDataTypes().getBounds().get(type);
            String lbound = bound.getLower();
            String ubound = bound.getUpper();

            // Set the boundaries on the tooltip
            if (textField != null)
                textField.setTooltip(new Tooltip("[" + lbound + ".." + ubound + "]"));
        }
    }

    AbstractTableCell(IDataTestItem testCase) {
        this.testCase = testCase;
        inputCellHandler = new InputCellHandler();
        inputCellHandler.setTestCase(testCase);
    }

    public IDataTestItem getTestCase() {
        return testCase;
    }

    protected void showText(CellType type) {
        DataNode dataNode = getTreeTableRow().getTreeItem().getValue();

//        if (dataNode instanceof ValueDataNode && type == CellType.EXPECTED) {
//            String assertMethod = ((ValueDataNode) dataNode).getAssertMethod();
//            if (AssertMethod.ASSERT_FALSE.equals(assertMethod)
//                || AssertMethod.ASSERT_TRUE.equals(assertMethod)
//                || AssertMethod.ASSERT_NULL.equals(assertMethod)
//                || AssertMethod.ASSERT_NOT_EQUAL.equals(assertMethod)) {
//                UIController.showErrorDialog("Cant edit value with current assert method", "Error", "Unable to edit value");
//                return;
//            }
//        }

        if (dataNode != null) {
            logger.debug("The type of data node corresponding to the cell: " + dataNode.getClass());
            // if it is a return variable node then ignore
            boolean isReturnNode = dataNode instanceof ValueDataNode && ((ValueDataNode) dataNode).isExpected();

//            if (dataNode instanceof ValueDataNode) {
//                logger.debug("Support assert method:");
//                String[] supportedMethod = ((ValueDataNode) dataNode).getAllSupportedAssertMethod();
//                for (String s : supportedMethod) {
//                    logger.debug(s);
//                }
//            }

//            if ((isReturnNode && type == CellType.INPUT) || (!isReturnNode && type == CellType.EXPECTED)) {
            if ((isReturnNode && type == CellType.INPUT)) {
//                disable();
                return;
            }

            // Các node cần nhập vào text field
            if (dataNode instanceof NormalDataNode // number and character
                    || (dataNode instanceof OneDimensionDataNode && !((OneDimensionDataNode) dataNode).isFixedSize()) // array cua normal data
                    || (dataNode instanceof MultipleDimensionDataNode && !((MultipleDimensionDataNode) dataNode).isFixedSize())
                    || (dataNode instanceof ListBaseDataNode && !(dataNode instanceof STLArrayDataNode))
                    || dataNode instanceof PointerDataNode || dataNode instanceof NumberOfCallNode) {
                setText(null);
                setGraphic(textField);
                textField.setText(getValueForTextField(dataNode));
                textField.selectAll();
                textField.requestFocus();

                textField.addEventFilter(MouseEvent.MOUSE_ENTERED, event -> {
                    addBoundaryTooltip();
                });

            } else if (dataNode instanceof EnumDataNode // enum
                    || dataNode instanceof ClassDataNode // include SubClassDataNode
                    || dataNode instanceof UnionDataNode  // union
                    || dataNode instanceof TemplateSubprogramDataNode
                    || dataNode instanceof SmartPointerDataNode
                    || dataNode instanceof VoidPointerDataNode
                    || dataNode instanceof FunctionPointerDataNode
                    || (dataNode instanceof SubprogramNode && ((SubprogramNode) dataNode).isStubable())) {
                // Các node cần có combo-box
                setGraphic(createComboBox(dataNode));
                setText(null);

            } else if (dataNode instanceof UnresolvedDataNode && testCase instanceof TestCase) {
                UnresolvedDataNode unresolvedDataNode = (UnresolvedDataNode) dataNode;
                ParameterUserCodeDialogController controller = ParameterUserCodeDialogController.getInstance(unresolvedDataNode);
                if (controller != null && controller.getStage() != null) {
                    controller.setTestCase((TestCase) testCase);
                    controller.showAndWaitStage(UIController.getPrimaryStage());
                }

            } else {
                logger.debug("Do not support to enter data for " + dataNode.getClass());
            }
        } else {
            logger.debug("There is no matching between a cell and a data node");
        }
    }

    private String getValueForTextField(DataNode dataNode) {
        if (dataNode instanceof NormalDataNode) {
            return ((NormalDataNode) dataNode).getValue();

        } else if (dataNode instanceof OneDimensionDataNode && !((OneDimensionDataNode) dataNode).isFixedSize()) {
            return String.valueOf(((OneDimensionDataNode) dataNode).getSize());

        } else if (dataNode instanceof PointerDataNode) {
            // con trỏ coi như array
            PointerDataNode pointerNode = (PointerDataNode) dataNode;
            return String.valueOf(pointerNode.getAllocatedSize());

        } else if (dataNode instanceof MultipleDimensionDataNode) {
            // mảng 2 chiều của int, char
            MultipleDimensionDataNode arrayNode = (MultipleDimensionDataNode) dataNode;
//            if (arrayNode.isSetSize()) {
//                StringBuilder sizesInString = new StringBuilder();
//                int lastIdx = arrayNode.getSizes().length - 1;
//                for (int i = 0; i < lastIdx; i++)
//                    sizesInString.append(arrayNode.getSizes()[i]).append(" x ");
//                sizesInString.append(arrayNode.getSizes()[lastIdx]);
//                return sizesInString.toString();
//            }
            return String.valueOf(arrayNode.getSizeOfDimension(0));

        } else if (dataNode instanceof ListBaseDataNode && !(dataNode instanceof STLArrayDataNode)) {
            ListBaseDataNode vectorNode = (ListBaseDataNode) dataNode;
            if (vectorNode.isSetSize()) {
                return String.valueOf(vectorNode.getSize());
            }
        }

        return null;
    }

    protected void saveValueWhenUsersPressEnter() {
//            logger.debug("Set event when users click enter on the cell");
        if (textField == null) {
            textField = new TextField();
            textField.setOnKeyPressed(t -> {
                if (t.getCode() == KeyCode.ESCAPE) {
                    escapePressed = true;
                }
            });

            textField.setOnKeyReleased((KeyEvent t) -> {
                if (t.getCode() == KeyCode.ENTER) {
                    commitEdit(textField.getText());
                }
            });
        }
    }

    protected ComboBox<String> createComboBox(DataNode dataNode) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setEditable(false);
        ObservableList<String> options = FXCollections.observableArrayList();

        // enum oke
        if (dataNode instanceof EnumDataNode) {
            List<String> list = ((EnumDataNode) dataNode).getAllNameEnumItems();
            options = FXCollections.observableArrayList(list);
            if (!((EnumDataNode) dataNode).isSetValue()) {
                comboBox.setValue(SpecialCharacter.EMPTY);
            } else {
                comboBox.setValue(((EnumDataNode) dataNode).getValue());
            }
            comboBox.setEditable(true);
        }
        // union oke
        else if (dataNode instanceof UnionDataNode) {
            INode node = ((UnionDataNode) dataNode).getCorrespondingType();
            if (node instanceof UnionNode) {
                UnionNode unionNode = (UnionNode) node;
                List<Node> list = unionNode.getChildren();
                for (Node child : list) {
                    options.add(child.getName());
                }
            }

            comboBox.setValue("Select attribute");
            String field = ((UnionDataNode) dataNode).getSelectedField();
            if (field != null) {
                comboBox.setValue(field);
            }
        }
        // subclass (cũng là class) oke
        else if (dataNode instanceof SubClassDataNode) {
            try {
                List<ICommonFunctionNode> list = ((SubClassDataNode) dataNode).getConstructorsOnlyInCurrentClass();

                for (ICommonFunctionNode node : list) {
                    if (!options.contains(node.getName())) {
                        options.add(node.getName());
                    }
                }

                comboBox.setValue("Select constructor");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (dataNode instanceof ClassDataNode) {
            // class oke
            options.add(SpecialCharacter.EMPTY);

            List<INode> list = ((ClassDataNode) dataNode).getDerivedClass();
            for (INode node : list)
                options.add(node.getName());

            comboBox.setValue("Select real class");
        } else if (dataNode instanceof TemplateSubprogramDataNode) {
            ICommonFunctionNode functionNode = (ICommonFunctionNode) ((TemplateSubprogramDataNode) dataNode).getFunctionNode();

            List<DefinitionFunctionNode> suggestions = TemplateUtils.getPossibleTemplateArguments(functionNode);

            for (INode suggestion : suggestions)
                options.add(suggestion.toString());

            comboBox.setValue("Select template parameters");
        } else if (dataNode instanceof VoidPointerDataNode) {
            options.addAll(
                    OPTION_VOID_POINTER_PRIMITIVE_TYPES,
                    OPTION_VOID_POINTER_STRUCTURE_TYPES
            );
            String realType = ((VoidPointerDataNode) dataNode).getReferenceType();
            if (realType != null) {
                comboBox.setValue(realType);
            } else {
                comboBox.setValue("Unknown type");
            }
        } else if (dataNode instanceof SmartPointerDataNode) {
            SmartPointerDataNode smartPtrDataNode = (SmartPointerDataNode) dataNode;
            String[] constructors = smartPtrDataNode.getConstructorsWithTemplateArgument();

            options.addAll(constructors);
            comboBox.setValue("Choose constructor");
        } else if (dataNode instanceof FunctionPointerDataNode) {
            FunctionPointerDataNode cast = (FunctionPointerDataNode) dataNode;
            List<String> matches = cast.getPossibleFunctions().stream()
                    .map(INode::getName)
                    .collect(Collectors.toList());
            options.addAll(matches);
            options.add("NULL");
        }

        comboBox.setItems(options);
        // Chỉnh sửa cho combobox vừa với ô của tree table.
        comboBox.setMaxWidth(getTableColumn().getMaxWidth());
        // Khi chọn giá trị trong combobox thì commit giá trị đó.
        comboBox.valueProperty().addListener((ov, oldValue, newValue) -> commitEdit(newValue));
        return comboBox;
    }

    protected ContextMenu setupContextMenu(DataNode dataNode) {
        ContextMenu contextMenu = new ContextMenu();

        if (testCase instanceof TestPrototype)
            return contextMenu;

        if (dataNode instanceof ValueDataNode
                && ((ValueDataNode) dataNode).isSupportUserCode()) {
            MenuItem miUserCode = new MenuItem("User Code");
            miUserCode.setOnAction(event -> {
                ParameterUserCodeDialogController controller = ParameterUserCodeDialogController
                        .getInstance((ValueDataNode) dataNode);
                if (controller != null && controller.getStage() != null) {
                    controller.setTestCase((TestCase) testCase);
                    controller.showAndWaitStage(UIController.getPrimaryStage());
                    inputCellHandler.update(this, dataNode);
                    TestCaseManager.exportBasicTestCaseToFile((TestCase) testCase);

                    if (getTreeTableRow().getTreeItem() instanceof TestDataTreeItem) {
                        TestDataTreeItem treeItem = (TestDataTreeItem) getTreeTableRow().getTreeItem();
                        // reload các con của tree item
                        if (!(dataNode instanceof NormalCharacterDataNode || dataNode instanceof NormalNumberDataNode
                                || dataNode instanceof EnumDataNode))
                            TestCaseTreeTableController.loadChildren(testCase, treeItem);
                    }
                }
            });

            contextMenu.getItems().add(miUserCode);
        }
        return contextMenu;
    }

    @Override
    public void cancelEdit() {
        if (escapePressed) {
            // this is a cancel event after escape key
            super.cancelEdit();
            logger.debug("ESCAPE to canceled the edit on the cell");
        } else {
            // this is not a cancel event after escape key
            // we interpret it as commit.
            DataNode dataNode = getTreeTableRow().getItem();
            if (dataNode instanceof NormalDataNode // number and character
                    || (dataNode instanceof OneDimensionDataNode && !((OneDimensionDataNode) dataNode).isFixedSize()) // array cua normal data
                    || (dataNode instanceof MultipleDimensionDataNode && !((MultipleDimensionDataNode) dataNode).isFixedSize())
                    || (dataNode instanceof ListBaseDataNode && !(dataNode instanceof STLArrayDataNode))
                    || dataNode instanceof PointerDataNode) {
                String newText = textField.getText(); // get the new text from the view
                commitEdit(newText); // commit the new text to the model
            }
        }
        getTreeTableView().refresh();
    }

    @Override
    public void commitEdit(String newValue) {
//        if (!isEditing()) return;

        final TreeTableView<DataNode> treeTable = getTreeTableView();
        if (treeTable != null) {
            // Inform the TableView of the edit being ready to be committed.
            TreeTableColumn.CellEditEvent editEvent = new TreeTableColumn.CellEditEvent(
                    treeTable,
                    tablePos,
                    TreeTableColumn.editCommitEvent(),
                    newValue
            );

            Event.fireEvent(getTableColumn(), editEvent);

            super.cancelEdit(); // cannel before do commit to avoid doing commitEdit twice

            doCommitEdit(newValue);
        }
    }

    protected abstract void doCommitEdit(String newValue);

    protected void clearValue(DataNode dataNode) {
        if (dataNode instanceof NormalDataNode) {
            NormalDataNode normalNode = (NormalDataNode) dataNode;
            normalNode.setValue(null);
        } else if (dataNode instanceof EnumDataNode) {
            EnumDataNode enumNode = (EnumDataNode) dataNode;
            enumNode.setValue(null);
            enumNode.setValueIsSet(false);
        }
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
    }

    protected void disable() {
        setDisable(true);
        setStyle("-fx-text-fill: #808080");
    }

    protected static boolean isMultipleValue(String newValue) {
        return newValue.trim().matches("\\{.+\\}");
    }

    protected static String[] preprocessValue(String newValue) {
        int start = newValue.indexOf(SpecialCharacter.OPEN_BRACE) + 1;
        int end = newValue.lastIndexOf(SpecialCharacter.CLOSE_BRACE);
        String regex = ",(?![^()]*\\))(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)";
        String[] values = newValue.substring(start, end).split(regex, -1);
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
        }
        return values;
    }

    protected void onRetrieveValue(ValueDataNode valueDataNode, String newValue) throws Exception {
        if (isMultipleValue(newValue)) {
//            if (valueDataNode.isStubArgument()) {
//                ValueDataNode primaryNode = valueDataNode.getIterators().get(0).getDataNode();
//                primaryNode.getIterators().removeIf(i -> i.getDataNode() != primaryNode);
//                String[] values = preprocessValue(newValue);
//                for (int i = 0; i < values.length; i++) {
//                    if (i != 0) {
//                        Iterator iterator = new Iterator(primaryNode.clone());
//                        iterator.setStartIdx(i + 1);
//                        iterator.setRepeat(1);
//                        primaryNode.getIterators().add(iterator);
//                        inputCellHandler.commitEdit(iterator.getDataNode(), values[i]);
//                    } else {
//                        inputCellHandler.commitEdit(primaryNode, values[i]);
//                        primaryNode.getIterators().get(0).setRepeat(1);
//                    }
//                }
//
//                // jump to first iteration
//                TestDataTreeItem child = new TestDataTreeItem(primaryNode);
//                if (primaryNode.getName().equals("RETURN"))
//                    child.setColumnType(TestDataTreeItem.ColumnType.INPUT);
//                else
//                    child.setColumnType(TestDataTreeItem.ColumnType.EXPECTED);
//                TreeItem<DataNode> current = getTreeTableRow().getTreeItem();
//                TreeItem<DataNode> parent = current.getParent();
//
//                int index = parent.getChildren().indexOf(current);
//                parent.getChildren().remove(index);
//                parent.getChildren().add(index, child);
//
//                TestCaseTreeTableController.loadChildren(testCase, child);
//
//            } else {
//            String content = "Multiple values entering is only supported on stub parameters and return variable";
            String content = "Multiple values entering is not supported anymore";
            UIController.showErrorDialog(content, "Test Data Entering", "Invalid value");
//            }

        } else {
            inputCellHandler.commitEdit(valueDataNode, newValue);
            valueDataNode.getIterators().get(0).setRepeat(Iterator.FILL_ALL);
        }
    }

    public static final String OPTION_VOID_POINTER_PRIMITIVE_TYPES = "Primitive Types";
    public static final String OPTION_VOID_POINTER_STRUCTURE_TYPES = "Structure Types";
    public static final String OPTION_VOID_POINTER_USER_CODE = "User Code";
}