package com.dse.user_code.controllers;

import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.user_code.UserCodeManager;
import com.dse.user_code.objects.AbstractUserCode;
import com.dse.user_code.objects.ParameterUserCode;
import com.dse.user_code.objects.TestCaseUserCode;
import com.dse.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class UserCodeManagerController implements Initializable {
    @FXML
    private TextField tfSearch;
    @FXML
    private ListView<AbstractUserCode> lvUserCodes;
    @FXML
    private Tab tabCode;
    @FXML
    private Tab tabCodes;
    @FXML
    private Tab tabSetUp;
    @FXML
    private Tab tabTearDown;
    @FXML
    private TabPane tpContent;
    @FXML
    private ListView<String> lvIncludes;
    @FXML
    private ComboBox<String> cbbType;
    @FXML
    private Button addUserCodeBtn;
    @FXML
    private Button duplicateUserCodeBtn;
    @FXML
    private Button editUserCodeBtn;
    @FXML
    private Button deleteUserCodeBtn;

    private CodeArea codeArea;
    private CodeArea setUpCodeArea;
    private CodeArea tearDownCodeArea;
    private Stage stage;

    public void initialize(URL location, ResourceBundle resources) {
        lvUserCodes.setCellFactory(param -> new ListCell<AbstractUserCode>() {
            @Override
            protected void updateItem(AbstractUserCode item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText(null);
                } else if (item.getName() != null) {
                    setText(item.getName());
                    ContextMenu contextMenu = new ContextMenu();
                    setContextMenu(contextMenu);

                    addDuplicateUserCode(item);
                    addEditUserCode(item);
                    addDeleteUserCode(item);
                }
            }

            private void addDuplicateUserCode(AbstractUserCode item) {
                MenuItem mi = new MenuItem("Duplicate");
                if (item != null) {
                    mi.setOnAction(event -> duplicateUserCode(item));
                    getContextMenu().getItems().add(mi);
                }
            }

            private void addEditUserCode(AbstractUserCode item) {
                MenuItem mi = new MenuItem("Edit");
                if (item != null) {
                    mi.setOnAction(event -> showWindowToEdit(item));
                    getContextMenu().getItems().add(mi);
                }
            }

            private void addDeleteUserCode(AbstractUserCode item) {
                MenuItem mi = new MenuItem("Delete");
                if (item != null) {
                    mi.setOnAction(event -> deleteUserCode(item));
                    getContextMenu().getItems().add(mi);
                }
            }
        });

        //init code areas;
        codeArea = UserCodeManager.formatCodeArea("User code...", true, false);
        setUpCodeArea = UserCodeManager.formatCodeArea("User code...", true, false);
        tearDownCodeArea = UserCodeManager.formatCodeArea("User code...", true, false);
        tabCode.setContent(codeArea);
        tabSetUp.setContent(setUpCodeArea);
        tabTearDown.setContent(tearDownCodeArea);
        tpContent.getTabs().remove(tabCodes);

        lvUserCodes.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> showContentOfUserCode(newValue));

        cbbType.getItems().addAll(UserCodeManager.USER_CODE_TYPE_ALL,
                UserCodeManager.USER_CODE_TYPE_PARAM,
                UserCodeManager.USER_CODE_TYPE_TEST_CASE);
        cbbType.setValue(UserCodeManager.USER_CODE_TYPE_ALL);

        updateUserCodesList();

