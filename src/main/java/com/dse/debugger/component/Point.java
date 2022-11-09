package com.dse.debugger.component;

import com.dse.debugger.component.breakpoint.BreakPoint;
import com.dse.debugger.controller.BreakPropertiesController;
import com.dse.debugger.controller.DebugController;
import com.dse.guifx_v3.helps.UIController;
import com.dse.probe_point_manager.controllers.AddEditProbePointController;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Point extends Circle {

    private ContextMenu menu = new ContextMenu();
    MenuItem disable = new MenuItem("Disable");
    MenuItem enable = new MenuItem("Enable");
    MenuItem properties = new MenuItem("Properties");
    private int line;
    private String path;
    private BreakPoint breakPoint;

    public Point(double radius, int line, String path) {
        super(radius);
        this.setLine(line);
        this.setPath(path);
//        enable.setDisable(true);
        menu.getItems().addAll(enable,disable, properties);
        setAction();
    }

    private Color booToColor(boolean selected) {
        if (selected) return Color.INDIANRED;
        else return Color.DARKGRAY;
    }

    private void setAction() {
        disable.setOnAction(e -> {
            // handle disable breakpoint
            DebugController.getDebugController().getGdb().disableBreakPoint(this.breakPoint);
            this.breakPoint.setEnabled("n");
            this.breakPoint.setSelected(false);
//            enable.setDisable(false);
//            disable.setDisable(true);
        });
        enable.setOnAction(e -> {
            // handle enable breakpoint
            DebugController.getDebugController().getGdb().enableBreakPoint(this.breakPoint);
            this.breakPoint.setEnabled("y");
            this.breakPoint.setSelected(true);
//            enable.setDisable(true);
//            disable.setDisable(false);
        });
        properties.setOnAction(e -> {
            Stage window = BreakPropertiesController.getWindow(this.breakPoint);
            if (window != null) {
                window.setResizable(false);
                window.initModality(Modality.WINDOW_MODAL);
                window.initOwner(UIController.getPrimaryStage().getScene().getWindow());
                window.show();
            }
        });
        setOnContextMenuRequested(e -> {
            menu.show(this, e.getScreenX(), e.getScreenY());
        });
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setBreak(BreakPoint breakPoint) {
        this.breakPoint = breakPoint;
        SimpleBooleanProperty breakProperty = this.breakPoint.selectedProperty();
        enable.disableProperty().bind(breakProperty);
        disable.disableProperty().bind(breakProperty.not());
        if (breakPoint.getEnabled().equals("y")) {
            this.breakPoint.setSelected(true);
//            enable.setDisable(true);
//            disable.setDisable(false);
        } else {
            this.breakPoint.setSelected(false);
//            enable.setDisable(false);
//            disable.setDisable(true);
        }
        ObjectProperty<Paint> colorObjectProperty = this.fillProperty();
        ObjectBinding<Paint> paintObjectBinding = Bindings.createObjectBinding(() -> booToColor(breakProperty.get()), breakProperty);
        colorObjectProperty.bind(paintObjectBinding);
    }
}
