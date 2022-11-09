package com.dse.guifx_v3.controllers.build_environment;

import com.dse.compiler.AvailableCompiler;
import com.dse.compiler.Compiler;
import com.dse.environment.Environment;
import com.dse.environment.EnvironmentSearch;
import com.dse.environment.object.EnviroCompilerNode;
import com.dse.environment.object.EnviroDefinedVariableNode;
import com.dse.environment.object.EnvironmentRootNode;
import com.dse.environment.object.IEnvironmentNode;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.guifx_v3.objects.hint.HintContent;
import com.dse.logger.AkaLogger;
import com.dse.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ChooseCompilerController extends AbstractCustomController implements Initializable {
    final static AkaLogger logger = AkaLogger.get(ChooseCompilerController.class);

    @FXML
    public TextField tfIncludeFlag, tfDefineFlag, tfDebugCommand, tfOutfileFlag, tfOutfileExtension, tfLinkCommand;

    @FXML
    private ComboBox<String> cbCompilers;

    @FXML
    private TextField preprocessCmd, compileCmd;

    @FXML
    private ListView<EnviroDefinedVariableNode> lvDefinedVariable;

    @FXML
    private Button newDefinedVariableBtn;

    @FXML
    private Button editDefinedVariableBtn;

    @FXML
    private Button deleteDefinedVariableBtn;

    @FXML
    private Button parseCmdBtn;

    @FXML
    private Button testSettingBtn;

    private Compiler compiler;


    public void initialize(URL location, ResourceBundle resources) {
        // get all possible compilers
        for (Class<?> compiler : AvailableCompiler.class.getClasses()) {
            try{
                String name = compiler.getField("NAME").get(null).toString();
                cbCompilers.getItems().add(name);
            } catch (Exception ex) {
                logger.error("Cant parse " + compiler.toString() + " compiler setting");
            }
        }

        // set the default compiler
        if (Utils.isWindows())
            cbCompilers.setValue(AvailableCompiler.C_GNU_NATIVE_WINDOWS_MINGW.NAME);
        else
            cbCompilers.setValue(AvailableCompiler.C_GNU_NATIVE.NAME);
        updateCommandCorrespondingToCompiler();

        // set event where we add defined variables
        lvDefinedVariable.setCellFactory(param -> new ListCell<EnviroDefinedVariableNode>() {
            @Override
            protected void updateItem(EnviroDefinedVariableNode item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText(null);
                } else if (item.getName() != null) {
                    String text = item.getName();

                    if (item.getValue() != null && !item.getValue().isEmpty())
                        text += "=" + item.getValue();

                    setText(text);
                }
            }
        });

        // add Hints to Buttons
        Hint.tooltipNode(newDefinedVariableBtn, HintContent.EnvBuilder.Compiler.NEW_DEFINED_VAR);
//        Hint.tooltipNode(editDefinedVariableBtn, "Edit an existing variable");
//        Hint.tooltipNode(deleteDefinedVariableBtn, "Delete a variable");
        Hint.tooltipNode(parseCmdBtn, HintContent.EnvBuilder.Compiler.PARSE_COMMAND);
        Hint.tooltipNode(testSettingBtn, HintContent.EnvBuilder.Compiler.TEST_SETTING);
    }

    @Override
    public void validate() {
        // nothing to do
        setValid(true);
    }

    @Override
    public void save() {
        updateEnviroCompilerNodeInEnvironmentTree();
        updateDefinedVariableNodeInEnvironmentTree();
    }

    /**
     * @return true if we can update/add compiler node in the environment tree
     */
    private boolean updateEnviroCompilerNodeInEnvironmentTree() {
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(Environment.getInstance().getEnvironmentRootNode(), new EnviroCompilerNode());

        if (nodes.size() == 1) {
            updateEnviroCompilerNode(((EnviroCompilerNode) nodes.get(0)));
            logger.debug("Environment configuration:\n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());
            return true;
        } else if (nodes.size() == 0) {
            EnviroCompilerNode compilerNode = updateEnviroCompilerNode(new EnviroCompilerNode());
            Environment.getInstance().getEnvironmentRootNode().addChild(compilerNode);
            logger.debug("Configuration of the environment:\n" + Environment.getInstance().getEnvironmentRootNode().exportToFile());
            return true;
        } else {
            logger.debug("There are more than two compiler options!");
            return false;
        }
    }

    private EnviroCompilerNode updateEnviroCompilerNode(EnviroCompilerNode node) {
        node.setName(cbCompilers.getValue());

        node.setPreprocessCmd(preprocessCmd.getText());
        node.setCompileCmd(compileCmd.getText());
        node.setLinkCmd(tfLinkCommand.getText());
        node.setDebugCmd(tfDebugCommand.getText());

        node.setIncludeFlag(tfIncludeFlag.getText());
        node.setDefineFlag(tfDefineFlag.getText());
        node.setOutputFlag(tfOutfileFlag.getText());
        node.setDebugFlag(AvailableCompiler.TEMPLATE.DEBUG_FLAG);

        node.setOutputExt(tfOutfileExtension.getText());

        return node;
    }

    private void updateDefinedVariableNodeInEnvironmentTree() {
        EnvironmentRootNode rootEnv = Environment.getInstance().getEnvironmentRootNode();
        // remove all defined variables
        List<IEnvironmentNode> children = rootEnv.getChildren();
        for (int i = children.size() - 1; i >= 0; i--)
            if (children.get(i) instanceof EnviroDefinedVariableNode)
                // defined variables are stored in the first children level
                children.remove(i);

        // save defined variable in GUI
        for (EnviroDefinedVariableNode definedVariableNode : lvDefinedVariable.getItems()) {
            children.add(definedVariableNode);
            definedVariableNode.setParent(rootEnv);
            rootEnv.addChild(definedVariableNode);
        }
    }

    public void loadFromEnvironment() {
        logger.debug("Load compiler from current environment");

        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroCompilerNode());

        if (nodes.size() == 1) {
            // load commands from .env file
            EnviroCompilerNode enviroCompilerNode = getEnviroCompilerNode();
            cbCompilers.setValue(enviroCompilerNode.getName());
            preprocessCmd.setText(enviroCompilerNode.getPreprocessCmd());
            compileCmd.setText(enviroCompilerNode.getCompileCmd());
            tfDefineFlag.setText(enviroCompilerNode.getDefineFlag());
            tfIncludeFlag.setText(enviroCompilerNode.getIncludeFlag());
            tfOutfileFlag.setText(enviroCompilerNode.getOutputFlag());
            tfOutfileExtension.setText(enviroCompilerNode.getOutputExt());
            tfLinkCommand.setText(enviroCompilerNode.getLinkCmd());
            tfDebugCommand.setText(enviroCompilerNode.getDebugCmd());


            // load defined variables from .env file
            List<IEnvironmentNode> definedVariableNodes = getEnviroDefinedVariableNode();
            for (IEnvironmentNode definedVariableNode : definedVariableNodes)
                if (definedVariableNode instanceof EnviroDefinedVariableNode)
                    lvDefinedVariable.getItems().add((EnviroDefinedVariableNode) definedVariableNode);
        }
    }

    private List<IEnvironmentNode> getEnviroDefinedVariableNode() {
        Map<String, String> definedVariables = new HashMap<>();
        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> definedVariableNodes = EnvironmentSearch.searchNode(root, new EnviroDefinedVariableNode());
        return definedVariableNodes;
    }

    private EnviroCompilerNode getEnviroCompilerNode() {
        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroCompilerNode());
        if (nodes.size() == 1) {
            EnviroCompilerNode compilerNode = (EnviroCompilerNode) nodes.get(0);
            return compilerNode;
        } else
            return null;
    }

    public void updateCommandCorrespondingToCompiler() {
        // nothing to do because we have nothing to validate
        if (cbCompilers.getValue() != null) {
            Compiler compiler = createTemporaryCompiler(cbCompilers.getValue());
            preprocessCmd.setText(compiler.getPreprocessCommand());
            compileCmd.setText(compiler.getCompileCommand());
            tfDefineFlag.setText(compiler.getDefineFlag());
            tfIncludeFlag.setText(compiler.getIncludeFlag());
            tfOutfileFlag.setText(compiler.getOutputFlag());
            tfOutfileExtension.setText(compiler.getOutputExtension());
            tfLinkCommand.setText(compiler.getLinkCommand());
            tfDebugCommand.setText(compiler.getDebugCommand());
        }
    }

    private Compiler createTemporaryCompiler(String opt) {
        if (opt != null) {
            for (Class<?> c : AvailableCompiler.class.getClasses()) {
                try {
                    String name = c.getField("NAME").get(null).toString();

                    if (name.equals(opt))
                        return new Compiler(c);
                } catch (Exception ex) {
                    logger.error("Cant parse " + c.toString() + " compiler setting");
                }
            }
        }

        return null;
    }

    @FXML
    public void newDefinedVariable() {
        Stage popUpWindow = DefineVariablePopupController.getPopupWindowNew(lvDefinedVariable);

        // block the environment window
        assert popUpWindow != null;
        popUpWindow.initModality(Modality.WINDOW_MODAL);
        popUpWindow.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
        popUpWindow.show();
    }

    @FXML
    public void editDefinedVariable() {
        EnviroDefinedVariableNode variableNode = lvDefinedVariable.getSelectionModel().getSelectedItem();
        if (variableNode != null) {
            Stage popUpWindow = DefineVariablePopupController.getPopupWindowEdit(lvDefinedVariable, variableNode);

            // block the environment window
            assert popUpWindow != null;
            popUpWindow.initModality(Modality.WINDOW_MODAL);
            popUpWindow.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
            popUpWindow.show();
        }
    }

    @FXML
    public void deleteDefinedVariable() {
        EnviroDefinedVariableNode variableNode = lvDefinedVariable.getSelectionModel().getSelectedItem();
        if (variableNode != null) {
            Environment.getInstance().getEnvironmentRootNode().getChildren().remove(variableNode);
            lvDefinedVariable.getItems().remove(variableNode);
            lvDefinedVariable.refresh();
        }
    }

    private void updateCompiler() {
        EnvironmentRootNode root = Environment.getInstance().getEnvironmentRootNode();
        List<IEnvironmentNode> nodes = EnvironmentSearch.searchNode(root, new EnviroCompilerNode());

        if (!nodes.isEmpty())
            compiler = Environment.getInstance().importCompiler((EnviroCompilerNode) nodes.get(0));

        for (EnviroDefinedVariableNode defineNode : lvDefinedVariable.getItems()) {
            String define = defineNode.getName();
            if (defineNode.getValue() != null)
                define += "=" + defineNode.getValue();

            compiler.getDefines().add(define);
        }
    }

    @FXML
    public void testSetting() {
        FXMLLoader loader;
        try {
            save();
            updateCompiler();

            loader = new FXMLLoader(Object.class.getResource("/FXML/envbuilding/TestSettings.fxml"));
            Parent parent = loader.load();
            Scene scene = new Scene(parent);
            TestSettingsController controller = loader.getController();
            controller.setCompiler(compiler);

            Stage testSettingStage = new Stage();
            controller.setStage(testSettingStage);

            testSettingStage.setScene(scene);
            testSettingStage.setTitle("Test Settings");
            testSettingStage.setResizable(false);
            testSettingStage.initModality(Modality.WINDOW_MODAL);
            testSettingStage.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
            testSettingStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseCmd(ActionEvent actionEvent) {
        FXMLLoader loader;
        try {
            save();
            updateCompiler();

            loader = new FXMLLoader(Object.class.getResource("/FXML/envbuilding/ParseCommandLine.fxml"));
            Parent parent = loader.load();
            Scene scene = new Scene(parent);
            ParseCommandLineController controller = loader.getController();
            controller.setCompiler(compiler);
            controller.setLvOriginDefines(lvDefinedVariable);

            Stage stage = new Stage();
            controller.setStage(stage);

            stage.setScene(scene);
            stage.setTitle("Parse Command Line");
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(UIController.getEnvironmentBuilderStage().getScene().getWindow());
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}