//        // add Hints to Buttons
//        Hint.tooltipNode(addUserCodeBtn, "Add new user code");
//        Hint.tooltipNode(duplicateUserCodeBtn, "Duplicate selected user code");
//        Hint.tooltipNode(editUserCodeBtn, "Edit selected user code");
//        Hint.tooltipNode(deleteUserCodeBtn, "Delete selected user code");
    }

    public void updateUserCodesList() {
        final ObservableList<AbstractUserCode> userCodesList =
                FXCollections.observableArrayList(UserCodeManager.getInstance().getAllExistedUserCode());

        FilteredList<AbstractUserCode> filteredList = new FilteredList<>(userCodesList, e -> true);
        filteredList.setPredicate((Predicate<? super AbstractUserCode>) userCode
                -> isSatisfy(userCode, cbbType.getValue(), tfSearch.getText()));
        lvUserCodes.setItems(filteredList);
        if (! lvUserCodes.getItems().isEmpty()) {
            lvUserCodes.getSelectionModel().select(0);
            showContentOfUserCode(lvUserCodes.getSelectionModel().getSelectedItem());
        }

        cbbType.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    filteredList.setPredicate((Predicate<? super AbstractUserCode>) userCode
                            -> isSatisfy(userCode, cbbType.getValue(), tfSearch.getText()));
                    lvUserCodes.setItems(filteredList);
                });
        tfSearch.setOnKeyReleased(e -> {
            tfSearch.textProperty().addListener(((observable, oldValue, newValue) -> {
                filteredList.setPredicate((Predicate<? super AbstractUserCode>) userCode
                        -> isSatisfy(userCode, cbbType.getValue(), tfSearch.getText()));
                lvUserCodes.setItems(filteredList);
            }));

            lvUserCodes.refresh();
        });
    }

    private boolean isSatisfy(AbstractUserCode userCode, String type, String searchText) {
        boolean isSatisfyType = false;
        switch (type) {
            case UserCodeManager.USER_CODE_TYPE_ALL:
                isSatisfyType = true;
                break;
            case UserCodeManager.USER_CODE_TYPE_PARAM:
                isSatisfyType = userCode instanceof ParameterUserCode;
                break;
            case UserCodeManager.USER_CODE_TYPE_TEST_CASE:
                isSatisfyType = userCode instanceof TestCaseUserCode;
                break;
        }

        return isSatisfyType && isSatisfySearchFilter(userCode, searchText);
    }

    private boolean isSatisfySearchFilter(AbstractUserCode userCode, String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return true;
        }
        return userCode.getName().contains(searchText);
    }

    public static UserCodeManagerController getInstance() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/user_code/UserCodeManager.fxml"));
        try {
            Parent parent = loader.load();
            UserCodeManagerController controller = loader.getController();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Manage user codes");

            controller.setStage(stage);
            controller.loadContent();

            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadContent() {
//        lvUserCodes.getItems().addAll(UserCodeManager.getInstance().getAllExistedUserCode());
//        userCodesList.addAll(lvUserCodes.getItems());
    }

    private void showCode(AbstractUserCode userCode) {
        if (userCode instanceof ParameterUserCode) {
            tpContent.getTabs().set(0, tabCode);

            String content = userCode.getContent();
            codeArea.clear();
            codeArea.replaceText(content);
        } else if (userCode instanceof TestCaseUserCode) {
            tpContent.getTabs().set(0, tabCodes);

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

    public void showStage(Stage ownerStage) {
        if (stage != null) {
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(ownerStage);
            stage.show();
        }
    }

    @FXML
    public void addUserCode() {
        AddEditUserCodeController controller = AddEditUserCodeController.getInstanceToAddUserCode();
        if (controller != null) {
            controller.setParentController(this);
            controller.showStage(stage);
        }
    }

    private void showWindowToEdit(AbstractUserCode userCode) {
        AddEditUserCodeController controller = AddEditUserCodeController.getInstanceToEditUserCode(userCode);
        if (controller != null) {
            controller.setParentController(this);
            controller.showStage(stage);
        }
    }

    @FXML
    void editUserCode() {
        AbstractUserCode userCode = lvUserCodes.getSelectionModel().getSelectedItem();
        if (userCode != null) showWindowToEdit(userCode);
    }

    @FXML
    void close() {
        if (stage != null) {
            stage.close();
        }
    }

    private void deleteUserCode(AbstractUserCode userCode) {
        // delete from UserCodeManager
        UserCodeManager.getInstance().removeParamUserCode((ParameterUserCode) userCode);
        // delete from disk
        String absolutePath = UserCodeManager.getInstance().getAbsolutePathOfUserCode(userCode);
        Utils.deleteFileOrFolder(new File(absolutePath));
        updateUserCodesList();
    }

    @FXML
    void deleteUserCode() {
        AbstractUserCode userCode = lvUserCodes.getSelectionModel().getSelectedItem();
        if (userCode != null) {
            deleteUserCode(userCode);
        }
    }

    private void duplicateUserCode(AbstractUserCode userCode) {
        String absolutePath = UserCodeManager.getInstance().getAbsolutePathOfUserCode(userCode);
        AbstractUserCode clone = UserCodeManager.getInstance().importUserCode(new File(absolutePath));
        int cloneId = new Random().nextInt(1000000);
        clone.setId(cloneId);
        // export to file
        UserCodeManager.getInstance().exportUserCodeToFile(clone);
        // update on RAM and GUI
        UserCodeManager.getInstance().putUserCode(clone);
        updateUserCodesList();
        lvUserCodes.getSelectionModel().select(clone);
        showContentOfUserCode(clone);
        updateUserCodesList();
    }

    @FXML
    void duplicateUserCode() {
        AbstractUserCode userCode = lvUserCodes.getSelectionModel().getSelectedItem();
        if (userCode != null) {
            duplicateUserCode(userCode);
        }
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public ListView<AbstractUserCode> getLvUserCodes() {
        return lvUserCodes;
    }
}
