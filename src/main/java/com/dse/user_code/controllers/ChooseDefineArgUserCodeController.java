package com.dse.user_code.controllers;

import com.dse.user_code.UserCodeManager;
import com.dse.user_code.objects.AbstractUserCode;
import com.dse.user_code.objects.ParameterUserCode;
import com.dse.user_code.objects.UsedParameterUserCode;
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

public class ChooseDefineArgUserCodeController implements Initializable {
    @FXML
    private TextField tfSearch;
    @FXML
    private ListView<ParameterUserCode> lvUserCodes;
    @FXML
    private Tab tabCode;
    @FXML
    private ListView<String> lvIncludes;

    private Stage stage;
    private CodeArea codeArea;
    private ParameterUserCodeDialogController parentController;

    public void initialize(URL location, ResourceBundle resources) {
        codeArea = UserCodeManager.formatCodeArea("User code...", true, false);
        tabCode.setContent(codeArea);

        List<ParameterUserCode> userCodes = UserCodeManager
                .getInstance().getAllParameterUserCodes();
        lvUserCodes.getItems().addAll(userCodes);
        lvUserCodes.setCellFactory(param -> new ListCell<ParameterUserCode>() {
            @Override
            protected void updateItem(ParameterUserCode item, boolean empty) {
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

        FilteredList<ParameterUserCode> filteredList =
                new FilteredList<>(lvUserCodes.getItems(), e -> true);

        tfSearch.setOnKeyReleased(e -> {
            tfSearch.textProperty().addListener(((observable, oldValue, newValue) -> {
                filteredList.setPredicate((Predicate<? super ParameterUserCode>) userCode
                        -> isSatisfySearchFilter(userCode, tfSearch.getText()));
                lvUserCodes.setItems(filteredList);
            }));

            lvUserCodes.refresh();
        });
    }

    public static ChooseDefineArgUserCodeController getInstance() {
        FXMLLoader loader = new FXMLLoader(Object.class
                .getResource("/FXML/user_code/ChooseExistedDefineArgumentUserCode.fxml"));
        try {
            Parent parent = loader.load();
            ChooseDefineArgUserCodeController controller = loader.getController();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Choose Existed Define Argument User Code");

            controller.setStage(stage);
            controller.loadContent();

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
        if (userCode != null) {
            String content = userCode.getContent();
            codeArea.clear();
            codeArea.replaceText(content);
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

    public void showStage(Stage ownerStage) {
        if (stage != null) {
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(ownerStage);
            stage.show();
        }
    }
    private void loadContent() {

    }

    @FXML
    public void choose() {
        ParameterUserCode userCode = lvUserCodes.getSelectionModel().getSelectedItem();
        if (userCode != null) {
            UsedParameterUserCode usedUC = new UsedParameterUserCode();
            usedUC.setType(UsedParameterUserCode.TYPE_REFERENCE);
            usedUC.setId(userCode.getId());
            parentController.setExistedUserCode(userCode);
            parentController.loadContent(usedUC);
            if (stage != null) {
                stage.close();
            }
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setParentController(ParameterUserCodeDialogController parentController) {
        this.parentController = parentController;
    }
}
