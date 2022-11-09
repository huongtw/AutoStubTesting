package com.dse.thread.task;

import com.dse.boundary.BoundaryManager;
import com.dse.config.WorkspaceConfig;
import com.dse.coverage.InstructionComputator;
import com.dse.guifx_v3.controllers.main_view.BaseSceneController;
import com.dse.guifx_v3.controllers.main_view.MenuBarController;
import com.dse.environment.Environment;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.helps.InstructionMapping;
import com.dse.guifx_v3.helps.UIController;
import com.dse.regression.ChangesBetweenSourcecodeFiles;
import com.dse.regression.WorkspaceUpdater;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.thread.AbstractAkaTask;
import com.dse.thread.AkaThread;
import com.dse.user_code.UserCodeManager;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.File;

/**
 * Load an environment
 */
public class EnvironmentLoaderTask extends AbstractEnvironmentLoadTask {

    private File environmentFile;

    public EnvironmentLoaderTask(File environmentFile) {
        this.environmentFile = environmentFile;
    }

    protected void handleNoChange() {
        InstructionMapping.loadInstructions();
        Platform.runLater(() -> {
            LoadingPopupController.getInstance().close();
            loadGUIWhenOpeningEnv();
            MenuBarController.addEnvironmentToHistory(environmentFile);
            MenuBarController.getMenuBarController().refresh();
            updateProgress(MAX_PROGRESS, MAX_PROGRESS);
        });
    }

    protected void handleChanges() {
        // STEP: show a dialog to inform changes
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = MenuBarController.showDialogWhenComparingSourcecode(Alert.AlertType.WARNING, "Warning",
                        "Found changes in the testing project!\n" +
                                "Do you still want to load the environment?",
                        ChangesBetweenSourcecodeFiles.getModifiedSourcecodeFilesInString());
                alert.showAndWait().ifPresent(type -> {
                    if (type.getText().toLowerCase().equals("no")) {
                        UIController.showInformationDialog("You stopped loading the environment file due to changes in source code files"
                                , "Information", "Stop loading environment");

                    } else if (type.getText().toLowerCase().equals("yes")) {
                        // update workspace with modification
                        new AkaThread(new AbstractAkaTask<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                new WorkspaceUpdater().update();
                                MenuBarController.addEnvironmentToHistory(environmentFile);
                                // re-compute the number of statements/branches
                                InstructionComputator.compute();
                                return null;
                            }
                        }).start();

                        loadGUIWhenOpeningEnv();
                        MenuBarController.getMenuBarController().refresh();
                    }
                });
            }
        });
    }

    /**
     * @return true if we can load GUI successfully
     */
    private boolean loadGUIWhenOpeningEnv() {
        UIController.clear();

        MenuBarController.getMenuBarController().loadProjectStructureTree();
        MenuBarController.getMenuBarController().loadTestCaseNavigator();

        TestCaseManager.clearMaps();
        TestCaseManager.initializeMaps();

        // after load testcase, load probe points and regression scripts
        UIController.loadProbePoints();
        UIController.loadRegressionScripts();

        MenuBarController.getMenuBarController().loadUnitTestableState();
        // load boundary of variable types
        BoundaryManager.getInstance().clear();
        BoundaryManager.getInstance().loadExistedBoundaries();
        // load user codes
        UserCodeManager.getInstance().clear();
        UserCodeManager.getInstance().loadExistedUserCodes();

//        loadInstructions();

        // update coverage type displaying on bottom,...
        BaseSceneController.getBaseSceneController().updateInformation();

        UIController.showSuccessDialog("Loading environment is successfully", "Success",
                "The environment has been loaded");
        return true;
    }
}
