package com.dse.guifx_v3.controllers.build_environment;

import com.dse.config.AkaConfig;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.*;
import com.dse.environment.Environment;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import com.dse.logger.AkaLogger;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SummaryController extends AbstractCustomController implements Initializable {
    private final static AkaLogger logger = AkaLogger.get(SummaryController.class);

    @FXML
    private TextField tfWorkSpace;
    @FXML
    private TextField tfPreprocessorCommand;
    @FXML
    private TextField tfCompileCommand;
    @FXML
    private TextField environmentName;
    @FXML
    private TextField tfTestingMethod;
    @FXML
    private TextField coverageType;
    @FXML
    private TextField whitebox;
    @FXML
    private TextField testableSourceDirectories;
    @FXML
    private TextField typeHandledSourceDirectories;
    @FXML
    private TextField libraryIncludeDirectories;
//    @FXML
//    private TextField stubDependencies;
    @FXML
    private TextField uuts;
    @FXML
    private TextField compilerTemplate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setValid(true);
    }

    public void next() {
        logger.debug("Environment configuration:\n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());
    }

    private void clear() {
        tfWorkSpace.setText("");
        tfPreprocessorCommand.setText("");
        tfCompileCommand.setText("");
        environmentName.setText("");
        tfTestingMethod.setText("");
        coverageType.setText("");
        whitebox.setText("");
        testableSourceDirectories.setText("");
        typeHandledSourceDirectories.setText("");
        libraryIncludeDirectories.setText("");
        uuts.setText("");
        compilerTemplate.setText("");
    }

    public void update() {
        clear();
        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        // update compiler template
        List<IEnvironmentNode> nodes;
        nodes = EnvironmentSearch.searchNode(root, new EnviroCompilerNode());
        if (nodes.size() == 1) {
            EnviroCompilerNode compilerNode = (EnviroCompilerNode) nodes.get(0);
            String text = compilerNode.getName();
            compilerTemplate.setText(text);
            // update preprocessor command
            tfPreprocessorCommand.setText(compilerNode.getPreprocessCmd());
            // update compile command
            tfCompileCommand.setText(compilerNode.getCompileCmd());
        }

        // update testing method
        nodes = EnvironmentSearch.searchNode(root, new EnviroTestingMethod());
        if (nodes.size() == 1) {
            String text = ((EnviroTestingMethod) nodes.get(0)).getMethod();
            tfTestingMethod.setText(text);
        }

        // update environment name and work space
        nodes = EnvironmentSearch.searchNode(root, new EnviroNameNode());
        if (nodes.size() == 1) {
            String name = ((EnviroNameNode) nodes.get(0)).getName();
            // update environment
            environmentName.setText(name);
            // update work space
            tfWorkSpace.setText(new AkaConfig().fromJson().getWorkingDirectory() + File.separator + name);
        }

        // update coverage type
        nodes = EnvironmentSearch.searchNode(root, new EnviroCoverageTypeNode());
        if (nodes.size() == 1) {
            coverageType.setText(((EnviroCoverageTypeNode) nodes.get(0)).getCoverageType());
        }

        // update whitebox
        nodes = EnvironmentSearch.searchNode(root, new EnviroWhiteBoxNode());
        whitebox.setText("FALSE");
        if (nodes.size() == 1) {
            if (((EnviroWhiteBoxNode) nodes.get(0)).isActive()) {
                whitebox.setText("TRUE");
            }
        }

        // update testable source directories
        nodes = EnvironmentSearch.searchNode(root, new EnviroSearchListNode());
        if (nodes.size() >= 1) {
            String text = ((EnviroSearchListNode) nodes.get(0)).getSearchList();
            testableSourceDirectories.setText(text);
            // todo: need to display all instead of the first path
        }

        // update type-handled source directories
        nodes = EnvironmentSearch.searchNode(root, new EnviroTypeHandledSourceDirNode());
        if (nodes.size() >= 1) {
            String text = ((EnviroTypeHandledSourceDirNode) nodes.get(0)).getTypeHandledSourceDir();
            typeHandledSourceDirectories.setText(text);
            // todo: need to display all instead of the first path
        }

        // update library include directories
        nodes = EnvironmentSearch.searchNode(root, new EnviroLibraryIncludeDirNode());
        if (nodes.size() >= 1) {
            String text = ((EnviroLibraryIncludeDirNode) nodes.get(0)).getLibraryIncludeDir();
            libraryIncludeDirectories.setText(text);
            // todo: need to display all instead of the first path
        }

        // update stub dependencies

        // update UUT(s)(t)
        nodes = EnvironmentSearch.searchNode(root, new EnviroUUTNode());
        if (nodes.size() >= 1) {
            String text = ((EnviroUUTNode) nodes.get(0)).getName();
            uuts.setText(text);
        }
    }

    @Override
    public void validate() {
        // nothing to do
    }

    @Override
    public void save() {
        // nothing to do
    }

    @Override
    public void loadFromEnvironment() {
        // nothing to do
    }
}
