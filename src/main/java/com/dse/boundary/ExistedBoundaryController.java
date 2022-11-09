package com.dse.boundary;

import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.Factory;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ExistedBoundaryController implements Initializable {

    @FXML
    private Button duplicateBoundaryBtn;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private ListView<BoundOfDataTypes> lvExistedBoundaries;

    private Stage stage;

    public static ExistedBoundaryController getInstance() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/boundary/ExistedBoundary.fxml"));
        try {
            Parent parent = loader.load();
            ExistedBoundaryController controller = loader.getController();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Configure boundary of variable types");

            controller.setStage(stage);
            controller.loadContent();

            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        lvExistedBoundaries.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                BoundOfDataTypes boundOfDataTypes = lvExistedBoundaries.getSelectionModel().getSelectedItem();
                boolean isDefaultModel = isDefault(boundOfDataTypes.getBounds().getName());
                btnDelete.setDisable(isDefaultModel);
                btnEdit.setDisable(isDefaultModel);
            }
        });

        lvExistedBoundaries.setCellFactory(param -> new ListCell<BoundOfDataTypes>() {
            @Override
            protected void updateItem(BoundOfDataTypes item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText(null);
                } else if (item.getBounds().getName() != null) {
                    String usingBoundaryName = BoundaryManager.getInstance().getUsingBoundaryName();
                    if (usingBoundaryName.equals(item.getBounds().getName())) {
                        setGraphic(Factory.getIcon(item));
                    }
                    setText(item.getBounds().getName());
                    ContextMenu contextMenu = new ContextMenu();
                    setContextMenu(contextMenu);

                    addDuplicateBoundary(item);
                    addEditBoundary(item);
                    addDeleteBoundary(item);
                }
            }

            private void addEditBoundary(BoundOfDataTypes item) {
                MenuItem mi = new MenuItem("Edit");
                if (item != null) {
                    if (isDefault(item.getBounds().getName())) {
                        mi.setDisable(true);
                    }
                    mi.setOnAction(event -> showStageToEditBoundary(item));
                    getContextMenu().getItems().add(mi);
                }
            }

            private void addDeleteBoundary(BoundOfDataTypes item) {
                MenuItem mi = new MenuItem("Delete");
                mi.setOnAction(event -> deleteBoundary(item));

                if (isDefault(item.getBounds().getName())) {
                    mi.setDisable(true);
                }
                getContextMenu().getItems().add(mi);
            }

            private void addDuplicateBoundary(BoundOfDataTypes item) {
                MenuItem mi = new MenuItem("Duplicate");
                mi.setOnAction(event -> duplicateBoundary(item));

                getContextMenu().getItems().add(mi);
            }

        });

