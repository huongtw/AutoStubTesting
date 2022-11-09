package com.dse.thread.task;

import com.dse.guifx_v3.controllers.main_view.BaseSceneController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class BackgroundTasksMonitorController implements Initializable {
    /**
     * Singleton patern like
     */
    private static BackgroundTasksMonitorController controller = null;
    private static Stage stage = null;

    private static void prepare() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/background_tasks_gui/BackgroundTasksMonitor.fxml"));
        try {
            Parent parent = loader.load();
            Scene scene = new Scene(parent);
            stage = new Stage();
            stage.setTitle("Background Tasks");
            stage.setScene(scene);
            stage.setResizable(false);

            stage.initModality(Modality.WINDOW_MODAL);
            stage.setAlwaysOnTop(true);
//            stage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
//                @Override
//                public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
//                    stage.close();
//                }
//            });

            controller = loader.getController();

        } catch (Exception e) {
             e.printStackTrace();
        }
    }

    @FXML
    private VBox vBox;


    public void initialize(URL location, ResourceBundle resources) {
        vBox.getChildren().clear();
//        render();
    }

    public static Stage getStage() {
        if (stage == null) prepare();
        return stage;
    }

    public static BackgroundTasksMonitorController getController() {
        if (controller == null) prepare();
        return controller;
    }

    public void render() {
//        addBackgroundTask();
        stage.show();
    }

    public boolean isRendering() {
        return stage.isShowing();
    }

    public void hide() {
        stage.close();
    }

    public void addBackgroundTask(int index, BackgroundTaskObjectController taskObjectController) {
//        BackgroundTaskObjectController taskObjectController = BackgroundTaskObjectController.getNewInstance();
        if (taskObjectController != null) {
//            taskObjectController.setlTitle("Mot");
            vBox.getChildren().add(index, taskObjectController.getContent());
            updateRunningProcessLabel();
        }
    }

    public void addBackgroundTask(BackgroundTaskObjectController taskObjectController) {
        if (taskObjectController != null) {
            vBox.getChildren().add(taskObjectController.getContent());
            updateRunningProcessLabel();
        }
    }

    public int removeBackgroundTask(BackgroundTaskObjectController taskObjectController) {
        int index = -1;
        if (taskObjectController != null) {
            if (vBox.getChildren().contains(taskObjectController.getContent())) {
                index = vBox.getChildren().indexOf(taskObjectController.getContent());
                vBox.getChildren().remove(index);
            }
        }

        updateRunningProcessLabel();

        if (vBox.getChildren().size() == 0) {
            hide();
        }

        return index;
    }

    private void updateRunningProcessLabel() {
        int numOfProcess = vBox.getChildren().size();
        if (numOfProcess == 1) {
            BaseSceneController.getBaseSceneController().getlRunningProcesses().setText(numOfProcess + " process is running...");
            BaseSceneController.getBaseSceneController().getlRunningProcesses().setVisible(true);
        } else if (numOfProcess > 1) {
            BaseSceneController.getBaseSceneController().getlRunningProcesses().setText(numOfProcess + " processes are running...");
            BaseSceneController.getBaseSceneController().getlRunningProcesses().setVisible(true);
        } else {
            BaseSceneController.getBaseSceneController().getlRunningProcesses().setText(null);
            BaseSceneController.getBaseSceneController().getlRunningProcesses().setVisible(false);
        }
    }
}
