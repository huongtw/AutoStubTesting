package com.dse.user_code.controllers;

import com.dse.testcase_manager.AbstractTestCase;
import com.dse.user_code.UserCodeManager;
import com.dse.user_code.objects.AbstractUserCode;
import com.dse.user_code.objects.TestCaseUserCode;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class ChooseTestCaseUserCodeController implements Initializable {
    @FXML
    private TextField tfSearch;
    @FXML
    private ListView<TestCaseUserCode> lvUserCodes;
    @FXML
    private Tab tabSetUp;
    @FXML
    private Tab tabTearDown;
    @FXML
    private ListView<String> lvIncludes;

    private Stage stage;
    private CodeArea setUpCodeArea;
    private CodeArea tearDownCodeArea;
    private AbstractTestCase abstractTestCase;

    public void initialize(URL location, ResourceBundle resources) {
        setUpCodeArea = UserCodeManager.formatCodeArea("User code...", true, false);
        tearDownCodeArea = UserCodeManager.formatCodeArea("User code...", true, false);
        tabSetUp.setContent(setUpCodeArea);
        tabTearDown.setContent(tearDownCodeArea);

        List<TestCaseUserCode> userCodes = UserCodeManager
                .getInstance().getAllTestCaseUserCodes();
        lvUserCodes.getItems().addAll(userCodes);
        lvUserCodes.setCellFactory(param -> new ListCell<TestCaseUserCode>() {
            @Override
            protected void updateItem(TestCaseUserCode item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText(null);
                } else if (item.getName() != null) {
                    setText(item.getName());
                }
            }
        });

        if (! lvUserCodes.getItems().isEmpty()) {
            lvUserCodes.getSelectionModel().select(0);
            showContentOfUserCode(lvUserCodes.getSelectionModel().getSelectedItem());
        }

        lvUserCodes.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> showContentOfUserCode(newValue));

        FilteredList<TestCaseUserCode> filteredList =
                new FilteredList<>(lvUserCodes.getItems(), e -> true);

        tfSearch.setOnKeyReleased(e -> {
            tfSearch.textProperty().addListener(((observable, oldValue, newValue) -> {
                filteredList.setPredicate((Predicate<? super TestCaseUserCode>) userCode
                        -> isSatisfySearchFilter(userCode, tfSearch.getText()));
                lvUserCodes.setItems(filteredList);
            }));

            lvUserCodes.refresh();
        });
    }

    public static ChooseTestCaseUserCodeController getInstance() {
        FXMLLoader loader = new FXMLLoader(Object.class
                .getResource("/FXML/user_code/ChooseTestCaseUserCode.fxml"));
        try {
            Parent parent = loader.load();
            ChooseTestCaseUserCodeController controller = loader.getController();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Choose Existed Define Argument User Code");

            controller.setStage(stage);

            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isSatisfySearchFilter(AbstractUserCode userCode, String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return true;
        }
        return userCode.getName().contains(searchText);
    }

    private void showCode(AbstractUserCode userCode) {
        if (userCode instanceof TestCaseUserCode) {
            String setUpContent = ((TestCaseUserCode) userCode).getSetUpContent();
            String tearDownContent = ((TestCaseUserCode) userCode).getTearDownContent();
            setUpCodeArea.clear();
            setUpCodeArea.replaceText(setUpContent);
            tearDownCodeArea.clear();
            tearDownCodeArea.replaceText(tearDownContent);
        }
    }

    private void showIncludes(AbstractUserCode userCode) {
        if (userCode != null) {
            lvIncludes.getItems().clear();
            lvIncludes.getItems().addAll(userCode.getIncludePaths());
        }
    }

    public void showContentOfUserCode(AbstractUserCode userCode) {
        showCode(userCode);
        showIncludes(userCode);
    }

    public void showStageAndWait(Stage ownerStage) {
        if (stage != null) {
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(ownerStage);
            stage.showAndWait();
        }
    }

    @FXML
    void choose() {
        TestCaseUserCode userCode = lvUserCodes.getSelectionModel().getSelectedItem();
        abstractTestCase.setTestCaseUserCode(userCode);

        if (stage != null) {
            stage.close();
        }
    }

    public void setAbstractTestCase(AbstractTestCase abstractTestCase) {
        this.abstractTestCase = abstractTestCase;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
