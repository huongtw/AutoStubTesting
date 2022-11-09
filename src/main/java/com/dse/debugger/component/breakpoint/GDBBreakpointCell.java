package com.dse.debugger.component.breakpoint;


import com.dse.debugger.controller.BreakPointController;
import com.dse.util.Utils;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Callback;


public class GDBBreakpointCell extends CheckBoxListCell<BreakPoint> {

    private Label fileName;
    private Label line;
    private Tooltip filePath;

    public GDBBreakpointCell(Callback<BreakPoint, ObservableValue<Boolean>> getSelectedProperty) {
        super(getSelectedProperty);
        fileName = new Label();
        line = new Label();
        filePath = new Tooltip();
    }

    @Override
    public void updateItem(BreakPoint item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) {
            fileName.setText(item.getFile() + ": ");
            line.setText("[line: " + item.getLine() + "]");
            filePath.setText(Utils.normalizePath(item.getFile()));
            ((CheckBox) getGraphic()).selectedProperty().addListener(((observable, oldValue, newValue) -> {
                BreakPoint brItem = getItem();
                if (brItem != null) {
                    if (newValue) {
                        setStyle("");
                        BreakPointController.getBreakPointController().enable(getItem());
                    } else {
                        setStyle("-fx-background-color: #ceffce");
                        BreakPointController.getBreakPointController().disable(getItem());
                    }
                }
            }));
            setText(fileName.getText() + line.getText());
            setTooltip(filePath);
        } else {
            setGraphic(null);
            setText(null);
        }
    }
}
