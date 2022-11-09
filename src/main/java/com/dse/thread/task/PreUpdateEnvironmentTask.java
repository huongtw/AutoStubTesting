package com.dse.thread.task;

import com.dse.compiler.message.ICompileMessage;
import com.dse.config.WorkspaceConfig;
import com.dse.coverage.InstructionComputator;
import com.dse.environment.*;
import com.dse.environment.object.EnvironmentRootNode;
import com.dse.guifx_v3.controllers.build_environment.BaseController;
import com.dse.guifx_v3.controllers.main_view.BaseSceneController;
import com.dse.guifx_v3.controllers.main_view.MenuBarController;
import com.dse.guifx_v3.controllers.object.LoadingPopupController;
import com.dse.guifx_v3.helps.InstructionMapping;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.*;
import com.dse.project_init.ProjectClone;
import com.dse.regression.IncrementalBuildingConfirmWindowController;
import com.dse.regression.UpdateEnvironmentConfirmWindowController;
import com.dse.search.Search;
import com.dse.search.condition.SourcecodeFileNodeCondition;
import com.dse.testcase_manager.TestCaseManager;
import com.dse.user_code.UserCodeManager;
import com.dse.util.PathUtils;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.dse.guifx_v3.helps.UIController.getPrimaryStage;

/**
 * Load an environment
 */
public class PreUpdateEnvironmentTask extends AbstractEnvironmentLoadTask {

    private List<INode> addSourceNodes;
    private List<INode> deleteSourceNodes;

    @Override
    protected WorkspaceLoader generateLoader() {
        String physicalTreePath = new WorkspaceConfig().fromJson().getPhysicalJsonFile();
        File physicalTreeFile = new File(physicalTreePath);

        ProjectNode root = Environment.getInstance().getProjectNode();
        WorkspaceLoader loader = new WorkspaceLoader(root);
        loader.setPhysicalTreePath(physicalTreeFile);
        loader.setShouldCompileAgain(false);
        loader.setElementFolderOfOldVersion(new WorkspaceConfig().fromJson().getElementDirectory());

        return loader;
    }

    @Override
    protected Void call() throws Exception {
        // load md5 and last modified
        String physicalTreePath = new WorkspaceConfig().fromJson().getPhysicalJsonFile();
        File physicalTreeFile = new File(physicalTreePath);

        Node prevRoot = new PhysicalTreeImporter().importTree(physicalTreeFile);
        List<ISourcecodeFileNode> prevSources = Search.searchNodes(prevRoot, new SourcecodeFileNodeCondition());
        Node newRoot = Environment.getInstance().getProjectNode();
        List<ISourcecodeFileNode> newSources = Search.searchNodes(newRoot, new SourcecodeFileNodeCondition());

        addSourceNodes = new ArrayList<>();

        for (ISourcecodeFileNode source : newSources) {
            source.getChildren().clear();
            String path = source.getAbsolutePath();
            ISourcecodeFileNode correspondingNode = prevSources.stream()
                    .filter(n -> PathUtils.equals(n.getAbsolutePath(), path))
                    .findFirst()
                    .orElse(null);
            if (correspondingNode != null) {
                logger.debug("Load md5 and last modified of " + source.getName());
                source.setLastModifiedDate(correspondingNode.getLastModifiedDate());
                source.setMd5(correspondingNode.getMd5());
                prevSources.remove(correspondingNode);
            } else {
                addSourceNodes.add(source);
            }
        }

        deleteSourceNodes = new ArrayList<>(prevSources);

        Environment.getInstance().saveEnvironmentScriptToFile();

        return super.call();
    }

    @Override
    protected boolean isChanged() {
        return super.isChanged() || !addSourceNodes.isEmpty() || !deleteSourceNodes.isEmpty();
    }

