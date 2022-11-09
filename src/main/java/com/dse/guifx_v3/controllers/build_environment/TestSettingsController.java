package com.dse.guifx_v3.controllers.build_environment;

import com.dse.guifx_v3.objects.FXFileView;
import com.dse.guifx_v3.objects.hint.Hint;
import com.dse.compiler.Compiler;
import com.dse.compiler.Terminal;
import com.dse.guifx_v3.objects.hint.HintContent;
import com.dse.thread.AbstractAkaTask;
import com.dse.thread.AkaThread;
import com.dse.util.CompilerUtils;
import com.dse.util.SpecialCharacter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;

public class TestSettingsController implements Initializable {
    @FXML
    public CodeArea caCommand;

    @FXML
    public CodeArea caStdout;

    @FXML
    public CodeArea caStderr;

    @FXML
    public Button btnTest;
    public Button btnLoad;
    public AnchorPane apCommand;
    public AnchorPane apStdout;
    public AnchorPane apStderr;

    @FXML
    private ComboBox<String> cbFunctionalityToTest;

    @FXML
    private TextField tfFileToProcess;

    private Compiler compiler;

    private ProcessTask processTask;

    private Stage stage;

    public void initialize(URL location, ResourceBundle resources) {
        cbFunctionalityToTest.getItems().addAll(
                FUNCTIONALITY_PREPROCESSOR,
                FUNCTIONALITY_COMPILER/*,
                FUNCTIONALITY_PARSER,
                FUNCTIONALITY_DEBUGGER*/
        );

        cbFunctionalityToTest.setValue(FUNCTIONALITY_PREPROCESSOR);

        caStdout.multiPlainChanges().successionEnds(Duration.ofMillis(100))
                .subscribe(ignore -> caStdout
                        .setStyleSpans(0, FXFileView.computeHighlighting(caStdout.getText())));
        caStdout.getStylesheets().add(Object.class.getResource("/css/keywords.css").toExternalForm());

        caStdout.setEditable(false);
        caStderr.setEditable(false);

        initializeScrollPane(apCommand, caCommand);
        initializeScrollPane(apStdout, caStdout);
        initializeScrollPane(apStderr, caStderr);

        // add Hints to Buttons
        Hint.tooltipNode(btnTest, HintContent.EnvBuilder.Compiler.TEST_RUN);
        Hint.tooltipNode(btnLoad, HintContent.EnvBuilder.Compiler.TEST_SELECT_FILE);
    }

    private void initializeScrollPane(AnchorPane pane, CodeArea codeArea) {
        VirtualizedScrollPane<CodeArea> visualizedScrollPane = new VirtualizedScrollPane<>(codeArea);
        AnchorPane.setBottomAnchor(visualizedScrollPane, 0.0);
        AnchorPane.setTopAnchor(visualizedScrollPane, 0.0);
        AnchorPane.setLeftAnchor(visualizedScrollPane, 0.0);
        AnchorPane.setRightAnchor(visualizedScrollPane, 0.0);

        pane.getChildren().add(visualizedScrollPane);
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        this.stage.setOnCloseRequest(event -> {
            if (processTask != null)
                processTask.cancel(true);
        });
    }

    public void load() {
        FileChooser fileChooser = new FileChooser();
        File fileToProcess = fileChooser.showOpenDialog(stage);

        if (fileToProcess != null) {
            tfFileToProcess.setText(fileToProcess.getAbsolutePath());
            initializeDefaultCommand();
        }
    }

    private void initializeDefaultCommand() {
        String command = SpecialCharacter.EMPTY;

        String filePath = tfFileToProcess.getText();

        if (!filePath.isEmpty()) {
            switch (cbFunctionalityToTest.getValue()) {
                case FUNCTIONALITY_PREPROCESSOR:
                    command = compiler.generatePreprocessCommand(filePath);
                    break;
                case FUNCTIONALITY_COMPILER:
                    String outPath = filePath + compiler.getOutputExtension();
                    command = compiler.generateCompileCommand(filePath, outPath);
                    break;
            }
        }

        caCommand.replaceText(command);
    }

    public Compiler getCompiler() {
        return compiler;
    }

    public void setCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    private final static String FUNCTIONALITY_PREPROCESSOR = "Preprocessor";
    private final static String FUNCTIONALITY_COMPILER = "Compiler";
    private final static String FUNCTIONALITY_PARSER = "Parser";
    private final static String FUNCTIONALITY_DEBUGGER = "Debugger";

    public void test(ActionEvent actionEvent) {
        caStdout.replaceText(PROCESSING);
        caStderr.replaceText(PROCESSING);
        btnTest.setDisable(true);

        processTask = new ProcessTask(caCommand.getText());
        AkaThread processThread = new AkaThread(processTask);
        processThread.setDaemon(true);
        processThread.start();
    }

    public void changeFunction(ActionEvent actionEvent) {
        initializeDefaultCommand();
    }

    private class ProcessTask extends AbstractAkaTask<String> {
        private String command;
        private String stdout = SpecialCharacter.EMPTY;
        private String stderr = SpecialCharacter.EMPTY;

        public ProcessTask(String command) {
            this.command = command;
        }

        public String getStderr() {
            return stderr;
        }

        public String getStdout() {
            return stdout;
        }

        @Override
        public String call() {
            try {
                String[] script = CompilerUtils.prepareForTerminal(compiler, command.replace("\n", " "));

                Terminal terminal = new Terminal(script);
                stdout = terminal.getStdout();
                stderr = terminal.getStderr();
                
            } catch (Exception ex) {
                stderr = ex.getMessage();
            }

            return stderr;
        }

        @Override
        protected void succeeded() {
            caStderr.replaceText(stderr);
            caStdout.replaceText(stdout);
            btnTest.setDisable(false);
        }
    }

    private static final String PROCESSING = "processing...";
}
