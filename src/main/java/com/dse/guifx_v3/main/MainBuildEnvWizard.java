package com.dse.guifx_v3.main;

import com.dse.guifx_v3.controllers.build_environment.BaseController;
import com.dse.guifx_v3.helps.UIController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.dse.logger.AkaLogger;

public class MainBuildEnvWizard extends Application {
    final static AkaLogger logger = AkaLogger.get(MainBuildEnvWizard.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        UIController.setPrimaryStage(primaryStage);
        Scene scene = BaseController.getBaseScene();
        UIController.setEnvironmentBuilderStage(primaryStage);
        primaryStage.setTitle("Build Environment");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

//        Environment.initialize_by_default();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
