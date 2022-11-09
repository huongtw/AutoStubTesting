package com.dse.regression;

import com.dse.config.WorkspaceConfig;
import com.dse.coverage.InstructionComputator;
import com.dse.environment.Environment;
import com.dse.environment.WorkspaceCreation;
import com.dse.guifx_v3.controllers.main_view.LeftPaneController;
import com.dse.guifx_v3.controllers.main_view.MDIWindowController;
import com.dse.guifx_v3.controllers.main_view.MenuBarController;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.ProjectNode;
import com.dse.testcasescript.object.TestNameNode;
import com.dse.testcase_manager.*;
import com.dse.logger.AkaLogger;
import com.dse.thread.AkaThread;
import com.dse.thread.task.RunInAnotherThreadTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class IncrementalBuildingConfirmWindowController extends AbstractApplyChangesConfirm {

    final static AkaLogger logger = AkaLogger.get(IncrementalBuildingConfirmWindowController.class);

    public static IncrementalBuildingConfirmWindowController getInstance() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/IncrementalBuildingConfirmWindow.fxml"));
        try {
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setTitle("Incremental Building Confirmation");

            IncrementalBuildingConfirmWindowController controller = loader.getController();
            controller.setStage(stage);

            return controller;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void ok() {
        // popup loading
        LoadingPopupController loadingPopup = LoadingPopupController.newInstance("Incremental building");
        loadingPopup.initOwnerStage(UIController.getPrimaryStage());
        loadingPopup.show();

        RunInAnotherThreadTask beforeTask = new RunInAnotherThreadTask();
        beforeTask.setCallBack(this::applyChange);

        beforeTask.setOnSucceeded(event -> {
            // delete "will be delete" test cases
            for (TestNameNode nameNode : deletedTestCases) {
                String name = nameNode.getName();
                // remove from MDI window
                MDIWindowController.getMDIWindowController().removeTestCaseTab(name);
            }

            LeftPaneController.getLeftPaneController().clear();
//        MDIWindowController.getMDIWindowController().clearModifiedSourceNode();
            MenuBarController.getMenuBarController().loadProjectStructureTree();
            // loadTestCaseNavigator will refresh TestCaseManager with new ProjectNode of Environment
            MenuBarController.getMenuBarController().loadTestCaseNavigator();

            TestCaseManager.clearMaps();
            TestCaseManager.initializeMaps();

            RunInAnotherThreadTask afterTask = new RunInAnotherThreadTask();
            afterTask.setCallBack(() -> {
                // step 2:
                UIController.loadProbePoints();

                UIController.loadRegressionScripts();

                // step 3:
                UIController.insertIncludesForProbePoints();
                // reanalyze dependencies
                try {
                    ProjectNode projectRootNode = Environment.getInstance().getProjectNode();

                    InstructionComputator.compute();

                    new WorkspaceCreation().exportSourcecodeFileNodeToWorkingDirectory(projectRootNode,
                            new WorkspaceConfig().fromJson().getElementDirectory(),
                            new WorkspaceConfig().fromJson().getDependencyDirectory());
                    Platform.runLater(() -> UIController
                            .showSuccessDialog("Dependency analyzer successes", "Dependency analyzer", "Success"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> UIController
                            .showErrorDialog("Dependency analyzer caught an unexpected error", "Dependency analyzer", "Fail"));
                }

                // update source code file node on active source code tabs
                UIController.updateActiveSourceCodeTabs();
                if (listener != null)
                    Platform.runLater(() -> listener.onAllow());
            });
            afterTask.setOnSucceeded(e -> loadingPopup.close());

            new AkaThread(afterTask).start();
        });

        new AkaThread(beforeTask).start();

        MDIWindowController.getMDIWindowController().clear();

        if (stage != null)
            stage.close();
    }

    @FXML
    public void cancel() {
        isConfirm = false;
        if (Environment.getBackupEnvironment() != null) {
            Environment.restoreEnvironment();
            if (stage != null) {
                stage.close();
            }
        }
    }
}
