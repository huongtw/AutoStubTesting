package com.dse.guifx_v3.main;

import com.dse.cli.Main;
import com.dse.config.AkaConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.guifx_v3.controllers.LicenseController;
import com.dse.guifx_v3.controllers.main_view.BaseSceneController;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.logger.AkaLogger;
import com.dse.thread.AkaThreadManager;
import com.dse.util.SpecialCharacter;
import com.sun.javafx.stage.StageHelper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppStart extends Application {
    @Override
    public void start(Stage primaryStage) {
        UIController.setPrimaryStage(primaryStage);

        Scene baseScene = BaseSceneController.getBaseScene();
        baseScene.getStylesheets().add("/css/treetable.css");
        primaryStage.setTitle("Aka Automation Tool");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/FXML/bamboo.png")));
        primaryStage.setScene(baseScene);

        // set size to maximized and not resizable
        primaryStage.setMaximized(false);
        primaryStage.setResizable(true);
        primaryStage.show();

        // save aka config
        new AkaConfig().fromJson()
                .setOpenWorkspaceConfig("")
                .setOpeningWorkspaceDirectory("")
                .addOpeningWorkspaces("")
                .exportToJson();

        // license controller
        LicenseController controller = LicenseController.getInstance();
        if (controller != null) {
            controller.getLicense();
            if (controller.isExpiredLicense() || !controller.isValidLicense()) {
                controller.setLicenseView();
                Stage stage = controller.getStage();
                if (stage != null) {
                    stage.setResizable(false);
                    stage.initModality(Modality.WINDOW_MODAL);
                    stage.initOwner(UIController.getPrimaryStage().getScene().getWindow());
                    stage.show();
                    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent event) {
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (controller.isExpiredLicense() || !controller.isValidLicense()) {
                                        UIController.shutdown();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        }

        // add reset aka config to all newly stage
        UIController.addOnFocusListener(primaryStage);

        // add reset aka config to all newly stage
        StageHelper.getStages().addListener(new ListChangeListener<Stage>() {
            @Override
            public void onChanged(Change<? extends Stage> c) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        List<? extends Stage> addedStage = c.getAddedSubList();
                        for (Stage stage : addedStage) {
                            // add on focus to reset aka config
                            UIController.addOnFocusListener(stage);
                        }
                    }
                }

            }
        });

        // close aka
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        UIController.shutdown();
                    }
                });
            }
        });
    }

    private static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("-cli")) {
            Main.maincli(args);
        } else {
            launch(args);
        }
    }

    public static String getPID() {
        return PID;
    }
}
