package com.dse.user_code.controllers;

import com.dse.user_code.UserCodeManager;
import com.dse.user_code.objects.AbstractUserCode;
import com.dse.user_code.objects.ParameterUserCode;
import com.dse.user_code.objects.TestCaseUserCode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AddEditUserCodeController implements Initializable {

    @FXML
    private ComboBox<String> cbbType;
    @FXML
    private SplitPane splitPane;
    @FXML
    private Tab tabUserCode;
    @FXML
    private Tab tabUserCodes;
    @FXML
    private Tab tabSetUp;
    @FXML
    private Tab tabTearDown;
    @FXML
    private TabPane tpContent;
    @FXML
    private ListView<String> lvIncludeFiles;
    @FXML
    private TextField tfName;

    private Stage stage;
    private AbstractUserCode userCode;
    private CodeArea compileErrorArea = null;
    private CodeArea userCodeArea;
    private CodeArea setUpCodeArea;
    private CodeArea tearDownCodeArea;
    private String type;
    private UserCodeManagerController parentController;

    public void initialize(URL location, ResourceBundle resources) {
        cbbType.getItems().addAll(UserCodeManager.USER_CODE_TYPE_PARAM,
                UserCodeManager.USER_CODE_TYPE_TEST_CASE);
        cbbType.setValue(UserCodeManager.USER_CODE_TYPE_PARAM);
        cbbType.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue.equals(UserCodeManager.USER_CODE_TYPE_PARAM)) {
                        chooseParameterUserCodeType();
                    } else if (newValue.equals(UserCodeManager.USER_CODE_TYPE_TEST_CASE)) {
                        chooseTestCaseUserCodeType();
                    }
                });

        tfName.setOnKeyReleased(e -> validate());

    }

    public static AddEditUserCodeController getInstanceToEditUserCode(AbstractUserCode userCode) {
        AddEditUserCodeController controller = getInstance();
        if (controller != null) {
            controller.getStage().setTitle("Edit User Code");
            controller.setUserCode(userCode);
            controller.setType(AddEditUserCodeController.TYPE_EDIT);
            controller.loadContent(userCode);
        }

        return controller;
    }

    public static AddEditUserCodeController getInstanceToAddUserCode() {
        AddEditUserCodeController controller = getInstance();
        if (controller != null) {
            controller.getStage().setTitle("Add User Code");
            controller.setType(AddEditUserCodeController.TYPE_ADD);
            controller.initContent();
        }

        return controller;
    }

    private static AddEditUserCodeController getInstance() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/user_code/AddEditUserCode.fxml"));
        try {
            Parent parent = loader.load();
            AddEditUserCodeController controller = loader.getController();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);

            controller.setStage(stage);
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void initContent() {
        // load content to code editor
        String prevCode = "// User code here...";
        CodeArea userCodeArea = UserCodeManager.formatCodeArea(prevCode, true, true);
        CodeArea setUpCodeArea = UserCodeManager.formatCodeArea(prevCode, true, true);
        CodeArea tearDownCodeArea = UserCodeManager.formatCodeArea(prevCode, true, true);
        setUserCodeArea(userCodeArea);
        setSetUpCodeArea(setUpCodeArea);
        setTearDownCodeArea(tearDownCodeArea);

        tabUserCode.setContent(userCodeArea);
        tabSetUp.setContent(setUpCodeArea);
        tabTearDown.setContent(tearDownCodeArea);
        tpContent.getTabs().remove(tabUserCodes); // because default is Parameter type


        // initialize compile message
        CodeArea compileError = UserCodeManager.formatCodeArea("Compile message...", false, false);
        setCompileErrorArea(compileError);
        splitPane.getItems().set(1, compileError);
    }

    private void resetCompileMessage() {
        compileErrorArea.clear();
        compileErrorArea.replaceText("Compile message...");
    }

    private void loadContent(AbstractUserCode userCode) {
        initContent();
        cbbType.setDisable(true);
        if (userCode instanceof ParameterUserCode) {
            cbbType.setValue(UserCodeManager.USER_CODE_TYPE_PARAM);
            chooseParameterUserCodeType();
            // load content to code editor
            String prevCode = userCode.getContent();
            userCodeArea.clear();
            userCodeArea.replaceText(prevCode);
        } else if (userCode instanceof TestCaseUserCode) {
            cbbType.setValue(UserCodeManager.USER_CODE_TYPE_TEST_CASE);
            chooseTestCaseUserCodeType();
            setUpCodeArea.clear();
            tearDownCodeArea.clear();
            setUpCodeArea.replaceText(((TestCaseUserCode) userCode).getSetUpContent());
            tearDownCodeArea.replaceText(((TestCaseUserCode) userCode).getTearDownContent());
        }

        lvIncludeFiles.getItems().addAll(userCode.getIncludePaths());
        tfName.setText(userCode.getName());
    }

    public void showStage(Stage stageToInitOwner) {
        if (stage != null) {
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(stageToInitOwner);
            stage.show();
        }
    }

    private void chooseParameterUserCodeType() {
        tpContent.getTabs().set(0, tabUserCode);
        resetCompileMessage();
    }

    private void chooseTestCaseUserCodeType() {
        tpContent.getTabs().set(0, tabUserCodes);
        resetCompileMessage();
    }

    @FXML
    public void cancel() {
        if (stage != null) stage.close();
    }

    @FXML
    public void compileUserCode() {

    }

    @FXML
    public void addIncludeFile() {
        final FileChooser fileChooser = new FileChooser();
        File includeFile = fileChooser.showOpenDialog(stage);
        if (includeFile != null && includeFile.exists()) {
            lvIncludeFiles.getItems().add(includeFile.getAbsolutePath());
        }
    }

    @FXML
    public void deleteIncludeFile() {
        String includePath = lvIncludeFiles.getSelectionModel().getSelectedItem();
        if (includePath != null) {
            lvIncludeFiles.getItems().remove(includePath);
            lvIncludeFiles.refresh();
        }
    }

    private boolean validateName() {
        String text = tfName.getText().trim();
        if (text.isEmpty()) return false;
        return text.split(" ").length == 1 && text.split(",\\s*").length == 1;
    }

    private void highlightInvalidTextField(TextField textField) {
        textField.setStyle("-fx-border-color: red; -fx-border-width: 1");
    }

    private boolean validate() {
        if (!validateName()) {
            highlightInvalidTextField(tfName);
            return false;
        } else {
            tfName.setStyle("");
            return true;
        }
    }

    @FXML
    public void ok() {
        if (!validate()) return;

        if (type.equals(TYPE_ADD)) {
            AbstractUserCode userCode = null;
            if (cbbType.getValue().equals(UserCodeManager.USER_CODE_TYPE_PARAM)) {
                userCode = new ParameterUserCode();
                userCode.setContent(userCodeArea.getText());
            } else if (cbbType.getValue().equals(UserCodeManager.USER_CODE_TYPE_TEST_CASE)) {
                userCode = new TestCaseUserCode();
                ((TestCaseUserCode) userCode).setSetUpContent(setUpCodeArea.getText());
                ((TestCaseUserCode) userCode).setTearDownContent(tearDownCodeArea.getText());
            }

            if (userCode != null) {
                userCode.setName(tfName.getText().trim());
                userCode.getIncludePaths().clear();
                userCode.getIncludePaths().addAll(lvIncludeFiles.getItems());
                UserCodeManager.getInstance().putUserCode(userCode);
                parentController.updateUserCodesList();
                UserCodeManager.getInstance().exportUserCodeToFile(userCode);
            }
        } else if (type.equals(TYPE_EDIT)) {
            userCode.setName(tfName.getText().trim());
            userCode.getIncludePaths().clear();
            userCode.getIncludePaths().addAll(lvIncludeFiles.getItems());

            if (userCode instanceof ParameterUserCode) {
                userCode.setContent(userCodeArea.getText());
            } else if (userCode instanceof TestCaseUserCode) {
                ((TestCaseUserCode) userCode).setSetUpContent(setUpCodeArea.getText());
                ((TestCaseUserCode) userCode).setTearDownContent(tearDownCodeArea.getText());
            }

            parentController.getLvUserCodes().refresh();
            UserCodeManager.getInstance().exportUserCodeToFile(userCode);
            parentController.showContentOfUserCode(userCode);
        }
        if (stage != null) stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public AbstractUserCode getUserCode() {
        return userCode;
    }

    public void setUserCode(AbstractUserCode userCode) {
        this.userCode = userCode;
    }

    public void setUserCodeArea(CodeArea userCodeArea) {
        this.userCodeArea = userCodeArea;
    }

    public void setCompileErrorArea(CodeArea compileErrorArea) {
        this.compileErrorArea = compileErrorArea;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setParentController(UserCodeManagerController parentController) {
        this.parentController = parentController;
    }

    public void setSetUpCodeArea(CodeArea setUpCodeArea) {
        this.setUpCodeArea = setUpCodeArea;
    }

    public void setTearDownCodeArea(CodeArea tearDownCodeArea) {
        this.tearDownCodeArea = tearDownCodeArea;
    }

    private final static String TYPE_ADD = "ADD";
    private final static String TYPE_EDIT = "EDIT";
}
