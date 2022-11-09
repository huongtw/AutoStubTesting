package com.dse.testdata;

import com.dse.boundary.PrimitiveBound;
import auto_testcase_generation.testdatagen.RandomInputGeneration;
import com.dse.config.AkaConfig;
import com.dse.config.IFunctionConfigBound;
import com.dse.guifx_v3.controllers.ChooseRealTypeController;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.AbstractTableCell;
import com.dse.parser.object.ICommonFunctionNode;
import com.dse.parser.object.INode;
import com.dse.parser.object.NumberOfCallNode;
import com.dse.project_init.ProjectClone;
import com.dse.testcase_manager.IDataTestItem;
import com.dse.testdata.gen.module.TreeExpander;
import com.dse.testdata.object.*;
import com.dse.testdata.object.stl.ListBaseDataNode;
import com.dse.testdata.object.stl.STLArrayDataNode;
import com.dse.testdata.object.stl.SmartPointerDataNode;
import com.dse.testdata.object.stl.StdFunctionDataNode;
import com.dse.logger.AkaLogger;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.dse.util.VariableTypeUtils;
import com.dse.boundary.DataSizeModel;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class InputCellHandler implements IInputCellHandler {

    public final static AkaLogger logger = AkaLogger.get(InputCellHandler.class);

    // store the template type to real type in template function, e.g, "T"->"int"
    // key: template type
    // value: real type
    private Map<String, String> realTypeMapping;
    private boolean inAutoGenMode = false;
    private IDataTestItem testCase;

    public InputCellHandler() {

    }

    @Override
    public void update(TreeTableCell<DataNode, String> cell, DataNode dataNode) {
        // first "if" to display "USER CODE" for parameters that use user code
        if (dataNode instanceof ValueDataNode && ((ValueDataNode) dataNode).isUseUserCode()) {
            cell.setEditable(true);
            cell.setText(((ValueDataNode) dataNode).getUserCodeDisplayText());

        } else if (dataNode instanceof NumberOfCallNode) {
            cell.setEditable(true);
            cell.setText(((NumberOfCallNode) dataNode).getValue());

        } else if (dataNode instanceof NormalNumberDataNode) {
            // int
            cell.setEditable(true);
            cell.setText(((NormalNumberDataNode) dataNode).getValue());

        } else if (dataNode instanceof NormalCharacterDataNode) {
            // char
            cell.setEditable(true);
            cell.setText(((NormalCharacterDataNode) dataNode).getValue());

        } else if (dataNode instanceof NormalStringDataNode) {
            // char
            cell.setEditable(true);
            cell.setText("<<Size: " + dataNode.getChildren().size() + ">>");

        } else if (dataNode instanceof EnumDataNode) {
            // enum
            cell.setText("Select value");
            if (((EnumDataNode) dataNode).isSetValue()) {
                cell.setText(((EnumDataNode) dataNode).getValue());
            }

        } else if (dataNode instanceof UnionDataNode) {
            // union
            cell.setText("Select attribute");
            String field = ((UnionDataNode) dataNode).getSelectedField();
            if (field != null) {
                cell.setText(field);
            }

        } else if (dataNode instanceof SubClassDataNode) {
            // subclass
            cell.setEditable(true);
            SubClassDataNode subClassDataNode = (SubClassDataNode) dataNode;
            cell.setText("Select constructor");
            if (subClassDataNode.getSelectedConstructor() != null) {
                // Show constructor class name
                cell.setText(subClassDataNode.getSelectedConstructor().getName());
            }

        } else if (dataNode instanceof ClassDataNode) {
            // class
            cell.setEditable(true);
            ClassDataNode classDataNode = (ClassDataNode) dataNode;
            cell.setText("Select real class");
            if (classDataNode.getSubClass() != null) {
                // Show class name
                cell.setText(classDataNode.getSubClass().getRawType());
            }

        } else if (dataNode instanceof OneDimensionDataNode) {
            // array
            OneDimensionDataNode arrayNode = (OneDimensionDataNode) dataNode;
            if (arrayNode.isFixedSize()) {
                cell.setText(toSize(((OneDimensionDataNode) dataNode).getSize() + ""));
            } else {
                cell.setEditable(true);
                cell.setText("<<Define size>>");
                if (arrayNode.isSetSize()) {
                    cell.setText(toSize(arrayNode.getSize() + ""));
                }
            }

        } else if (dataNode instanceof PointerDataNode) {
            // con trỏ coi như array
            cell.setEditable(true);
            PointerDataNode arrayNode = (PointerDataNode) dataNode;
            cell.setText("<<Define size>>");
            if (arrayNode.isSetSize()) {
                cell.setText(toSize(arrayNode.getAllocatedSize() + ""));
            }

        } else if (dataNode instanceof MultipleDimensionDataNode) {
            // mảng 2 chiều của int, char
            cell.setEditable(true);
            MultipleDimensionDataNode arrayNode = (MultipleDimensionDataNode) dataNode;
            cell.setText("<<Define size>>");
            if (arrayNode.isSetSize()) {
                if (arrayNode.getSizeOfDimension(0) > 0) {
                    StringBuilder sizesInString = new StringBuilder();
                    int lastIdx = arrayNode.getSizes().length - 1;
                    for (int i = 0; i < lastIdx; i++)
                        sizesInString.append(arrayNode.getSizes()[i]).append(" x ");
                    sizesInString.append(arrayNode.getSizes()[lastIdx]);
                    cell.setText(toSize(sizesInString + ""));
                } else {
                    cell.setText(toSize("0"));
                }
            }

        } else if (dataNode instanceof ListBaseDataNode) {
            ListBaseDataNode vectorNode = (ListBaseDataNode) dataNode;
            cell.setEditable(!(dataNode instanceof STLArrayDataNode));
            cell.setText("<<Define size>>");
            if (vectorNode.isSetSize()) {
                cell.setText(toSize(vectorNode.getSize() + ""));
            }

        } else if (dataNode instanceof TemplateSubprogramDataNode) {
            cell.setEditable(true);
            cell.setText("Change template");

        } else if (dataNode instanceof SmartPointerDataNode) {
            cell.setEditable(true);
            cell.setText("Choose constructor");

        } else if (dataNode instanceof FunctionPointerDataNode) {
            INode selected = ((FunctionPointerDataNode) dataNode).getSelectedFunction();
            if (selected != null) {
                cell.setText(selected.getName());
            } else {
                cell.setText("<<Choose reference>>");
            }
        } else if (dataNode instanceof VoidPointerDataNode) {
            cell.setEditable(false);
            // String shorten = ((VoidPointerDataNode) dataNode).getUserCode();
            // shorten = shorten.replace("\n", "↵");
            // cell.setText(shorten);
            String realType = ((VoidPointerDataNode) dataNode).getReferenceType();
            if (realType != null) {
                cell.setText(realType);
            } else {
                cell.setText("Choose real type");
            }

        } else if (dataNode instanceof OtherUnresolvedDataNode) {
            cell.setEditable(false);
            String shorten = ((OtherUnresolvedDataNode) dataNode).getUserCode().getContent();
            shorten = shorten.replace("\n", "↵");
            cell.setText(shorten);

        } else if (dataNode instanceof NullPointerDataNode) {
            cell.setText(NullPointerDataNode.NULL_PTR);
            cell.setGraphic(null);

        } else {
            cell.setText(null);
            cell.setGraphic(null);
        }
    }

    @Override
    public void commitEdit(ValueDataNode dataNode, String newValue) throws Exception {
        boolean is_realCommit = true;

        String type = dataNode.getRealType();
        type = VariableTypeUtils.removeRedundantKeyword(type);

        if (dataNode instanceof NormalNumberDataNode || dataNode instanceof NumberOfCallNode) {
            String normalizedValue = Utils.preprocessorLiteral(newValue);

            if (type.equals("unsigned long long int") || type.equals("unsigned long long")
                    || type.equals("uint64_t") || type.equals("uint_least64_t") || type.equals("uint_fast64_t")) {
                try {
                    // test convert to numberous value
                    Double.parseDouble(normalizedValue);
                    ((NormalNumberDataNode) dataNode).setValue(newValue);
                } catch (NumberFormatException e) {
                    if (isInAutoGenMode())
                        logger.error("Invalid value of " + type, e);
                    else
                        UIController.showErrorDialog("Invalid value of " + type, "Test data entering", "Invalid value");
                }
            } else if (VariableTypeUtils.isNumFloat(type)) {
                try {
                    double value = Double.parseDouble(normalizedValue);
                    PrimitiveBound bound = Environment.getBoundOfDataTypes().getBounds().get(type);
                    if (bound != null) {
                        if (value >= Double.parseDouble(bound.getLower())
                                && value <= Double.parseDouble(bound.getUpper())) {
                            ((NormalNumberDataNode) dataNode).setValue(newValue);
                        } else {
                            // nothing to do
                            if (isInAutoGenMode())
                                logger.error("Value " + value + " out of scope " + bound);
                            else
                                UIController.showErrorDialog("Value " + value + " out of scope " + bound,
                                        "Test data entering", "Invalid value");
                        }
                    }
                } catch (Exception e) {
                    if (isInAutoGenMode())
                        logger.error("Invalid value of " + type, e);
                    else
                        UIController.showErrorDialog("Invalid value of " + type, "Test data entering", "Invalid value");
                    logger.error("Do not handle when committing " + dataNode.getClass());
                }
            } else if (VariableTypeUtils.isTimet(type) || VariableTypeUtils.isSizet(type)) {
                try {
                    // test convert to numberous value
                    Double.parseDouble(normalizedValue);
                    ((NormalNumberDataNode) dataNode).setValue(newValue);
                } catch (NumberFormatException e) {
                    if (isInAutoGenMode())
                        logger.error("Invalid value of " + type, e);
                    else
                        UIController.showErrorDialog("Invalid value of " + type, "Test data entering", "Invalid value");
                }
            } else if (VariableTypeUtils.isBoolBasic(type)) {
                if (newValue.equalsIgnoreCase("true") || newValue.equalsIgnoreCase("false"))
                    ((NormalNumberDataNode) dataNode).setValue(newValue.toLowerCase());
                else {
                    try {
                        long value = Long.parseLong(normalizedValue);
                        PrimitiveBound bound = Environment.getBoundOfDataTypes().getBounds().get(type);
                        if (value >= Long.parseLong(bound.getLower())
                                && value <= Long.parseLong(bound.getUpper())) {
                            if (value != 0)
                                ((NormalNumberDataNode) dataNode).setValue("true");
                            else
                                ((NormalNumberDataNode) dataNode).setValue("false");
                        } else {
                            // nothing to do
                            if (isInAutoGenMode())
                                logger.error("Value " + value + " out of scope " + bound);
                            else
                                UIController.showErrorDialog("Value " + value + " out of scope " + bound,
                                        "Test data entering", "Invalid value");
                        }
                    } catch (Exception e) {
                        if (isInAutoGenMode())
                            logger.error("Invalid value of " + type, e);
                        else
                            UIController.showErrorDialog("Invalid value of " + type, "Test data entering",
                                    "Invalid value");
                    }
                }
            } else {
                try {
                    long value = Long.parseLong(normalizedValue);
                    PrimitiveBound bound = Environment.getBoundOfDataTypes().getBounds().get(type);
                    if (bound != null) {
                        if (value >= Long.parseLong(bound.getLower())
                                && value <= Long.parseLong(bound.getUpper())) {
                            if (dataNode instanceof NumberOfCallNode) {
                                ((NumberOfCallNode) dataNode).setValue(newValue);
                                TreeExpander expander = new TreeExpander();
                                expander.setRealTypeMapping(this.realTypeMapping);
                                expander.expandTree(dataNode);
                            } else {
                                ((NormalNumberDataNode) dataNode).setValue(newValue);
                            }
                        } else {
                            // nothing to do
                            if (isInAutoGenMode())
                                logger.error("Value " + value + " out of scope " + bound);
                            else
                                UIController.showErrorDialog("Value " + value + " out of scope " + bound,
                                        "Test data entering", "Invalid value");
                        }
                    }
                } catch (Exception e) {
                    if (isInAutoGenMode())
                        logger.error(
                                "Wrong input for " + dataNode.getName() + "; unit = " + dataNode.getUnit().getName(),
                                e);
                    else
                        UIController.showErrorDialog(
                                "Wrong input for " + dataNode.getName() + "; unit = " + dataNode.getUnit().getName(),
                                "Test data entering", "Invalid value");
                }
            }
        } else if (dataNode instanceof NormalCharacterDataNode) {
            // CASE: Type character
            if (newValue.startsWith(NormalCharacterDataNode.VISIBLE_CHARACTER_PREFIX)
                    && newValue.endsWith(NormalCharacterDataNode.VISIBLE_CHARACTER_PREFIX)) {
                String character = newValue.substring(1, newValue.length() - 1);
                String ascii = NormalCharacterDataNode.getCharacterToACSIIMapping().get(character);
                if (ascii != null)
                    ((NormalCharacterDataNode) dataNode).setValue(ascii);
                else {
                    if (!isInAutoGenMode()) {
                        UIController.showErrorDialog("You type wrong character for the type " + dataNode.getRawType()
                                + " in src " + dataNode.getUnit().getName() +
                                NormalCharacterDataNode.RULE, "Wrong input of character", "Fail");
                    }
                    logger.error("Do not handle when the length of text > 1 for character parameter");
                }
            } else {
                try {
                    // CASE: Type ascii
                    String normalizedValue = Utils.preprocessorLiteral(newValue);
                    long value = Long.parseLong(normalizedValue);
                    DataSizeModel dataSizeModel = Environment.getBoundOfDataTypes().getBounds();
                    PrimitiveBound bound = dataSizeModel.get(type);
                    if (bound == null)
                        bound = dataSizeModel.get(type.replace("std::", "").trim());
                    if (bound == null) {
                        if (isInAutoGenMode())
                            logger.error("You type wrong character for the type " + dataNode.getRawType()
                                    + " in src " + dataNode.getUnit().getName() +
                                    NormalCharacterDataNode.RULE);
                        else
                            UIController
                                    .showErrorDialog("You type wrong character for the type " + dataNode.getRawType()
                                            + " in src " + dataNode.getUnit().getName() +
                                            NormalCharacterDataNode.RULE, "Wrong input of character", "Fail");

                    } else if (value <= bound.getUpperAsDouble() && value >= bound.getLowerAsDouble()) {
                        ((NormalCharacterDataNode) dataNode).setValue(newValue);

                    } else {
                        if (isInAutoGenMode())
                            logger.error("Value " + newValue + " is out of bound " + dataNode.getRawType()
                                    + "[" + bound.getLowerAsDouble() + "," + bound.getUpperAsDouble() + "]"
                                    + " in src " + dataNode.getUnit().getName() +
                                    NormalCharacterDataNode.RULE);
                        else
                            UIController
                                    .showErrorDialog("Value " + newValue + " is out of bound " + dataNode.getRawType()
                                            + "[" + bound.getLowerAsDouble() + "," + bound.getUpperAsDouble() + "]"
                                            + " in src " + dataNode.getUnit().getName() +
                                            NormalCharacterDataNode.RULE, "Wrong input of character", "Fail");
                    }
                } catch (Exception e) {
                    if (!isInAutoGenMode()) {
                        UIController.showErrorDialog("You type wrong character for the type " + dataNode.getRawType()
                                + " in src " + dataNode.getUnit().getName() +
                                NormalCharacterDataNode.RULE, "Wrong input of character", "Fail");
                    }
                    logger.error("Do not handle when the length of text > 1 for character parameter");
                }
            }
        } else if (dataNode instanceof NormalStringDataNode) {
            try {
                long lengthOfString = Long.parseLong(newValue);
                if (lengthOfString < 0) {
                    throw new Exception("Negative length of string");
                }
                ((NormalStringDataNode) dataNode).setAllocatedSize(lengthOfString);
                TreeExpander expander = new TreeExpander();
                expander.setRealTypeMapping(this.realTypeMapping);
                expander.expandTree(dataNode);
            } catch (Exception e) {
                if (!(newValue.startsWith(SpecialCharacter.DOUBLE_QUOTES)
                        && newValue.endsWith(SpecialCharacter.DOUBLE_QUOTES))) {
                    ((NormalStringDataNode) dataNode)
                            .setValue(SpecialCharacter.DOUBLE_QUOTES + newValue + SpecialCharacter.DOUBLE_QUOTES);
                } else {
                    ((NormalStringDataNode) dataNode).setValue(newValue);
                }
                if (!isInAutoGenMode()) {
                    UIController.showErrorDialog("Length of a string must be >=0 and is an integer",
                            "Wrong length of string", "Invalid length");
                } else {
                    TreeExpander expander = new TreeExpander();
                    expander.setRealTypeMapping(this.realTypeMapping);
                    expander.expandTree(dataNode);
                }
            }
        } else if (dataNode instanceof EnumDataNode) {
            // enum oke
            ((EnumDataNode) dataNode).setValue(newValue);
            ((EnumDataNode) dataNode).setValueIsSet(true);

        } else if (dataNode instanceof UnionDataNode) {
            // union oke
            // expand tree với thuộc tính được chọn ở combobox
            ((UnionDataNode) dataNode).setField(newValue);
            new TreeExpander().expandStructureNodeOnDataTree(dataNode, newValue);

        } else if (dataNode instanceof StructDataNode) {
            // do nothing

        } else if (dataNode instanceof SubClassDataNode) {
            // subclass (cũng là class) oke
            ((SubClassDataNode) dataNode).chooseConstructor(newValue);
            new TreeExpander().expandTree(dataNode);

        } else if (dataNode instanceof ClassDataNode) {
            // class oke
            ((ClassDataNode) dataNode).setSubClass(newValue);

        } else if (dataNode instanceof OneDimensionDataNode) {
            // array cua normal data. oke
            int size = Integer.parseInt(newValue);
            OneDimensionDataNode currentNode = (OneDimensionDataNode) dataNode;
            if (!currentNode.isFixedSize()) {
                currentNode.setSize(size);
                currentNode.setSizeIsSet(true);

                TreeExpander expander = new TreeExpander();
                expander.setRealTypeMapping(this.realTypeMapping);
                expander.expandTree(dataNode);
            }

        } else if (dataNode instanceof PointerDataNode) {
            // con trỏ coi như array
            int size = Integer.parseInt(newValue);
            PointerDataNode currentNode = (PointerDataNode) dataNode;
            currentNode.setAllocatedSize(size);
            currentNode.setSizeIsSet(true);

            TreeExpander expander = new TreeExpander();
            expander.setRealTypeMapping(this.realTypeMapping);
            expander.expandTree(dataNode);

        } else if (dataNode instanceof MultipleDimensionDataNode) {
            int sizeA = Integer.parseInt(newValue);
            MultipleDimensionDataNode currentNode = (MultipleDimensionDataNode) dataNode;
            if (!currentNode.isFixedSize()) {
                currentNode.setSizeOfDimension(0, sizeA);
                currentNode.setSizeIsSet(true);

                TreeExpander expander = new TreeExpander();
                expander.expandTree(dataNode);
            }

        } else if (dataNode instanceof TemplateSubprogramDataNode) {
            dataNode.getChildren().clear();
            ((TemplateSubprogramDataNode) dataNode).setRealFunctionNode(newValue);
            // ((TemplateDataNode) dataNode).generateArgumentsAndReturnVariable();

        } else if (dataNode instanceof SmartPointerDataNode) {
            dataNode.getChildren().clear();
            ((SmartPointerDataNode) dataNode).chooseConstructor(newValue);
            new TreeExpander().expandTree(dataNode);

        } else if (dataNode instanceof ListBaseDataNode && !(dataNode instanceof STLArrayDataNode)) {
            // array cua normal data. oke
            int size = Integer.parseInt(newValue);
            ListBaseDataNode currentNode = (ListBaseDataNode) dataNode;
            currentNode.setSize(size);
            currentNode.setSizeIsSet(true);

            TreeExpander expander = new TreeExpander();
            expander.expandTree(dataNode);

        } else if (dataNode instanceof FunctionPointerDataNode) {
            FunctionPointerDataNode fpDataNode = (FunctionPointerDataNode) dataNode;
            if (newValue.equals("NULL")) {
                fpDataNode.setSelectedFunction(null);
            } else {
                fpDataNode.getPossibleFunctions()
                        .stream()
                        .filter(f -> f.getName().equals(newValue))
                        .findFirst()
                        .ifPresent(f -> {
                            if (f instanceof ICommonFunctionNode) {
                                commitSelectedReference(fpDataNode, (ICommonFunctionNode) f);
                            }
                        });
            }
        } else if (dataNode instanceof OtherUnresolvedDataNode) {
            logger.debug("OtherUnresolvedDataNode");
            // ((OtherUnresolvedDataNode) dataNode).setUserCode(newValue);

        } else if (dataNode instanceof VoidPointerDataNode) {
            commitVoidPtrInputMethod(dataNode, newValue);
            if (((VoidPointerDataNode) dataNode).getReferenceType() == null) {
                is_realCommit = false;
            }

        } else if (dataNode instanceof StdFunctionDataNode) {
            if (newValue.isEmpty()) {
                ((StdFunctionDataNode) dataNode).setBody(newValue);
            } else {
                ((StdFunctionDataNode) dataNode).setBody(String.format("return %s;", newValue));
            }

        } else
            logger.error("Do not support to enter data for " + dataNode.getClass());

        if (is_realCommit) {
            dataNode.setUseUserCode(false);
        }
    }

    private void commitVoidPtrInputMethod(DataNode dataNode, String inputMethod) {
        if (isInAutoGenMode()) {
            ChooseRealTypeController controller = new ChooseRealTypeController();

            final String DELIMITER_BETWEEN_ATTRIBUTE = ",";
            final String DELIMITER_BETWEEN_KEY_AND_VALUE = "=";
            String[] elements = inputMethod.split(DELIMITER_BETWEEN_ATTRIBUTE);
            Map<String, String> elementMap = new HashMap<>();
            for (String element : elements)
                elementMap.put(
                        element.split(DELIMITER_BETWEEN_KEY_AND_VALUE)[0],
                        element.split(DELIMITER_BETWEEN_KEY_AND_VALUE)[1]);

            String category = elementMap.get(RandomInputGeneration.VOID_POINTER____SELECTED_CATEGORY);
            controller.setTestCase(testCase);
            controller.loadContent(category);

            if (category.equals(AbstractTableCell.OPTION_VOID_POINTER_PRIMITIVE_TYPES)
                    || category.equals(AbstractTableCell.OPTION_VOID_POINTER_STRUCTURE_TYPES)) {
                String coreType = elementMap.get(RandomInputGeneration.VOID_POINTER____SELECTED_CORE_TYPE);
                ObservableList<INode> possibleNodes = controller.getLvRealTypes().getItems();

                int level = Integer.parseInt(elementMap.get(RandomInputGeneration.VOID_POINTER____POINTER_LEVEL));
                for (INode possibleNode : possibleNodes)
                    if (possibleNode.getName().equals(coreType)) {
                        controller.chooseANode(possibleNode, testCase, (ValueDataNode) dataNode, level);
                        break;
                    }
            }
        } else {
            if (inputMethod.equals(AbstractTableCell.OPTION_VOID_POINTER_PRIMITIVE_TYPES)
                    || inputMethod.equals(AbstractTableCell.OPTION_VOID_POINTER_STRUCTURE_TYPES)) {
                ChooseRealTypeController controller = ChooseRealTypeController.getInstance(dataNode);
                if (controller != null && controller.getStage() != null) {
                    controller.setTestCase(testCase);
                    controller.loadContent(inputMethod);
                    Stage window = controller.getStage();
                    window.setResizable(false);
                    window.initModality(Modality.WINDOW_MODAL);
                    window.initOwner(UIController.getPrimaryStage());
                    window.showAndWait();
                }
            }
        }
    }

    public void commitSelectedReference(FunctionPointerDataNode fpDataNode, ICommonFunctionNode f) {
        fpDataNode.setSelectedFunction(f);
        if (testCase != null) {
            // TODO: relative
            String filePath = Utils.getSourcecodeFile(f).getAbsolutePath();
            String cloneFilePath = ProjectClone.getClonedFilePath(filePath);
            if (!new File(cloneFilePath).exists())
                cloneFilePath = filePath;
            testCase.putOrUpdateDataNodeIncludes(fpDataNode, cloneFilePath);
        }
    }

    public void setTestCase(IDataTestItem testCase) {
        this.testCase = testCase;
    }

    public IDataTestItem getTestCase() {
        return testCase;
    }

    public void setRealTypeMapping(Map<String, String> realTypeMapping) {
        this.realTypeMapping = realTypeMapping;
    }

    private String toSize(String size) {
        if (size.equals("0") || size.equals("-1"))
            return "<<Size: NULL>>";
        else
            return "<<Size: " + size + ">>";
    }

    public void setInAutoGenMode(boolean inAutoGenMode) {
        this.inAutoGenMode = inAutoGenMode;
    }

    public boolean isInAutoGenMode() {
        return inAutoGenMode;
    }
}
