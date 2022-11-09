package com.dse.guifx_v3.controllers;

import com.dse.boundary.PrimitiveBound;
import com.dse.config.IFunctionConfigBound;
import com.dse.guifx_v3.objects.bound.BoundOfVariableTypeConfiguration;
import com.dse.guifx_v3.objects.bound.LowerBoundOfVariableTypeConfigurationFactory;
import com.dse.guifx_v3.objects.bound.UpperBoundOfVariableTypeConfigurationFactory;
import com.dse.boundary.BoundOfDataTypes;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class BoundOfVariableTypeConfigurationController implements Initializable {

    @FXML
    private TreeTableView<BoundOfVariableTypeConfiguration> ttvBoundOfVariableTypeConfiguration;

    @FXML
    private TreeTableColumn<BoundOfVariableTypeConfiguration, String> colVariableType;

    @FXML
    private TreeTableColumn<BoundOfVariableTypeConfiguration, String> colLower;

    @FXML
    private TreeTableColumn<BoundOfVariableTypeConfiguration, String> colUpper;

    private TreeItem<BoundOfVariableTypeConfiguration> root = new TreeItem<>();

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

    public void loadContent(BoundOfDataTypes boundOfDataTypes) {
        colLower.setCellFactory(new LowerBoundOfVariableTypeConfigurationFactory(boundOfDataTypes));
        colUpper.setCellFactory(new UpperBoundOfVariableTypeConfigurationFactory(boundOfDataTypes));
        List<BoundOfVariableTypeConfiguration> parameters = new ArrayList<>();

        // load data
        Map<String, PrimitiveBound> bounds = boundOfDataTypes.getBounds();
        for (String varName : bounds.keySet()) {
            IFunctionConfigBound varBound = bounds.get(varName);
            if (varBound instanceof PrimitiveBound) {
                parameters.add(new BoundOfVariableTypeConfiguration(boundOfDataTypes,
                        varName,
                        ((PrimitiveBound) varBound).getLower(),
                        ((PrimitiveBound) varBound).getUpper()));
            }
        }

        //
        for (BoundOfVariableTypeConfiguration param : parameters) {
            TreeItem<BoundOfVariableTypeConfiguration> item = new TreeItem<>(param);
            root.getChildren().add(item);
        }
    }
}
