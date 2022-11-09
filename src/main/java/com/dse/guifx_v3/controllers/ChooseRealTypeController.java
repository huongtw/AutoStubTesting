package com.dse.guifx_v3.controllers;

import com.dse.guifx_v3.objects.AbstractTableCell;
import com.dse.logger.AkaLogger;
import com.dse.parser.object.*;
import com.dse.project_init.ProjectClone;
import com.dse.testcase_manager.IDataTestItem;
import com.dse.testdata.gen.module.TreeExpander;
import com.dse.testdata.gen.module.type.PointerTypeInitiation;
import com.dse.testdata.object.DataNode;
import com.dse.testdata.object.IDataNode;
import com.dse.testdata.object.ValueDataNode;
import com.dse.testdata.object.VoidPointerDataNode;
import com.dse.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class ChooseRealTypeController implements Initializable {
    private final static AkaLogger logger = AkaLogger.get(ChooseRealTypeController.class);

    @FXML
    private TextField tfSearch;
    @FXML
    private TextArea taSignature;
    @FXML
    private ListView<INode> lvRealTypes = new ListView<>();
//    @FXML
//    private TextField tfSizeOfArray;
    @FXML
    private TextField tfLevelOfPointer;
    //    @FXML
//    private CheckBox chbArray;
//    @FXML
//    private CheckBox chbPointer;
    @FXML
    private SplitPane spTypesAndContent;

    private Stage stage;
    private IDataNode dataNode;
    private IDataTestItem testCase;
    private final ObservableList<INode> typesList = FXCollections.observableArrayList();

    public void initialize(URL location, ResourceBundle resources) {
//        disableAll();
        FilteredList<INode> filteredList = new FilteredList<>(typesList, e -> true);

        tfLevelOfPointer.setText("1");
        tfLevelOfPointer.setOnKeyReleased(e -> validateTFLevelOfPointer());
//        tfSizeOfArray.setO(e -> validateTFSizeOfArray());

        tfSearch.setOnKeyReleased(e -> {
            tfSearch.textProperty().addListener(((observable, oldValue, newValue) -> {
                filteredList.setPredicate((Predicate<? super INode>) typeNode -> {
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    return typeNode.getName().contains(newValue);
                });
                lvRealTypes.setItems(filteredList);
            }));

            lvRealTypes.refresh();
        });
        lvRealTypes.setCellFactory(new Callback<ListView<INode>, ListCell<INode>>() {
            @Override
            public ListCell<INode> call(ListView<INode> param) {
                return new ListCell<INode>() {
                    @Override
                    protected void updateItem(INode item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            setText(item.getName());
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });

        lvRealTypes.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> showSignature(newValue));
    }

    public static ChooseRealTypeController getInstance(IDataNode dataNode) {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/manual_input/ChooseRealType.fxml"));
        try {
            Parent parent = loader.load();
            ChooseRealTypeController controller = loader.getController();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Choose real type for void pointer");

            controller.setStage(stage);
            controller.setDataNode(dataNode);
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadContent(String inputMethod) {
        if (inputMethod.equals(AbstractTableCell.OPTION_VOID_POINTER_PRIMITIVE_TYPES)) {
            lvRealTypes.getItems().addAll(VariableTypeUtils.getAllPrimitiveTypeNodes());
            // remove text area signature
            if (spTypesAndContent != null)
                spTypesAndContent.getItems().remove(1);
        } else if (inputMethod.equals(AbstractTableCell.OPTION_VOID_POINTER_STRUCTURE_TYPES)) {
            lvRealTypes.getItems().addAll(VariableTypeUtils.getAllStructureNodes());
        }
        typesList.addAll(lvRealTypes.getItems());

        if (!lvRealTypes.getItems().isEmpty()) {
            lvRealTypes.getSelectionModel().select(0);
            showSignature(lvRealTypes.getItems().get(0));
        }
    }

    private void showSignature(INode typeNode) {
        if (typeNode != null) {
            if (typeNode instanceof StructureNode) {
                if (taSignature != null) {
                    taSignature.clear();
                    taSignature.appendText("Absolute path:\n" +
                            typeNode.getAbsolutePath());
                    taSignature.appendText("\n--------------------------");
                    taSignature.appendText("\n\n");
                    taSignature.appendText(((StructureNode) typeNode).getAST().getRawSignature());
                }
            } else {
                if (taSignature != null)
                    taSignature.setText(typeNode.getAbsolutePath());
            }
        }
    }

    @FXML
    void cancel() {
        if (stage != null) {
            stage.close();
        }
    }

//    private int[] getSizes() throws Exception {
//        if (!chbArray.isSelected()) {
//            return new int[0];
//        } else {
//            String input = tfSizeOfArray.getText();
//            String[] sizes = input.trim().split(",\\s*");
//            int[] intSizes = new int[sizes.length];
//            for (int i = 0; i < sizes.length; i++)
//                intSizes[i] = Integer.parseInt(sizes[i]);
//            return intSizes;
//        }
//    }

    private int getLevel() throws Exception {
//        if (!chbPointer.isSelected()) {
//            return 0;
//        } else {
//            return Integer.parseInt(tfLevelOfPointer.getText());
//        }
        int level = Integer.parseInt(tfLevelOfPointer.getText());
        if (level >= 1) {
            return level;
        } else {
            logger.debug("level = " + level + " < 1, invalid!");
            throw new Exception("level = " + level + " < 1, invalid!");
        }
    }

//    @FXML
//    void choose() {
//        if (!validate()) return;
//
//        INode typeNode = lvRealTypes.getSelectionModel().getSelectedItem();
//        String coreType = typeNode.getName();
//
//        if (typeNode instanceof StructureNode) {
//            String additionalHeaders = testCase.getAdditionalHeaders();
//            //TO DO: relative
//            String filePath = Utils.getSourcecodeFile(typeNode).getAbsolutePath();
//            String includeStm = String.format("#include \"%s\"", filePath);
//
//            if (additionalHeaders == null)
//                additionalHeaders = includeStm;
//            else if (!additionalHeaders.contains(includeStm))
//                additionalHeaders += SpecialCharacter.LINE_BREAK + includeStm;
//
//            testCase.setAdditionalHeaders(additionalHeaders);
//
//            if (typeNode instanceof StructNode)
//                coreType = "struct " + coreType;
//            else if (typeNode instanceof EnumNode)
//                coreType = "enum " + coreType;
//            else if (typeNode instanceof UnionNode)
//                coreType = "union " + coreType;
//        }
//
//        // backup children of data node
//        List<IDataNode> oldChildren = new ArrayList<>(dataNode.getChildren());
//        try {
//            // build Type
//            int level = getLevel();
////            int[] sizes = getSizes();
//            String referType = buildType(coreType, level);
//
//            // Generate variable node
//            VariableNode v = parseStmToGetCorrespondingVar(coreType, level);
//            v.setCorrespondingNode(typeNode);
//
//            // generate child data node
//            ValueDataNode child = null;
//            VoidPointerDataNode parent = (VoidPointerDataNode) dataNode;
//            dataNode.getChildren().clear();
//            if (chbArray.isSelected()) { // array
//                if (sizes.length == 1) {
//                    child = new OneDimensionTypeInitiation(v, parent).execute();
//                } else if (sizes.length > 1) {
//                    child = new MultipleDimensionTypeInitiation(v, parent).execute();
//                }
//
//            } else if (chbPointer.isSelected()) { // only pointer
//                child = new PointerTypeInitiation(v, parent).execute();
//
//            } else { // core type
//                if (VariableTypeUtils.isBasic(coreType)) {
//                    child = new BasicTypeInitiation(v, parent).execute();
//                } else {
//                    child = new StructureTypeInitiation(v, parent).execute();
//                }
//
//            }
//            if (child != null) {
//                child.setName(NAME_REFERENCE);
//                parent.setReferenceType(referType);
//                parent.setInputMethod(VoidPointerDataNode.InputMethod.AVAILABLE_TYPES);
//                new TreeExpander().expandTree(child);
//            } else {
//                throw new Exception("Not supported type: " + referType);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            dataNode.setChildren(oldChildren);
//        }
//
//        stage.close();
//    }

//    @FXML
//    void choose() {
//        if (!validate()) return;
//
//        INode typeNode = lvRealTypes.getSelectionModel().getSelectedItem();
//        try {
//            chooseANode(typeNode, testCase, (ValueDataNode) dataNode, getLevel());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        stage.close();
//    }

    private String getCoreType(INode typeNode) {
        String coreType = typeNode.getName();

        if (typeNode instanceof StructNode && !(typeNode instanceof StructTypedefNode))
            coreType = "struct " + coreType;
        else if (typeNode instanceof EnumNode && !(typeNode instanceof EnumTypedefNode))
            coreType = "enum " + coreType;
        else if (typeNode instanceof UnionNode && !(typeNode instanceof UnionTypedefNode))
            coreType = "union " + coreType;

        return coreType;
    }

    public void chooseANode(INode typeNode, IDataTestItem testCase, ValueDataNode dataNode, int level) {
        String coreType = getCoreType(typeNode);

        if (typeNode instanceof StructureNode) {
            //TODO: relative
            String filePath = Utils.getSourcecodeFile(typeNode).getAbsolutePath();
            String cloneFilePath = ProjectClone.getClonedFilePath(filePath);
            if (!new File(cloneFilePath).exists())
                cloneFilePath = filePath;
//            String includeStm = String.format("#include \"%s\"", cloneFilePath);
            if (testCase != null) {
//                testCase.appendAdditionHeader(includeStm);
                testCase.putOrUpdateDataNodeIncludes(dataNode, cloneFilePath);
            }
        }

        // backup children of data node
        List<IDataNode> oldChildren = new ArrayList<>(dataNode.getChildren());
        try {
            // build Type
            String referType = buildType(coreType, level);

            // Generate variable node
            VariableNode v = parseStmToGetCorrespondingVar(coreType, level, dataNode);
            v.setCorrespondingNode(typeNode);

            // generate child data node
            VoidPointerDataNode parent = (VoidPointerDataNode) dataNode;
            dataNode.getChildren().clear();

            ValueDataNode child = new PointerTypeInitiation(v, parent).execute();

                if (child != null) {
                    child.setName(NAME_REFERENCE);
                    parent.setReferenceType(referType);
                    parent.setInputMethod(VoidPointerDataNode.InputMethod.AVAILABLE_TYPES);
                    parent.setUserCode(null);
                new TreeExpander().expandTree(child);
            } else {
                throw new Exception("Not supported type: " + referType);
            }

        } catch (Exception e) {
            e.printStackTrace();
            dataNode.setChildren(oldChildren);
        }
    }

    @FXML
    void choose() {
        if (!validate()) return;

        INode typeNode = lvRealTypes.getSelectionModel().getSelectedItem();
        try {
            int level = getLevel();
            if (dataNode instanceof ValueDataNode)
                chooseANode(typeNode, testCase, (ValueDataNode) dataNode, level);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stage.close();
    }

    private boolean validate() {
//        return validateTFLevelOfPointer() && validateTFSizeOfArray();
        return validateTFLevelOfPointer();
    }

    private boolean validateTFLevelOfPointer() {
        try {
            getLevel();
            tfLevelOfPointer.setStyle("");
            return true;
        } catch (Exception e) {
            highlightInvalidTextField(tfLevelOfPointer);
            return false;
        }
    }

//    private boolean validateTFSizeOfArray() {
//        try {
//            getSizes();
//            tfSizeOfArray.setStyle("");
//            return true;
//        } catch (Exception e) {
//            highlightInvalidTextField(tfSizeOfArray);
//            return false;
//        }
//    }

    private void highlightInvalidTextField(TextField textField) {
        textField.setStyle("-fx-border-color: red; -fx-border-width: 1");
    }

//    private String buildType(String coreType, int level, int[] sizes) {
//        StringBuilder typeBuilder = new StringBuilder();
//        typeBuilder.append(coreType);
//        for (int i = 0; i < level; i++) {
//            typeBuilder.append("*");
//        }
//        for (int i = 0; i < sizes.length; i++) {
//            typeBuilder.append("[").append(sizes[i]);
//            typeBuilder.append("]");
//        }
//        return typeBuilder.toString();
//    }

    private String buildType(String coreType, int level) {
        StringBuilder typeBuilder = new StringBuilder();
        typeBuilder.append(coreType);
        for (int i = 0; i < level; i++) {
            typeBuilder.append("*");
        }
        return typeBuilder.toString();
    }

    //    private VariableNode parseStmToGetCorrespondingVar(String coreType, int level, int[] sizes) {
//        VariableNode v = new VariableNode();
//
//        StringBuilder stmBuilder = new StringBuilder();
//        stmBuilder.append(coreType);
//        for (int i = 0; i < level; i++) {
//            stmBuilder.append("*");
//        }
//        stmBuilder.append(" ").append(dataNode.getName());
//        for (int i = 0; i < sizes.length; i++) {
//            stmBuilder.append("[").append(sizes[i]);
//            stmBuilder.append("]");
//        }
//        String stm = stmBuilder.toString();
//        IASTNode ast = Utils.convertToIAST(stm);
//        if (ast instanceof IASTDeclarationStatement) {
//            IASTDeclaration declaration = ((IASTDeclarationStatement) ast).getDeclaration();
//            if (declaration instanceof IASTSimpleDeclaration) {
//                v.setAST(declaration);
//                VariableNode parentVar = ((VoidPointerDataNode) dataNode).getCorrespondingVar();
//                v.setParent(parentVar);
//                v.setAbsolutePath(parentVar.getAbsolutePath() + File.separator + v.getName());
//            } else {
//                logger.error("The declaration is not an IASTSimpleDeclaration");
//            }
//        } else {
//            logger.error("The ast is not an IASTDeclarationStatement");
//        }
//        return v;
//    }
    private VariableNode parseStmToGetCorrespondingVar(String coreType, int level, DataNode dataNode) {
        VariableNode v = new VariableNode();

        StringBuilder stmBuilder = new StringBuilder();
        stmBuilder.append(coreType);
        for (int i = 0; i < level; i++) {
            stmBuilder.append("*");
        }
        if (dataNode != null && dataNode.getName() != null)
            stmBuilder.append(" ").append(NAME_REFERENCE);
        else
            stmBuilder.append(" ").append("tmp");
//        for (int i = 0; i < sizes.length; i++) {
//            stmBuilder.append("[").append(sizes[i]);
//            stmBuilder.append("]");
//        }
        String stm = stmBuilder.toString();
        IASTNode ast = Utils.convertToIAST(stm);
        if (ast instanceof IASTDeclarationStatement) {
            IASTDeclaration declaration = ((IASTDeclarationStatement) ast).getDeclaration();
            if (declaration instanceof IASTSimpleDeclaration) {
                v.setAST(declaration);
                VariableNode parentVar = ((VoidPointerDataNode) dataNode).getCorrespondingVar();
                v.setParent(parentVar);
                v.setAbsolutePath(parentVar.getAbsolutePath() + File.separator + v.getName());
            } else {
                logger.error("The declaration is not an IASTSimpleDeclaration");
            }
        } else {
            logger.error("The ast is not an IASTDeclarationStatement");
        }
        return v;
    }

//    private void disableAll() {
//        tfSizeOfArray.setDisable(true);
//        tfLevelOfPointer.setDisable(true);
//    }

    @FXML
    public void chooseArray() {
//        tfSizeOfArray.setDisable(!chbArray.isSelected());
    }

    @FXML
    public void choosePointer() {
//        tfLevelOfPointer.setDisable(!chbPointer.isSelected());
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setDataNode(IDataNode dataNode) {
        this.dataNode = dataNode;
    }

    public IDataNode getDataNode() {
        return dataNode;
    }

    public void setTestCase(IDataTestItem testCase) {
        this.testCase = testCase;
    }

    public IDataTestItem getTestCase() {
        return testCase;
    }

    public ListView<INode> getLvRealTypes() {
        return lvRealTypes;
    }

    public final static String NAME_REFERENCE = "value";
}
