package com.dse.probe_point_manager.objects;

import com.dse.parser.object.ISourcecodeFileNode;
import com.dse.parser.object.SourcecodeFileNode;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.function.IntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProbePointFXCodeView {
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

    private ProbePointFXCodeView() {
    }

    public static CodeArea getCodeEditor(Pane parentPane, ObservableSet<Integer> observableProbePointLines,
                                         ISourcecodeFileNode sourcecodeFileNode) {
        CodeArea codeEditor = new CodeArea();
        parentPane.getChildren().add(new VirtualizedScrollPane<>(codeEditor));
        // to display probe point icon in the starting of code line
        IntFunction<Node> probePointFactory = new ProbePointFactory(codeEditor, observableProbePointLines, sourcecodeFileNode);
        // to set line number of code line
        IntFunction<Node> lineNumberFactory = LineNumberFactory.get(codeEditor);
        codeEditor.setParagraphGraphicFactory((int line) -> new HBox(
                lineNumberFactory.apply(line),
                probePointFactory.apply(line)
        ));
        codeEditor.multiPlainChanges().successionEnds(Duration.ofMillis(100))
                .subscribe(ignore -> codeEditor.setStyleSpans(0, computeHighlighting(codeEditor.getText())));
        codeEditor.setMouseOverTextDelay(Duration.ofMillis(300));

        return codeEditor;
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
        if (line.substring(0, 1).equals("#")) return false;


        String[] instructionParts = line.split("\\s*,\\s*|\\s+");

//        try {
//            headingSet.valueOf(instructionParts[0].toUpperCase());
//            return true;
//        } catch (IllegalArgumentException e){
//            return false;
//        }
        return true;
    }


}