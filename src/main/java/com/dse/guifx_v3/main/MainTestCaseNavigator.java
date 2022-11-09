package com.dse.guifx_v3.main;

import com.dse.guifx_v3.controllers.main_view.LeftPaneController;
import com.dse.guifx_v3.helps.UIController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;

public class MainTestCaseNavigator extends Application {
    @Override
    public void start(Stage primaryStage) {
        UIController.setPrimaryStage(primaryStage);
        Scene baseScene;
        baseScene = new Scene(LeftPaneController.getLeftPane());
        baseScene.getStylesheets().add("/css/treetable.css");
        primaryStage.setTitle("Testcase Navigator Test");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/FXML/bamboo.png")));
        primaryStage.setScene(baseScene);
        UIController.loadTestCasesNavigator(new File("datatest/hoannv/environments/VERY_LARGE_PROJECT_v4.tst"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
