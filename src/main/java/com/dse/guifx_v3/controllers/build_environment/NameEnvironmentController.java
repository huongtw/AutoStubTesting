package com.dse.guifx_v3.controllers.build_environment;

import com.dse.config.AkaConfig;
import com.dse.config.WorkspaceConfig;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.EnviroNameNode;
import com.dse.environment.object.EnvironmentRootNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.util.SpecialCharacter;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameEnvironmentController extends AbstractCustomController implements Initializable {
    final static AkaLogger logger = AkaLogger.get(NameEnvironmentController.class);
    @FXML
    private TextField tfEnvironmentName;

    // use when updating environment
    private String ignoredEnvName;

    public void initialize(URL location, ResourceBundle resources) {
        // nothing to do
    }

    @Override
    public void loadFromEnvironment() {
        tfEnvironmentName.setDisable(true);

        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroNameNode());

        if (nodes.size() == 1) {
            String name = ((EnviroNameNode) nodes.get(0)).getName();
            tfEnvironmentName.setText(name);
            ignoredEnvName = name;
            validate();

            logger.debug("Load Environment's name from current environment");
        } else if (nodes.size() == 0) {
            logger.error("Invalid Environment. There are no EnviroNameNode");
        } else {
            logger.debug("There are more than one name options!");
        }
    }

    @Override
    public void save() {
        String newEnvName = tfEnvironmentName.getText().trim();

        validate();

        if (!isValid()) {
            UIController.showErrorDialog("The environment name can't be empty, start with a number and contain white spaces or special characters", "Environment Name", "The selected name: \"" + newEnvName + "\" is invalid");
            return;
        }

        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroNameNode());

        if (nodes.size() >= 2) {
            logger.error("Error when saving the name of environment");
            UIController.showErrorDialog("Could not save the configuration of this step", "Environment Name", "Could not save environment name");
        } else if (nodes.size() == 1) {
            ((EnviroNameNode) nodes.get(0)).setName(newEnvName);
            logger.debug("Environment configuration:\n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());

        } else {
            EnviroNameNode nameNode = new EnviroNameNode();
            nameNode.setName(newEnvName);
            root.addChild(nameNode);
            logger.debug("Environment configuration:\n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());
        }

//        validate();
    }

    private boolean validateTFEnvironmentName() {
        String name = tfEnvironmentName.getText().trim();

        // Regex to check valid env name.
        String regex = "^[A-Za-z]\\w*$";

        // Compile the ReGex
        Pattern p = Pattern.compile(regex);

        // Pattern class contains matcher() method
        // to find matching between given username
        // and regular expression.
        Matcher m = p.matcher(name);

        // Return if the username
        // matched the ReGex
        if (!m.matches()) {
            tfEnvironmentName.setStyle("-fx-border-color: red");
            return false;
        }

//        // check if the name existed
//        checkNameExist(name);

        tfEnvironmentName.setStyle("");
        return true;
    }

    @Deprecated
    private boolean checkNameExist(String name) {
        File wd = new File(new AkaConfig().fromJson().getWorkingDirectory());
        if (wd.exists() && wd.isDirectory()) {
            for (File file : Objects.requireNonNull(wd.listFiles())) {
                if (file.getName().equals(ignoredEnvName + WorkspaceConfig.ENV_EXTENSION)
                        || file.getName().equals(ignoredEnvName))
                    // use when updating environment
                    continue;
                else if (file.getName().equals(name + WorkspaceConfig.ENV_EXTENSION)) {
                    tfEnvironmentName.setStyle("-fx-border-color: red");
                    return false;
                }
            }
        }
        return true;
    }

    @FXML
    public void validate() {
        setValid(validateTFEnvironmentName());
        highlightInvalidStep();
    }

}
