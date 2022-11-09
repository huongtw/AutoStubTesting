package com.dse.boundary;

import com.dse.config.IFunctionConfigBound;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.Environment;
import com.dse.guifx_v3.objects.bound.BoundOfVariableTypeConfiguration;
import com.dse.guifx_v3.objects.bound.LowerBoundOfVariableTypeConfigurationFactory;
import com.dse.guifx_v3.objects.bound.UpperBoundOfVariableTypeConfigurationFactory;
import com.dse.util.Utils;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.*;

public class EditBoundaryController implements Initializable {
    @FXML
    private TreeTableView<BoundOfVariableTypeConfiguration> ttvBoundOfVariableTypeConfiguration;
    @FXML
    private TreeTableColumn<BoundOfVariableTypeConfiguration, String> colVariableType;
    @FXML
    private TreeTableColumn<BoundOfVariableTypeConfiguration, String> colLower;
    @FXML
    private TreeTableColumn<BoundOfVariableTypeConfiguration, String> colUpper;
    @FXML
    private TextField tfName;

    private TreeItem<BoundOfVariableTypeConfiguration> root = new TreeItem<>();
    private Stage stage;
    private BoundOfDataTypes boundOfDataTypes;
    private ListView<BoundOfDataTypes> lvExistedBoundaries;

    @FXML
    void ok() {
        if (validateName()) {
            String oldName = boundOfDataTypes.getBounds().getName();
            String newName = tfName.getText();
            if (newName.equals(oldName)) {
                Environment.exportBoundOfDataTypeToFile(boundOfDataTypes);
            } else { // the name is changed
                if (oldName != null && ! oldName.equals("")) {
                    // delete old boundary
                    BoundaryManager.getInstance().getNameToBoundaryMap().remove(oldName);
                    Utils.deleteFileOrFolder(new File(Environment.getBoundOfDataTypePath(boundOfDataTypes)));
                    // create new boundary with new name
                    boundOfDataTypes.getBounds().setName(newName);
                    BoundaryManager.getInstance().getNameToBoundaryMap().put(newName, boundOfDataTypes);
                    Environment.exportBoundOfDataTypeToFile(boundOfDataTypes);
                } else {
                    // create new boundary with new name
                    boundOfDataTypes.getBounds().setName(newName);
                    BoundaryManager.getInstance().getNameToBoundaryMap().put(newName, boundOfDataTypes);
                    Environment.exportBoundOfDataTypeToFile(boundOfDataTypes);
                    lvExistedBoundaries.getItems().add(boundOfDataTypes);
                }
            }
            lvExistedBoundaries.refresh();
            stage.close();
        }
    }

    private boolean validateName() {
        String name = tfName.getText();
        if (name == null || name.equals(""))
            return false;
        else if (! name.equals(boundOfDataTypes.getBounds().getName())) {// change name of the boundary
            // check if the name is existed
            File boundaryFolder = new File( new WorkspaceConfig().fromJson().getBoundOfDataTypeDirectory());
            for (File file : Objects.requireNonNull(boundaryFolder.listFiles())) {
                if (file.getName().replace(".json", "").equals(name)) {
                    return false;
                }
            }
        }
        return true;
    }

    @FXML
    void cancel() {
        if (stage != null) {
            stage.close();
        }
    }

    public void initialize(URL location, ResourceBundle resources) {
        ttvBoundOfVariableTypeConfiguration.setRoot(root);
        colVariableType.setCellValueFactory(param -> {
            BoundOfVariableTypeConfiguration value = param.getValue().getValue();
            if (value != null) {
                String name = value.getVariableType();
                return new SimpleStringProperty(name);
            } else return new SimpleStringProperty();
        });

        colLower.setCellValueFactory(param -> {
            BoundOfVariableTypeConfiguration value = param.getValue().getValue();
            if (value != null) {
                String name = value.getLower();
                return new SimpleStringProperty(name);
            } else return new SimpleStringProperty();
        });

        colUpper.setCellValueFactory(param -> {
            BoundOfVariableTypeConfiguration value = param.getValue().getValue();
            if (value != null) {
                String name = value.getUpper();
                return new SimpleStringProperty(name);
            } else return new SimpleStringProperty();
        });
    }

    public static EditBoundaryController getInstance(BoundOfDataTypes boundOfDataTypes) {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/boundary/EditBoundary.fxml"));
        try {
            Parent parent = loader.load();
            EditBoundaryController controller = loader.getController();
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Edit Bound of variable function Configuration");

            controller.setStage(stage);
            controller.setBoundOfDataTypes(boundOfDataTypes);
            controller.loadContent(boundOfDataTypes);

            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadContent(BoundOfDataTypes boundOfDataTypes) {
        tfName.setText(boundOfDataTypes.getBounds().getName());

        colLower.setCellFactory(new LowerBoundOfVariableTypeConfigurationFactory(boundOfDataTypes));
        colUpper.setCellFactory(new UpperBoundOfVariableTypeConfigurationFactory(boundOfDataTypes));
        List<BoundOfVariableTypeConfiguration> parameters = new ArrayList<>();

        // load data
        Map<String, PrimitiveBound> bounds = boundOfDataTypes.getBounds();
        for (String varName : bounds.keySet()) {
            PrimitiveBound varBound = bounds.get(varName);
            if (varBound != null) {
                parameters.add(
                        new BoundOfVariableTypeConfiguration(boundOfDataTypes,
                            varName,
                            varBound.getLower(),
                            varBound.getUpper()
                        )
                );
            }
        }

        //
        for (BoundOfVariableTypeConfiguration param : parameters) {
            TreeItem<BoundOfVariableTypeConfiguration> item = new TreeItem<>(param);
            root.getChildren().add(item);
        }
    }

    public void setLvExistedBoundaries(ListView<BoundOfDataTypes> lvExistedBoundaries) {
        this.lvExistedBoundaries = lvExistedBoundaries;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public void setBoundOfDataTypes(BoundOfDataTypes boundOfDataTypes) {
        this.boundOfDataTypes = boundOfDataTypes;
    }
}
