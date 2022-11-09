package com.dse.guifx_v3.controllers.build_environment;

import com.dse.config.AkaConfig;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import com.dse.logger.AkaLogger;
import com.dse.guifx_v3.objects.hint.Hint;

import java.io.File;

public abstract class AbstractCustomController {
    final static AkaLogger logger = AkaLogger.get(AbstractCustomController.class);

    public static class STAGE {
        public static final int UPDATING_ENV_FROM_OPENING_ENV = 1; //default
        public static final int CREATING_NEW_ENV_FROM_BLANK_GUI = 2;
        public static final int CREATING_NEW_ENV_FROM_OPENING_GUI = 3;
    }
    public static int ENVIRONMENT_STATUS = STAGE.UPDATING_ENV_FROM_OPENING_ENV;

    private boolean isValid = false;
    private Label labelStep;

    /**
     * Validate the correctness of input elements in the environment builder window.
     */
    public abstract void validate();

    /**
     * Save the current configuration of the environment builder window.
     */
    public abstract void save();

//    public abstract void back();

    /**
     * Load the configuration from .env file and display on GUI
     */
    public abstract void loadFromEnvironment();

    public void setWorkingDirectory(FileChooser fileChooser) {
        String workingDirectory = new AkaConfig().fromJson().getWorkingDirectory();
        if (workingDirectory != null && new File(workingDirectory).exists())
            fileChooser.setInitialDirectory(new File(workingDirectory));
        else {
            logger.error("There is problem in the working directory. It may not be set up!");
        }
    }

    public void setLabelStep(Label labelStep) {
        this.labelStep = labelStep;
    }

    public Label getLabelStep() {
        return labelStep;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public void highlightInvalidStep() {
        if (isValid) {
            labelStep.setTextFill(Color.BLACK);
        } else {
            labelStep.setTextFill(Color.RED);
        }
    }
}
