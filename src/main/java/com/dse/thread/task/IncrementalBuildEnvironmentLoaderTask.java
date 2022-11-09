package com.dse.thread.task;

import com.dse.coverage.InstructionComputator;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.parser.object.INode;
import com.dse.regression.ChangesBetweenSourcecodeFiles;
import com.dse.regression.IncrementalBuildingConfirmWindowController;
import javafx.application.Platform;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class IncrementalBuildEnvironmentLoaderTask extends AbstractEnvironmentLoadTask {

    private AllowEditModeListener listener;

    public void setOnAllow(AllowEditModeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void handleNoChange() {
        UIController.showSuccessDialog("There is no change in source code files when rebuilding incrementally",
                "Incremental build", "No change");
        // restore Environment if find out any compile error
        Environment.restoreEnvironment();
        Platform.runLater(() -> {
            if (listener != null)
                listener.onAllow();
        });
    }

    @Override
    protected void handleChanges() {
        Platform.runLater(() -> {
            IncrementalBuildingConfirmWindowController controller =
                    IncrementalBuildingConfirmWindowController.getInstance();
            if (controller != null) {
                Stage window = controller.getStage();
                controller.setListener(listener);
                if (window != null) {
                    window.setResizable(false);
                    window.initModality(Modality.WINDOW_MODAL);
                    window.initOwner(UIController.getPrimaryStage().getScene().getWindow());
                    window.setOnCloseRequest(event -> Environment.restoreEnvironment());
                    window.show();
                }
            }
        });

        InstructionComputator.compute();

        logger.debug("Deleted Nodes: ");
        for (String str : ChangesBetweenSourcecodeFiles.deletedPaths) {
            logger.debug(str);
        }
        logger.debug("End Deleted Node.");
        logger.debug("Modified Nodes:");
        for (INode node : ChangesBetweenSourcecodeFiles.modifiedNodes) {
            logger.debug(node.getAbsolutePath());
        }
        logger.debug("End Modified Nodes.");
        logger.debug("Added Nodes:");
        for (INode node : ChangesBetweenSourcecodeFiles.addedNodes) {
            logger.debug(node.getAbsolutePath());
        }
        logger.debug("End Added Nodes.");
    }

    public interface AllowEditModeListener {
        void onAllow();
    }

}
