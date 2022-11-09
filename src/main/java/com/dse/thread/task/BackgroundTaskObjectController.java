package com.dse.thread.task;

import com.dse.guifx_v3.helps.Factory;
import com.dse.thread.AbstractAkaTask;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BackgroundTaskObjectController implements Initializable {
    @FXML
    private Label lTitle;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Button bStop;

    private AnchorPane content;
    private Task task;
    private String cancelTitle;
    private BackgroundTaskObjectController parent = null;
    private List<BackgroundTaskObjectController> children = new ArrayList<>();
    private boolean isStop;

    public void initialize(URL location, ResourceBundle resources) {
        Image cancel = new Image(Factory.class.getResourceAsStream("/icons/cancel_background_task.png"));
        bStop.setText(null);
        bStop.setGraphic(new ImageView(cancel));
        progressBar.setProgress(0);
        progressIndicator.setProgress(0);
        isStop = false;
    }
    public static BackgroundTaskObjectController getNewInstance() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/background_tasks_gui/BackgroundTaskObject.fxml"));
        try {
            AnchorPane content = loader.load();
            BackgroundTaskObjectController controller = loader.getController();
            controller.setContent(content);
            return controller;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setlTitle(String title) {
        lTitle.setText(title);
    }

    public ProgressIndicator getProgressIndicator() {
        return progressIndicator;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setContent(AnchorPane content) {
        this.content = content;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    @FXML
    public void stop() {
        if (task != null && task.isRunning()) {
            BackgroundTaskObjectController stopTaskController = BackgroundTaskObjectController.getNewInstance();
            StopTask stopTask = new StopTask(task);
            stopTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    // remove task if done
                    Platform.runLater(() -> BackgroundTasksMonitorController.getController().removeBackgroundTask(stopTaskController));
                }
            });

            if (cancelTitle == null) cancelTitle = "Stopping";
            if (stopTaskController != null) {
                stopTaskController.setlTitle(cancelTitle);
                stopTaskController.setTask(stopTask);
                stopTaskController.getbStop().setDisable(true);

                stopTaskController.getProgressBar().progressProperty().bind(stopTask.progressProperty());
                stopTaskController.getProgressIndicator().progressProperty().bind(stopTask.progressProperty());

                // delete concurrent task and add cancel task
                int index = BackgroundTasksMonitorController.getController().removeBackgroundTask(this);
                if (index >= 0) {
                    BackgroundTasksMonitorController.getController().addBackgroundTask(index, stopTaskController);
                }

                new Thread(stopTask).start();
                isStop = true;

                if (parent != null && !(parent.isStop)) {
                    parent.stop();
                }

                for (BackgroundTaskObjectController child : children) {
                    if (child != null && ! (child.isStop))
                        child.stop();
                }
            }
        }
    }

    public boolean isStop() {
        return isStop;
    }

    private static class StopTask extends AbstractAkaTask<Object> {
        private Task task;

        StopTask(Task task) {
            this.task = task;
        }

        @Override
        protected Object call() throws Exception {
            updateProgress(1, 2);
            task.cancel(false);
            updateProgress(2, 2);
            Thread.sleep(1000);
            return null;
        }
    }

    public void setCancelTitle(String cancelTitle) {
        this.cancelTitle = cancelTitle;
    }

    public AnchorPane getContent() {
        return content;
    }

    public void setParent(BackgroundTaskObjectController parent) {
        this.parent = parent;
    }

    public List<BackgroundTaskObjectController> getChildren() {
        return children;
    }

    public Button getbStop() {
        return bStop;
    }
}
