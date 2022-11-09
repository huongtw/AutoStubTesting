package com.dse.guifx_v3.objects;

import com.dse.parser.object.*;
import javafx.scene.layout.AnchorPane;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.*;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class using to create code viewer pane with syntax styled for C++
 *
 * @author zizoz
 */

public class FXFileView {

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

    private CodeArea codeArea;
    private List<String> fileData;
    private IASTFileLocation location;

    public FXFileView(INode node) {
        assert (node!=null);

        // INode sourcecodeFileNode = node;
        // while (!(sourcecodeFileNode instanceof SourcecodeFileNode) && sourcecodeFileNode != null) {
        //     sourcecodeFileNode = sourcecodeFileNode.getParent();
        // }

        // if (sourcecodeFileNode != null) {
        //     this.fileData = this.readData(sourcecodeFileNode.getAbsolutePath());

        //     // if the node is not a source code file, we need to move the cursor to the area of the block corresponding
        //     // to this node
        //     if (!(node instanceof SourcecodeFileNode))
        //         this.location = ((CustomASTNode) node).getAST().getFileLocation();
        // }
        INode sourcecodeFileNode = node;
        while (!(sourcecodeFileNode instanceof SourcecodeFileNode) && sourcecodeFileNode != null) {
            sourcecodeFileNode = sourcecodeFileNode.getParent();
        }

        if (sourcecodeFileNode != null) {
            this.fileData = this.readData(sourcecodeFileNode.getAbsolutePath());

            // if the node is not a source code file, we need to move the cursor to the area of the block corresponding
            // to this node
            if (node instanceof LambdaFunctionNode) {
                this.location = ((LambdaFunctionNode) node).getOriginalAST().getFileLocation();
            } else if (!(node instanceof SourcecodeFileNode))
                this.location = ((CustomASTNode) node).getAST().getFileLocation();
        }
    }

    /**
     * Get main UI of source code viewer
     *
     * @param isHighlight true if user want to hightlight and focus to a function
     * @return normal pane
     */
    public AnchorPane getAnchorPane(boolean isHighlight) {
        String sampleCode = String.join("\n", fileData);
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        Subscription cleanupWhenNoLongerNeed = codeArea.multiPlainChanges().successionEnds(Duration.ofMillis(100))
                .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));
        codeArea.replaceText(0, 0, sampleCode);
        codeArea.setEditable(true);
        if (isHighlight)
            highlightFunction();
        VirtualizedScrollPane virtualizedScrollPane = new VirtualizedScrollPane<>(codeArea);
        AnchorPane anchorPane = new AnchorPane(virtualizedScrollPane);
        AnchorPane.setBottomAnchor(virtualizedScrollPane, 0.0);
        AnchorPane.setTopAnchor(virtualizedScrollPane, 0.0);
        AnchorPane.setLeftAnchor(virtualizedScrollPane, 0.0);
        AnchorPane.setRightAnchor(virtualizedScrollPane, 0.0);
        codeArea.getStylesheets().add(Object.class.getResource("/css/keywords.css").toExternalForm());
        return anchorPane;
    }

    public void setEditable(boolean isEditable) {
        codeArea.setEditable(isEditable);
    }

    /**
     * Highlight and focus a function
     */
    private void highlightFunction() {
        try {
            int start = location.getStartingLineNumber() - 1;
            int end = location.getEndingLineNumber() - 1;
            int startPos = codeArea.position(start, 0).toOffset();
            int endPos = codeArea.position(end, fileData.get(end).length()).toOffset();
            int bias = 0;
            if (start > 5)
                bias = 5;
            codeArea.showParagraphInViewport(start - bias);
            codeArea.selectRange(startPos, endPos);
        } catch (Exception e) {
            // TODO: fix bug of displaying file localename.c
        }
    }

    /**
     * Computing highlighting code syntax after edit code
     *
     * @param text code need to be computed
     * @return all style for code area
     */
    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while (matcher.find()) {
            String styleClass = matcher.group("KEYWORD") != null ? "keyword" :
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

    /**
     * Read data from file path
     *
     * @param path path to file
     * @return data in string
     */
    private List<String> readData(String path) {
        List<String> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                data.add(sCurrentLine);
            }

        } catch (IOException e) {
              e.printStackTrace();
        }
        return data;
    }

    public CodeArea getCodeArea() {
        return codeArea;
    }
}