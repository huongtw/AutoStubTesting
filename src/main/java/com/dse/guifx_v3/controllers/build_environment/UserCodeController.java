package com.dse.guifx_v3.controllers.build_environment;

import com.dse.compiler.Compiler;
import com.dse.compiler.Terminal;
import com.dse.compiler.message.CompileMessage;
import com.dse.compiler.message.ICompileMessage;
import com.dse.config.AkaConfig;
import com.dse.environment.Environment;
import com.dse.guifx_v3.helps.UIController;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.guifx_v3.objects.hint.HintContent;
import com.dse.user_code.UserCodeManager;
import com.dse.user_code.envir.EnvironmentUserCode;
import com.dse.user_code.envir.type.EnvirUserCodeItem;
import com.dse.util.CompilerUtils;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class UserCodeController extends AbstractCustomController implements Initializable {

    @FXML
    private Button bCompile;

    @FXML
    private TabPane tabPane;

    @FXML
    private Label tabAdd;

//    @FXML
//    private TextField tfSearch;
//
//    @FXML
//    private ListView<DataEnvirUserCode> lvUserCodes;
//
//    @FXML
//    private Tab tabCode;
//
//
//    @FXML
//    private Tab tabHeader;
//
//    @FXML
//    private ListView<String> lvIncludes;
//
//    private CodeArea headerCodeArea;
//
//    private CodeArea dataCodeArea;

    private EnvironmentUserCode manager;

    private void addUserCode() {
        Alert alert = new Alert(Alert.AlertType.NONE, SpecialCharacter.EMPTY, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Add Data User Code");

        VBox container = new VBox();
        container.setPadding(new Insets(10, 10, 0, 0));

        Label label = new Label("Enter user code name");
        container.getChildren().add(label);

        TextField textField = new TextField();
        container.getChildren().add(textField);

        alert.getDialogPane().setContent(container);

        Optional<ButtonType> optional = alert.showAndWait();
        if (optional.get() == ButtonType.OK) {
            String name = textField.getText();
            if (!name.isEmpty()) {
                EnvirUserCodeItem userCode = new EnvirUserCodeItem();
                userCode.setName(name);
                manager.add(userCode);

                addTab(userCode);
            }
        }
    }

    public void initializeView() {
        if (manager == null)
            manager = new EnvironmentUserCode();

        switch (AbstractCustomController.ENVIRONMENT_STATUS) {
            case STAGE.CREATING_NEW_ENV_FROM_BLANK_GUI:
            case STAGE.CREATING_NEW_ENV_FROM_OPENING_GUI:
                break;

            case STAGE.UPDATING_ENV_FROM_OPENING_ENV:
                manager.importFromFile();
                manager.list().forEach(this::addTab);

                break;
        }

        bCompile.setDisable(!isCompilable());

        // add Hints to Buttons
        Hint.tooltipNode(bCompile, HintContent.EnvBuilder.UserCode.COMPILE);
    }

    private boolean isCompilable() {
        return Environment.getInstance().getCompiler() != null;
    }

    private void addTab(EnvirUserCodeItem userCode) {
        boolean isExist = tabPane.getTabs().stream()
                .filter(t -> t.getText() != null)
                .anyMatch(t -> t.getText().equals(userCode.getName()));

        if (isExist)
            return;

        Tab newTab = new Tab(userCode.getName());
        CodeArea codeArea = UserCodeManager.formatCodeArea(userCode.getContent(), true, true);
        newTab.setContent(codeArea);

        newTab.setOnClosed(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                manager.list().remove(userCode);
                tabPane.getTabs().remove(newTab);
            }
        });

        int lastIndex = tabPane.getTabs().size() - 1;
        tabPane.getTabs().add(lastIndex, newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Hint.tooltipNode(tabAdd, HintContent.EnvBuilder.UserCode.ADD);

        tabAdd.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                addUserCode();
            }
        });

        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);

        setValid(true);
    }

    @Override
    public void validate() {
        // nothing to do
    }

    public void save() {
        for (Tab tab : tabPane.getTabs()) {
            Node contentNode = tab.getContent();
            if (contentNode instanceof CodeArea) {
                String content = ((CodeArea) contentNode).getText();
                String name = tab.getText();

                EnvirUserCodeItem item = new EnvirUserCodeItem();
                item.setName(name);
                item.setContent(content);
                manager.add(item);
            }
        }
    }

    @Override
    public void loadFromEnvironment() {
        // nothing to do
    }

    public void export() {
        if (manager != null) {
            EnvironmentUserCode.setInstance(manager);
            manager.exportToFile();
            clear();
        }
    }

    public void clear() {
        manager = null;
    }

    public void onCompile() throws IOException, InterruptedException {
        Compiler compiler = Environment.getInstance().getCompiler();
        String localDirectory = new AkaConfig().fromJson().getLocalDirectory();
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        Node contentNode = selectedTab.getContent();
        if (contentNode instanceof CodeArea) {
            String content = ((CodeArea) contentNode).getText();
            String tempPath = localDirectory + File.separator + selectedTab.getText() + EnvironmentUserCode.FILE_EXT;

            Utils.writeContentToFile(content, tempPath);

            String command = compiler.getCompileCommand() + SpecialCharacter.SPACE + "\"" + tempPath + "\"";
            String[] script = CompilerUtils.prepareForTerminal(compiler, command);

            String rawMessage = new Terminal(script).get();

            Utils.deleteFileOrFolder(new File(tempPath));

            ICompileMessage compileMessage = new CompileMessage(rawMessage, tempPath);
            compileMessage.setCompilationCommand(command);

            if (compileMessage.getType() == ICompileMessage.MessageType.ERROR) {
                UIController.showErrorDialog(rawMessage, "Compile Error", "Failed to compile this user code");
            } else {
                UIController.showSuccessDialog("Compile successfully", "Compile Success", "Success");
            }
        }
    }

}
