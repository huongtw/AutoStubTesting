package com.dse.probe_point_manager.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class FailedToCompilePopupController {
    private Stage stage;

    @FXML
    private TextArea taCompileMessage;

    @FXML
    public void ok() {
        if (stage != null) {
            stage.close();
        }
    }

    public static Stage getPopupWindow(String message) {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/probe_point/FailedToCompilePopup.fxml"));
        try {
            Parent parent = loader.load();
            FailedToCompilePopupController controller = loader.getController();
            Scene scene = new Scene(parent);

            Stage popUpWindow = new Stage();
            popUpWindow.setScene(scene);
            popUpWindow.setTitle("Failed to compile");
            popUpWindow.setResizable(false);
            controller.setStage(popUpWindow);
            controller.setCompileMessage(message);
            return popUpWindow;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCompileMessage(String message) {
        this.taCompileMessage.setText(message);
    }
}
