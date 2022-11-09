package com.dse.debugger.component.frame;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

import java.io.File;

public class GDBFrameListCell extends ListCell<GDBFrame> {
    private HBox hBox;
    private Label index;
    private Label funcName;
    private Label fileAndLine;

    public GDBFrameListCell() {
        super();
        index = new Label();
        funcName = new Label();
        fileAndLine = new Label();
        hBox = new HBox(index, funcName, fileAndLine);
        hBox.setSpacing(2);
    }

    @Override
    protected void updateItem(GDBFrame item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) {
            String fileName = item.getFile();
            if (fileName != null) {
                fileAndLine.setText(fileName.substring(fileName.lastIndexOf(File.separator) + 1) + ":" + item.getLine());
            }
            index.setText(item.getLevel() + ": ");
            funcName.setText(item.getFunc());

            Platform.runLater(() -> setGraphic(hBox));
        } else {
            Platform.runLater(() -> setGraphic(null));
        }
    }
}
