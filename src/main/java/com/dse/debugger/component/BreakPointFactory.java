package com.dse.debugger.component;

import com.dse.debugger.component.breakpoint.BreakPointManager;
import com.dse.debugger.controller.DebugController;
import com.dse.debugger.gdb.GDB;
import com.dse.debugger.gdb.analyzer.OutputGDB;
import com.dse.debugger.component.breakpoint.BreakPoint;
import com.dse.debugger.utils.CodeViewHelpers;
import com.dse.debugger.controller.BreakPointController;
import com.dse.debugger.utils.FXCodeView;
import com.dse.util.Utils;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.fxmisc.richtext.CodeArea;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.IntFunction;

public class BreakPointFactory implements IntFunction<Node> {

    private static final String TAG_BREAK = "break";
    private static final Background DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.web("#ddd"), null, null));
    private final CodeArea area;
    private String path;

    private ObservableSet<BreakPoint> list;

    public BreakPointFactory(CodeArea area, ObservableSet<BreakPoint> list, String path) {
        this.area = area;
        this.list = list;
        this.path = Utils.normalizePath(path);

        // TODO: fix breakpoint when source code has changed (probe point / edit new source code)
        area.plainTextChanges()
                .filter(ptc -> ptc.getRemoved().contains("\n")
                        || ptc.getInserted().contains("\n")
                        || area.getParagraph(area.getCurrentParagraph()).getText().trim().isEmpty())
                .subscribe((a) -> updateBreakpointsListByParagraphs(a.getInserted(), a.getRemoved()));

        list.addListener((SetChangeListener<BreakPoint>) change -> {
            if (change.wasRemoved() && list.isEmpty()) {
                clearBreakpoints();
            }
        });
    }
    public static void updateBreak(Point circ) {
        int line = circ.getLine();
        String path = circ.getPath();
        BreakPoint breakPoint = BreakPointManager.getBreakPointManager().searchBreakFromLineAndPath(path,line);
        if (breakPoint == null){
            circ.setVisible(false);
        } else {
            circ.setBreak(breakPoint);
        }
    }

    String normalizeFile(String path) {
        path = path.replaceAll("\\\\", "/");
        return path;
    }

    @Override
    public Node apply(int value) {
        int line = value + 1;
        Point circ = new Point(5,line,this.path);
        BreakPoint breakPoint = BreakPointManager.getBreakPointManager().searchBreakFromLineAndPath(path,line);
        updateBreak(circ);
        if (breakPoint != null) {
            BreakPointManager.getBreakPointManager().getActivePoints().add(circ);
        }
        StackPane pane = new StackPane();
        StackPane.setAlignment(circ, Pos.CENTER);
        pane.setPrefSize(15, 15);
        pane.getChildren().add(circ);
        pane.setBackground(DEFAULT_BACKGROUND);
        pane.setCursor(Cursor.HAND);

        pane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                GDB gdb = DebugController.getDebugController().getGdb();
                if (circ.isVisible()) {
                    CodeViewHelpers.tryRemoveParagraphStyle(area, value, TAG_BREAK);
                    OutputGDB outputGDB = gdb.deleteBreakPoint(breakPoint);
                    BreakPointController.getBreakPointController().deleteBreakPoint(breakPoint);
                    list.remove(breakPoint);
                } else {
                    int indexOfInstructionLine = value;
                    int numOfParagraphs = area.getParagraphs().size();
                    if (FXCodeView.isInstructionLine(area.getParagraph(indexOfInstructionLine).getText())) {
                        String file = normalizeFile(this.path);
                        String full = this.path;
                        BreakPoint newBreakPoint = gdb.addNewBreakPoint(line, this.path);
                        newBreakPoint.setFile(file);
                        newBreakPoint.setFull(full);
                            BreakPointController.getBreakPointController().addBreakPoint(newBreakPoint);
                            list.add(newBreakPoint);
                            circ.setBreak(newBreakPoint);
                        CodeViewHelpers.addParagraphStyle(area, indexOfInstructionLine, TAG_BREAK);
                    }
                }
            }
        });
        list.addListener(new WeakSetChangeListener<>(
                        change -> {
                            if (breakPoint == null)
                                circ.setVisible(false);
                            else {
                                circ.setVisible(list.contains(breakPoint));
                            }
                        }));
        return pane;
    }

    private void updateBreakpointsListByParagraphs(String inserted, String removed) {
        TreeSet<BreakPoint> temp = new TreeSet<>();
        list.forEach(e -> CodeViewHelpers.addParagraphStyle(area, e.getLine() - 1, TAG_BREAK));
        for (int i = 0; i < area.getParagraphs().size(); i++) {
            if (area.getParagraph(i).getParagraphStyle().stream().anyMatch(style -> style.equals(TAG_BREAK))) {
                BreakPoint breakPoint = BreakPointManager.getBreakPointManager().searchBreakFromLineAndPath(path,i+1);
                if (breakPoint != null)
                    temp.add(breakPoint);
            }
        }
        list.retainAll(temp);
        list.addAll(temp);
    }

    private void clearBreakpoints() {
        for (int i = 0; i < area.getParagraphs().size(); i++) {
            if (area.getParagraph(i).getParagraphStyle().stream().anyMatch(style -> style.equals(TAG_BREAK))) {
                CodeViewHelpers.tryRemoveParagraphStyle(area, i, TAG_BREAK);
            }
        }
    }
}
