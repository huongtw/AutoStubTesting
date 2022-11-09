package com.dse.guifx_v3.objects.popups;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SomeTestCasesAreNotSuccessPopupController implements Initializable {
    private Stage popUpWindow;

    @FXML
    private ListView<String> listView;
    @FXML
    private Button bContinue;

    public void initialize(URL location, ResourceBundle resources) {
        listView.getItems().clear();
    }
    public void cancel() {
        if (popUpWindow != null) {
            popUpWindow.close();
        }
    }

    public static SomeTestCasesAreNotSuccessPopupController getInstance(List<String> testCaseNames) {
        try {
            FXMLLoader loader;
            loader = new FXMLLoader(Object.class.getResource("/FXML/popups/SomeTestCasesAreNotSuccessPopup.fxml"));
            Parent parent = loader.load();
            Scene scene = new Scene(parent);
            SomeTestCasesAreNotSuccessPopupController controller = loader.getController();

            controller.loadListView(testCaseNames);

            Stage popUpWindow = new Stage();
            popUpWindow.setScene(scene);
            popUpWindow.setTitle("Notification");
            popUpWindow.setResizable(false);
            controller.setPopUpWindow(popUpWindow);
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadListView(List<String> testCaseNames) {
        this.listView.getItems().addAll(testCaseNames);
    }

    public void setPopUpWindow(Stage popUpWindow) {
        this.popUpWindow = popUpWindow;
    }

    public Button getbContinue() {
        return bContinue;
    }

    public Stage getPopUpWindow() {
        return popUpWindow;
    }
}