    protected void handleNoChange() {
//        WorkspaceConfig wkConfig = new WorkspaceConfig().fromJson();
//        Utils.deleteFileOrFolder(new File(wkConfig.getDependencyDirectory()));
//        Utils.deleteFileOrFolder(new File(wkConfig.getPhysicalJsonFile()));
//        Utils.deleteFileOrFolder(new File(wkConfig.getElementDirectory()));

        ExecutorService es = Executors.newSingleThreadExecutor();
        UpdateEnvironmentTask task = new UpdateEnvironmentTask();
        es.submit(task);
        es.shutdown();

        try {
            es.awaitTermination(10, TimeUnit.MINUTES);

            Platform.runLater(() -> {
                LoadingPopupController.getInstance().close();
            });

            BuildEnvironmentResult result = task.get();
            int success = result.getExitCode();

            if (success != BaseController.BUILD_NEW_ENVIRONMENT.SUCCESS.CREATE_ENVIRONMENT) {
                switch (success) {
                    case BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.DUPLICATE_ENV_FILE:
                    case BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.DUPLICATE_TST_FILE: {
                        UIController.showErrorDialog("Could not rebuild the environment because the name of the new environment exists", "Rebuild environment", "Fail");
                        break;
                    }

                    case BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.LINKAGE:
                        showLinkError(result.getLinkMessage());
                        break;

                    case BaseController.BUILD_NEW_ENVIRONMENT.FAILURE.COMPILATION: {
                        showResolver(result);
                        break;
                    }

                    default: {
                        UIController.showErrorDialog("Could not rebuild the environment due to unexpected error", "Rebuild environment", "Fail");
                        break;
                    }
                }
            } else {
               updateOthers();
            }
        } catch (Exception e) {
            e.printStackTrace();
            UIController.showErrorDialog("Update environment caught an unexpected error", "Update environment", "Fail");
        }
    }

    private void showResolver(BuildEnvironmentResult result) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                BaseController.showResolver(result);
            }
        });
    }


    private void updateOthers() throws Exception {
        // load user codes
        UserCodeManager.getInstance().clear();
        UserCodeManager.getInstance().loadExistedUserCodes();

        ProjectNode projectRootNode = Environment.getInstance().getProjectNode();

        String physicalTreePath = new WorkspaceConfig().fromJson().getPhysicalJsonFile();
        File physicalTreeFile = new File(physicalTreePath);
        new PhysicalTreeExporter().export(physicalTreeFile, projectRootNode);

        new WorkspaceCreation().exportSourcecodeFileNodeToWorkingDirectory(projectRootNode,
                new WorkspaceConfig().fromJson().getElementDirectory(),
                new WorkspaceConfig().fromJson().getDependencyDirectory());

        InstructionMapping.loadInstructions();

        Executors.newSingleThreadExecutor().submit(new ProjectClone.CloneThread()).get();

        Platform.runLater(() -> {
            // clear every thing (MDIWindows, messange windows)
            UIController.clear();
            BaseController.resetStatesInAllWindows();

            File testcasesScriptFile = new File(new WorkspaceConfig().fromJson().getTestscriptFile());

            UIController.loadTestCasesNavigator(testcasesScriptFile);
            TestCaseManager.clearMaps();
            TestCaseManager.initializeMaps();

            // update information displaying on bottom of screen
            BaseSceneController.getBaseSceneController().updateInformation();

            loadProjectStructureTree();

            File envFile = new File(new WorkspaceConfig().fromJson().getEnvironmentFile());
            MenuBarController.addEnvironmentToHistory(envFile);

            UIController.getEnvironmentBuilderStage().close();

            UIController.showSuccessDialog("Rebuild the environment successfully", "Rebuild environment", "Success");

        });
    }

    private void showLinkError(ICompileMessage linkMessage) {
        Platform.runLater(() -> {
                ButtonType ignore = new ButtonType("Ignore", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                Alert alert = new Alert(Alert.AlertType.WARNING, SpecialCharacter.EMPTY, ignore, cancel);
                alert.setTitle("Build Environment Error");
                alert.setHeaderText("Failed to link this project");

                TextArea textArea = new TextArea();
                textArea.setWrapText(true);
                textArea.setEditable(false);
                textArea.setMinHeight(350);
                textArea.setText(linkMessage.getMessage());

                alert.getDialogPane().setContent(textArea);
                alert.initOwner(getPrimaryStage());

                Optional<ButtonType> result = alert.showAndWait();

                if (result.orElse(ignore) == cancel) {
                    alert.close();
                } else {
                    try {
                        updateOthers();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        });
    }

    private void loadProjectStructureTree() {
        IProjectNode root = Environment.getInstance().getProjectNode();
        root.setName(Environment.getInstance().getName());
        UIController.loadProjectStructureTree(root);
    }

    protected void handleChanges() {
        // STEP: show a dialog to confirm changes
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                LoadingPopupController.getInstance().close();

                UpdateEnvironmentConfirmWindowController controller = UpdateEnvironmentConfirmWindowController.getInstance();
                if (controller != null) {
                    Stage window = controller.getStage();
                    if (window != null) {
                        window.setResizable(false);
                        window.initModality(Modality.WINDOW_MODAL);
                        window.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
                        window.show();
                    }
                }
            }
        });

        InstructionComputator.compute();
    }
}
