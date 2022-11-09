package com.dse.debugger.controller;

import com.dse.debugger.component.variable.GDBTreeCellVar;
import com.dse.debugger.component.variable.GDBVar;
import com.dse.logger.AkaLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class VariableController implements Initializable {
    private final static AkaLogger logger = AkaLogger.get(VariableController.class);

    private static VariableController variableController = null;
    private static TitledPane titledPane = null;

    private static void prepare() {
        FXMLLoader loader = new FXMLLoader(VariableController.class.getResource("/FXML/debugger/Variables.fxml"));
        try {
            titledPane = loader.load();
            variableController = loader.getController();
        } catch (Exception e) {
            logger.debug("Can not load Variable UI");
            e.printStackTrace();
        }
    }

    public static TitledPane getTitledPane() {
        if (titledPane == null) prepare();
        return titledPane;
    }

    public static VariableController getVariableController() {
        if (variableController == null) prepare();
        return variableController;
    }

    @FXML
    TreeView<GDBVar> varTreeView;

    private final TreeItem<GDBVar> rootNode = new TreeItem<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        varTreeView.setShowRoot(false);
        varTreeView.setEditable(false);
        rootNode.setExpanded(true);
        varTreeView.setRoot(rootNode);
        varTreeView.setCellFactory(param -> new GDBTreeCellVar());
    }

    public void clearAll() {
        this.varTreeView.getRoot().getChildren().clear();
    }

    public void updateVariables() {
        ArrayList<TreeItem<GDBVar>> varTree = DebugController.getDebugController().getGdb().buildTreeVars();
        this.rootNode.getChildren().clear();
        this.rootNode.getChildren().addAll(varTree);
    }
}