//        // add Hints to Buttons
//        Hint.tooltipNode(duplicateBoundaryBtn, "Duplicate selected boundary");
//        Hint.tooltipNode(btnEdit, "Edit selected boundary");
//        Hint.tooltipNode(btnDelete, "Delete selected boundary");
    }

    private boolean isDefault(String model) {
        return model.equals(BoundOfDataTypes.MODEL_ILP32)
                || model.equals(BoundOfDataTypes.MODEL_LP64)
                || model.equals(BoundOfDataTypes.MODEL_LP32)
                || model.equals(BoundOfDataTypes.MODEL_LLP64);
    }

    private void loadContent() {
        lvExistedBoundaries.getItems().addAll(BoundaryManager.getInstance().getExistedBoundaries());
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void duplicateBoundary() {
        BoundOfDataTypes origin = lvExistedBoundaries.getSelectionModel().getSelectedItem();
        duplicateBoundary(origin);
    }

    private void duplicateBoundary(BoundOfDataTypes origin) {
        switch (origin.getBounds().getName()) {
            case BoundOfDataTypes.MODEL_ILP32: {
                BoundOfDataTypes clone = new BoundOfDataTypes();
                clone.setBounds(clone.createILP32());
                showStageToEditBoundary(clone);
                break;
            }
            case BoundOfDataTypes.MODEL_LP64: {
                BoundOfDataTypes clone = new BoundOfDataTypes();
                clone.setBounds(clone.createLP64());
                showStageToEditBoundary(clone);
                break;
            }
            case BoundOfDataTypes.MODEL_LP32: {
                BoundOfDataTypes clone = new BoundOfDataTypes();
                clone.setBounds(clone.createLP32());
                showStageToEditBoundary(clone);
                break;
            }
            case BoundOfDataTypes.MODEL_LLP64: {
                BoundOfDataTypes clone = new BoundOfDataTypes();
                clone.setBounds(clone.createLLP64());
                showStageToEditBoundary(clone);
                break;
            }
            default: {
                BoundOfDataTypes clone;
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(BoundOfDataTypes.class, new BoundOfDataTypesDeserializer());
                Gson customGson = gsonBuilder.create();
                clone = customGson.fromJson(Utils.readFileContent(Environment.getBoundOfDataTypePath(origin)),
                        BoundOfDataTypes.class);
                showStageToEditBoundary(clone);
                break;
            }
        }
    }

    @FXML
    public void editBoundary() {
        BoundOfDataTypes boundOfDataTypes = lvExistedBoundaries.getSelectionModel().getSelectedItem();

        if (boundOfDataTypes != null) {
            if (boundOfDataTypes.getBounds().getName().equals(BoundOfDataTypes.MODEL_ILP32)
                    || boundOfDataTypes.getBounds().getName().equals(BoundOfDataTypes.MODEL_LP64)) {
                // todo: notify user can not edit default model data size
                return;
            }

            showStageToEditBoundary(boundOfDataTypes);
        }
    }

    private void showStageToEditBoundary(BoundOfDataTypes boundOfDataTypes) {
        EditBoundaryController controller = EditBoundaryController.getInstance(boundOfDataTypes);
        if (controller != null && controller.getStage() != null) {
            controller.setLvExistedBoundaries(lvExistedBoundaries);
            Stage window = controller.getStage();
            window.setResizable(false);
            window.initModality(Modality.WINDOW_MODAL);
            window.initOwner(stage);
            window.show();
        }
    }

    @FXML
    public void deleteBoundary() {
        BoundOfDataTypes boundOfDataTypes = lvExistedBoundaries.getSelectionModel().getSelectedItem();
        if (boundOfDataTypes.getBounds().getName().equals(BoundOfDataTypes.MODEL_ILP32)
                || boundOfDataTypes.getBounds().getName().equals(BoundOfDataTypes.MODEL_LP64)) {
            return;
        }
        deleteBoundary(boundOfDataTypes);
    }

    private void deleteBoundary(BoundOfDataTypes boundOfDataTypes) {
        if (boundOfDataTypes != null) {
            // delete from BoundaryManager map
            BoundaryManager.getInstance().getNameToBoundaryMap().remove(boundOfDataTypes.getBounds().getName());
            // if delete using Boundary then set using Boundary as default
            if (BoundaryManager.getInstance().getUsingBoundaryName().equals(boundOfDataTypes.getBounds().getName())) {
                BoundaryManager.getInstance().setUsingBoundaryName(BoundOfDataTypes.MODEL_ILP32);
                BoundaryManager.getInstance().exportUsingBoundaryName();
            }
            // delete from disk
            Utils.deleteFileOrFolder(new File(Environment.getBoundOfDataTypePath(boundOfDataTypes)));
            // delete from lvExistedBoundaries
            lvExistedBoundaries.getItems().remove(boundOfDataTypes);


        }
    }

    @FXML
    public void choose() {
        BoundOfDataTypes boundOfDataTypes = lvExistedBoundaries.getSelectionModel().getSelectedItem();
        if (boundOfDataTypes != null) {
            BoundaryManager.getInstance().setUsingBoundaryName(boundOfDataTypes.getBounds().getName());
            // update on disk
            BoundaryManager.getInstance().exportUsingBoundaryName();
            lvExistedBoundaries.refresh();
        }
    }

    @FXML
    public void close() {
        stage.close();
    }

    @FXML
    public void view(ActionEvent actionEvent) {
        duplicateBoundary();
    }
}
