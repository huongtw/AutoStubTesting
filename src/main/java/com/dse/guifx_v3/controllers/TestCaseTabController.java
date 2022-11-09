package com.dse.guifx_v3.controllers;

import com.dse.guifx_v3.helps.UIController;
import com.dse.testcase_manager.IDataTestItem;
import com.dse.testcase_manager.TestCase;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.user_code.UserCodeManager;
import com.dse.user_code.controllers.ChooseTestCaseUserCodeController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


public class TestCaseTabController implements Initializable {

    @FXML
    public TabPane tabPane;
    @FXML
    private Tab tabUserCode;
    @FXML
    private SplitPane spUserCode;
    @FXML
    private ListView<String> lvIncludeFiles;
    @FXML
    private Button bAddIncludeFile;
    @FXML
    private Button bDeleteIncludeFile;
    @FXML
    private Tab tabParameterTree;

    private IDataTestItem testCase;
    private CodeArea caSetUp;
    private CodeArea caTearDown;


    public void initialize(URL location, ResourceBundle resources) {
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

    public void loadContent() {
        if (testCase instanceof TestCase) {
            TestCase testCase = (TestCase) this.testCase;
            // initialize code area
            String setUp = testCase.getTestCaseUserCode().getSetUpContent();
            String tearDown = testCase.getTestCaseUserCode().getTearDownContent();
            CodeArea setUpCodeArea = UserCodeManager.formatCodeArea(setUp, true, true);
            CodeArea tearDownCodeArea = UserCodeManager.formatCodeArea(tearDown, true, true);
            setCaSetUp(setUpCodeArea);
            setCaTearDown(tearDownCodeArea);
            spUserCode.getItems().set(0, setUpCodeArea);
            spUserCode.getItems().set(1, tearDownCodeArea);

//            //tmp
//            setUpCodeArea.setOnKeyReleased(event -> saveTestCaseUserCode());
//            tearDownCodeArea.setOnKeyReleased(event -> saveTestCaseUserCode());

            lvIncludeFiles.getItems().addAll(testCase.getTestCaseUserCode().getIncludePaths());
        } else {
            tabPane.getTabs().remove(tabUserCode);
        }
    }

    @FXML
    private void saveTestCaseUserCode() {
        if (this.testCase instanceof TestCase) {
            TestCase testCase = (TestCase) this.testCase;
            testCase.getTestCaseUserCode().setSetUpContent(caSetUp.getText());
            testCase.getTestCaseUserCode().setTearDownContent(caTearDown.getText());
            testCase.getTestCaseUserCode().getIncludePaths().clear();
            testCase.getTestCaseUserCode().getIncludePaths().addAll(lvIncludeFiles.getItems());
            TestCaseManager.exportBasicTestCaseToFile(testCase);
        }
    }

    @FXML
    void addIncludeFile() {
        final FileChooser fileChooser = new FileChooser();
//        addExtensionFilterWhenAddIncludeFile(fileChooser);
        File includeFile = fileChooser.showOpenDialog(UIController.getPrimaryStage());
        if (includeFile != null && includeFile.exists()) {
            lvIncludeFiles.getItems().add(includeFile.getAbsolutePath());
        }
    }
    @FXML
    void deleteIncludeFile() {
        String path = lvIncludeFiles.getSelectionModel().getSelectedItem();
        if (path != null) {
            lvIncludeFiles.getItems().remove(path);
            lvIncludeFiles.refresh();
        }
    }

    public void setTestcaseTreeTable(AnchorPane tcTreeTable) {
//        // to keep the divider as designed
//        splitPane.getItems().add(0, tcTreeTable);
//        splitPane.getItems().remove(1);
        tabParameterTree.setContent(tcTreeTable);
    }

    @FXML
    public void loadExistedUserCode() {
        if (this.testCase instanceof TestCase) {
            TestCase testCase = (TestCase) this.testCase;
            ChooseTestCaseUserCodeController controller = ChooseTestCaseUserCodeController.getInstance();
            if (controller != null) {
                controller.setAbstractTestCase(testCase);
                controller.showStageAndWait(UIController.getPrimaryStage());
                lvIncludeFiles.getItems().clear();
                lvIncludeFiles.getItems().addAll(testCase.getTestCaseUserCode().getIncludePaths());

                caSetUp.clear();
                caTearDown.clear();
                caSetUp.replaceText(testCase.getTestCaseUserCode().getSetUpContent());
                caTearDown.replaceText(testCase.getTestCaseUserCode().getTearDownContent());
            }
        }
    }

    public void setTestCase(IDataTestItem testCase) {
        this.testCase = testCase;
    }

    private void setCaSetUp(CodeArea caSetUp) {
        this.caSetUp = caSetUp;
    }

    private void setCaTearDown(CodeArea caTearDown) {
        this.caTearDown = caTearDown;
    }
}
