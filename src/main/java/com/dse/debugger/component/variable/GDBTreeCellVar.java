package com.dse.debugger.component.variable;

import com.dse.debugger.component.watches.WatchPoint;
import com.dse.debugger.controller.DebugController;
import com.dse.debugger.controller.WatchController;
import com.dse.guifx_v3.helps.UIController;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import javafx.util.converter.NumberStringConverter;

import java.util.Optional;

public class GDBTreeCellVar extends TreeCell<GDBVar> {
    private HBox hBox;
    private Label icon;
    private Label name;
    private Label value;
    private Label type;

    private ContextMenu contextMenu;
    private MenuItem display = new MenuItem("Display as array");
    private MenuItem assign = new MenuItem("Set value");

    public GDBTreeCellVar() {
        super();
        icon = new Label();
        name = new Label();
        name.setStyle("-fx-text-fill: black; -fx-font-weight: bold");
        value = new Label();
        type = new Label();
        type.setStyle("-fx-font-style: italic; -fx-text-fill: brown");
        hBox = new HBox(icon, name, value, type);
        hBox.setSpacing(10);
        contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(display, assign);
    }

    @Override
    protected void updateItem(GDBVar item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) {
            WatchPoint watchPoint = item.getWatchPoint();
            if (watchPoint == null) {
                setting(item);
                setStyle("");
            } else {
                if (watchPoint.isNull()){
                    System.out.println(watchPoint);
                    System.out.println(item);
                    value.setText("= Out of scope");
                    if (DebugController.isExit == false) {
                        setStyle("-fx-background-color: #cb494d");
                    }
                } else {
                    setting(item);
                    setStyle("");
                }
            }
            name.setText(item.getRealName());
            setGraphic(hBox);
        } else {
            setGraphic(null);
        }
        if (DebugController.isExit == true) {
            setStyle("-fx-background-color: white");
        }
    }

    private void setting(GDBVar item){
        if (item.getDynamic() != null) {
            icon.setGraphic(new ImageView(new Image(Object.class.getResourceAsStream("/icons/gdb_dynamic_16.png"))));
        } else {
            setAssign(item);
            if (item.getNumchild() > 0) {
                setDisplay(item);
                icon.setGraphic(new ImageView(new Image(Object.class.getResourceAsStream("/icons/gdb_point_var_16.png"))));
            } else {
                icon.setGraphic(new ImageView(new Image(Object.class.getResourceAsStream("/icons/gdb_var_16.png"))));
            }
            setContextMenu(contextMenu);
            value.setText(item.getValue());
            type.setText(item.getType());
        }
    }

    private void setDisplay(GDBVar item) {
        assign.setDisable(false);
        display.setDisable(false);
        display.setOnAction(e -> {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle("Display as array");
            dialog.setHeaderText("Enter size of array");

            // Set the button types.
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField startField = new TextField();
            startField.setPromptText("Start index");
            startField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));
            TextField endField = new TextField();
            endField.setPromptText("End index");
            endField.setTextFormatter(new TextFormatter<>(new NumberStringConverter()));

            grid.add(new Label("Start index:"), 0, 0);
            grid.add(startField, 1, 0);
            grid.add(new Label("End index:"), 0, 1);
            grid.add(endField, 1, 1);
            dialog.getDialogPane().setContent(grid);

            Platform.runLater(() -> {
                startField.setText(String.valueOf(item.getStartIdx()));
                endField.requestFocus();
            });

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.APPLY) {
                    return new Pair<>(startField.getText(), endField.getText());
                }
                return null;
            });

            Optional<Pair<String, String>> result = dialog.showAndWait();
            result.ifPresent(startEnd -> {
                int start = Integer.parseInt(startEnd.getKey());
                int end = Integer.parseInt(startEnd.getValue());
                if (end < start || start < 0 || end < 1) {
                    UIController.showErrorDialog("Please enter correct value for size", "Error", "Wrong Size");
                    return;
                }
                if (item.getSize() == 0 || item.getSize() < end + 1) {
                    item.setSize(end + 1);
                }
                item.setStartIdx(start);
                item.setEndIdx(end);
                DebugController.getDebugController().getGdb().updateVariable(item, getTreeView());
            });
        });
    }

    private void setAssign(GDBVar item) {
        display.setDisable(true);
        assign.setDisable(false);
        assign.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Assign value to variable");
            dialog.setContentText("New value");
            // TODO: check type input
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(value -> {
                if (value.trim().toLowerCase().equals("null"))
                    value = "0x0";
//                if (length <= 0){
//                    UIController.showErrorDialog("Size must be greater than 0","Error", "Wrong Size");
//                } else {
                DebugController.getDebugController().getGdb().assignVariable(item, value, getTreeView());
                WatchController.getWatchController().updateWatches();
            });

        });
    }
}
