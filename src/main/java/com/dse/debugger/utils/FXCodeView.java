package com.dse.debugger.utils;

import com.dse.debugger.component.breakpoint.BreakPoint;
import com.dse.debugger.controller.DebugController;
import com.dse.debugger.component.BreakPointFactory;
import com.dse.debugger.component.variable.GDBTreeCellVar;
import com.dse.debugger.component.variable.GDBVar;
import javafx.collections.ObservableSet;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FXCodeView {
    private static final String[] KEYWORDS = new String[]{
            "asm", "else", "new", "this", "auto", "enum", "operator", "throw",
            "explicit", "private", "true", "break", "export", "protected",
            "try", "case", "extern", "public", "typedef", "catch", "false", "register",
            "typeid", "reinterpret_cast", "typename", "class", "for",
            "return", "union", "const", "friend", "const_cast", "goto",
            "using", "continue", "if", "sizeof", "virtual", "default", "inline", "include",
            "static", "delete", "static_cast", "volatile", "do", "struct",
            "wchar_t", "mutable", "switch", "while", "dynamic_cast", "namespace", "template"
    };

    private static final String[] PRIMITIVES = new String[]{
            "bool", "char", "float", "double", "void", "unsigned", "long", "short", "signed", "int"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PRIMITIVE_PATTERN = "\\b(" + String.join("|", PRIMITIVES) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"]|\n)*\"";
    private static final String COMMENT_PATTERN = "(/\\*([^*]|[\\r\\n]|(\\*+([^*/]|[\\r\\n])))*\\*+/)|(//.*)";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PRIMITIVE>" + PRIMITIVE_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    private FXCodeView() {
    }

    public static CodeArea getCodeEditor(Pane parentPane, ObservableSet<BreakPoint> observableBreakpointLines, String path) {
        CodeArea codeEditor = new CodeArea();
        parentPane.getChildren().add(new VirtualizedScrollPane<>(codeEditor));
        IntFunction<Node> breakpointFactory = new BreakPointFactory(codeEditor, observableBreakpointLines,path);
        IntFunction<Node> lineNumberFactory = LineNumberFactory.get(codeEditor);
        codeEditor.setParagraphGraphicFactory((int line) -> new HBox(
                lineNumberFactory.apply(line),
                breakpointFactory.apply(line)
        ));
        codeEditor.multiPlainChanges().successionEnds(Duration.ofMillis(100))
                .subscribe(ignore -> codeEditor.setStyleSpans(0, computeHighlighting(codeEditor.getText())));
        codeEditor.setMouseOverTextDelay(Duration.ofMillis(300));
        Popup popup = new Popup();
        popup.setHideOnEscape(true);
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        TreeView<GDBVar> treeView = new TreeView<>();
        treeView.setCellFactory(param -> new GDBTreeCellVar());
        treeView.setPrefWidth(400);
        treeView.setPrefHeight(200);
        AnchorPane.setBottomAnchor(treeView, 0.0);
        AnchorPane.setLeftAnchor(treeView, 0.0);
        AnchorPane.setRightAnchor(treeView, 0.0);
        AnchorPane.setTopAnchor(treeView, 0.0);
        popup.getContent().add(treeView);
        codeEditor.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, evt -> {
            if (DebugController.getDebugController().getGdb().isExecuting()){
                int chIdx = evt.getCharacterIndex();
                int beginIndex = chIdx;
                int endIndex = chIdx;

                char index = codeEditor.getText(beginIndex,beginIndex + 1).toCharArray()[0];
                while (checkNameOfVar(index)){
                    beginIndex--;
                    index = codeEditor.getText(beginIndex,beginIndex + 1).toCharArray()[0];
                }
                index = codeEditor.getText(endIndex,endIndex + 1).toCharArray()[0];
                while (checkNameOfVar(index)){
                    endIndex++;
                    index = codeEditor.getText(endIndex,endIndex + 1).toCharArray()[0];
                }
                System.out.println(String.format("event:%s begin:%s end:%s",chIdx,beginIndex,endIndex));
                if ( beginIndex +1 <= endIndex){
                    TreeItem<GDBVar> varTreeItem = buildPopup(codeEditor.getText(beginIndex +1,endIndex));
                    if (varTreeItem != null){
                        varTreeItem.setExpanded(true);
                        treeView.setRoot(varTreeItem);
                        Point2D pos = evt.getScreenPosition();
                        popup.show(codeEditor,pos.getX(),pos.getY() +10);
                    }
                }
            }
        });
//        codeEditor.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, evt -> {
//        });
        popup.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, evt -> popup.hide());
        return codeEditor;
    }

    private static boolean checkNameOfVar(char index) {
        if (index == 95){
            return true;
        }
        if (index < 48){
            return false;
        }
        if(index > 57 && index < 65){
            return false;
        }
        if (index > 90 && index < 97){
            return false;
        }
        return index <= 122;
    }

    private static TreeItem<GDBVar> buildPopup(String variableName){
        return DebugController.getDebugController().getGdb().showVar(variableName);
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("PRIMITIVE") != null ? "primitive" :
                                    matcher.group("PAREN") != null ? "paren" :
                                            matcher.group("BRACE") != null ? "brace" :
                                                    matcher.group("BRACKET") != null ? "bracket" :
                                                            matcher.group("SEMICOLON") != null ? "semicolon" :
                                                                    matcher.group("STRING") != null ? "string" :
                                                                            matcher.group("COMMENT") != null ? "comment" :
                                                                                    null; /* never happens */
            assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    public static boolean isInstructionLine(String line) {
        Pattern patternComment = Pattern.compile(COMMENT_PATTERN);
        line = patternComment.matcher(line).replaceAll("");
        if (line.isEmpty()) return false;

        Pattern patternLabel = Pattern.compile(STRING_PATTERN);
        line = patternLabel.matcher(line).replaceAll("");

        line = line.trim();
        if (line.isEmpty()) return false;
        if (line.startsWith("#")) return false;


//        String[] instructionParts = line.split("\\s*,\\s*|\\s+");

//        try {
//            headingSet.valueOf(instructionParts[0].toUpperCase());
//            return true;
//        } catch (IllegalArgumentException e){
//            return false;
//        }
        return true;
    }


}