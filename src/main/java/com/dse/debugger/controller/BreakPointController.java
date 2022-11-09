package com.dse.debugger.controller;

import com.dse.debugger.gdb.analyzer.OutputGDB;
import com.dse.debugger.component.breakpoint.BreakPoint;
import com.dse.debugger.component.breakpoint.GDBBreakpointCell;
import com.dse.logger.AkaLogger;
import com.dse.util.PathUtils;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class BreakPointController implements Initializable {

    private static final AkaLogger logger = AkaLogger.get(BreakPointController.class);

    private static BreakPointController breakPointController = null;
    private static AnchorPane pane = null;

    private static void prepare() {
        FXMLLoader loader = new FXMLLoader(Object.class.getResource("/FXML/debugger/BreakpointManager.fxml"));
        try {
            pane = loader.load();
            breakPointController = loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BreakPointController getBreakPointController(){
        if (breakPointController == null) prepare();
        return breakPointController;
    }

    public static AnchorPane getPane() {
        if (pane == null) prepare();
        return pane;
    }

    @FXML
    ListView<BreakPoint> breakListView;

    private ObservableList<BreakPoint> breakList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.breakListView.setItems(this.breakList);
        Callback<BreakPoint, ObservableValue<Boolean>> itemToBoolean = BreakPoint::selectedProperty;
        this.breakListView.setCellFactory(e -> new GDBBreakpointCell(itemToBoolean));
        this.breakListView.getSelectionModel().selectedItemProperty()
                .addListener((((observable, oldValue, newValue) -> {
                    if (newValue != null){
                        if (oldValue != newValue){
                            DebugController.getDebugController().openCurrentHitLine(newValue.getLine(), newValue.getFile(),false);
                        }
                    }
                })));
    }

    public void disable(BreakPoint breakPoint){
        OutputGDB outputGDB = DebugController.getDebugController().getGdb().disableBreakPoint(breakPoint);
        if (outputGDB == null){
            logger.debug("Can not get result from GDB");
        } else {
            if (outputGDB.isError()){
                logger.debug("Disable breakpoint number " + breakPoint.getNumber() + " failed");
                // todo: show error dialog
            } else {
                logger.debug("Disable breakpoint number " + breakPoint.getNumber() + " successfully");
                breakPoint.setEnabled("n");
                breakPoint.setSelected(false);
            }
        }
        DebugController.getDebugController().updateBreakPointFile();
    }

    public void enable(BreakPoint breakPoint){
        OutputGDB outputGDB = DebugController.getDebugController().getGdb().enableBreakPoint(breakPoint);
        if (outputGDB == null){
            logger.debug("Can not get result from GDB");
        } else {
            if (outputGDB.isError()){
                logger.debug("Enable breakpoint number " + breakPoint.getNumber() + " failed");
                // todo: show error dialog
            } else {
                logger.debug("Enable breakpoint number " + breakPoint.getNumber() + " successfully");
                breakPoint.setEnabled("y");
                breakPoint.setSelected(true);
            }
        }
        DebugController.getDebugController().updateBreakPointFile();
    }

    public void addBreakPoint(BreakPoint breakPoint){
        breakList.add(breakPoint);
    }

    public void deleteBreakPoint(BreakPoint breakPoint){
        breakList.remove(breakPoint);
    }

    public BreakPoint searchBreakPoint(int line, String filePath){
        for (BreakPoint e : breakList) {
            if (e.getLine() == line + 1
                    && (PathUtils.equals(e.getFull(), filePath)
                    || PathUtils.equals(e.getFile(), filePath))) {
                return e;
            }
        }
        return null;
    }

    public void clearAll() {
        this.breakList.clear();
        this.breakListView.getItems().clear();
        this.breakListView.setItems(this.breakList);
    }

    public void addAll(ArrayList<BreakPoint> allBreaks) {
        allBreaks.forEach(this::addBreakPoint);
    }
}
