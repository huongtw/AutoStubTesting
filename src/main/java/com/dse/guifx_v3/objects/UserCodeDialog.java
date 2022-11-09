package com.dse.guifx_v3.objects;

import com.dse.compiler.Compiler;
import com.dse.compiler.Terminal;
import com.dse.environment.Environment;
import com.dse.util.CompilerUtils;
import com.dse.util.SpecialCharacter;
import com.dse.util.Utils;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;
import java.time.Duration;

public abstract class UserCodeDialog extends Alert {
    private CodeArea codeArea, compileError;

    public UserCodeDialog(String headerText, String content) {
        super(AlertType.NONE);
        setHeaderText(headerText);
        setTitle("User Code Editor");

        initialize(content);
        addEventListener();
    }

    private void initialize(String prevCode) {
        SplitPane pane = new SplitPane();
        pane.setOrientation(Orientation.VERTICAL);

        AnchorPane codeEditor = initializeCodeEditor(prevCode);
        pane.getItems().add(codeEditor);

        AnchorPane compileError = initializeCompileError();
        pane.getItems().add(compileError);

        getDialogPane().setContent(pane);
    }

    public void addEventListener() {
        ButtonType compileButton = new ButtonType("Compile", ButtonBar.ButtonData.LEFT);
        getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL, compileButton);

        final Button btnOk = (Button) getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(ActionEvent.ACTION, event -> setOnOkRequest());

        final Button btnCompile = (Button) getDialogPane().lookupButton(compileButton);
        btnCompile.addEventFilter(ActionEvent.ACTION, event -> {
            String error = compile();
            if (error.isEmpty())
                error = "Compile success.";
            else
                error = simplify(error);
            compileError.replaceText(error);
            event.consume();
        });

        setOnCloseRequest(event -> setOnCloseRequest());
    }


    private CodeArea formatCodeArea(String content, boolean lineNumber, boolean editable) {
        CodeArea codeArea = new CodeArea();

        if (lineNumber)
            codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        codeArea.multiPlainChanges().successionEnds(Duration.ofMillis(100))
                .subscribe(ignore -> codeArea
                        .setStyleSpans(0, FXFileView.computeHighlighting(codeArea.getText())));

        codeArea.replaceText(content);
        codeArea.setEditable(editable);
        codeArea.setWrapText(true);

        codeArea.getStylesheets().add(Object.class.getResource("/css/keywords.css").toExternalForm());

        return codeArea;
    }

    private AnchorPane generateScrollPane(CodeArea codeArea, int width, int height) {
        VirtualizedScrollPane<CodeArea> visualizedScrollPane = new VirtualizedScrollPane<>(codeArea);
        AnchorPane anchorPane = new AnchorPane(visualizedScrollPane);
        AnchorPane.setBottomAnchor(visualizedScrollPane, 0.0);
        AnchorPane.setTopAnchor(visualizedScrollPane, 0.0);
        AnchorPane.setLeftAnchor(visualizedScrollPane, 0.0);
        AnchorPane.setRightAnchor(visualizedScrollPane, 0.0);
        anchorPane.setPrefWidth(width);
        anchorPane.setPrefHeight(height);

        return anchorPane;
    }

    private AnchorPane initializeCompileError() {
        compileError = formatCodeArea("Compile message...", false, false);
        return generateScrollPane(compileError, 550, 100);
    }

    private AnchorPane initializeCodeEditor(String prevCode) {
        codeArea = formatCodeArea(prevCode, true, true);
        return generateScrollPane(codeArea, 550, 350);
    }

    private String simplify(String message) {
        String[] lines = message.split("\\R");

        StringBuilder shorten = new StringBuilder();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].replace(getArchivePath(), SpecialCharacter.EMPTY);

            if (line.matches(":[0-9]+:[0-9]+:(.*)")) {
                String[] indexes = line.substring(0, line.indexOf(": ") + 1).split(":");
                String other = line.substring(line.indexOf(": ") + 1);
                int linePos = Integer.parseInt(indexes[1]) - 3;
                line = ":" + linePos + ":" + indexes[2] + ":" + other;
            }

            shorten.append(line).append("\n");
        }

        return shorten.toString();
    }

    public abstract void setOnOkRequest();

    public abstract void setOnCloseRequest();

    public String getContent() {
        return codeArea.getText();
    }

    public CodeArea getCodeArea() {
        return codeArea;
    }

    private void generateTemporaryFile() {
        String source = String.format(getTemplate(), getContent());
        Utils.writeContentToFile(source, getArchivePath());
    }

    public abstract String getTemplate();

    public abstract String getArchivePath();

    private String compile() {
        generateTemporaryFile();
        String filePath = getArchivePath();

        String message;

        try {
            Compiler compiler = Environment.getInstance().getCompiler();
            String outPath = filePath + compiler.getOutputExtension();
            String command = compiler.generateCompileCommand(filePath, outPath);
            String[] script = CompilerUtils.prepareForTerminal(compiler, command);
            message = new Terminal(script).get();

        } catch (Exception ex) {
            message = ex.getMessage();

        } finally {
            Utils.deleteFileOrFolder(new File(filePath));

        }

        return message;
    }
}
