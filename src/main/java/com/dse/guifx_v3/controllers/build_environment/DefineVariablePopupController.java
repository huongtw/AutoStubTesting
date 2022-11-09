package com.dse.guifx_v3.controllers.build_environment;

import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.EnviroDefinedVariableNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.List;

public class DefineVariablePopupController {
    private ListView<EnviroDefinedVariableNode> listView;
    private EnviroDefinedVariableNode variableNode;
    private Stage popUpWindow;
    private String type;

    @FXML
    private TextField tfName;
    @FXML
    private TextField tfValue;

    public void ok() {
        String name = tfName.getText();
        String value = tfValue.getText();
        if (name == null || name.trim().isEmpty()) return;

        if (type.equals(DefineVariablePopupController.TYPE_NEW)) {
            if (isExisted(name)){
                UIController.showErrorDialog("The name " + name +" has been defined before", "Defined variable", "The name is existed");
                return;
            } else {
                EnviroDefinedVariableNode variableNode = new EnviroDefinedVariableNode();
                variableNode.setName(name);
                if (value == null || value.length() == 0)
                    variableNode.setValue("");
                else
                    variableNode.setValue(value);
                variableNode.setValue(value);
                Environment.getInstance().getEnvironmentRootNode().addChild(variableNode);
                listView.getItems().add(variableNode);
                listView.refresh();
            }
        } else if (type.equals(DefineVariablePopupController.TYPE_EDIT)) {
            if (!variableNode.getName().equals(name))
                UIController.showErrorDialog("Do not allow to change the name of defined variable",
                        "Defined variable", "Unable to change the name of existing defined variable");
            else {
                variableNode.setName(name);
                if (value == null || value.length() == 0)
                    variableNode.setValue("");
                else
                    variableNode.setValue(value);
                listView.refresh();
            }
        }

        if (popUpWindow != null) {
            popUpWindow.close();
        }
    }

    private boolean isExisted(String name){
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroDefinedVariableNode());
        for (IEnvironmentNode node: nodes)
            if (node instanceof EnviroDefinedVariableNode){
                if (((EnviroDefinedVariableNode) node).getName().equals(name))
                    return true;
            }
        return false;
    }
    public void cancel() {
        if (popUpWindow != null) {
            popUpWindow.close();
        }
    }

    public static Stage getPopupWindowNew(ListView<EnviroDefinedVariableNode> listView) {
        FXMLLoader loader;
        try {
            loader = new FXMLLoader(Object.class.getResource("/FXML/envbuilding/DefineVariablePopup.fxml"));
            Parent parent = loader.load();
            Scene scene = new Scene(parent);
            DefineVariablePopupController controller = loader.getController();

            controller.setListView(listView);
            controller.setType(TYPE_NEW);

            Stage popUpWindow = new Stage();
            popUpWindow.setScene(scene);
            popUpWindow.setTitle("Define New Variable");
            popUpWindow.setResizable(false);
            controller.setPopUpWindow(popUpWindow);
            return popUpWindow;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Stage getPopupWindowEdit(ListView<EnviroDefinedVariableNode> listView, EnviroDefinedVariableNode variableNode) {
        FXMLLoader loader;
        try {
            loader = new FXMLLoader(Object.class.getResource("/FXML/envbuilding/DefineVariablePopup.fxml"));
            Parent parent = loader.load();
            Scene scene = new Scene(parent);
            DefineVariablePopupController controller = loader.getController();

            controller.setType(TYPE_EDIT);
            controller.setListView(listView);
            controller.setVariableNode(variableNode);
            controller.setTfName(variableNode.getName());
            controller.setTfValue(variableNode.getValue());

            Stage popUpWindow = new Stage();
            popUpWindow.setScene(scene);
            popUpWindow.setTitle("Edit Defined Variable");
            popUpWindow.setResizable(false);
            controller.setPopUpWindow(popUpWindow);
            return popUpWindow;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setListView(ListView<EnviroDefinedVariableNode> listView) {
        this.listView = listView;
    }

    private void setPopUpWindow(Stage popUpWindow) {
        this.popUpWindow = popUpWindow;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setVariableNode(EnviroDefinedVariableNode variableNode) {
        this.variableNode = variableNode;
    }

    private void setTfName(String name) {
        tfName.setText(name);
    }

    @FXML
    public void keypressEnter(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            ok();
        }
    }

    private void setTfValue(String value) {
        tfValue.setText(value);
    }

    public static final String TYPE_NEW = "NEW";
    public static final String TYPE_EDIT = "EDIT";
}
