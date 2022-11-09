package com.dse.guifx_v3.controllers.build_environment;

import com.dse.compiler.Compiler;
import com.dse.environment.object.EnviroDefinedVariableNode;
import com.dse.environment.object.EnviroLibraryIncludeDirNode;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.guifx_v3.objects.hint.HintContent;
import com.dse.util.CompilerUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ParseCommandLineController implements Initializable {
    
    @FXML
    public Button btnParse;

    @FXML
    public Button btnCancel;

    @FXML
    public Button btnOk;

    @FXML
    public ListView<EnviroLibraryIncludeDirNode> lvIncludes;

    @FXML
    public ListView<EnviroDefinedVariableNode> lvDefines;

    @FXML
    public CodeArea caCommand;

    private Compiler compiler;

    private Stage stage;

    private ListView<EnviroLibraryIncludeDirNode> lvOriginIncludes;

    private ListView<EnviroDefinedVariableNode> lvOriginDefines;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lvDefines.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lvIncludes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        lvDefines.setCellFactory(param -> new ListCell<EnviroDefinedVariableNode>() {
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

        lvIncludes.setCellFactory(param -> new ListCell<EnviroLibraryIncludeDirNode>() {
            @Override
            protected void updateItem(EnviroLibraryIncludeDirNode item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setGraphic(null);
                    setText(null);
                } else if (item.getLibraryIncludeDir() != null) {
                    setText(item.getLibraryIncludeDir());
                }
            }
        });

        // add Hints to Buttons
        Hint.tooltipNode(btnParse, HintContent.EnvBuilder.Compiler.PARSE_COMMAND);
    }

    @FXML
    public void parse(ActionEvent actionEvent) {
        String command = caCommand.getText();

        Compiler result = CompilerUtils.parseCommand(command, compiler);

        updateDefines(result.getDefines());
        updateIncludes(result.getIncludePaths());
    }

    @FXML
    public void cancel(ActionEvent actionEvent) {
        stage.close();
    }

    @FXML
    public void save(ActionEvent actionEvent) {
        for (EnviroLibraryIncludeDirNode include: lvIncludes.getSelectionModel().getSelectedItems()) {
            if (!lvOriginIncludes.getItems().contains(include))
                lvOriginIncludes.getItems().add(include);
        }

        for (EnviroDefinedVariableNode define: lvDefines.getSelectionModel().getSelectedItems()) {
            if (!lvOriginDefines.getItems().contains(define))
                lvOriginDefines.getItems().add(define);
        }

        stage.close();
    }

    private void updateDefines(List<String> defines) {
        lvDefines.getItems().clear();

        for (String define : defines) {
            String[] temp = define.split("=");

            EnviroDefinedVariableNode variableNode = new EnviroDefinedVariableNode();

            if (temp.length > 0)
                variableNode.setName(temp[0]);
            if (temp.length == 2)
                variableNode.setValue(temp[1]);

            if (!lvDefines.getItems().contains(variableNode))
                lvDefines.getItems().add(variableNode);
        }

        lvDefines.getSelectionModel().selectAll();
    }

    private void updateIncludes(List<String> includes) {
        lvIncludes.getItems().clear();

        for (String include : includes) {
            EnviroLibraryIncludeDirNode includeNode = new EnviroLibraryIncludeDirNode();
            includeNode.setLibraryIncludeDir(include);

            if (!lvIncludes.getItems().contains(includeNode))
                lvIncludes.getItems().add(includeNode);
        }

        lvIncludes.getSelectionModel().selectAll();
    }

    public void setCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setLvOriginDefines(ListView<EnviroDefinedVariableNode> lvOriginDefines) {
        this.lvOriginDefines = lvOriginDefines;
    }

    public void setLvOriginIncludes(ListView<EnviroLibraryIncludeDirNode> lvOriginIncludes) {
        this.lvOriginIncludes = lvOriginIncludes;
    }
}
