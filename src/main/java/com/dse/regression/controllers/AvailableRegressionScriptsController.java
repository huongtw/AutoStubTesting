package com.dse.regression.controllers;

import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.probe_point_manager.controllers.AddEditProbePointController;
import com.dse.regression.RegressionScriptManager;
import com.dse.regression.objects.RegressionScript;
import com.dse.util.Utils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AvailableRegressionScriptsController implements Initializable {

    private Stage stage;

    @FXML
    private ListView<RegressionScript> lvAvailableRegressionScripts;
    
    @FXML
    private TextArea taCommands;

    @FXML
    private Button runScriptBtn;

    @FXML
    private Button newScriptBtn;

    @FXML
    private Button editScriptBtn;

    @FXML
    private Button deleteScriptBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lvAvailableRegressionScripts.setCellFactory(param -> new ListCell<RegressionScript>() {
            @Override
            protected void updateItem(RegressionScript item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText(null);
                } else if (item.getName() != null) {
                    setText(item.getName());
                    ContextMenu contextMenu = new ContextMenu();
                    setContextMenu(contextMenu);

                    addEditRegressionScript(item);
                    addDeleteRegressionScript(item);
                    addRunRegressionScript(item);
                }
            }

            private void addEditRegressionScript(RegressionScript item) {
                MenuItem mi = new MenuItem("Edit");
                mi.setOnAction(event -> {
                    if (item != null) {
                        Stage window = AddEditRegressionScriptController.getWindow(AddEditProbePointController.TYPE_EDIT, item, lvAvailableRegressionScripts);
                        if (window != null) {
                            window.setResizable(false);
                            window.initModality(Modality.WINDOW_MODAL);
                            window.initOwner(UIController.getPrimaryStage().getScene().getWindow());
                            window.show();
                        }
                    }
                });
                getContextMenu().getItems().add(mi);
            }

            private void addRunRegressionScript(RegressionScript item) {
                MenuItem mi = new MenuItem("Run");
                mi.setOnAction(event -> {
                    if (item != null) {
                        if (stage != null) {
                            stage.close();
                        }
                        RegressionScriptManager.getInstance().runRegressionScript(item);
                    }
                });
                getContextMenu().getItems().add(mi);
            }

            private void addDeleteRegressionScript(RegressionScript item) {
                MenuItem mi = new MenuItem("Delete");
                mi.setOnAction(event -> {
                    if (item != null) {
                        RegressionScriptManager.getInstance().deleteRegressionScriptFile(item);
                        lvAvailableRegressionScripts.getItems().remove(item);
                        lvAvailableRegressionScripts.refresh();
                        RegressionScriptManager.getInstance().remove(item);
                    }
                });
                getContextMenu().getItems().add(mi);
            }

        });
        lvAvailableRegressionScripts.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<RegressionScript>() {
            @Override
            public void changed(ObservableValue<? extends RegressionScript> observable, RegressionScript oldValue, RegressionScript newValue) {
                if (newValue != null) {
                    taCommands.setText(Utils.readFileContent(newValue.getScriptFilePath()));
                }
            }
        });
        lvAvailableRegressionScripts.getItems().clear();
        List<RegressionScript> regressionScripts = RegressionScriptManager.getInstance().getAllRegressionScripts();
        lvAvailableRegressionScripts.getItems().addAll(regressionScripts);

//        // add Hints to Buttons
//        Hint.tooltipNode(runScriptBtn, "Run selected script");
//        Hint.tooltipNode(newScriptBtn, "Add new script");
//        Hint.tooltipNode(editScriptBtn, "Edit selected script");
//        Hint.tooltipNode(deleteScriptBtn, "Delete selected script");
    }

    public static AvailableRegressionScriptsController getInstance() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/regression_script/AvailableRegressionScripts.fxml"));
        try {
            Parent parent = loader.load();
            AvailableRegressionScriptsController controller = loader.getController();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Manage Regression Scripts");

            controller.setStage(stage);

            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    void close() {
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    void deleteScript() {
        RegressionScript regressionScript = lvAvailableRegressionScripts.getSelectionModel().getSelectedItem();
        if (regressionScript != null) {
            RegressionScriptManager.getInstance().deleteRegressionScriptFile(regressionScript);
            lvAvailableRegressionScripts.getItems().remove(regressionScript);
            lvAvailableRegressionScripts.refresh();
            RegressionScriptManager.getInstance().remove(regressionScript);
        }
    }

    @FXML
    public void runScript() {
        RegressionScript regressionScript = lvAvailableRegressionScripts.getSelectionModel().getSelectedItem();
        if (regressionScript != null) {
            if (stage != null) {
                stage.close();
            }
            RegressionScriptManager.getInstance().runRegressionScript(regressionScript);
        }
    }

    @FXML
    void editScript() {
        RegressionScript regressionScript = lvAvailableRegressionScripts.getSelectionModel().getSelectedItem();
        if (regressionScript != null) {
            Stage window = AddEditRegressionScriptController.getWindow(AddEditProbePointController.TYPE_EDIT, regressionScript, lvAvailableRegressionScripts);
            if (window != null) {
                window.setResizable(false);
                window.initModality(Modality.WINDOW_MODAL);
                window.initOwner(UIController.getPrimaryStage().getScene().getWindow());
                window.show();
            }
        }
    }

    @FXML
    void newScript() {
        RegressionScript regressionScript = RegressionScript.getNewRandomNameProbePoint();
        Stage window = AddEditRegressionScriptController.getWindow(AddEditRegressionScriptController.TYPE_ADD, regressionScript, lvAvailableRegressionScripts);
        if (window != null) {
            window.setResizable(false);
            window.initModality(Modality.WINDOW_MODAL);
            window.initOwner(this.stage.getScene().getWindow());
            window.show();
        }
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
