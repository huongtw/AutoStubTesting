package com.dse.user_code.controllers;

import com.dse.testcase_execution.ITestcaseExecution;
import com.dse.testcase_execution.TestcaseExecution;
import com.dse.compiler.Terminal;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.testcase_manager.TestCase;
import com.dse.testdata.comparable.AssertUserCodeMapping;
import com.dse.testdata.object.IUserCodeNode;
import com.dse.testdata.object.ValueDataNode;
import com.dse.testdata.object.VoidPointerDataNode;
import com.dse.user_code.UserCodeManager;
import com.dse.user_code.objects.AbstractUserCode;
import com.dse.user_code.objects.AssertUserCode;
import com.dse.user_code.objects.ParameterUserCode;
import com.dse.user_code.objects.UsedParameterUserCode;
import com.dse.util.CompilerUtils;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ParameterUserCodeDialogController implements Initializable {

    @FXML
    private TabPane tabPane;
    @FXML
    private Button bHelp;
    @FXML
    private CheckBox chbSaveToReUse;
    @FXML
    private TextField tfName;
    @FXML
    private TextField tfNameOfExistedUC;
    @FXML
    private SplitPane splitPane;
    @FXML
    private Tab tabUserCode, tabInclude;
    @FXML
    private Button bAddIncludeFile;
    @FXML
    private Button bDeleteIncludeFile;
    @FXML
    private ListView<String> lvIncludeFiles;
    @FXML
    private CheckBox chbUseExistedUC;

    private boolean isParameterDefine = true;

    private Stage stage;
    private ValueDataNode dataNode;
    private CodeArea codeArea;
    private CodeArea compileErrorArea;
    private boolean isCompiled;
    private boolean isCompilable;
    private ParameterUserCode existedUserCode;
    private TestCase testCase;

    public void initialize(URL location, ResourceBundle resources) {
        disableTFName();
        chbSaveToReUse.selectedProperty().addListener((observable, oldValue, newValue)
                -> tfName.setDisable(!newValue));
        chbUseExistedUC.selectedProperty().addListener(((observable, oldValue, newValue)
                -> setOnOffUseExistedUserCode(newValue)));
        lvIncludeFiles.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) setText(item);
                        else setText("");
                    }
                };
            }
        });
    }

    public static ParameterUserCodeDialogController getInstance(ValueDataNode dataNode) {
        FXMLLoader loader = new FXMLLoader(Object.class
                .getResource("/FXML/user_code/UserCodeToDefineArgument.fxml"));
        try {
            Parent parent = loader.load();
            ParameterUserCodeDialogController controller = loader.getController();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Input user code to define argument");

            controller.setDataNode(dataNode);
            controller.setStage(stage);
            controller.loadContent();
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setAssertMode() {
        isParameterDefine = false;
        bHelp.setVisible(true);
        tabPane.getTabs().remove(tabInclude);
//        tabInclude.setDisable(true);
    }

    public static ParameterUserCodeDialogController getAssertInstance(ValueDataNode actual) {
        FXMLLoader loader = new FXMLLoader(Object.class
                .getResource("/FXML/user_code/UserCodeToDefineArgument.fxml"));
        try {
            Parent parent = loader.load();
            ParameterUserCodeDialogController controller = loader.getController();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Input user code to assert argument");

            controller.setAssertMode();
            controller.setDataNode(actual);
            controller.setStage(stage);
            controller.loadContent();

            return controller;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadContent() {
        // initialize compile message
        CodeArea compileError = UserCodeManager.formatCodeArea("Compile message...", false, false);
        setCompileErrorArea(compileError);

        // initialize code area
        String prevCode = "// User code...";
        CodeArea userCodeArea = UserCodeManager.formatCodeArea(prevCode, true, true);
        setCodeArea(userCodeArea);
        tabUserCode.setContent(userCodeArea);

        AbstractUserCode abstractUserCode;
        if (!isParameterDefine) {
            abstractUserCode = dataNode.getAssertUserCode();
        } else {
            abstractUserCode = ((IUserCodeNode) dataNode).getUserCode();
        }

        if (abstractUserCode != null)
            loadContent((ParameterUserCode) abstractUserCode);
    }

    private void clearExistedUserCodeInfo() {
        existedUserCode = null;
        chbUseExistedUC.setSelected(false);
        tfNameOfExistedUC.setText("");
        chbUseExistedUC.setDisable(true);
    }

    private void updateExistedUserCodeInfo() {
        tfNameOfExistedUC.setText(existedUserCode.getName());
        chbUseExistedUC.setSelected(true);
        chbUseExistedUC.setDisable(false);
    }

    public void loadContent(UsedParameterUserCode userCode) {
        codeArea.clear();
        lvIncludeFiles.getItems().clear();
        compileErrorArea.clear();
        compileErrorArea.replaceText("Compile message...");

        if (userCode.getType().equals(UsedParameterUserCode.TYPE_CODE)) {
            codeArea.replaceText(userCode.getContent());
            lvIncludeFiles.getItems().addAll(userCode.getIncludePaths());
            clearExistedUserCodeInfo();
        } else if (userCode.getType().equals(UsedParameterUserCode.TYPE_REFERENCE)) {
            int id = userCode.getId();
            ParameterUserCode reference = UserCodeManager
                    .getInstance().getParamUserCodeById(id);
            codeArea.replaceText(reference.getContent());
            lvIncludeFiles.getItems().addAll(reference.getIncludePaths());

            this.existedUserCode = reference;
            updateExistedUserCodeInfo();
        }
    }

    public void loadContent(ParameterUserCode userCode) {
        codeArea.clear();
        lvIncludeFiles.getItems().clear();
        compileErrorArea.clear();
        compileErrorArea.replaceText("Compile message...");
        codeArea.replaceText(userCode.getContent());
        lvIncludeFiles.getItems().addAll(userCode.getIncludePaths());
    }

    @FXML
    public void addIncludeFile() {
        final FileChooser fileChooser = new FileChooser();
//        addExtensionFilterWhenAddIncludeFile(fileChooser);
        File includeFile = fileChooser.showOpenDialog(stage);
        if (includeFile != null && includeFile.exists()) {
            lvIncludeFiles.getItems().add(includeFile.getAbsolutePath());
        }
    }

    @FXML
    public void deleteIncludeFile() {
        String path = lvIncludeFiles.getSelectionModel().getSelectedItem();
        if (path != null) {
            lvIncludeFiles.getItems().remove(path);
            lvIncludeFiles.refresh();
        }
    }

    @FXML
    public void cancel() {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    void compileUserCode() {
        String error = compile();
        if (error.isEmpty()) {
            UIController.showSuccessDialog("Compile User Code successfully.", "Compile Success", "Success");
            splitPane.getItems().remove(compileErrorArea);
            isCompilable = true;
        } else {
            UIController.showErrorDialog("Failed to Compile User Code.", "Compile Failure", "Failure");
            compileErrorArea.replaceText(error);
            splitPane.getItems().set(1, compileErrorArea);
            splitPane.getDividers().get(0).setPosition(0.7);
        }

        isCompiled = true;
    }

    private String simplify(String message) {
        String[] lines = message.split("\\R");

        StringBuilder shorten = new StringBuilder();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].replace(getArchivePath(), SpecialCharacter.EMPTY);

            if (line.matches(":[0-9]+:[0-9]+:(.*)")) {
                String[] indexes = line.substring(0, line.indexOf(": ") + 1).split(":");
                String other = line.substring(line.indexOf(": ") + 1);
                int linePos = Integer.parseInt(indexes[1]) - 3;
                line = ":" + linePos + ":" + indexes[2] + ":" + other;
            }

            shorten.append(line).append("\n");
        }

        return shorten.toString();
    }

    @FXML
    void loadExistedUserCode() {
        ChooseDefineArgUserCodeController controller
                = ChooseDefineArgUserCodeController.getInstance();
        if (controller != null) {
            controller.setParentController(this);
            controller.showStage(stage);
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
        if (!isParameterDefine) {
            AssertUserCode userCode = new AssertUserCode();
            userCode.setType(UsedParameterUserCode.TYPE_CODE);
            userCode.setContent(codeArea.getText());

            try {
                AssertUserCodeMapping.convert(getContent());

                if (stage != null)
                    stage.close();

                dataNode.setAssertUserCode(userCode);

            } catch (Exception ex) {
                UIController.showErrorDialog(ex.getMessage(), "Error", "Wrong arguments number");
            }

            return;
        }

        UsedParameterUserCode userCode = new UsedParameterUserCode();
        // chbUseExistedUC is enable if and only if the existedUserCode != null
        if (chbUseExistedUC.isSelected()) { // save reference of user code to Test case
            userCode.setType(UsedParameterUserCode.TYPE_REFERENCE);
            userCode.setId(existedUserCode.getId());

        } else {
            if (chbSaveToReUse.isSelected()) {
                // if save to reuse then create new DefineArgumentUserCode
                // and put to UserCodeManager
                if (validate()) {
                    ParameterUserCode reference = new ParameterUserCode();
                    reference.setContent(codeArea.getText());
                    reference.getIncludePaths().addAll(lvIncludeFiles.getItems());
                    reference.setName(tfName.getText().trim());

                    UserCodeManager.getInstance().putUserCode(reference);
                    UserCodeManager.getInstance().exportUserCodeToFile(reference);

                    // then save reference of this user code to Test case
                    userCode.setType(UsedParameterUserCode.TYPE_REFERENCE);
                    userCode.setId(reference.getId());
                }
            } else {
                // save user code directly to Test case
                userCode.setType(UsedParameterUserCode.TYPE_CODE);
                userCode.setContent(codeArea.getText());
                userCode.getIncludePaths().addAll(lvIncludeFiles.getItems());

            }
        }

        ((IUserCodeNode) dataNode).setUserCode(userCode);
        if (dataNode != null) {
            dataNode.setUseUserCode(true);
        }

        if (dataNode instanceof VoidPointerDataNode) {
            ((VoidPointerDataNode) dataNode).setReferenceType(null);
        }

//        if (dataNode instanceof VoidPointerDataNode) {
//            ((VoidPointerDataNode) dataNode).setReferenceType("USER CODE");
//            ((VoidPointerDataNode) dataNode).setInputMethod(VoidPointerDataNode.InputMethod.USER_CODE);
//        }

        testCase.putOrUpdateDataNodeIncludes(dataNode);
        dataNode.getChildren().clear();

        if (stage != null) {
            stage.close();
        }
    }

    private void disableTFName() {
        tfName.setDisable(true);
    }

    private void applyTemporary() throws Exception {
        String content = codeArea.getText();

        if (isParameterDefine) {
            UsedParameterUserCode userCode = new UsedParameterUserCode();
            userCode.setType(UsedParameterUserCode.TYPE_CODE);
            userCode.setContent(content);
            userCode.getIncludePaths().addAll(lvIncludeFiles.getItems());
            dataNode.setUserCode(userCode);
            dataNode.setUseUserCode(true);
            testCase.putOrUpdateDataNodeIncludes(dataNode);
        } else {
            AssertUserCode userCode = new AssertUserCode();
            userCode.setType(UsedParameterUserCode.TYPE_CODE);
            userCode.setContent(content);
            AssertUserCodeMapping.convert(getContent());
            dataNode.setAssertUserCode(userCode);
        }
    }

    private String compile() {
        boolean isUserCodeOn = dataNode.isUseUserCode();
        String assertMethod = dataNode.getAssertMethod();

        AbstractUserCode prevUserCode;
        if (isParameterDefine)
            prevUserCode = dataNode.getUserCode();
        else
            prevUserCode = dataNode.getAssertUserCode();

        String message, filePath = null;

        try {
            applyTemporary();

            filePath = generateTemporaryFile();
            String outPath = filePath + Environment.getInstance().getCompiler().getOutputExtension();
            String command = Environment.getInstance().getCompiler().generateCompileCommand(filePath, outPath) + " -w";
            String[] script = CompilerUtils.prepareForTerminal(Environment.getInstance().getCompiler(), command);

            message = new Terminal(script).get();

        } catch (Exception ex) {
            message = "Unexpected exception: " + ex.getMessage();

        } finally {
            if (isParameterDefine) {
                testCase.getAdditionalIncludePathsMap().remove(dataNode);
                dataNode.setUseUserCode(isUserCodeOn);
                dataNode.setUserCode(prevUserCode);
                testCase.putOrUpdateDataNodeIncludes(dataNode);
            } else {
                dataNode.setAssertMethod(assertMethod);
                dataNode.setAssertUserCode((AssertUserCode) prevUserCode);
            }

            if (filePath != null)
                Utils.deleteFileOrFolder(new File(filePath));
        }

        return message.trim();
    }

    public void showAndWaitStage(Stage ownerStage) {
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(ownerStage);
        stage.showAndWait();
    }

    private void setOnOffUseExistedUserCode(boolean isUse) {
        if (isUse) {
//            // reload user code
//            loadContent(existedUserCode);

            // disable all
            disableTFName();
            chbSaveToReUse.setSelected(false);
            chbSaveToReUse.setDisable(true);
            codeArea.setEditable(false);
            bAddIncludeFile.setDisable(true);
            bDeleteIncludeFile.setDisable(true);
        } else {
            // enable
            chbSaveToReUse.setDisable(false);
            codeArea.setEditable(true);
            bAddIncludeFile.setDisable(false);
            bDeleteIncludeFile.setDisable(false);
        }
    }

    public String getTemplate() {
        return "#include \"" + ((IUserCodeNode) dataNode).getContextPath() + "\"\n" +
                "\n" +
                "int main() {\n" +
                "%s\n" +
                "return 0;" +
                "}\n";
    }

    public String getArchivePath() {
        return ((IUserCodeNode) dataNode).getTemporaryPath();
    }

    private String generateTemporaryFile() throws Exception {
        TestcaseExecution executor = new TestcaseExecution();

        executor.setFunction(testCase.getFunctionNode());
        executor.setTestCase(testCase);
        executor.setMode(ITestcaseExecution.IN_AUTOMATED_TESTDATA_GENERATION_MODE);

        executor.initializeConfigurationOfTestcase(testCase);
        executor.generateTestDriver(testCase);

        return testCase.getSourceCodeFile();
//        String source = String.format(getTemplate(), getContent());
//        Utils.writeContentToFile(source, getArchivePath());
    }

    public String getContent() {
        return codeArea.getText();
    }

    public void setCodeArea(CodeArea codeArea) {
        codeArea.setOnKeyReleased(event -> {
            isCompilable = false;
            isCompiled = false;
            clearExistedUserCodeInfo();
        });
        this.codeArea = codeArea;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public void setCompileErrorArea(CodeArea compileErrorArea) {
        this.compileErrorArea = compileErrorArea;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public void setDataNode(ValueDataNode dataNode) {
        this.dataNode = dataNode;
    }

    public void setExistedUserCode(ParameterUserCode existedUserCode) {
        this.existedUserCode = existedUserCode;
        chbUseExistedUC.setDisable(false);
    }

    public void help(ActionEvent actionEvent) {
        if (isParameterDefine) {
            //TODO: show alert
        } else {
            Alert info = new Alert(Alert.AlertType.INFORMATION, SpecialCharacter.EMPTY, ButtonType.OK);
            info.setTitle("Help User Code");
            info.setHeaderText("Assert User Code Functions");

            final WebView reportView = new WebView();
            final WebEngine webEngine = reportView.getEngine();
            webEngine.setUserStyleSheetLocation(Object.class.getResource("/css/report_style.css").toString());

            String content = Utils.readResourceContent(HELP_CONTENT_PATH);
            webEngine.loadContent(content);
            info.getDialogPane().setContent(reportView);

            info.showAndWait();
        }
    }

    private static final String HELP_CONTENT_PATH = "/assert_user_code.html";
}
