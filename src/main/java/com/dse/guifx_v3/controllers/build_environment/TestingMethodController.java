package com.dse.guifx_v3.controllers.build_environment;

import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.EnviroTestingMethod;
import com.dse.environment.object.EnvironmentRootNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TestingMethodController extends AbstractCustomController implements Initializable {
    @FXML
    private ToggleGroup rmiTestingMethod;
    @FXML
    private Label lLinkOptionsObjectFileTesting;
    @FXML
    private TextField tfObjectFileTesting;
    @FXML
    private Button bLoadObjectFileTesting;
    @FXML
    private Label lLinkOptionsLibraryInterfaceTesting;
    @FXML
    private TextField tfLibraryInterfaceTesting;
    @FXML
    private Button bLoadLibraryInterfaceTesting;
    @FXML
    private RadioButton rbTraditionalUnitTesting;

    public void initialize(URL location, ResourceBundle resources) {
        setValid(true);
    }

    private void disableAll() {
        lLinkOptionsLibraryInterfaceTesting.setDisable(true);
        tfLibraryInterfaceTesting.setDisable(true);
        bLoadLibraryInterfaceTesting.setDisable(true);

        lLinkOptionsObjectFileTesting.setDisable(true);
        tfObjectFileTesting.setDisable(true);
        bLoadObjectFileTesting.setDisable(true);
    }

    @FXML
    public void chooseTraditionalUnitTesting() {
        disableAll();
    }

    @FXML
    public void chooseObjectFileTesting() {
        disableAll();
        lLinkOptionsObjectFileTesting.setDisable(false);
        tfObjectFileTesting.setDisable(false);
        bLoadObjectFileTesting.setDisable(false);
    }

    @FXML
    public void chooseLibraryInterfaceTesting() {
        disableAll();
        lLinkOptionsLibraryInterfaceTesting.setDisable(false);
        tfLibraryInterfaceTesting.setDisable(false);
        bLoadLibraryInterfaceTesting.setDisable(false);
    }

    @FXML
    public void chooseTestDrivenDevelopment() {
        disableAll();
    }

    @FXML
    public void loadLinkOptionsForObjectFileTesting() {
        FileChooser fileChooser = new FileChooser();
        setWorkingDirectory(fileChooser);

        // load
        Stage envBuilderStage = UIController.getEnvironmentBuilderStage();
        File linkOptions = fileChooser.showOpenDialog(envBuilderStage);
        if (linkOptions != null) {
            tfObjectFileTesting.setText(linkOptions.getAbsolutePath());
        } else {
            System.out.println("Error when load link options");
        }
    }

    @FXML
    public void loadLinkOptionsForLibraryInterfaceTesting() {
        FileChooser fileChooser = new FileChooser();
        setWorkingDirectory(fileChooser);

        // load
        Stage envBuilderStage = UIController.getEnvironmentBuilderStage();
        File linkOptions = fileChooser.showOpenDialog(envBuilderStage);
        if (linkOptions != null) {
            tfLibraryInterfaceTesting.setText(linkOptions.getAbsolutePath());
        } else {
            System.out.println("Error when load link options");
        }
    }

    @Override
    public void validate() {
        // nothing to do
    }

    @Override
    public void save() {
        String testingMethod = "";
        if (rbTraditionalUnitTesting.isSelected())
            testingMethod = EnviroTestingMethod.TRADITIONAL_UNIT_TESTING;

        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroTestingMethod());

        // update to environment
        if (nodes.size() >= 2) {
            logger.error("Error when saving the testing method of environment");
            UIController.showErrorDialog("Could not save the configuration of this window"
                    , "Save", "Could not save");
            return;
        }

        if (nodes.size() == 1) {
            ((EnviroTestingMethod) nodes.get(0)).setMethod(testingMethod);
            logger.debug("Environment configuration:\n" + root.exportToFile());

        } else {// size == 0 then create new environment testing method node
            EnviroTestingMethod enviroTestingMethod = new EnviroTestingMethod();
            enviroTestingMethod.setMethod(testingMethod);
            root.addChild(enviroTestingMethod);
            logger.debug("Environment configuration:\n" + root.exportToFile());
        }
    }

    @Override
    public void loadFromEnvironment() {
        // nothing to do
    }
}
